package deepbluehaven.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final LogService logService;
    private static final BigDecimal EXCHANGE_RATE_USD = new BigDecimal("26200");

    public BookingService(BookingRepository bookingRepository,
                          ServiceOrderRepository serviceOrderRepository,
                          CustomerRepository customerRepository,
                          RoomRepository roomRepository,
                          NotificationService notificationService,
                          LogService logService) {
        this.bookingRepository = bookingRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
        this.logService = logService;
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
    public List<BookingHistoryDTO.Response> getAllBookingsForStaff() {
        List<Booking> bookings = bookingRepository.findAllWithDetailsAndRoom();
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
        BigDecimal subTotal = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal vatTax = subTotal.multiply(new BigDecimal("0.10")); // 10% VAT tax
        BigDecimal totalAmount = subTotal.add(vatTax);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setNote(note != null ? note : "Customer room booking (Pending approval)");

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setRoom(room);
        detail.setRoomType(room.getRoomType());
        detail.setCheckIn(targetCheckIn);
        detail.setCheckOut(targetCheckOut);
        detail.setPricePerNight(pricePerNight);
        detail.setSubTotal(subTotal);
        detail.setStatus(BookingStatus.PENDING);
        detail.setAction("CREATE_BOOKING_DETAIL");

        booking.getDetails().add(detail);
        bookingRepository.save(booking);

        int year = booking.getBookingTime().getYear();
        String bookingCode = String.format("DBH-%d-%03d", year, booking.getId());

        logService.logBookingStatusChange(booking, null, BookingStatus.PENDING, customerId, "Customer created booking " + bookingCode);

        notificationService.createCustomerNotification(
            customer,
            "Đặt phòng thành công",
            "Đơn đặt phòng " + bookingCode + " (" + room.getRoomType() + ") đã được tiếp nhận và đang chờ Lễ tân/Quản lý xác nhận.",
            NotificationType.BOOKING,
            "/booking/history"
        );

        return toHistoryResponse(booking, LocalDate.now());
    }

    @Transactional
    public boolean confirmBookingByStaff(Long bookingId, Long workerId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }
        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.PENDING) {
            return false;
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        if (booking.getDetails() != null) {
            for (BookingDetail d : booking.getDetails()) {
                d.setStatus(BookingStatus.CONFIRMED);
            }
        }
        bookingRepository.save(booking);
        logService.logBookingStatusChange(booking, oldStatus, BookingStatus.CONFIRMED, workerId, "Confirmed by staff #" + workerId);
        return true;
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
                    BookingStatus oldStatus = booking.getStatus();
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    logService.logBookingStatusChange(booking, oldStatus, BookingStatus.CANCELLED, customerId, "Cancelled by customer #" + customerId);
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
                logService.log(ObjectType.SERVICE, ActionCode.DELETE, orderId, "Customer #" + customerId + " cancelled service order #" + orderId);
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
            dto.setRoomNumber(firstDetail.getRoom() != null ? firstDetail.getRoom().getRoomNumber() : "—");
            dto.setRoomType(firstDetail.getRoomType() != null ? firstDetail.getRoomType().name() : "—");
            dto.setCheckIn(firstDetail.getCheckIn());
            dto.setCheckOut(firstDetail.getCheckOut());

            if (firstDetail.getCheckIn() != null && firstDetail.getCheckOut() != null) {
                long nights = ChronoUnit.DAYS.between(firstDetail.getCheckIn(), firstDetail.getCheckOut());
                dto.setNights(Math.max((int) nights, 1));
            } else {
                dto.setNights(1);
            }
        } else {
            dto.setRoomNumber("—");
            dto.setRoomType("—");
            dto.setCheckIn(today);
            dto.setCheckOut(today.plusDays(1));
            dto.setNights(1);
        }

        dto.setTotalAmountVnd(amountVnd);
        BigDecimal amountUsd = amountVnd.divide(EXCHANGE_RATE_USD, 2, RoundingMode.HALF_UP);
        dto.setTotalAmountUsd(amountUsd);

        dto.setRawStatus(booking.getStatus() != null ? booking.getStatus() : BookingStatus.PENDING);
        dto.setStatusText(getStatusLabel(booking.getStatus()));

        List<BookingHistoryDTO.ServiceItem> serviceItems = new ArrayList<>();
        if (booking.getDetails() != null) {
            for (BookingDetail d : booking.getDetails()) {
                Booking bObj = d.getBooking();
                if (bObj != null) {
                    List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(bObj.getId());
                    for (ServiceOrder order : orders) {
                        String name = (order.getService() != null) ? order.getService().getName() : "Extended Service";
                        int qty = order.getQuantity();
                        BigDecimal price = (order.getService() != null && order.getService().getBasePrice() != null) ? order.getService().getBasePrice() : BigDecimal.ZERO;
                        BigDecimal itemTotal = (order.getTotalPrice() != null) ? order.getTotalPrice() : price.multiply(BigDecimal.valueOf(qty));
                        boolean canCancel = (order.getStatus() == ServiceOrderStatus.PENDING || order.getStatus() == ServiceOrderStatus.CONFIRMED);
                        serviceItems.add(new BookingHistoryDTO.ServiceItem(order.getId(), name, qty, price, itemTotal, order.getStatus() != null ? order.getStatus().name() : "PENDING", canCancel));
                    }
                }
            }
        }
        dto.setServiceItems(serviceItems);

        return dto;
    }

    private String getStatusLabel(BookingStatus status) {
        if (status == null) return "Chờ xác nhận";
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case CHECKED_IN: return "Đã check-in";
            case CHECKED_OUT: return "Đã check-out";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
}
