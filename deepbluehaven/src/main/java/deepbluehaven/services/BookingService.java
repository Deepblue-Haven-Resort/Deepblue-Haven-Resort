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
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.pojo.InventoryItem;
import deepbluehaven.pojo.InventoryTransaction;
import deepbluehaven.pojo.Log;
import deepbluehaven.pojo.PricingRule;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.InventoryItemRepository;
import deepbluehaven.repositories.InventoryTransactionRepository;
import deepbluehaven.repositories.LogRepository;
import deepbluehaven.repositories.PricingRuleRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceOrderRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final LogRepository logRepository;
    private final NotificationService notificationService;
    private final LogService logService;
    private static final BigDecimal EXCHANGE_RATE_USD = new BigDecimal("26200");

    public BookingService(BookingRepository bookingRepository,
                          ServiceOrderRepository serviceOrderRepository,
                          CustomerRepository customerRepository,
                          RoomRepository roomRepository,
                          PricingRuleRepository pricingRuleRepository,
                          InventoryItemRepository inventoryItemRepository,
                          InventoryTransactionRepository inventoryTransactionRepository,
                          LogRepository logRepository,
                          NotificationService notificationService,
                          LogService logService) {
        this.bookingRepository = bookingRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.logRepository = logRepository;
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

        BigDecimal pricePerNight = room.getBasePrice() != null ? room.getBasePrice() : new BigDecimal("2100000");
        if (room.getRoomType() != null && pricingRuleRepository != null) {
            try {
                List<PricingRule> rules = pricingRuleRepository.findByRoomType(room.getRoomType());
                if (rules != null && !rules.isEmpty() && rules.get(0).getMultiplier() != null) {
                    pricePerNight = pricePerNight.multiply(rules.get(0).getMultiplier()).setScale(0, RoundingMode.HALF_UP);
                }
            } catch (Exception ignored) {}
        }
        BigDecimal subTotal = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal vatTax = subTotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
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

                    // Auto-cancel any pending service orders for this booking
                    List<ServiceOrder> serviceOrders = serviceOrderRepository.findByBookingId(booking.getId());
                    if (serviceOrders != null) {
                        for (ServiceOrder so : serviceOrders) {
                            if (so.getStatus() == ServiceOrderStatus.PENDING) {
                                so.setStatus(ServiceOrderStatus.CANCELLED);
                                so.setNote((so.getNote() != null && !so.getNote().isBlank() ? so.getNote() + " | " : "") + "Auto-cancelled upon booking cancellation");
                                serviceOrderRepository.save(so);
                            }
                        }
                    }

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

        BookingDetail firstDetail = (booking.getDetails() != null && !booking.getDetails().isEmpty()) ? booking.getDetails().get(0) : null;
        BigDecimal roomChargeVnd = BigDecimal.ZERO;
        BigDecimal pricePerNight = BigDecimal.ZERO;
        long nightsCount = 1;

        if (firstDetail != null) {
            if (firstDetail.getRoom() != null) {
                Room r = firstDetail.getRoom();
                dto.setRoomId(r.getId());
                dto.setRoomNumber(r.getRoomNumber());
                String typeName = (r.getRoomType() != null) ? r.getRoomType().name() : (firstDetail.getRoomType() != null ? firstDetail.getRoomType().name() : "Standard");
                dto.setRoomName("Room " + r.getRoomNumber() + " (" + typeName + ")");
                if (r.getImages() != null && !r.getImages().isEmpty()) {
                    dto.setRoomImageUrl(r.getImages().get(0));
                }
                if (r.getBasePrice() != null) {
                    pricePerNight = r.getBasePrice();
                }
            } else {
                dto.setRoomNumber("—");
                String typeName = (firstDetail.getRoomType() != null) ? firstDetail.getRoomType().name() : "Standard";
                dto.setRoomName("Room (" + typeName + ")");
            }
            dto.setRoomType(firstDetail.getRoomType() != null ? firstDetail.getRoomType().name() : "—");
            dto.setCheckIn(firstDetail.getCheckIn());
            dto.setCheckOut(firstDetail.getCheckOut());

            if (firstDetail.getCheckIn() != null && firstDetail.getCheckOut() != null) {
                nightsCount = Math.max(ChronoUnit.DAYS.between(firstDetail.getCheckIn(), firstDetail.getCheckOut()), 1);
            }
            dto.setNights(nightsCount);
            if (firstDetail.getPricePerNight() != null) {
                pricePerNight = firstDetail.getPricePerNight();
            }
            if (firstDetail.getSubTotal() != null && firstDetail.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
                roomChargeVnd = firstDetail.getSubTotal();
            } else {
                roomChargeVnd = pricePerNight.multiply(BigDecimal.valueOf(nightsCount));
            }
        } else {
            dto.setRoomNumber("—");
            dto.setRoomType("—");
            dto.setRoomName("Resort Booking");
            dto.setCheckIn(today);
            dto.setCheckOut(today.plusDays(1));
            dto.setNights(1);
            roomChargeVnd = (booking.getTotalAmount() != null) ? booking.getTotalAmount() : BigDecimal.ZERO;
        }

        if (roomChargeVnd.compareTo(BigDecimal.ZERO) == 0 && booking.getTotalAmount() != null) {
            roomChargeVnd = booking.getTotalAmount();
        }
        dto.setPricePerNightVnd(pricePerNight);
        dto.setRoomChargeVnd(roomChargeVnd);

        BookingStatus status = booking.getStatus() != null ? booking.getStatus() : BookingStatus.PENDING;
        dto.setRawStatus(status);
        dto.setStatusText(getStatusLabel(status));

        String timeGroup;
        String statusClass;
        switch (status) {
            case PENDING:
                timeGroup = "upcoming";
                statusClass = "booking-status--pending";
                break;
            case CONFIRMED:
                timeGroup = "upcoming";
                statusClass = "booking-status--confirmed";
                break;
            case CHECKED_IN:
                timeGroup = "current";
                statusClass = "booking-status--current";
                break;
            case CHECKED_OUT:
                timeGroup = "completed";
                statusClass = "booking-status--completed";
                break;
            case CANCELLED:
                timeGroup = "cancelled";
                statusClass = "booking-status--cancelled";
                break;
            default:
                timeGroup = "all";
                statusClass = "booking-status--pending";
                break;
        }
        dto.setTimeGroup(timeGroup);
        dto.setStatusClass(statusClass);

        List<BookingHistoryDTO.ServiceItem> serviceItems = new ArrayList<>();
        BigDecimal totalServicesVnd = BigDecimal.ZERO;

        List<ServiceOrder> orders = new ArrayList<>();
        if (booking.getId() != null) {
            orders.addAll(serviceOrderRepository.findByBookingId(booking.getId()));
        }

        if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
            List<ServiceOrder> custOrders = serviceOrderRepository.findByCustomerId(booking.getCustomer().getId());
            for (ServiceOrder co : custOrders) {
                if (co.getBooking() == null && !orders.contains(co)) {
                    orders.add(co);
                }
            }
        }

        boolean isBookingActive = (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CHECKED_IN);

        for (ServiceOrder order : orders) {
            // If booking is already checked out, cancelled, or completed, auto-cancel any leftover pending services
            if (!isBookingActive && order.getStatus() == ServiceOrderStatus.PENDING) {
                order.setStatus(ServiceOrderStatus.CANCELLED);
                order.setNote((order.getNote() != null && !order.getNote().isBlank() ? order.getNote() + " | " : "") + "Auto-cancelled: Booking is " + (booking.getStatus() != null ? booking.getStatus().getDisplayName() : "Checked Out"));
                serviceOrderRepository.save(order);
            }

            String name = (order.getService() != null) ? order.getService().getName() : "Extended Service";
            int qty = (order.getQuantity() != null) ? order.getQuantity() : 1;
            BigDecimal price = (order.getService() != null && order.getService().getBasePrice() != null) ? order.getService().getBasePrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = (order.getTotalPrice() != null) ? order.getTotalPrice() : price.multiply(BigDecimal.valueOf(qty));
            boolean canCancel = isBookingActive && (order.getStatus() == ServiceOrderStatus.PENDING || order.getStatus() == ServiceOrderStatus.CONFIRMED);
            serviceItems.add(new BookingHistoryDTO.ServiceItem(order.getId(), name, qty, price, itemTotal, order.getStatus() != null ? order.getStatus().name() : "PENDING", canCancel));
            if (order.getStatus() != ServiceOrderStatus.CANCELLED) {
                totalServicesVnd = totalServicesVnd.add(itemTotal);
            }
        }
        dto.setServiceItems(serviceItems);
        dto.setServiceChargeVnd(totalServicesVnd);
        dto.setDiscountVnd(BigDecimal.ZERO);
        BigDecimal subtotal = roomChargeVnd.add(totalServicesVnd);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        dto.setTaxVnd(tax);

        BigDecimal finalTotalAmountVnd = subtotal.add(tax);
        dto.setTotalAmountVnd(finalTotalAmountVnd);
        BigDecimal amountUsd = finalTotalAmountVnd.divide(EXCHANGE_RATE_USD, 2, RoundingMode.HALF_UP);
        dto.setTotalAmountUsd(amountUsd);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ServiceOrder> getAllServiceOrdersForStaff() {
        return serviceOrderRepository.findAllWithDetailsOrderByOrderTimeDesc();
    }

    @Transactional
    public ServiceOrder updateServiceOrderStatus(Long orderId, ServiceOrderStatus newStatus, Worker worker) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Service order not found: " + orderId));

        ServiceOrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        if (worker != null) {
            order.setProcessedBy(worker);
        }

        if (newStatus == ServiceOrderStatus.COMPLETED) {
            order.setCompletedTime(LocalDateTime.now());

            // Auto-inventory deduction for physical / Minibar / F&B items
            if (order.getService() != null) {
                deepbluehaven.pojo.Service s = order.getService();
                if (s.getCategory() == ServiceCategory.MINI_BAR || s.getCategory() == ServiceCategory.FOOD_BEVERAGE) {
                    String serviceName = s.getName();
                    List<InventoryItem> items = inventoryItemRepository.findByNameLike(serviceName.trim());
                    if (items != null && !items.isEmpty()) {
                        InventoryItem item = items.get(0);
                        int qtyDeduct = (order.getQuantity() != null) ? order.getQuantity() : 1;
                        int newQty = Math.max(0, (item.getQuantity() != null ? item.getQuantity() : 0) - qtyDeduct);
                        item.setQuantity(newQty);
                        inventoryItemRepository.save(item);

                        InventoryTransaction tx = new InventoryTransaction();
                        tx.setInventoryItem(item);
                        tx.setChangeAmount(-qtyDeduct);
                        tx.setReason("Service Order #" + order.getId() + " (" + s.getName() + ") completed for guest");
                        inventoryTransactionRepository.save(tx);

                        // Automated low stock alert for Managers
                        int threshold = (item.getMinThreshold() != null) ? item.getMinThreshold() : 5;
                        if (newQty <= threshold) {
                            notificationService.notifyManagers(
                                "Low Stock Alert: " + item.getName(),
                                "Inventory item '" + item.getName() + "' is low on stock (" + newQty + " " + (item.getUnit() != null ? item.getUnit() : "units") + " remaining). Threshold: " + threshold + ".",
                                NotificationType.SYSTEM,
                                "/manager/inventory"
                            );
                        }
                    }
                }
            }

            // Notify Customer if applicable
            if (order.getCustomer() != null) {
                notificationService.createCustomerNotification(
                    order.getCustomer(),
                    "Service Order Delivered",
                    "Your order for " + (order.getService() != null ? order.getService().getName() : "Resort Service") + " has been completed. Enjoy!",
                    NotificationType.SERVICE,
                    "/booking/history"
                );
            }
        }

        ServiceOrder saved = serviceOrderRepository.save(order);

        Log log = new Log();
        log.setObjectType(ObjectType.SERVICE);
        log.setObjectId(order.getId());
        log.setActionCode(ActionCode.UPDATE);
        log.setWorkerId(worker != null ? worker.getId() : 1L);
        log.setPreviousStatus(oldStatus != null ? oldStatus.name() : "PENDING");
        log.setCurrentStatus(newStatus.name());
        log.setMetadata("Service order #" + order.getId() + " status updated to " + newStatus);
        logRepository.save(log);

        return saved;
    }

    private String getStatusLabel(BookingStatus status) {
        if (status == null) 
            return "Pending";
        switch (status) {
            case PENDING: 
                return "Pending";
            case CONFIRMED: 
                return "Confirmed";
            case CHECKED_IN: 
                return "Checked In";
            case CHECKED_OUT: 
                return "Checked Out";
            case CANCELLED: 
                return "Cancelled";
            default: return status.name();
        }
    }
}
