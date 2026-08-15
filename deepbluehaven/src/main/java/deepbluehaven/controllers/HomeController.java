package deepbluehaven.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import deepbluehaven.dto.BookingHistoryDTO;
import deepbluehaven.dto.CustomerProfileDTO;
import deepbluehaven.dto.RoomCardViewDTO;
import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.CustomerService;
import deepbluehaven.services.DiscountService;
import deepbluehaven.services.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import deepbluehaven.pojo.Comment;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Room;
import deepbluehaven.repositories.CommentRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.services.LogService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final DiscountService discountService;
    private final CommentRepository commentRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final LogService logService;
    private final deepbluehaven.services.CommentAndInquiryService commentAndInquiryService;

    public HomeController(CustomerService customerService,
                          RoomService roomService,
                          BookingService bookingService,
                          DiscountService discountService,
                          CommentRepository commentRepository,
                          CustomerRepository customerRepository,
                          RoomRepository roomRepository,
                          LogService logService,
                          deepbluehaven.services.CommentAndInquiryService commentAndInquiryService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.discountService = discountService;
        this.commentRepository = commentRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.logService = logService;
        this.commentAndInquiryService = commentAndInquiryService;
    }


    @GetMapping("/")
    public String index() {
        return "customer/home";
    }

    @GetMapping("/home")
    public String home() {
        return "customer/home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "customer/dashboard";
    }

    @GetMapping("/rooms")
    public String roomsList(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        java.util.Set<Long> favoriteRoomIds = java.util.Collections.emptySet();
        if (session != null && session.getAttribute("loggedInCustomerId") != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            favoriteRoomIds = customerService.getFavoriteRoomIds(customerId);
        }
        model.addAttribute("rooms", roomService.getAvailableRoomCards(favoriteRoomIds));
        model.addAttribute("resortOptions", roomService.getResortFilterOptions());
        return "customer/rooms-list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isFavorite = false;
        if (session != null && session.getAttribute("loggedInCustomerId") != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            isFavorite = customerService.getFavoriteRoomIds(customerId).contains(id);
        }
        final boolean finalIsFavorite = isFavorite;
        return roomService.getRoomById(id)
                .map(room -> {
                    model.addAttribute("room", room);
                    model.addAttribute("roomAmenities", roomService.getAmenityViews(room));
                    model.addAttribute("isFavorite", finalIsFavorite);
                    return "customer/room-detail";
                })
                .orElse("redirect:/404");
    }

    @GetMapping("/services")
    public String showServices(@RequestParam(value = "bookingCode", required = false) String bookingCode, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        java.util.Set<Long> favoriteServiceIds = java.util.Collections.emptySet();
        boolean hasValidBooking = false;

        if (session != null && session.getAttribute("loggedInCustomerId") != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            favoriteServiceIds = customerService.getFavoriteServiceIds(customerId);
            List<BookingHistoryDTO.Response> validBookings = bookingService.getValidBookingsByCustomer(customerId);

            if (validBookings != null && !validBookings.isEmpty()) {
                hasValidBooking = true;
                model.addAttribute("customerBookings", validBookings);

                BookingHistoryDTO.Response selectedBooking = null;
                if (bookingCode != null && !bookingCode.isBlank()) {
                    selectedBooking = bookingService.getBookingByCode(bookingCode, customerId);
                    if (selectedBooking != null) {
                        deepbluehaven.pojo.enums.BookingStatus st = selectedBooking.getRawStatus();
                        if (st == deepbluehaven.pojo.enums.BookingStatus.CANCELLED || st == deepbluehaven.pojo.enums.BookingStatus.CHECKED_OUT || st == deepbluehaven.pojo.enums.BookingStatus.COMPLETED) {
                            selectedBooking = null;
                        }
                    }
                }
                if (selectedBooking == null) {
                    selectedBooking = validBookings.get(0);
                }
                model.addAttribute("selectedBooking", selectedBooking);
                model.addAttribute("selectedBookingCode", selectedBooking.getBookingCode());
            }
        }

        List<ServiceDTO.Response> services = customerService.getVisibleServices(favoriteServiceIds);
        model.addAttribute("services", services);
        model.addAttribute("hasValidBooking", hasValidBooking);
        return "customer/service";
    }

    @GetMapping("/favorites")
    public String favorites(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return "redirect:/login";
        }
        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        List<RoomCardViewDTO> favoriteRooms = customerService.getFavoriteRooms(customerId);
        List<ServiceDTO.Response> favoriteServices = customerService.getFavoriteServices(customerId);

        model.addAttribute("favoriteRooms", favoriteRooms);
        model.addAttribute("favoriteServices", favoriteServices);
        model.addAttribute("roomsCount", favoriteRooms.size());
        model.addAttribute("servicesCount", favoriteServices.size());
        model.addAttribute("totalCount", favoriteRooms.size() + favoriteServices.size());
        return "customer/favorites";
    }

    @PostMapping("/services/confirm")
    public String confirmServices() {
        return "redirect:/services";
    }

    @GetMapping("/profile")
    public String showCustomerProfile(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return "redirect:/login";
        }
        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        CustomerProfileDTO.Response profile = customerService.getCustomerProfile(customerId);
        model.addAttribute("profile", profile);

        int totalBookings = bookingService.getBookingHistoryByCustomer(customerId).size();
        int upcomingStays = bookingService.getValidBookingsByCustomer(customerId).size();
        model.addAttribute("totalBookingsCount", totalBookings);
        model.addAttribute("upcomingStaysCount", upcomingStays);

        return "customer/profile";
    }

    @GetMapping("/booking/history")
    public String bookingHistory(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return "redirect:/login";
        }

        Long currentCustomerId = (Long) session.getAttribute("loggedInCustomerId");

        List<BookingHistoryDTO.Response> bookings = bookingService.getBookingHistoryByCustomer(currentCustomerId);
        model.addAttribute("bookings", bookings);   
        return "customer/booking-history";
    }

    @PostMapping("/booking/comment/save")
    public String saveBookingComment(@RequestParam(value = "roomId", required = false) Long roomId,
                                     @RequestParam("rating") Integer rating,
                                     @RequestParam("content") String content,
                                     @RequestParam(value = "isComplaint", required = false, defaultValue = "false") Boolean isComplaint,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttrs) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return "redirect:/login";
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");

        try {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer != null) {
                Comment comment = new Comment();
                comment.setCustomer(customer);
                comment.setContent(content);
                comment.setRating(rating != null ? Math.min(Math.max(rating, 1), 5) : 5);
                comment.setIsComplaint(Boolean.TRUE.equals(isComplaint));
                if (roomId != null) {
                    Room room = roomRepository.findById(roomId).orElse(null);
                    comment.setRoom(room);
                }
                commentRepository.save(comment);

                logService.log(deepbluehaven.pojo.enums.ObjectType.SYSTEM, deepbluehaven.pojo.enums.ActionCode.CREATE, comment.getId(),
                        "Customer #" + customerId + " submitted review rating " + rating + " stars", session);

                redirectAttrs.addFlashAttribute("successMessage", "Thank you! Your review has been submitted successfully.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to submit review: " + e.getMessage());
        }

        return "redirect:/booking/history";
    }

    @GetMapping("/offers")
    public String showOffers(Model model) {
        model.addAttribute("offers", discountService.getActiveOffers());
        return "customer/offers";
    }

    @GetMapping("/offers/{offerCode}")
    public String showOfferDetail(@PathVariable String offerCode, Model model) {
        return discountService.getOfferByCode(offerCode)
                .map(offer -> {
                    model.addAttribute("offer", offer);
                    return "fragments/offer-detail :: offerDetail";
                })
                .orElse("redirect:/404");
    }

    @GetMapping("/about")
    public String showAbout(Model model) {
        return "customer/about";
    }

    @GetMapping("/contact")
    public String showContact(Model model) {
        model.addAttribute("resorts", roomService.getResortFilterOptions());
        return "customer/contact";
    }

    @PostMapping("/contact/send")
    public String sendContactInquiry(@RequestParam("fullName") String fullName,
                                     @RequestParam("email") String email,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     @RequestParam(value = "resortLocation", required = false) String resortLocation,
                                     @RequestParam(value = "inquiryType", required = false) String inquiryType,
                                     @RequestParam("message") String message,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttrs) {
        HttpSession session = request.getSession(false);
        try {
            commentAndInquiryService.saveNewInquiry(fullName, email, phone, resortLocation, inquiryType, message);
            logService.log(deepbluehaven.pojo.enums.ObjectType.SYSTEM, deepbluehaven.pojo.enums.ActionCode.CREATE, null,
                    "Inquiry received from " + fullName + " (" + email + (phone != null ? ", " + phone : "") + ") regarding " + inquiryType + " at " + resortLocation + ": " + message, session);
            redirectAttrs.addFlashAttribute("successMessage", "Thank you, " + fullName + "! Your inquiry has been sent to our concierge team. We will get back to you shortly.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Could not send message. Please contact our 24/7 hotline directly.");
        }
        return "redirect:/contact";
    }
}