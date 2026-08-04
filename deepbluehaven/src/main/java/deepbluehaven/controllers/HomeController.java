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
import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.CustomerService;
import deepbluehaven.services.DiscountService;
import deepbluehaven.services.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final DiscountService discountService;

    public HomeController(CustomerService customerService, RoomService roomService, BookingService bookingService, DiscountService discountService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.discountService = discountService;
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
    public String roomsList(Model model) {
        model.addAttribute("rooms", roomService.getAvailableRoomCards());
        model.addAttribute("resortOptions", roomService.getResortFilterOptions());
        return "customer/rooms-list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Long id, Model model) {
        return roomService.getRoomById(id)
                .map(room -> {
                    model.addAttribute("room", room);
                    model.addAttribute("roomAmenities", roomService.getAmenityViews(room));
                    return "customer/room-detail";
                })
                .orElse("redirect:/404");
    }

    @GetMapping("/services")
    public String showServices(@RequestParam(value = "bookingCode", required = false) String bookingCode, Model model, HttpServletRequest request) {
        List<ServiceDTO.Response> services = customerService.getVisibleServices();
        model.addAttribute("services", services);

        HttpSession session = request.getSession(false);
        boolean hasValidBooking = false;

        if (session != null && session.getAttribute("loggedInCustomerId") != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
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

        model.addAttribute("hasValidBooking", hasValidBooking);
        return "customer/service";
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
}