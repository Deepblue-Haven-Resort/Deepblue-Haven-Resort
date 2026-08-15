package deepbluehaven.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.CommentInquiryDTO;
import deepbluehaven.pojo.Comment;
import deepbluehaven.pojo.ContactInquiry;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.InquiryStatus;
import deepbluehaven.repositories.CommentRepository;
import deepbluehaven.repositories.ContactInquiryRepository;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class CommentAndInquiryService {

    private final CommentRepository commentRepository;
    private final ContactInquiryRepository contactInquiryRepository;
    private final WorkerRepository workerRepository;

    public CommentAndInquiryService(CommentRepository commentRepository,
                                    ContactInquiryRepository contactInquiryRepository,
                                    WorkerRepository workerRepository) {
        this.commentRepository = commentRepository;
        this.contactInquiryRepository = contactInquiryRepository;
        this.workerRepository = workerRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentInquiryDTO.CommentView> getComments(String filter) {
        List<Comment> comments;
        if ("complaints".equalsIgnoreCase(filter)) {
            comments = commentRepository.findByIsComplaintTrueOrderByCreatedAtDesc();
        } else if ("positive".equalsIgnoreCase(filter)) {
            comments = commentRepository.findByRatingGreaterThanEqualOrderByCreatedAtDesc(4);
        } else if ("negative".equalsIgnoreCase(filter)) {
            comments = commentRepository.findByRatingLessThanEqualOrderByCreatedAtDesc(3);
        } else {
            comments = commentRepository.findAllByOrderByCreatedAtDesc();
        }

        if ("unanswered".equalsIgnoreCase(filter)) {
            comments = comments.stream()
                    .filter(c -> c.getResponse() == null || c.getResponse().trim().isEmpty())
                    .collect(Collectors.toList());
        }

        return comments.stream().map(this::toCommentView).collect(Collectors.toList());
    }

    @Transactional
    public boolean replyToComment(Long commentId, String responseText, Long workerId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return false;

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;
        comment.setResponse(responseText);
        comment.setRespondedBy(worker);
        comment.setRespondedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(comment.getIsComplaint())) {
            comment.setIsResolved(true);
        }
        commentRepository.save(comment);
        return true;
    }

    @Transactional
    public boolean toggleResolveComplaint(Long commentId, Boolean isResolved) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return false;

        comment.setIsResolved(isResolved != null ? isResolved : !Boolean.TRUE.equals(comment.getIsResolved()));
        commentRepository.save(comment);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CommentInquiryDTO.InquiryView> getInquiries(String status) {
        List<ContactInquiry> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            try {
                InquiryStatus inquiryStatus = InquiryStatus.valueOf(status.toUpperCase());
                list = contactInquiryRepository.findByStatusOrderByCreatedAtDesc(inquiryStatus);
            } catch (Exception e) {
                list = contactInquiryRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            list = contactInquiryRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream().map(this::toInquiryView).collect(Collectors.toList());
    }

    @Transactional
    public boolean updateInquiry(Long inquiryId, InquiryStatus status, String replyNotes, Long workerId) {
        ContactInquiry inquiry = contactInquiryRepository.findById(inquiryId).orElse(null);
        if (inquiry == null) return false;

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;
        if (status != null) {
            inquiry.setStatus(status);
        }
        if (replyNotes != null) {
            inquiry.setReplyNotes(replyNotes);
        }
        if (status == InquiryStatus.RESOLVED || status == InquiryStatus.CONTACTED) {
            inquiry.setResolvedBy(worker);
            inquiry.setResolvedAt(LocalDateTime.now());
        }
        contactInquiryRepository.save(inquiry);
        return true;
    }

    @Transactional
    public ContactInquiry saveNewInquiry(String fullName, String email, String phone, String resortLocation, String inquiryType, String message) {
        ContactInquiry inquiry = new ContactInquiry();
        inquiry.setFullName(fullName);
        inquiry.setEmail(email);
        inquiry.setPhone(phone);
        inquiry.setResortLocation(resortLocation);
        inquiry.setInquiryType(inquiryType);
        inquiry.setMessage(message);
        inquiry.setStatus(InquiryStatus.PENDING);
        return contactInquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public CommentInquiryDTO.Statistics getStatistics() {
        CommentInquiryDTO.Statistics stats = new CommentInquiryDTO.Statistics();
        Double avgRating = commentRepository.getAverageRating();
        stats.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);
        stats.setTotalComments((long) commentRepository.findAll().size());
        stats.setComplaintsCount(commentRepository.countComplaints());
        stats.setUnansweredCount(commentRepository.countUnanswered());
        stats.setTotalInquiries((long) contactInquiryRepository.findAll().size());
        stats.setPendingInquiries(contactInquiryRepository.countPendingInquiries());
        return stats;
    }

    private CommentInquiryDTO.CommentView toCommentView(Comment c) {
        CommentInquiryDTO.CommentView view = new CommentInquiryDTO.CommentView();
        view.setId(c.getId());
        if (c.getCustomer() != null) {
            view.setCustomerId(c.getCustomer().getId());
            if (c.getCustomer().getProfile() != null) {
                view.setCustomerName(c.getCustomer().getProfile().getFullName());
                view.setCustomerAvatar(c.getCustomer().getProfile().getAvatarUrl());
            } else {
                view.setCustomerName("Guest #" + c.getCustomer().getId());
            }
        }

        if (c.getRoom() != null) {
            view.setTargetType("ROOM");
            view.setTargetName(c.getRoom().getRoomNumber() + " (" + c.getRoom().getRoomType() + ")");
        } else if (c.getService() != null) {
            view.setTargetType("SERVICE");
            view.setTargetName(c.getService().getName());
        } else if (c.getResort() != null) {
            view.setTargetType("RESORT");
            view.setTargetName(c.getResort().getName());
        } else {
            view.setTargetType("GENERAL");
            view.setTargetName("Resort Experience");
        }

        view.setRating(c.getRating() != null ? c.getRating() : 5);
        view.setContent(c.getContent());
        view.setIsComplaint(c.getIsComplaint());
        view.setIsResolved(c.getIsResolved());
        view.setResponse(c.getResponse());
        if (c.getRespondedBy() != null && c.getRespondedBy().getProfile() != null) {
            view.setRespondedByName(c.getRespondedBy().getProfile().getFullName());
        }
        view.setRespondedAt(c.getRespondedAt());
        view.setImages(c.getImages());
        view.setCreatedAt(c.getCreatedAt());
        return view;
    }

    private CommentInquiryDTO.InquiryView toInquiryView(ContactInquiry ci) {
        CommentInquiryDTO.InquiryView view = new CommentInquiryDTO.InquiryView();
        view.setId(ci.getId());
        view.setFullName(ci.getFullName());
        view.setEmail(ci.getEmail());
        view.setPhone(ci.getPhone());
        view.setResortLocation(ci.getResortLocation());
        view.setInquiryType(ci.getInquiryType());
        view.setMessage(ci.getMessage());
        view.setStatus(ci.getStatus());
        view.setReplyNotes(ci.getReplyNotes());
        if (ci.getResolvedBy() != null && ci.getResolvedBy().getProfile() != null) {
            view.setResolvedByName(ci.getResolvedBy().getProfile().getFullName());
        }
        view.setResolvedAt(ci.getResolvedAt());
        view.setCreatedAt(ci.getCreatedAt());
        return view;
    }
}
