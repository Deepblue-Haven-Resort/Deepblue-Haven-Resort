package deepbluehaven.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.BookingHistoryDTO;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.repositories.CustomerRepository;

import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceOrderRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private static final BigDecimal EXCHANGE_RATE_USD = new BigDecimal("26200");

    public BookingService(BookingRepository bookingRepository,
                          ServiceOrderRepository serviceOrderRepository,
                          CustomerRepository customerRepository,
                          RoomRepository roomRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryDTO.Response> getBookingHistoryByCustomer(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetailsAndRoom(customerId);
        List<BookingHistoryDTO.Response> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Booking booking : bookings) {
            result.add(toHistoryResponse(booking, today));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryDTO.Response> getValidBookingsByCustomer(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetailsAndRoom(customerId);
        List<BookingHistoryDTO.Response> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Booking booking : bookings) {
            BookingStatus status = booking.getStatus();
            if (status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED || status == BookingStatus.CHECKED_IN) {
                result.add(toHistoryResponse(booking, today));
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public BookingHistoryDTO.Response getBookingByCode(String bookingCode, Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetailsAndRoom(customerId);
        LocalDate today = LocalDate.now();
        for (Booking booking : bookings) {
            int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : today.getYear();
            String code = String.format("DBH-%d-%03d", year, booking.getId());
            if (code.equalsIgnoreCase(bookingCode) || String.valueOf(booking.getId()).equals(bookingCode)) {
                return toHistoryResponse(booking, today);
            }
        }
        return null;
    }

    @Transactional
    public BookingHistoryDTO.Response createRoomBooking(Long roomId, LocalDate checkIn, LocalDate checkOut, String note, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Room room = roomRepository.findByIdWithPessimisticLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        LocalDate targetCheckIn = (checkIn != null) ? checkIn : LocalDate.now().plusDays(1);
        LocalDate targetCheckOut = (checkOut != null) ? checkOut : targetCheckIn.plusDays(1);

        if (!targetCheckOut.isAfter(targetCheckIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
        boolean isOverlapped = bookingRepository.existsOverlappingBooking(roomId, targetCheckIn, targetCheckOut, activeStatuses);

        if (isOverlapped) {
            throw new IllegalStateException("Room is already booked from " + targetCheckIn + " to " + targetCheckOut + ". Please select another date.");
        }

        long nights = ChronoUnit.DAYS.between(targetCheckIn, targetCheckOut);
        nights = Math.max(nights, 1);

        BigDecimal pricePerNight = room.getBasePrice() != null ? room.getBasePrice() : new BigDecimal("1500000");
        BigDecimal totalAmount = pricePerNight.multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setNote(note != null ? note : "Customer room booking");

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setRoom(room);
        detail.setRoomType(room.getRoomType());
        detail.setCheckIn(targetCheckIn);
        detail.setCheckOut(targetCheckOut);
        detail.setPricePerNight(pricePerNight);
        detail.setSubTotal(totalAmount);
        detail.setStatus(BookingStatus.CONFIRMED);
        detail.setAction("CREATE_BOOKING_DETAIL");

        booking.getDetails().add(detail);
        bookingRepository.save(booking);

        int year = booking.getBookingTime().getYear();
        String bookingCode = String.format("DBH-%d-%03d", year, booking.getId());

        notificationService.createCustomerNotification(
            customer,
            "Đặt phòng thành công",
            "Đơn đặt phòng " + bookingCode + " (" + room.getRoomType() + ") đã được xác nhận thành công.",
            NotificationType.BOOKING,
            "/booking/history"
        );

        return toHistoryResponse(booking, LocalDate.now());
    }

    @Transactional
    public boolean cancelBooking(String bookingCode, Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetailsAndRoom(customerId);
        LocalDate today = LocalDate.now();
        for (Booking booking : bookings) {
            int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : today.getYear();
            String code = String.format("DBH-%d-%03d", year, booking.getId());
            if (code.equalsIgnoreCase(bookingCode) || String.valueOf(booking.getId()).equals(bookingCode)) {
                if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    notificationService.createCustomerNotification(
                        booking.getCustomer(),
                        "Đã hủy đơn đặt phòng",
                        "Đơn đặt phòng " + code + " đã được hủy thành công.",
                        NotificationType.BOOKING,
                        "/booking/history"
                    );
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional
    public boolean cancelServiceOrder(Long orderId, Long customerId) {
        return serviceOrderRepository.findById(orderId).map(order -> {
            boolean isOwner = (order.getCustomer() != null && order.getCustomer().getId().equals(customerId)) ||
                              (order.getBooking() != null && order.getBooking().getCustomer() != null && order.getBooking().getCustomer().getId().equals(customerId));
            if (!isOwner) return false;
            if (order.getStatus() == ServiceOrderStatus.PENDING || order.getStatus() == ServiceOrderStatus.CONFIRMED) {
                order.setStatus(ServiceOrderStatus.CANCELLED);
                serviceOrderRepository.save(order);
                Customer cust = order.getCustomer() != null ? order.getCustomer() : (order.getBooking() != null ? order.getBooking().getCustomer() : null);
                if (cust != null) {
                    String serviceName = order.getService() != null ? order.getService().getName() : "dịch vụ";
                    notificationService.createCustomerNotification(
                        cust,
                        "Đã hủy dịch vụ",
                        "Dịch vụ " + serviceName + " đã được hủy thành công.",
                        NotificationType.SERVICE,
                        "/booking/history"
                    );
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    private BookingHistoryDTO.Response toHistoryResponse(Booking booking, LocalDate today) {
        BookingHistoryDTO.Response dto = new BookingHistoryDTO.Response();

        int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : today.getYear();
        String bookingCode = String.format("DBH-%d-%03d", year, booking.getId());
        dto.setBookingCode(bookingCode);
        dto.setId(booking.getId());

        Customer customer = booking.getCustomer();
        if (customer != null) {
            CustomerProfile profile = customer.getProfile();
            if (profile != null) {
                dto.setGuestName(profile.getFullName() != null ? profile.getFullName() : customer.getUsername());
                dto.setGuestEmail(profile.getEmail() != null ? profile.getEmail() : "customer@deepbluehaven.com");
                dto.setGuestPhone(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "—");
            } else {
                dto.setGuestName(customer.getUsername());
                dto.setGuestEmail("customer@deepbluehaven.com");
                dto.setGuestPhone("—");
            }
        }

        BigDecimal amountVnd = (booking.getTotalAmount() != null) ? booking.getTotalAmount() : BigDecimal.ZERO;
        BookingDetail firstDetail = (booking.getDetails() != null && !booking.getDetails().isEmpty()) ? booking.getDetails().get(0) : null;

        if (firstDetail != null) {
            if (firstDetail.getRoom() != null) {
                Room room = firstDetail.getRoom();
                String formattedType = (room.getRoomType() != null) ? room.getRoomType().name() : "Standard";
                String roomNum = (room.getRoomNumber() != null) ? room.getRoomNumber() : "";

                dto.setRoomName(formattedType + " " + roomNum);
                dto.setRoomNumber(roomNum);
                dto.setRoomType(formattedType + " Room");
            } else {
                dto.setRoomName("Resort Accommodation");
                dto.setRoomNumber("To be assigned");
                dto.setRoomType("Standard Room");
            }

            dto.setCheckIn(firstDetail.getCheckIn());
            dto.setCheckOut(firstDetail.getCheckOut());

            if (firstDetail.getCheckIn() != null && firstDetail.getCheckOut() != null) {
                long nights = ChronoUnit.DAYS.between(firstDetail.getCheckIn(), firstDetail.getCheckOut());
                dto.setNights(Math.max(nights, 1));
            } else {
                dto.setNights(1);
            }

            BigDecimal pricePerNight = (firstDetail.getPricePerNight() != null)
                ? firstDetail.getPricePerNight() : (firstDetail.getRoom() != null && firstDetail.getRoom().getBasePrice() != null)
                ? firstDetail.getRoom().getBasePrice() : amountVnd.divide(BigDecimal.valueOf(Math.max(dto.getNights(), 1)), 0, RoundingMode.HALF_UP);

            BigDecimal roomCharge = (firstDetail.getSubTotal() != null) ? firstDetail.getSubTotal() : pricePerNight.multiply(BigDecimal.valueOf(dto.getNights()));
            dto.setPricePerNightVnd(pricePerNight);
            dto.setRoomChargeVnd(roomCharge);
        } else {
            dto.setNights(1);
            dto.setPricePerNightVnd(amountVnd);
            dto.setRoomChargeVnd(amountVnd);
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(booking.getId());
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        List<BookingHistoryDTO.ServiceItem> serviceItems = new ArrayList<>();

        if (orders != null) {
            for (ServiceOrder order : orders) {
                if (order.getStatus() == ServiceOrderStatus.CANCELLED) {
                    continue;
                }
                String name = (order.getService() != null) ? order.getService().getName() : "Service Add-on";
                int qty = (order.getQuantity() != null) ? order.getQuantity() : 1;
                BigDecimal price = (order.getService() != null && order.getService().getBasePrice() != null) ? order.getService().getBasePrice() : BigDecimal.ZERO;
                BigDecimal itemTotal = (order.getTotalPrice() != null) ? order.getTotalPrice() : price.multiply(BigDecimal.valueOf(qty));
                boolean canCancel = (order.getStatus() == ServiceOrderStatus.PENDING || order.getStatus() == ServiceOrderStatus.CONFIRMED);

                totalServiceCharge = totalServiceCharge.add(itemTotal);
                serviceItems.add(new BookingHistoryDTO.ServiceItem(order.getId(), name, qty, price, itemTotal, order.getStatus() != null ? order.getStatus().name() : "CONFIRMED", canCancel));
            }
        }

        dto.setServiceItems(serviceItems);
        dto.setServiceChargeVnd(totalServiceCharge);

        BigDecimal roomAndServices = dto.getRoomChargeVnd().add(totalServiceCharge);
        BigDecimal tax = roomAndServices.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);
        dto.setTaxVnd(tax);
        dto.setDiscountVnd(BigDecimal.ZERO);

        BigDecimal finalTotal = roomAndServices.add(tax);
        dto.setTotalAmountVnd(finalTotal);

        BigDecimal amountUsd = finalTotal.divide(EXCHANGE_RATE_USD, 0, RoundingMode.HALF_UP);
        dto.setTotalAmountUsd(amountUsd);

        dto.setSpecialRequest(booking.getNote());
        dto.setBookedOn(booking.getBookingTime());
        dto.setRawStatus(booking.getStatus());

        applyPresentationFormatting(dto, booking.getStatus(), firstDetail, today);

        return dto;
    }

    private void applyPresentationFormatting(BookingHistoryDTO.Response dto, BookingStatus status, BookingDetail firstDetail, LocalDate today) {
        boolean needsManualCheckIn = false;
        String timeGroup = "upcoming";
        String statusText = "Pending";
        String statusClass = "booking-status--upcoming";

        if (status == BookingStatus.CANCELLED) {
            timeGroup = "cancelled";
            statusText = "Cancelled";
            statusClass = "booking-status--cancelled";
        } else if (status == BookingStatus.COMPLETED || status == BookingStatus.CHECKED_OUT) {
            timeGroup = "completed";
            statusText = "Completed";
            statusClass = "booking-status--completed";
        } else if (status == BookingStatus.CHECKED_IN) {
            timeGroup = "current";
            statusText = "Checked In";
            statusClass = "booking-status--upcoming booking-status--current";
        } else if (status == BookingStatus.CONFIRMED) {
            boolean isWithinStayDates = false;
            if (firstDetail != null && firstDetail.getCheckIn() != null && firstDetail.getCheckOut() != null) {
                isWithinStayDates = (!today.isBefore(firstDetail.getCheckIn())) && (!today.isAfter(firstDetail.getCheckOut()));
            }

            if (isWithinStayDates) {
                timeGroup = "current";
                statusText = "Confirmed";
                statusClass = "booking-status--upcoming booking-status--current";
                needsManualCheckIn = true;
            } else {
                timeGroup = "upcoming";
                statusText = "Confirmed";
                statusClass = "booking-status--upcoming";
            }
        } else {
            timeGroup = "upcoming";
            statusText = "Pending";
            statusClass = "booking-status--upcoming";
        }

        dto.setTimeGroup(timeGroup);
        dto.setStatusText(statusText);
        dto.setStatusClass(statusClass);
        dto.setNeedsManualCheckIn(needsManualCheckIn);
    }
}
