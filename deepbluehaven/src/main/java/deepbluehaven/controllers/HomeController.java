package deepbluehaven.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.services.CustomerService;
import deepbluehaven.services.RoomService;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final RoomService roomService;

    public HomeController(CustomerService customerService, RoomService roomService) {
        this.customerService = customerService;
        this.roomService = roomService;
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
    public String showServices(Model model) {

        List<ServiceDTO.Response> services = customerService.getVisibleServices();
        model.addAttribute("services", services);
        return "customer/service";
    }

    @PostMapping("/services/confirm")
    public String confirmServices() {
        return "redirect:/services";
    }

    @GetMapping("/profile")
    public String showCustomerProfile() {
        return "customer/profile";
    }

    @GetMapping("/booking/history")
    public String showBookingHistory() {
        return "customer/booking-history";
    }

    @GetMapping("/offers")
    public String showOffers() {
        return "customer/offers";
    }

    @GetMapping("/offers/{offerCode}")
    public String showOfferDetail(
            @PathVariable String offerCode,
            Model model) {

        model.addAttribute(
                "offerCode",
                offerCode
        );

        return "customer/offer-detail";
    }
}