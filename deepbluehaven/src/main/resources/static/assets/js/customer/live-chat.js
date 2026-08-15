/**
 * Deep Blue Haven - Customer Live Chat Widget Controller
 */
(function() {
    'use strict';

    let currentSessionId = null;
    let lastMessageId = 0;
    let pollInterval = null;
    const POLLING_RATE_MS = 2500;

    // Elements
    const widget = document.getElementById('dbhChatWidget');
    const trigger = document.getElementById('dbhChatTrigger');
    const closeBtn = document.getElementById('dbhChatClose');
    const minimizeBtn = document.getElementById('dbhChatMinimize');
    const chatForm = document.getElementById('dbhChatForm');
    const messageInput = document.getElementById('dbhChatMessageInput');
    const messagesContainer = document.getElementById('dbhChatMessages');
    const chatBody = document.getElementById('dbhChatBody');
    const chatWelcome = document.getElementById('dbhChatWelcome');
    const quickReplies = document.getElementById('dbhQuickReplies');
    const badge = document.getElementById('dbhChatBadge');

    if (!widget || !trigger) return;

    // Detect Context Path
    function getContextPath() {
        const path = window.location.pathname;
        if (path.startsWith('/deepbluehaven')) {
            return '/deepbluehaven';
        }
        return '';
    }
    const CONTEXT_PATH = getContextPath();

    // Toggle Chat Window
    trigger.addEventListener('click', () => {
        const isActive = widget.classList.toggle('active');
        if (isActive) {
            if (badge) badge.style.display = 'none';
            if (!currentSessionId) {
                initSession();
            } else {
                startPolling();
                scrollToBottom();
            }
            if (messageInput) {
                setTimeout(() => messageInput.focus(), 150);
            }
        } else {
            stopPolling();
        }
    });

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            widget.classList.remove('active');
            stopPolling();
        });
    }

    if (minimizeBtn) {
        minimizeBtn.addEventListener('click', () => {
            widget.classList.remove('active');
            stopPolling();
        });
    }

    // Quick Reply Buttons
    if (quickReplies) {
        quickReplies.addEventListener('click', (e) => {
            const btn = e.target.closest('.dbh-quick-btn');
            if (btn && btn.dataset.text) {
                if (messageInput) {
                    messageInput.value = btn.dataset.text;
                    sendMessage();
                }
            }
        });
    }

    function updateWelcomeVisibility(hasMessages) {
        if (!chatWelcome) return;
        if (hasMessages) {
            chatWelcome.style.display = 'none';
        } else {
            chatWelcome.style.display = 'block';
        }
    }

    // Initialize or Resume Session
    async function initSession() {
        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/customer/session`);
            if (!resp.ok) return;
            const data = await resp.json();
            if (data && data.id) {
                currentSessionId = data.id;
                renderMessages(data.messages || []);
                startPolling();
            }
        } catch (err) {
            console.warn('[LiveChat] Session init failed:', err);
        }
    }

    // Render Messages Timeline
    function renderMessages(messages) {
        if (!messagesContainer) return;
        messagesContainer.innerHTML = '';
        lastMessageId = 0;

        if (Array.isArray(messages) && messages.length > 0) {
            updateWelcomeVisibility(true);
            messages.forEach(msg => {
                appendMessageBubble(msg);
                if (msg.id && msg.id > lastMessageId) {
                    lastMessageId = msg.id;
                }
            });
        } else {
            updateWelcomeVisibility(false);
        }
        scrollToBottom();
    }

    // Append a single bubble
    function appendMessageBubble(msg) {
        if (!messagesContainer) return;

        const el = document.createElement('div');
        const senderType = (msg.senderType || 'system').toLowerCase();
        el.className = `dbh-message dbh-message--${senderType}`;
        if (msg.id) el.dataset.id = msg.id;

        let senderHtml = '';
        if (senderType === 'staff') {
            const name = msg.senderName || 'Resort Concierge';
            senderHtml = `<span class="dbh-message__sender"><i class="fa-solid fa-headset"></i> ${escapeHtml(name)}</span>`;
        } else if (senderType === 'customer') {
            senderHtml = `<span class="dbh-message__sender">You</span>`;
        }

        const timeStr = msg.formattedTime || formatTime(msg.timestamp || new Date());

        el.innerHTML = `
            ${senderHtml}
            <div class="dbh-message__bubble">${escapeHtml(msg.content)}</div>
            ${timeStr ? `<span class="dbh-message__time">${timeStr}</span>` : ''}
        `;

        messagesContainer.appendChild(el);
        updateWelcomeVisibility(true);
    }

    // Send Customer Message
    async function sendMessage() {
        if (!messageInput) return;
        const text = messageInput.value.trim();
        if (!text) return;

        messageInput.value = '';

        // If session not ready, initialize first
        if (!currentSessionId) {
            await initSession();
        }
        if (!currentSessionId) return;

        // Optimistic UI Append
        const tempMsg = {
            senderType: 'CUSTOMER',
            content: text,
            formattedTime: formatTime(new Date())
        };
        appendMessageBubble(tempMsg);
        scrollToBottom();

        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/customer/send`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: currentSessionId,
                    content: text
                })
            });

            if (resp.ok) {
                const sentMsg = await resp.json();
                if (sentMsg && sentMsg.id && sentMsg.id > lastMessageId) {
                    lastMessageId = sentMsg.id;
                }
            }
        } catch (err) {
            console.error('[LiveChat] Send error:', err);
        }
    }

    // Chat Form Submit
    if (chatForm) {
        chatForm.addEventListener('submit', (e) => {
            e.preventDefault();
            sendMessage();
        });
    }

    // Polling for New Messages
    function startPolling() {
        stopPolling();
        pollInterval = setInterval(fetchNewMessages, POLLING_RATE_MS);
    }

    function stopPolling() {
        if (pollInterval) {
            clearInterval(pollInterval);
            pollInterval = null;
        }
    }

    async function fetchNewMessages() {
        if (!currentSessionId) return;

        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/messages?sessionId=${currentSessionId}&afterId=${lastMessageId}`);
            if (!resp.ok) return;

            const newMessages = await resp.json();
            if (Array.isArray(newMessages) && newMessages.length > 0) {
                newMessages.forEach(msg => {
                    if (msg.senderType !== 'CUSTOMER' || !isMessageAlreadyRendered(msg.id)) {
                        appendMessageBubble(msg);
                    }
                    if (msg.id > lastMessageId) {
                        lastMessageId = msg.id;
                    }
                });
                scrollToBottom();
            }
        } catch (err) {
            // Polling silently ignores transient errors
        }
    }

    function isMessageAlreadyRendered(id) {
        if (!id) return false;
        return !!messagesContainer.querySelector(`.dbh-message[data-id="${id}"]`);
    }

    function scrollToBottom() {
        if (chatBody) {
            setTimeout(() => {
                chatBody.scrollTop = chatBody.scrollHeight;
            }, 50);
        }
    }

    function formatTime(val) {
        if (!val) return '';
        const d = new Date(val);
        if (isNaN(d.getTime())) return '';
        const h = String(d.getHours()).padStart(2, '0');
        const m = String(d.getMinutes()).padStart(2, '0');
        return `${h}:${m}`;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

})();
