/**
 * Deep Blue Haven - Staff Live Chat Workspace Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    let activeSessionId = null;
    let lastMessageId = 0;
    let sessionPollInterval = null;
    let messagePollInterval = null;
    let currentFilter = 'ALL';
    let allSessionsCache = [];

    // Detect Context Path
    const CONTEXT_PATH = window.location.pathname.startsWith('/deepbluehaven') ? '/deepbluehaven' : '';

    // DOM Elements
    const sessionsList = document.getElementById('sessionsList');
    const sessionSearchInput = document.getElementById('sessionSearchInput');
    const filterButtons = document.querySelectorAll('.session-filter-btn');

    const chatNoSelection = document.getElementById('chatNoSelection');
    const chatActiveView = document.getElementById('chatActiveView');
    const chatGuestSidebar = document.getElementById('chatGuestSidebar');

    const headerGuestAvatar = document.getElementById('headerGuestAvatar');
    const headerGuestName = document.getElementById('headerGuestName');
    const headerGuestTier = document.getElementById('headerGuestTier');
    const headerSessionStatus = document.getElementById('headerSessionStatus');
    const headerAssignee = document.getElementById('headerAssignee');

    const btnJoinChat = document.getElementById('btnJoinChat');
    const btnResolveChat = document.getElementById('btnResolveChat');
    const chatMessagesFeed = document.getElementById('chatMessagesFeed');
    const staffChatInputForm = document.getElementById('staffChatInputForm');
    const staffMessageInput = document.getElementById('staffMessageInput');

    const dossierAvatar = document.getElementById('dossierAvatar');
    const dossierName = document.getElementById('dossierName');
    const dossierTier = document.getElementById('dossierTier');
    const dossierEmail = document.getElementById('dossierEmail');
    const dossierPhone = document.getElementById('dossierPhone');
    const dossierStartTime = document.getElementById('dossierStartTime');

    // Filter Buttons
    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            filterButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentFilter = btn.dataset.filter || 'ALL';
            renderSessionsList();
        });
    });

    // Search input
    if (sessionSearchInput) {
        sessionSearchInput.addEventListener('input', () => {
            renderSessionsList();
        });
    }

    // Canned Response Chips
    document.querySelectorAll('.canned-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            if (staffMessageInput && chip.dataset.reply) {
                staffMessageInput.value = chip.dataset.reply;
                staffMessageInput.focus();
            }
        });
    });

    // Input Enter Key handler (Enter to send, Shift+Enter for newline)
    if (staffMessageInput) {
        staffMessageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendStaffMessage();
            }
        });
    }

    if (staffChatInputForm) {
        staffChatInputForm.addEventListener('submit', (e) => {
            e.preventDefault();
            sendStaffMessage();
        });
    }

    // Join / Assign Chat Action
    if (btnJoinChat) {
        btnJoinChat.addEventListener('click', async () => {
            if (!activeSessionId) return;
            try {
                const resp = await fetch(`${CONTEXT_PATH}/api/chat/staff/session/${activeSessionId}/assign`, {
                    method: 'POST'
                });
                if (resp.ok) {
                    loadSessions();
                    loadSessionConversation(activeSessionId);
                }
            } catch (err) {
                console.error('[StaffChat] Assign error:', err);
            }
        });
    }

    // Resolve Chat Action
    if (btnResolveChat) {
        btnResolveChat.addEventListener('click', async () => {
            if (!activeSessionId) return;
            if (!confirm('Are you sure you want to mark this guest chat as RESOLVED?')) return;
            try {
                const resp = await fetch(`${CONTEXT_PATH}/api/chat/staff/session/${activeSessionId}/resolve`, {
                    method: 'POST'
                });
                if (resp.ok) {
                    loadSessions();
                    loadSessionConversation(activeSessionId);
                }
            } catch (err) {
                console.error('[StaffChat] Resolve error:', err);
            }
        });
    }

    // Fetch and Load Sessions Queue
    async function loadSessions() {
        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/staff/sessions`);
            if (!resp.ok) return;
            allSessionsCache = await resp.json();
            renderSessionsList();
        } catch (err) {
            console.warn('[StaffChat] Failed to load sessions:', err);
        }
    }

    // Render Sessions List from Cache
    function renderSessionsList() {
        if (!sessionsList) return;

        const query = (sessionSearchInput ? sessionSearchInput.value : '').toLowerCase().trim();

        let filtered = allSessionsCache.filter(s => {
            // Filter status
            if (currentFilter !== 'ALL' && s.status !== currentFilter) {
                return false;
            }
            // Search query
            if (query) {
                const name = (s.customerName || '').toLowerCase();
                const email = (s.customerEmail || '').toLowerCase();
                const lastMsg = (s.lastMessage || '').toLowerCase();
                return name.includes(query) || email.includes(query) || lastMsg.includes(query);
            }
            return true;
        });

        if (filtered.length === 0) {
            sessionsList.innerHTML = `
                <div class="sessions-empty">
                    <i class="fa-solid fa-inbox" style="font-size: 2rem; margin-bottom: 8px; display: block;"></i>
                    No conversations match this view
                </div>
            `;
            return;
        }

        sessionsList.innerHTML = '';
        filtered.forEach(s => {
            const el = document.createElement('div');
            el.className = `session-item-card ${activeSessionId === s.id ? 'active' : ''}`;
            el.dataset.id = s.id;

            const initial = (s.customerName || 'G').charAt(0).toUpperCase();
            const statusClass = (s.status || 'waiting').toLowerCase();

            el.innerHTML = `
                <div class="session-card-avatar">${initial}</div>
                <div class="session-card-body">
                    <div class="session-card-top">
                        <span class="session-guest-name">${escapeHtml(s.customerName || 'Guest')}</span>
                        <span class="session-card-time">${s.lastMessageTime || ''}</span>
                    </div>
                    <p class="session-last-msg">${escapeHtml(s.lastMessage || 'No messages')}</p>
                    <div class="session-card-badges">
                        <span class="status-badge-pill status-badge-pill--${statusClass}">${s.status}</span>
                        ${s.unreadCount > 0 ? `<span class="unread-badge-pill">${s.unreadCount}</span>` : ''}
                    </div>
                </div>
            `;

            el.addEventListener('click', () => {
                selectSession(s.id);
            });

            sessionsList.appendChild(el);
        });
    }

    // Select a Session
    function selectSession(sessionId) {
        activeSessionId = sessionId;
        renderSessionsList();
        loadSessionConversation(sessionId);
    }

    // Load Conversation Messages & Customer Dossier
    async function loadSessionConversation(sessionId) {
        const session = allSessionsCache.find(s => s.id === sessionId);
        if (!session) return;

        // Show active chat window and dossier
        if (chatNoSelection) chatNoSelection.style.display = 'none';
        if (chatActiveView) chatActiveView.style.display = 'flex';
        if (chatGuestSidebar) chatGuestSidebar.style.display = 'block';

        // Update Header
        const initial = (session.customerName || 'G').charAt(0).toUpperCase();
        if (headerGuestAvatar) headerGuestAvatar.textContent = initial;
        if (headerGuestName) headerGuestName.textContent = session.customerName || 'Guest';
        if (headerGuestTier) headerGuestTier.textContent = session.customerTier || 'Standard Guest';
        if (headerSessionStatus) {
            headerSessionStatus.textContent = session.status;
            headerSessionStatus.className = `status-indicator-badge status-indicator-badge--${session.status.toLowerCase()}`;
        }
        if (headerAssignee) {
            headerAssignee.textContent = session.assigneeName ? `Assigned: ${session.assigneeName}` : 'Unassigned';
        }

        // Update Dossier
        if (dossierAvatar) dossierAvatar.textContent = initial;
        if (dossierName) dossierName.textContent = session.customerName || 'Guest';
        if (dossierTier) dossierTier.textContent = session.customerTier || 'Standard Guest';
        if (dossierEmail) dossierEmail.textContent = session.customerEmail || '-';
        if (dossierPhone) dossierPhone.textContent = session.customerPhone || '-';
        if (dossierStartTime) dossierStartTime.textContent = formatDateTime(session.startTime);

        // Mark read
        fetch(`${CONTEXT_PATH}/api/chat/staff/session/${sessionId}/read`, { method: 'POST' });

        // Load Messages
        lastMessageId = 0;
        if (chatMessagesFeed) chatMessagesFeed.innerHTML = '<div class="sessions-loading"><i class="fa-solid fa-spinner fa-spin"></i> Loading...</div>';

        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/messages?sessionId=${sessionId}`);
            if (resp.ok) {
                const messages = await resp.json();
                renderMessagesFeed(messages);
                startMessagePolling();
            }
        } catch (err) {
            console.error('[StaffChat] Error loading messages:', err);
        }

        if (staffMessageInput) staffMessageInput.focus();
    }

    // Render Messages Feed
    function renderMessagesFeed(messages) {
        if (!chatMessagesFeed) return;
        chatMessagesFeed.innerHTML = '';
        lastMessageId = 0;

        messages.forEach(msg => {
            appendFeedMessage(msg);
            if (msg.id && msg.id > lastMessageId) {
                lastMessageId = msg.id;
            }
        });
        scrollFeedToBottom();
    }

    function appendFeedMessage(msg) {
        if (!chatMessagesFeed) return;

        const el = document.createElement('div');
        el.className = `feed-message feed-message--${(msg.senderType || 'system').toLowerCase()}`;
        el.dataset.id = msg.id || '';

        let senderName = '';
        if (msg.senderType === 'STAFF') {
            senderName = msg.senderName || 'Staff';
        } else if (msg.senderType === 'CUSTOMER') {
            senderName = msg.senderName || 'Guest';
        }

        const timeStr = msg.formattedTime || formatTime(msg.timestamp);

        el.innerHTML = `
            ${senderName ? `<span class="feed-message__sender">${escapeHtml(senderName)}</span>` : ''}
            <div class="feed-message__bubble">${escapeHtml(msg.content)}</div>
            ${timeStr ? `<span class="feed-message__time">${timeStr}</span>` : ''}
        `;

        chatMessagesFeed.appendChild(el);
    }

    // Send Staff Message
    async function sendStaffMessage() {
        if (!staffMessageInput || !activeSessionId) return;
        const text = staffMessageInput.value.trim();
        if (!text) return;

        staffMessageInput.value = '';

        // Optimistic UI Append
        const tempMsg = {
            senderType: 'STAFF',
            senderName: 'You',
            content: text,
            formattedTime: formatTime(new Date())
        };
        appendFeedMessage(tempMsg);
        scrollFeedToBottom();

        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/staff/send`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: activeSessionId,
                    content: text
                })
            });

            if (resp.ok) {
                const sent = await resp.json();
                if (sent && sent.id && sent.id > lastMessageId) {
                    lastMessageId = sent.id;
                }
                loadSessions(); // refresh queue stats
            }
        } catch (err) {
            console.error('[StaffChat] Send error:', err);
        }
    }

    // Polling Messages for Selected Session
    function startMessagePolling() {
        stopMessagePolling();
        messagePollInterval = setInterval(fetchNewMessages, 2500);
    }

    function stopMessagePolling() {
        if (messagePollInterval) {
            clearInterval(messagePollInterval);
            messagePollInterval = null;
        }
    }

    async function fetchNewMessages() {
        if (!activeSessionId) return;
        try {
            const resp = await fetch(`${CONTEXT_PATH}/api/chat/messages?sessionId=${activeSessionId}&afterId=${lastMessageId}`);
            if (!resp.ok) return;

            const newMsgs = await resp.json();
            if (Array.isArray(newMsgs) && newMsgs.length > 0) {
                newMsgs.forEach(msg => {
                    if (msg.senderType !== 'STAFF' || !isFeedMessageAlreadyRendered(msg.id)) {
                        appendFeedMessage(msg);
                    }
                    if (msg.id > lastMessageId) {
                        lastMessageId = msg.id;
                    }
                });
                scrollFeedToBottom();
            }
        } catch (err) {
            // silent ignore
        }
    }

    function isFeedMessageAlreadyRendered(id) {
        if (!id) return false;
        return !!chatMessagesFeed.querySelector(`.feed-message[data-id="${id}"]`);
    }

    function scrollFeedToBottom() {
        if (chatMessagesFeed) {
            chatMessagesFeed.scrollTop = chatMessagesFeed.scrollHeight;
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

    function formatDateTime(val) {
        if (!val) return '-';
        const d = new Date(val);
        if (isNaN(d.getTime())) return '-';
        return d.toLocaleString();
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

    // Initialize Page
    loadSessions();
    sessionPollInterval = setInterval(loadSessions, 4000);
});
