package deepbluehaven.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.ChatDTO;
import deepbluehaven.pojo.ChatMessage;
import deepbluehaven.pojo.ChatSession;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ChatStatus;
import deepbluehaven.pojo.enums.MessageType;
import deepbluehaven.pojo.enums.SenderType;
import deepbluehaven.repositories.ChatMessageRepository;
import deepbluehaven.repositories.ChatSessionRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       CustomerRepository customerRepository,
                       WorkerRepository workerRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
    }

    @Transactional
    public ChatDTO.SessionView getOrCreateActiveSession(Long customerId, String guestName, String guestEmail) {
        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId).orElse(null);
        }

        if (customer == null) {
            List<Customer> all = customerRepository.findAll();
            if (!all.isEmpty()) {
                customer = all.get(0);
            }
        }

        if (customer == null) {
            return null;
        }

        Optional<ChatSession> activeOpt = chatSessionRepository.findFirstByCustomerIdAndStatusNotOrderByUpdatedAtDesc(
                customer.getId(), ChatStatus.RESOLVED);

        ChatSession session;
        if (activeOpt.isPresent()) {
            session = activeOpt.get();
        } else {
            session = new ChatSession();
            session.setCustomer(customer);
            session.setStatus(ChatStatus.WAITING);
            session.setRead(false);
            session = chatSessionRepository.save(session);

            ChatMessage welcome = new ChatMessage();
            welcome.setChatSession(session);
            welcome.setSenderType(SenderType.SYSTEM);
            welcome.setSenderName("Deep Blue Haven Concierge");
            welcome.setMessageType(MessageType.TEXT);
            welcome.setContent("Welcome to Deep Blue Haven Resort! Our 24/7 Guest Relations & Concierge team is here to assist you.");
            welcome.setRead(true);
            chatMessageRepository.save(welcome);
        }

        return toSessionView(session, true);
    }

    @Transactional(readOnly = true)
    public ChatDTO.SessionView getSessionDetails(Long sessionId) {
        return chatSessionRepository.findById(sessionId)
                .map(s -> toSessionView(s, true))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ChatDTO.MessageView> getMessages(Long sessionId, Long afterId) {
        List<ChatMessage> messages;
        if (afterId != null && afterId > 0) {
            messages = chatMessageRepository.findNewMessages(sessionId, afterId);
        } else {
            messages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);
        }
        return messages.stream().map(this::toMessageView).collect(Collectors.toList());
    }

    @Transactional
    public ChatDTO.MessageView sendCustomerMessage(Long sessionId, String content, String senderName) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session == null) return null;

        if (session.getStatus() == ChatStatus.RESOLVED) {
            session.setStatus(ChatStatus.WAITING);
        }
        session.setRead(false);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        ChatMessage msg = new ChatMessage();
        msg.setChatSession(session);
        msg.setSenderType(SenderType.CUSTOMER);
        msg.setSenderId(session.getCustomer() != null ? session.getCustomer().getId() : null);
        msg.setSenderName(senderName != null && !senderName.trim().isEmpty() ? senderName : "Guest");
        msg.setMessageType(MessageType.TEXT);
        msg.setContent(content);
        msg.setRead(false);
        msg = chatMessageRepository.save(msg);

        return toMessageView(msg);
    }

    @Transactional
    public ChatDTO.MessageView sendStaffMessage(Long sessionId, Long workerId, String content) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session == null) return null;

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;
        if (worker != null && session.getAssignee() == null) {
            session.setAssignee(worker);
        }

        if (session.getStatus() == ChatStatus.WAITING) {
            session.setStatus(ChatStatus.IN_PROGRESS);
        }
        session.setRead(true);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        String staffName = worker != null && worker.getProfile() != null 
                ? worker.getProfile().getFullName()
                : "Resort Concierge";

        ChatMessage msg = new ChatMessage();
        msg.setChatSession(session);
        msg.setSenderType(SenderType.STAFF);
        msg.setSenderId(worker != null ? worker.getId() : null);
        msg.setSenderName(staffName);
        msg.setMessageType(MessageType.TEXT);
        msg.setContent(content);
        msg.setRead(true);
        msg = chatMessageRepository.save(msg);

        return toMessageView(msg);
    }

    @Transactional(readOnly = true)
    public List<ChatDTO.SessionView> getStaffSessions(String statusFilter) {
        List<ChatSession> sessions;
        if ("WAITING".equalsIgnoreCase(statusFilter)) {
            sessions = chatSessionRepository.findByStatusOrderByUpdatedAtDesc(ChatStatus.WAITING);
        } else if ("IN_PROGRESS".equalsIgnoreCase(statusFilter)) {
            sessions = chatSessionRepository.findByStatusOrderByUpdatedAtDesc(ChatStatus.IN_PROGRESS);
        } else if ("RESOLVED".equalsIgnoreCase(statusFilter)) {
            sessions = chatSessionRepository.findByStatusOrderByUpdatedAtDesc(ChatStatus.RESOLVED);
        } else {
            sessions = chatSessionRepository.findAllByOrderByUpdatedAtDesc();
        }

        return sessions.stream().map(s -> toSessionView(s, false)).collect(Collectors.toList());
    }

    @Transactional
    public boolean assignSession(Long sessionId, Long workerId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session == null) return false;

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;
        session.setAssignee(worker);
        session.setStatus(ChatStatus.IN_PROGRESS);
        session.setRead(true);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        String staffName = worker != null && worker.getProfile() != null 
                ? worker.getProfile().getFullName()
                : "Staff Member";

        ChatMessage sysMsg = new ChatMessage();
        sysMsg.setChatSession(session);
        sysMsg.setSenderType(SenderType.SYSTEM);
        sysMsg.setSenderName("System");
        sysMsg.setMessageType(MessageType.TEXT);
        sysMsg.setContent(staffName + " has joined the conversation to assist you.");
        sysMsg.setRead(true);
        chatMessageRepository.save(sysMsg);

        return true;
    }

    @Transactional
    public boolean resolveSession(Long sessionId, Long workerId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session == null) return false;

        session.setStatus(ChatStatus.RESOLVED);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        ChatMessage sysMsg = new ChatMessage();
        sysMsg.setChatSession(session);
        sysMsg.setSenderType(SenderType.SYSTEM);
        sysMsg.setSenderName("System");
        sysMsg.setMessageType(MessageType.TEXT);
        sysMsg.setContent("This conversation has been marked as resolved. Feel free to message us anytime!");
        sysMsg.setRead(true);
        chatMessageRepository.save(sysMsg);

        return true;
    }

    @Transactional
    public void markSessionRead(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setRead(true);
            chatSessionRepository.save(session);
            List<ChatMessage> unreadMessages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);
            for (ChatMessage m : unreadMessages) {
                if (!m.isRead()) {
                    m.setRead(true);
                    chatMessageRepository.save(m);
                }
            }
        }
    }

    private ChatDTO.SessionView toSessionView(ChatSession s, boolean includeMessages) {
        ChatDTO.SessionView view = new ChatDTO.SessionView();
        view.setId(s.getId());
        if (s.getCustomer() != null) {
            view.setCustomerId(s.getCustomer().getId());
            if (s.getCustomer().getProfile() != null) {
                view.setCustomerName(s.getCustomer().getProfile().getFullName());
                view.setCustomerEmail(s.getCustomer().getProfile().getEmail());
                view.setCustomerPhone(s.getCustomer().getProfile().getPhoneNumber());
                view.setCustomerAvatar(s.getCustomer().getProfile().getAvatarUrl());
            } else {
                view.setCustomerName("Guest #" + s.getCustomer().getId());
            }
            if (s.getCustomer().getProfile() != null && s.getCustomer().getProfile().getSegment() != null) {
                view.setCustomerTier(s.getCustomer().getProfile().getSegment());
            } else {
                view.setCustomerTier("Standard Guest");
            }
        }

        if (s.getAssignee() != null) {
            view.setAssigneeId(s.getAssignee().getId());
            if (s.getAssignee().getProfile() != null) {
                view.setAssigneeName(s.getAssignee().getProfile().getFullName());
            }
        }

        view.setStatus(s.getStatus());
        view.setRead(s.isRead());
        view.setStartTime(s.getStartTime());
        view.setUpdatedAt(s.getUpdatedAt());

        List<ChatMessage> allMessages = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(s.getId());
        if (!allMessages.isEmpty()) {
            ChatMessage last = allMessages.get(allMessages.size() - 1);
            view.setLastMessage(last.getContent());
        } else {
            view.setLastMessage("No messages yet");
        }

        view.setUnreadCount(chatMessageRepository.countUnreadByStaff(s.getId()));

        if (includeMessages) {
            view.setMessages(allMessages.stream().map(this::toMessageView).collect(Collectors.toList()));
        } else {
            view.setMessages(new ArrayList<>());
        }

        return view;
    }

    private ChatDTO.MessageView toMessageView(ChatMessage m) {
        ChatDTO.MessageView view = new ChatDTO.MessageView();
        view.setId(m.getId());
        if (m.getChatSession() != null) {
            view.setSessionId(m.getChatSession().getId());
        }
        view.setSenderId(m.getSenderId());
        view.setSenderName(m.getSenderName());
        view.setSenderType(m.getSenderType());
        view.setMessageType(m.getMessageType());
        view.setContent(m.getContent());
        view.setRead(m.isRead());
        view.setTimestamp(m.getTimestamp());
        return view;
    }
}
