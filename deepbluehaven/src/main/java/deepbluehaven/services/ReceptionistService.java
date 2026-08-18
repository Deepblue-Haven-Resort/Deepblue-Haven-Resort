package deepbluehaven.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.ReceptionistDTO;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;
import deepbluehaven.pojo.BookingLog;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerDiscount;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Discount;
import deepbluehaven.pojo.Invoice;
import deepbluehaven.pojo.InvoiceStatusLog;
import deepbluehaven.pojo.Log;
import deepbluehaven.pojo.MembershipTier;
import deepbluehaven.pojo.PaymentTransaction;
import deepbluehaven.pojo.PricingRule;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.RoomStatusLog;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.TaskType;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.CustomerDiscountStatus;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.pojo.enums.InvoiceStatus;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.InvoiceRepository;
import deepbluehaven.repositories.LogRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceOrderRepository;
import deepbluehaven.repositories.TaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class ReceptionistService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final InvoiceRepository invoiceRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final TaskRepository taskRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final LogRepository logRepository;
    private final CustomerRewardService customerRewardService;
    private final WorkerPerformanceService workerPerformanceService;

    @PersistenceContext
    private EntityManager entityManager;

    public ReceptionistService(BookingRepository bookingRepository,
                               RoomRepository roomRepository,
                               InvoiceRepository invoiceRepository,
                               ServiceOrderRepository serviceOrderRepository,
                               TaskRepository taskRepository,
                               CustomerRepository customerRepository,
                               NotificationService notificationService,
                               LogRepository logRepository,
                               CustomerRewardService customerRewardService,
                               WorkerPerformanceService workerPerformanceService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.invoiceRepository = invoiceRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.taskRepository = taskRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.logRepository = logRepository;
        this.customerRewardService = customerRewardService;
        this.workerPerformanceService = workerPerformanceService;
    }

    @Transactional(readOnly = true)
    public ReceptionistDTO.DashboardView getDashboardData() {
        ReceptionistDTO.DashboardView dto = new ReceptionistDTO.DashboardView();

        List<Booking> allBookings = bookingRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();

        int pendingCheckIn = 0;
        int pendingCheckOut = 0;
        int inHouse = 0;
        int availableRooms = 0;

        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING) {
                pendingCheckIn++;
            } else if (b.getStatus() == BookingStatus.CHECKED_IN) {
                inHouse++;
                pendingCheckOut++;
            }
        }

        for (Room r : allRooms) {
            if (r.getStatus() == RoomStatus.AVAILABLE) {
                availableRooms++;
            }
        }

        List<Room> availRooms = roomRepository.findByStatus(RoomStatus.AVAILABLE);
        List<ReceptionistDTO.AvailableRoomOption> availOptions = new ArrayList<>();
        for (Room r : availRooms) {
            availOptions.add(new ReceptionistDTO.AvailableRoomOption(
                r.getId(), r.getRoomNumber(), r.getRoomType() != null ? r.getRoomType().name() : "Standard", formatVnd(r.getBasePrice())
            ));
        }

        dto.setPendingCheckInsCount(pendingCheckIn);
        dto.setPendingCheckOutsCount(pendingCheckOut);
        dto.setInHouseGuestsCount(inHouse);
        dto.setAvailableRoomsCount(availableRooms);
        dto.setAvailableRoomsList(availOptions);

        dto.setCheckInQueue(getPendingCheckInQueue());
        dto.setCheckOutQueue(getPendingCheckOutQueue());
        dto.setRecentActivities(getRecentActivities());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ReceptionistDTO.CheckInQueueItem> getPendingCheckInQueue() {
        List<Booking> bookings = bookingRepository.findAll();
        List<ReceptionistDTO.CheckInQueueItem> queue = new ArrayList<>();
        List<Room> availableRooms = roomRepository.findByStatus(RoomStatus.AVAILABLE);

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING) {
                ReceptionistDTO.CheckInQueueItem item = new ReceptionistDTO.CheckInQueueItem();
                item.setBookingId(b.getId());
                item.setBookingCode(String.format("DBH-%d-%03d", (b.getBookingTime() != null ? b.getBookingTime().getYear() : 2026), b.getId()));
                
                Customer cust = b.getCustomer();
                if (cust != null && cust.getProfile() != null) {
                    item.setCustomerName(cust.getProfile().getFullName());
                    item.setCustomerPhone(cust.getProfile().getPhoneNumber() != null ? cust.getProfile().getPhoneNumber() : "N/A");
                    item.setCustomerEmail(cust.getProfile().getEmail() != null ? cust.getProfile().getEmail() : "customer@deepbluehaven.com");
                } else if (cust != null) {
                    item.setCustomerName(cust.getUsername());
                    item.setCustomerPhone("N/A");
                    item.setCustomerEmail("customer@deepbluehaven.com");
                } else {
                    item.setCustomerName("Guest");
                    item.setCustomerPhone("N/A");
                    item.setCustomerEmail("guest@deepbluehaven.com");
                }

                if (b.getDetails() != null && !b.getDetails().isEmpty()) {
                    BookingDetail detail = b.getDetails().get(0);
                    item.setRoomTypeName(detail.getRoomType() != null ? detail.getRoomType().name() : "Standard Room");
                    if (detail.getRoom() != null) {
                        item.setAssignedRoomId(detail.getRoom().getId());
                        item.setAssignedRoomNumber(detail.getRoom().getRoomNumber());
                    }
                    item.setCheckInDate(detail.getCheckIn());
                    item.setCheckOutDate(detail.getCheckOut());
                    if (detail.getCheckIn() != null && detail.getCheckOut() != null) {
                        long nights = ChronoUnit.DAYS.between(detail.getCheckIn(), detail.getCheckOut());
                        item.setNights(Math.max(nights, 1));
                    }
                } else {
                    item.setRoomTypeName("Standard Room");
                    item.setCheckInDate(LocalDate.now());
                    item.setCheckOutDate(LocalDate.now().plusDays(1));
                    item.setNights(1);
                }

                item.setTotalAmountStr(formatVnd(b.getTotalAmount()));
                item.setSpecialNote(b.getNote() != null ? b.getNote() : "No special request");
                item.setRawStatus(b.getStatus());

                List<ReceptionistDTO.AvailableRoomOption> roomOptions = new ArrayList<>();
                for (Room r : availableRooms) {
                    roomOptions.add(new ReceptionistDTO.AvailableRoomOption(
                        r.getId(),
                        r.getRoomNumber(),
                        r.getRoomType() != null ? r.getRoomType().name() : "Standard",
                        formatVnd(r.getBasePrice())
                    ));
                }
                item.setAvailableRooms(roomOptions);

                queue.add(item);
            }
        }
        return queue;
    }

    @Transactional
    public void executeCheckIn(ReceptionistDTO.CheckInRequest request, Worker receptionist) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + request.getRoomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room " + room.getRoomNumber() + " is currently " + room.getStatus() + " and not available for check-in.");
        }

        BookingStatus oldBookingStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CHECKED_IN);
        if (booking.getDetails() != null && !booking.getDetails().isEmpty()) {
            BookingDetail detail = booking.getDetails().get(0);
            detail.setRoom(room);
            detail.setStatus(BookingStatus.CHECKED_IN);
            detail.setAction("CHECK_IN");
        }
        bookingRepository.save(booking);

        RoomStatus oldRoomStatus = room.getStatus();
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        BookingLog bLog = new BookingLog();
        bLog.setBooking(booking);
        bLog.setActorId(receptionist != null ? receptionist.getId() : 1L);
        bLog.setPreviousStatus(oldBookingStatus);
        bLog.setCurrentStatus(BookingStatus.CHECKED_IN);
        bLog.setNote("Guest checked in to Room " + room.getRoomNumber() + ". Note: " + (request.getNotes() != null ? request.getNotes() : "Standard check-in"));
        bLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(bLog);

        RoomStatusLog rLog = new RoomStatusLog();
        rLog.setRoom(room);
        rLog.setWorker(receptionist);
        rLog.setPreviousStatus(oldRoomStatus);
        rLog.setCurrentStatus(RoomStatus.OCCUPIED);
        rLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(rLog);

        Log log = new Log();
        log.setObjectType(ObjectType.BOOKING);
        log.setObjectId(booking.getId());
        log.setActionCode(ActionCode.CHECK_IN);
        log.setWorkerId(receptionist != null ? receptionist.getId() : 1L);
        log.setPreviousStatus("CONFIRMED");
        log.setCurrentStatus("CHECKED_IN");
        log.setMetadata("Guest checked in to Room " + room.getRoomNumber());
        logRepository.save(log);

        if (booking.getCustomer() != null) {
            String code = String.format("DBH-%d-%03d", (booking.getBookingTime() != null ? booking.getBookingTime().getYear() : 2026), booking.getId());
            notificationService.createCustomerNotification(
                booking.getCustomer(),
                "Check-In Completed",
                "Welcome! You have successfully checked in to Room " + room.getRoomNumber() + " (Booking " + code + "). Enjoy your stay!",
                NotificationType.BOOKING,
                "/booking/history"
            );
        }
    }

    @Transactional
    public void executeRoomMove(ReceptionistDTO.RoomMoveRequest request, Worker receptionist) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Only currently CHECKED_IN bookings can be moved.");
        }

        Room newRoom = roomRepository.findById(request.getNewRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Destination room not found: " + request.getNewRoomId()));

        if (newRoom.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Destination Room " + newRoom.getRoomNumber() + " is not ready for check-in (Current status: " + newRoom.getStatus() + ")");
        }

        BookingDetail detail = (booking.getDetails() != null && !booking.getDetails().isEmpty()) ? booking.getDetails().get(0) : null;
        if (detail == null) {
            throw new IllegalStateException("Booking detail missing for Booking #" + booking.getId());
        }

        Room oldRoom = detail.getRoom();
        if (oldRoom != null && oldRoom.getId().equals(newRoom.getId())) {
            throw new IllegalArgumentException("Guest is already assigned to Room " + oldRoom.getRoomNumber());
        }

        String moveReason = (request.getReason() != null && !request.getReason().isBlank()) ? request.getReason() : "Guest requested room change";
        String oldRoomNum = (oldRoom != null) ? oldRoom.getRoomNumber() : "Unassigned";

        if (oldRoom != null) {
            RoomStatus oldRStatus = oldRoom.getStatus();
            oldRoom.setStatus(RoomStatus.CLEANING);
            roomRepository.save(oldRoom);

            RoomStatusLog oldRLog = new RoomStatusLog();
            oldRLog.setRoom(oldRoom);
            oldRLog.setWorker(receptionist);
            oldRLog.setPreviousStatus(oldRStatus);
            oldRLog.setCurrentStatus(RoomStatus.CLEANING);
            oldRLog.setTimestamp(LocalDateTime.now());
            entityManager.persist(oldRLog);

            try {
                Task cleaningTask = new Task();
                cleaningTask.setRoom(oldRoom);
                cleaningTask.setAction("Room Move Turnover Cleaning (Moved to " + newRoom.getRoomNumber() + ")");
                TaskType tt = entityManager.createQuery("select tt from TaskType tt", TaskType.class).getResultStream().findFirst().orElse(null);
                if (tt != null) {
                    cleaningTask.setTaskType(tt);
                }
                cleaningTask.setStatus(deepbluehaven.pojo.enums.TaskStatus.CLEANING);
                cleaningTask.setAssignedBy(receptionist);
                entityManager.persist(cleaningTask);
            } catch (Exception ignored) {}
        }

        RoomStatus newRStatus = newRoom.getStatus();
        newRoom.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(newRoom);

        RoomStatusLog newRLog = new RoomStatusLog();
        newRLog.setRoom(newRoom);
        newRLog.setWorker(receptionist);
        newRLog.setPreviousStatus(newRStatus);
        newRLog.setCurrentStatus(RoomStatus.OCCUPIED);
        newRLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(newRLog);

        detail.setRoom(newRoom);
        if (newRoom.getRoomType() != null) {
            detail.setRoomType(newRoom.getRoomType());
        }
        bookingRepository.save(booking);

        BookingLog bLog = new BookingLog();
        bLog.setBooking(booking);
        bLog.setActorId(receptionist != null ? receptionist.getId() : 1L);
        bLog.setPreviousStatus(BookingStatus.CHECKED_IN);
        bLog.setCurrentStatus(BookingStatus.CHECKED_IN);
        bLog.setNote("Room moved from Room " + oldRoomNum + " to Room " + newRoom.getRoomNumber() + ". Reason: " + moveReason);
        bLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(bLog);

        Log log = new Log();
        log.setObjectType(ObjectType.ROOM);
        log.setObjectId(newRoom.getId());
        log.setActionCode(ActionCode.UPDATE);
        log.setWorkerId(receptionist != null ? receptionist.getId() : 1L);
        log.setPreviousStatus(oldRoomNum);
        log.setCurrentStatus(newRoom.getRoomNumber());
        log.setMetadata("Booking #" + booking.getId() + " switched room: " + oldRoomNum + " -> " + newRoom.getRoomNumber() + " | Reason: " + moveReason);
        logRepository.save(log);

        if (booking.getCustomer() != null) {
            notificationService.createCustomerNotification(
                booking.getCustomer(),
                "Room Move Confirmed",
                "Your reservation has been transferred to Room " + newRoom.getRoomNumber() + " (" + (newRoom.getRoomType() != null ? newRoom.getRoomType().name() : "Standard") + "). Please contact the front desk for your new keycard.",
                NotificationType.BOOKING,
                "/booking/history"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReceptionistDTO.AvailableRoomOption> getAvailableRoomsForMove() {
        return roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .map(r -> new ReceptionistDTO.AvailableRoomOption(
                        r.getId(),
                        r.getRoomNumber(),
                        r.getRoomType() != null ? r.getRoomType().name() : "Standard",
                        formatVnd(r.getBasePrice()) + " VND"
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReceptionistDTO.CheckOutQueueItem> getPendingCheckOutQueue() {
        List<Booking> bookings = bookingRepository.findAll();
        List<ReceptionistDTO.CheckOutQueueItem> queue = new ArrayList<>();

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CHECKED_IN) {
                ReceptionistDTO.CheckOutQueueItem item = new ReceptionistDTO.CheckOutQueueItem();
                item.setBookingId(b.getId());
                item.setBookingCode(String.format("DBH-%d-%03d", (b.getBookingTime() != null ? b.getBookingTime().getYear() : 2026), b.getId()));

                Customer cust = b.getCustomer();
                if (cust != null && cust.getProfile() != null) {
                    item.setCustomerName(cust.getProfile().getFullName());
                    item.setCustomerPhone(cust.getProfile().getPhoneNumber() != null ? cust.getProfile().getPhoneNumber() : "N/A");
                } else if (cust != null) {
                    item.setCustomerName(cust.getUsername());
                    item.setCustomerPhone("N/A");
                } else {
                    item.setCustomerName("Guest");
                    item.setCustomerPhone("N/A");
                }

                Room assignedRoom = null;
                if (b.getDetails() != null && !b.getDetails().isEmpty()) {
                    BookingDetail detail = b.getDetails().get(0);
                    assignedRoom = detail.getRoom();
                    item.setRoomNumber(detail.getRoom() != null ? detail.getRoom().getRoomNumber() : "N/A");
                    item.setRoomTypeName(detail.getRoomType() != null ? detail.getRoomType().name() : "Standard Room");
                    item.setCheckInDate(detail.getCheckIn());
                    item.setCheckOutDate(detail.getCheckOut());
                    if (detail.getCheckIn() != null && detail.getCheckOut() != null) {
                        long nights = ChronoUnit.DAYS.between(detail.getCheckIn(), detail.getCheckOut());
                        item.setNightsStayed(Math.max(nights, 1));
                    }
                } else {
                    item.setRoomNumber("101");
                    item.setRoomTypeName("Standard Room");
                    item.setCheckInDate(LocalDate.now().minusDays(2));
                    item.setCheckOutDate(LocalDate.now());
                    item.setNightsStayed(2);
                }

                BookingDetail bDetail = (b.getDetails() != null && !b.getDetails().isEmpty()) ? b.getDetails().get(0) : null;
                BigDecimal pricingMultiplier = BigDecimal.ONE;
                String pricingNote = "Standard Rate";
                if (assignedRoom != null && assignedRoom.getRoomType() != null) {
                    try {
                        List<PricingRule> rules = entityManager.createQuery(
                            "select pr from PricingRule pr where pr.roomType = :rt", PricingRule.class)
                            .setParameter("rt", assignedRoom.getRoomType())
                            .getResultList();
                        if (!rules.isEmpty() && rules.get(0).getMultiplier() != null) {
                            pricingMultiplier = rules.get(0).getMultiplier();
                            pricingNote = "PricingRule (" + pricingMultiplier.stripTrailingZeros().toPlainString() + "x Multiplier)";
                        }
                    } catch (Exception ignored) {}
                }

                BigDecimal roomCharge = BigDecimal.ZERO;
                if (bDetail != null && bDetail.getSubTotal() != null && bDetail.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
                    roomCharge = bDetail.getSubTotal();
                } else if (bDetail != null && bDetail.getPricePerNight() != null) {
                    roomCharge = bDetail.getPricePerNight().multiply(BigDecimal.valueOf(item.getNightsStayed()));
                } else if (assignedRoom != null && assignedRoom.getBasePrice() != null) {
                    BigDecimal pNight = assignedRoom.getBasePrice().multiply(pricingMultiplier).setScale(0, RoundingMode.HALF_UP);
                    roomCharge = pNight.multiply(BigDecimal.valueOf(item.getNightsStayed()));
                } else {
                    roomCharge = (b.getTotalAmount() != null) ? b.getTotalAmount() : new BigDecimal("2100000");
                }

                item.setPricingRuleMultiplier(pricingMultiplier);
                item.setPricingRuleNote(pricingNote);
                item.setRoomCharge(roomCharge);

                List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(b.getId());
                BigDecimal serviceTotal = BigDecimal.ZERO;
                List<ReceptionistDTO.ServiceOrderItem> orderItems = new ArrayList<>();
                if (orders != null) {
                    for (ServiceOrder so : orders) {
                        if (so.getStatus() != ServiceOrderStatus.CANCELLED) {
                            BigDecimal itemPrice = so.getTotalPrice() != null ? so.getTotalPrice() : BigDecimal.ZERO;
                            serviceTotal = serviceTotal.add(itemPrice);

                            ReceptionistDTO.ServiceOrderItem sItem = new ReceptionistDTO.ServiceOrderItem();
                            sItem.setOrderId(so.getId());
                            sItem.setServiceName(so.getService() != null ? so.getService().getName() : "Service Add-on");
                            sItem.setQuantity(so.getQuantity() != null ? so.getQuantity() : 1);
                            sItem.setTotalPriceStr(formatVnd(itemPrice));
                            sItem.setStatusStr(so.getStatus() != null ? so.getStatus().name() : "CONFIRMED");
                            orderItems.add(sItem);
                        }
                    }
                }
                item.setServiceOrders(orderItems);
                item.setServiceCharge(serviceTotal);

                BigDecimal grossSubtotal = roomCharge.add(serviceTotal);

                BigDecimal totalDiscountAmount = BigDecimal.ZERO;
                BigDecimal tierDiscountAmount = BigDecimal.ZERO;
                BigDecimal voucherDiscountAmount = BigDecimal.ZERO;
                String discountCode = null;
                Long appliedCdId = null;
                if (b.getCustomer() != null && b.getCustomer().getId() != null) {
                    Long custId = b.getCustomer().getId();

                    try {
                        CustomerProfile profile = entityManager.find(CustomerProfile.class, custId);
                        if (profile != null && profile.getMembershipTier() != null) {
                            MembershipTier tier = profile.getMembershipTier();
                            if (tier.getDiscountRate() != null && tier.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
                                tierDiscountAmount = grossSubtotal.multiply(tier.getDiscountRate()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                                discountCode = tier.getTierName() + " (" + tier.getDiscountRate() + "%)";
                            }
                        }
                    } catch (Exception ignored) {}

                    try {
                        List<CustomerDiscount> custDiscounts = entityManager.createQuery(
                            "select cd from CustomerDiscount cd join fetch cd.discount d where cd.customer.id = :cId and cd.status = :st", CustomerDiscount.class)
                            .setParameter("cId", custId)
                            .setParameter("st", CustomerDiscountStatus.AVAILABLE)
                            .getResultList();
                        if (!custDiscounts.isEmpty()) {
                            CustomerDiscount cd = custDiscounts.get(0);
                            Discount d = cd.getDiscount();
                            appliedCdId = cd.getId();
                            if (discountCode != null) {
                                discountCode = d.getCode() + " + " + discountCode;
                            } else {
                                discountCode = d.getCode();
                            }
                            if (d.getType() == DiscountType.PERCENTAGE) {
                                voucherDiscountAmount = grossSubtotal.multiply(d.getDiscountValue()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                            } else {
                                voucherDiscountAmount = d.getDiscountValue();
                            }
                        }
                    } catch (Exception ignored) {}
                }

                totalDiscountAmount = tierDiscountAmount.add(voucherDiscountAmount);
                if (totalDiscountAmount.compareTo(grossSubtotal) > 0) {
                    totalDiscountAmount = grossSubtotal;
                }

                item.setDiscountAmount(totalDiscountAmount);
                item.setDiscountCode(discountCode);
                item.setAppliedCustomerDiscountId(appliedCdId);
                item.setDiscountAmountStr(formatVnd(totalDiscountAmount));

                BigDecimal netSubtotal = grossSubtotal.subtract(totalDiscountAmount);
                BigDecimal tax = netSubtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
                BigDecimal totalFolio = netSubtotal.add(tax);

                BigDecimal depositPaid = BigDecimal.ZERO;
                try {
                    List<Invoice> invs = invoiceRepository.findByBookingId(b.getId());
                    if (invs != null && !invs.isEmpty()) {
                        for (Invoice inv : invs) {
                            if (inv.getPaidAmount() != null && inv.getPaidAmount().compareTo(depositPaid) > 0) {
                                depositPaid = inv.getPaidAmount();
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (depositPaid.compareTo(BigDecimal.ZERO) == 0 && (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN)) {
                    depositPaid = totalFolio.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
                }

                BigDecimal netRemainingPayable = totalFolio.subtract(depositPaid);
                if (netRemainingPayable.compareTo(BigDecimal.ZERO) < 0) {
                    netRemainingPayable = BigDecimal.ZERO;
                }

                item.setDepositPaid(depositPaid);
                item.setDepositPaidStr(formatVnd(depositPaid));
                item.setNetRemainingPayable(netRemainingPayable);
                item.setNetRemainingPayableStr(formatVnd(netRemainingPayable));

                item.setTaxAmount(tax);
                item.setTotalFolio(totalFolio);

                item.setRoomChargeStr(formatVnd(roomCharge));
                item.setServiceChargeStr(formatVnd(serviceTotal));
                item.setTaxStr(formatVnd(tax));
                item.setTotalFolioStr(formatVnd(totalFolio));

                queue.add(item);
            }
        }
        return queue;
    }

    @Transactional
    public void executeCheckOut(ReceptionistDTO.CheckOutRequest request, Worker receptionist) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Booking #" + booking.getId() + " is not currently CHECKED_IN.");
        }

        Room assignedRoom = null;
        if (booking.getDetails() != null && !booking.getDetails().isEmpty()) {
            assignedRoom = booking.getDetails().get(0).getRoom();
        }

        BookingDetail bDetail = (booking.getDetails() != null && !booking.getDetails().isEmpty()) ? booking.getDetails().get(0) : null;
        BigDecimal roomCharge = BigDecimal.ZERO;
        if (bDetail != null && bDetail.getSubTotal() != null && bDetail.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
            roomCharge = bDetail.getSubTotal();
        } else if (booking.getTotalAmount() != null) {
            roomCharge = booking.getTotalAmount();
        } else {
            roomCharge = new BigDecimal("2100000");
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(booking.getId());
        BigDecimal serviceTotal = BigDecimal.ZERO;
        if (orders != null) {
            for (ServiceOrder so : orders) {
                if (so.getStatus() == ServiceOrderStatus.PENDING) {
                    so.setStatus(ServiceOrderStatus.CANCELLED);
                    so.setNote((so.getNote() != null && !so.getNote().isBlank() ? so.getNote() + " | " : "") + "Auto-cancelled upon booking check-out");
                    serviceOrderRepository.save(so);
                } else if (so.getStatus() != ServiceOrderStatus.CANCELLED) {
                    serviceTotal = serviceTotal.add(so.getTotalPrice() != null ? so.getTotalPrice() : BigDecimal.ZERO);
                }
            }
        }
        // Surcharge calculation
        BigDecimal surchargeAmount = BigDecimal.ZERO;
        String surchargeType = request.getSurchargeType() != null ? request.getSurchargeType() : "NONE";
        BigDecimal oneNightPrice = (bDetail != null && bDetail.getPricePerNight() != null) ? bDetail.getPricePerNight() : (assignedRoom != null ? assignedRoom.getBasePrice() : new BigDecimal("1000000"));
        if (oneNightPrice == null) oneNightPrice = new BigDecimal("1000000");

        if ("EARLY_CHECKIN_30".equalsIgnoreCase(surchargeType)) {
            surchargeAmount = oneNightPrice.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        } else if ("EARLY_CHECKIN_50".equalsIgnoreCase(surchargeType)) {
            surchargeAmount = oneNightPrice.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
        } else if ("LATE_CHECKOUT_30".equalsIgnoreCase(surchargeType)) {
            surchargeAmount = oneNightPrice.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        } else if ("LATE_CHECKOUT_50".equalsIgnoreCase(surchargeType)) {
            surchargeAmount = oneNightPrice.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
        } else if ("CUSTOM".equalsIgnoreCase(surchargeType) && request.getCustomSurchargeAmount() != null) {
            surchargeAmount = request.getCustomSurchargeAmount();
        }

        BigDecimal grossSubtotal = roomCharge.add(serviceTotal).add(surchargeAmount);

        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal tierDiscountAmount = BigDecimal.ZERO;
        BigDecimal voucherDiscountAmount = BigDecimal.ZERO;
        CustomerDiscount activeCustDiscount = null;
        if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
            Long custId = booking.getCustomer().getId();

            try {
                CustomerProfile profile = entityManager.find(CustomerProfile.class, custId);
                if (profile != null && profile.getMembershipTier() != null) {
                    MembershipTier tier = profile.getMembershipTier();
                    if (tier.getDiscountRate() != null && tier.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
                        tierDiscountAmount = grossSubtotal.multiply(tier.getDiscountRate()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                    }
                }
            } catch (Exception ignored) {}

            try {
                if (request.getCustomerDiscountId() != null) {
                    CustomerDiscount cd = entityManager.find(CustomerDiscount.class, request.getCustomerDiscountId());
                    if (cd != null && cd.getStatus() == CustomerDiscountStatus.AVAILABLE) {
                        activeCustDiscount = cd;
                        Discount d = cd.getDiscount();
                        if (d != null) {
                            if (d.getType() == DiscountType.PERCENTAGE) {
                                voucherDiscountAmount = grossSubtotal.multiply(d.getDiscountValue()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                            } else {
                                voucherDiscountAmount = d.getDiscountValue();
                            }
                        }
                    }
                } else {
                    List<CustomerDiscount> custDiscounts = entityManager.createQuery(
                        "select cd from CustomerDiscount cd join fetch cd.discount d where cd.customer.id = :cId and cd.status = :st", CustomerDiscount.class)
                        .setParameter("cId", custId)
                        .setParameter("st", CustomerDiscountStatus.AVAILABLE)
                        .getResultList();
                    if (!custDiscounts.isEmpty()) {
                        activeCustDiscount = custDiscounts.get(0);
                        Discount d = activeCustDiscount.getDiscount();
                        if (d.getType() == DiscountType.PERCENTAGE) {
                            voucherDiscountAmount = grossSubtotal.multiply(d.getDiscountValue()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                        } else {
                            voucherDiscountAmount = d.getDiscountValue();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        totalDiscountAmount = tierDiscountAmount.add(voucherDiscountAmount);
        if (totalDiscountAmount.compareTo(grossSubtotal) > 0) {
            totalDiscountAmount = grossSubtotal;
        }

        BigDecimal netSubtotal = grossSubtotal.subtract(totalDiscountAmount);
        BigDecimal tax = netSubtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalFolio = netSubtotal.add(tax);

        Invoice invoice = entityManager.createQuery("select i from Invoice i where i.booking.id = :bId", Invoice.class)
                .setParameter("bId", booking.getId())
                .getResultStream().findFirst().orElse(null);

        if (invoice == null) {
            invoice = new Invoice();
            invoice.setBooking(booking);
            invoice.setCustomer(booking.getCustomer());
            invoice.setWorker(receptionist);
            invoice.setTotalAmount(totalFolio);
            invoice.setPaidAmount(totalFolio);
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setTimestamp(LocalDateTime.now());
            entityManager.persist(invoice);
        } else {
            InvoiceStatus oldInvStatus = invoice.getStatus();
            invoice.setPaidAmount(totalFolio);
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);

            InvoiceStatusLog invLog = new InvoiceStatusLog();
            invLog.setInvoice(invoice);
            invLog.setWorker(receptionist);
            invLog.setPreviousStatus(oldInvStatus);
            invLog.setCurrentStatus(InvoiceStatus.PAID);
            invLog.setTimestamp(LocalDateTime.now());
            entityManager.persist(invLog);
        }

        if (activeCustDiscount != null) {
            activeCustDiscount.setStatus(CustomerDiscountStatus.USED);
            activeCustDiscount.setUsedAt(LocalDateTime.now());
            activeCustDiscount.setReferenceInvoiceId(invoice.getId());
            if (activeCustDiscount.getDiscount() != null) {
                Discount d = activeCustDiscount.getDiscount();
                d.setUsageCount((d.getUsageCount() != null ? d.getUsageCount() : 0) + 1);
            }
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setInvoice(invoice);
        tx.setAmount(totalFolio);
        PaymentMethod pMethod;
        try {
            pMethod = PaymentMethod.valueOf(request.getPaymentMethodStr());
        } catch (Exception e) {
            pMethod = PaymentMethod.CREDIT_CARD;
        }
        tx.setPaymentMethod(pMethod);
        tx.setPaymentType(PaymentType.FINAL);
        tx.setTransactionRef(String.format("TX-%d-%05d", LocalDateTime.now().getYear(), (int)(Math.random() * 90000 + 10000)));
        tx.setAction("Checkout Folio Paid (" + pMethod.name() + ")");
        tx.setTimestamp(LocalDateTime.now());
        entityManager.persist(tx);

        // Process Customer Reward Points & Worker Performance Score
        if (booking.getCustomer() != null) {
            customerRewardService.processInvoicePayment(booking.getCustomer(), totalFolio, invoice.getId(), "Checkout Invoice Settlement");
        }
        if (receptionist != null) {
            workerPerformanceService.adjustWorkerPerformanceScore(receptionist, 1.0);
        }

        BookingStatus oldBStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CHECKED_OUT);
        if (booking.getDetails() != null && !booking.getDetails().isEmpty()) {
            booking.getDetails().get(0).setStatus(BookingStatus.CHECKED_OUT);
            booking.getDetails().get(0).setAction("CHECK_OUT");
        }
        bookingRepository.save(booking);

        // Automatically cancel any unfulfilled/pending service orders for this checked-out booking
        List<ServiceOrder> serviceOrders = serviceOrderRepository.findByBookingId(booking.getId());
        if (serviceOrders != null) {
            for (ServiceOrder so : serviceOrders) {
                if (so.getStatus() == ServiceOrderStatus.PENDING) {
                    so.setStatus(ServiceOrderStatus.CANCELLED);
                    so.setNote((so.getNote() != null && !so.getNote().isBlank() ? so.getNote() + " | " : "") + "Auto-cancelled upon booking check-out");
                    serviceOrderRepository.save(so);
                }
            }
        }

        BookingLog bLog = new BookingLog();
        bLog.setBooking(booking);
        bLog.setActorId(receptionist != null ? receptionist.getId() : 1L);
        bLog.setPreviousStatus(oldBStatus);
        bLog.setCurrentStatus(BookingStatus.CHECKED_OUT);
        bLog.setNote("Guest checked out. Invoice PAID (" + formatVnd(totalFolio) + " VND)");
        bLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(bLog);

        Log checkOutLog = new Log();
        checkOutLog.setObjectType(ObjectType.BOOKING);
        checkOutLog.setObjectId(booking.getId());
        checkOutLog.setActionCode(ActionCode.CHECK_OUT);
        checkOutLog.setWorkerId(receptionist != null ? receptionist.getId() : 1L);
        checkOutLog.setPreviousStatus(oldBStatus != null ? oldBStatus.name() : "CHECKED_IN");
        checkOutLog.setCurrentStatus("CHECKED_OUT");
        checkOutLog.setMetadata("Guest checked out. Invoice settled: " + formatVnd(totalFolio) + " VND");
        logRepository.save(checkOutLog);

        if (assignedRoom != null) {
            RoomStatus oldRStatus = assignedRoom.getStatus();
            assignedRoom.setStatus(RoomStatus.CLEANING);
            roomRepository.save(assignedRoom);

            RoomStatusLog rLog = new RoomStatusLog();
            rLog.setRoom(assignedRoom);
            rLog.setWorker(receptionist);
            rLog.setPreviousStatus(oldRStatus);
            rLog.setCurrentStatus(RoomStatus.CLEANING);
            rLog.setTimestamp(LocalDateTime.now());
            entityManager.persist(rLog);

            List<TaskType> taskTypes = entityManager.createQuery("select tt from TaskType tt", TaskType.class).getResultList();
            TaskType tt = (!taskTypes.isEmpty()) ? taskTypes.get(0) : null;

            Task task = new Task();
            task.setRoom(assignedRoom);
            task.setTaskType(tt);
            task.setAssignedBy(receptionist);
            task.setStatus(TaskStatus.PENDING);
            task.setAction("Post check-out room cleaning & turnover for Room " + assignedRoom.getRoomNumber());
            task.setDueTime(LocalDateTime.now().plusHours(1));
            task.setTimestamp(LocalDateTime.now());
            taskRepository.save(task);
        }

        if (booking.getCustomer() != null) {
            String code = String.format("DBH-%d-%03d", (booking.getBookingTime() != null ? booking.getBookingTime().getYear() : 2026), booking.getId());
            notificationService.createCustomerNotification(
                booking.getCustomer(),
                "Check-Out Complete",
                "Thank you for staying at Deep Blue Haven! Your invoice for booking " + code + " has been settled. Have a safe journey!",
                NotificationType.BOOKING,
                "/booking/history"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReceptionistDTO.RoomRackItem> getRoomGridData() {
        List<Room> rooms = roomRepository.findAll();
        List<Booking> checkedInBookings = bookingRepository.findAll();
        List<ReceptionistDTO.RoomRackItem> grid = new ArrayList<>();

        for (Room r : rooms) {
            ReceptionistDTO.RoomRackItem item = new ReceptionistDTO.RoomRackItem();
            item.setRoomId(r.getId());
            item.setRoomNumber(r.getRoomNumber());
            item.setRoomTypeName(r.getRoomType() != null ? r.getRoomType().name() : "Standard");
            
            int floor = 1;
            try {
                if (r.getRoomNumber() != null && r.getRoomNumber().length() >= 3) {
                    floor = Integer.parseInt(r.getRoomNumber().substring(0, 1));
                }
            } catch (Exception ignored) {}
            item.setFloor(floor);

            item.setStatus(r.getStatus());
            item.setStatusClass(r.getStatus() != null ? r.getStatus().name().toLowerCase() : "available");
            item.setCapacity(r.getCapacity() != null ? r.getCapacity() : 2);
            item.setBasePriceStr(formatVnd(r.getBasePrice()));
            item.setAreaStr(r.getArea() != null ? r.getArea() + "m²" : "35m²");
            item.setAvailable(r.getStatus() == RoomStatus.AVAILABLE);

            if (r.getStatus() == RoomStatus.OCCUPIED) {
                for (Booking b : checkedInBookings) {
                    if (b.getStatus() == BookingStatus.CHECKED_IN && b.getDetails() != null) {
                        for (BookingDetail d : b.getDetails()) {
                            if (d.getRoom() != null && d.getRoom().getId().equals(r.getId())) {
                                item.setGuestName(b.getCustomer() != null && b.getCustomer().getProfile() != null 
                                    ? b.getCustomer().getProfile().getFullName() : "Guest");
                                item.setCheckOutDateStr(d.getCheckOut() != null ? d.getCheckOut().toString() : "Tomorrow");
                                item.setBookingId(b.getId());
                                break;
                            }
                        }
                    }
                }
            }

            if (item.getGuestName() == null) item.setGuestName("-");
            if (item.getCheckOutDateStr() == null) item.setCheckOutDateStr("-");

            grid.add(item);
        }
        return grid;
    }

    @Transactional
    public void executeWalkInBooking(ReceptionistDTO.WalkInBookingRequest request, Worker receptionist) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + request.getRoomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room " + room.getRoomNumber() + " is currently " + room.getStatus() + " and not available.");
        }

        String phone = (request.getPhone() != null && !request.getPhone().isBlank()) ? request.getPhone().trim() : "09" + (int)(Math.random()*90000000 + 10000000);
        Customer customer = customerRepository.findAll().stream()
                .filter(c -> c.getProfile() != null && phone.equals(c.getProfile().getPhoneNumber()))
                .findFirst().orElse(null);

        if (customer == null) {
            customer = new Customer();
            customer.setUsername("walkin_" + System.currentTimeMillis() % 100000);
            customer.setPasswordHash("N/A");

            CustomerProfile profile = new CustomerProfile();
            profile.setCustomer(customer);
            profile.setFullName(request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName().trim() : "Walk-in Guest");
            profile.setPhoneNumber(phone);
            profile.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : "walkin@deepbluehaven.com");
            customer.setProfile(profile);

            customerRepository.save(customer);
        }

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(1);
        try {
            if (request.getCheckInDate() != null && !request.getCheckInDate().isBlank()) {
                checkIn = LocalDate.parse(request.getCheckInDate());
            }
            if (request.getCheckOutDate() != null && !request.getCheckOutDate().isBlank()) {
                checkOut = LocalDate.parse(request.getCheckOutDate());
            }
        } catch (Exception ignored) {}

        long nights = Math.max(ChronoUnit.DAYS.between(checkIn, checkOut), 1);
        BigDecimal basePrice = room.getBasePrice() != null ? room.getBasePrice() : new BigDecimal("2000000");
        BigDecimal totalAmount = basePrice.multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setNote("Walk-in Booking by " + (receptionist != null ? receptionist.getUsername() : "reception"));

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setRoom(room);
        detail.setRoomType(room.getRoomType());
        detail.setCheckIn(checkIn);
        detail.setCheckOut(checkOut);
        detail.setPricePerNight(basePrice);
        detail.setSubTotal(totalAmount);
        detail.setStatus(BookingStatus.CHECKED_IN);
        detail.setAction("WALK_IN_CHECK_IN");

        booking.getDetails().add(detail);
        bookingRepository.save(booking);

        RoomStatus oldRStatus = room.getStatus();
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        BookingLog bLog = new BookingLog();
        bLog.setBooking(booking);
        bLog.setActorId(receptionist != null ? receptionist.getId() : 1L);
        bLog.setPreviousStatus(BookingStatus.PENDING);
        bLog.setCurrentStatus(BookingStatus.CHECKED_IN);
        bLog.setNote("Walk-in Guest checked in to Room " + room.getRoomNumber());
        bLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(bLog);

        RoomStatusLog rLog = new RoomStatusLog();
        rLog.setRoom(room);
        rLog.setWorker(receptionist);
        rLog.setPreviousStatus(oldRStatus);
        rLog.setCurrentStatus(RoomStatus.OCCUPIED);
        rLog.setTimestamp(LocalDateTime.now());
        entityManager.persist(rLog);

        Log log = new Log();
        log.setObjectType(ObjectType.BOOKING);
        log.setObjectId(booking.getId());
        log.setActionCode(ActionCode.CREATE);
        log.setWorkerId(receptionist != null ? receptionist.getId() : 1L);
        log.setPreviousStatus("NONE");
        log.setCurrentStatus("CHECKED_IN");
        log.setMetadata("Walk-In Guest Booking created for " + customer.getUsername() + " in Room " + room.getRoomNumber());
        logRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<ReceptionistDTO.RoomRackItem>> getRoomGridGroupedByFloor() {
        List<ReceptionistDTO.RoomRackItem> allItems = getRoomGridData();
        Map<Integer, List<ReceptionistDTO.RoomRackItem>> roomsByFloor = new TreeMap<>();
        for (ReceptionistDTO.RoomRackItem item : allItems) {
            int floor = item.getFloor() > 0 ? item.getFloor() : 1;
            roomsByFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(item);
        }
        return roomsByFloor;
    }

    private List<ReceptionistDTO.ActivityItem> getRecentActivities() {
        List<ReceptionistDTO.ActivityItem> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        List<Log> logs = logRepository.findTop10ByOrderByTimestampDesc();
        if (logs != null && !logs.isEmpty()) {
            for (Log l : logs) {
                ReceptionistDTO.ActivityItem item = new ReceptionistDTO.ActivityItem();
                item.setTimeStr(l.getTimestamp() != null ? l.getTimestamp().format(fmt) : LocalDateTime.now().format(fmt));
                item.setCategory(l.getActionCode() != null ? l.getActionCode().name().toLowerCase() : "action");
                item.setCategoryLabel(l.getActionCode() != null ? l.getActionCode().name().replace("_", " ") : "Action");
                item.setIconClass(l.getActionCode() == ActionCode.CHECK_IN ? "fa-solid fa-key" : (l.getActionCode() == ActionCode.CHECK_OUT ? "fa-solid fa-receipt" : "fa-solid fa-list-check"));
                item.setSubject((l.getObjectType() != null ? l.getObjectType().name() : "OBJECT") + " #" + l.getObjectId());
                item.setPerformer("Worker #" + (l.getWorkerId() != null ? l.getWorkerId() : 1));
                item.setStatus(l.getCurrentStatus() != null ? l.getCurrentStatus() : "DONE");
                item.setStatusClass("status-success");
                item.setNote(l.getMetadata() != null ? l.getMetadata() : "System activity log event");
                list.add(item);
            }
            return list;
        }

        ReceptionistDTO.ActivityItem a1 = new ReceptionistDTO.ActivityItem();
        a1.setTimeStr(LocalDateTime.now().minusMinutes(12).format(fmt));
        a1.setCategory("booking");
        a1.setCategoryLabel("Check-In");
        a1.setIconClass("fa-solid fa-key");
        a1.setSubject("Room 304");
        a1.setPerformer("Receptionist");
        a1.setStatus("CHECKED_IN");
        a1.setStatusClass("status-success");
        a1.setNote("Guest John Smith checked in to Deluxe Suite");
        list.add(a1);

        ReceptionistDTO.ActivityItem a2 = new ReceptionistDTO.ActivityItem();
        a2.setTimeStr(LocalDateTime.now().minusMinutes(35).format(fmt));
        a2.setCategory("payment");
        a2.setCategoryLabel("Folio Settlement");
        a2.setIconClass("fa-solid fa-receipt");
        a2.setSubject("Invoice #INV-2026-004");
        a2.setPerformer("Receptionist");
        a2.setStatus("PAID");
        a2.setStatusClass("status-success");
        a2.setNote("Check-out payment settled via Credit Card");
        list.add(a2);

        return list;
    }

    @Transactional(readOnly = true)
    public ReceptionistDTO.CheckOutQueueItem getInvoiceFolioDetails(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElse(null);
        if (b == null) return null;

        ReceptionistDTO.CheckOutQueueItem item = new ReceptionistDTO.CheckOutQueueItem();
        item.setBookingId(b.getId());
        item.setBookingCode(String.format("DBH-%d-%03d", (b.getBookingTime() != null ? b.getBookingTime().getYear() : 2026), b.getId()));

        Customer cust = b.getCustomer();
        if (cust != null && cust.getProfile() != null) {
            item.setCustomerName(cust.getProfile().getFullName());
            item.setCustomerPhone(cust.getProfile().getPhoneNumber() != null ? cust.getProfile().getPhoneNumber() : "N/A");
        } else if (cust != null) {
            item.setCustomerName(cust.getUsername());
            item.setCustomerPhone("N/A");
        } else {
            item.setCustomerName("Guest");
            item.setCustomerPhone("N/A");
        }

        Room assignedRoom = null;
        if (b.getDetails() != null && !b.getDetails().isEmpty()) {
            BookingDetail detail = b.getDetails().get(0);
            assignedRoom = detail.getRoom();
            item.setRoomNumber(detail.getRoom() != null ? detail.getRoom().getRoomNumber() : "N/A");
            item.setRoomTypeName(detail.getRoomType() != null ? detail.getRoomType().name() : "Standard Room");
            item.setCheckInDate(detail.getCheckIn());
            item.setCheckOutDate(detail.getCheckOut());
            if (detail.getCheckIn() != null && detail.getCheckOut() != null) {
                long nights = ChronoUnit.DAYS.between(detail.getCheckIn(), detail.getCheckOut());
                item.setNightsStayed(Math.max(nights, 1));
            }
        } else {
            item.setRoomNumber("101");
            item.setRoomTypeName("Standard Room");
            item.setCheckInDate(LocalDate.now().minusDays(1));
            item.setCheckOutDate(LocalDate.now());
            item.setNightsStayed(1);
        }

        BookingDetail bDetail = (b.getDetails() != null && !b.getDetails().isEmpty()) ? b.getDetails().get(0) : null;
        BigDecimal roomCharge = BigDecimal.ZERO;
        if (bDetail != null && bDetail.getSubTotal() != null && bDetail.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
            roomCharge = bDetail.getSubTotal();
        } else if (bDetail != null && bDetail.getPricePerNight() != null) {
            roomCharge = bDetail.getPricePerNight().multiply(BigDecimal.valueOf(item.getNightsStayed()));
        } else if (assignedRoom != null && assignedRoom.getBasePrice() != null) {
            roomCharge = assignedRoom.getBasePrice().multiply(BigDecimal.valueOf(item.getNightsStayed()));
        } else {
            roomCharge = (b.getTotalAmount() != null) ? b.getTotalAmount() : new BigDecimal("2100000");
        }
        item.setRoomCharge(roomCharge);
        item.setRoomChargeStr(formatVnd(roomCharge));

        List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(b.getId());
        BigDecimal serviceTotal = BigDecimal.ZERO;
        List<ReceptionistDTO.ServiceOrderItem> orderItems = new ArrayList<>();
        if (orders != null) {
            for (ServiceOrder so : orders) {
                if (b.getStatus() == BookingStatus.CHECKED_OUT || b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.COMPLETED) {
                    if (so.getStatus() == ServiceOrderStatus.PENDING) {
                        continue;
                    }
                }
                if (so.getStatus() != ServiceOrderStatus.CANCELLED) {
                    BigDecimal itemPrice = so.getTotalPrice() != null ? so.getTotalPrice() : BigDecimal.ZERO;
                    serviceTotal = serviceTotal.add(itemPrice);

                    ReceptionistDTO.ServiceOrderItem sItem = new ReceptionistDTO.ServiceOrderItem();
                    sItem.setOrderId(so.getId());
                    sItem.setServiceName(so.getService() != null ? so.getService().getName() : "Service Add-on");
                    sItem.setQuantity(so.getQuantity() != null ? so.getQuantity() : 1);
                    sItem.setTotalPriceStr(formatVnd(itemPrice));
                    sItem.setStatusStr(so.getStatus() != null ? so.getStatus().name() : "CONFIRMED");
                    orderItems.add(sItem);
                }
            }
        }
        item.setServiceOrders(orderItems);
        item.setServiceCharge(serviceTotal);
        item.setServiceChargeStr(formatVnd(serviceTotal));

        BigDecimal grossSubtotal = roomCharge.add(serviceTotal);

        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal tierDiscountAmount = BigDecimal.ZERO;
        BigDecimal voucherDiscountAmount = BigDecimal.ZERO;
        String discountCode = null;
        if (b.getCustomer() != null && b.getCustomer().getId() != null) {
            Long custId = b.getCustomer().getId();
            try {
                CustomerProfile profile = entityManager.find(CustomerProfile.class, custId);
                if (profile != null && profile.getMembershipTier() != null) {
                    MembershipTier tier = profile.getMembershipTier();
                    if (tier.getDiscountRate() != null && tier.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
                        tierDiscountAmount = grossSubtotal.multiply(tier.getDiscountRate()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                        discountCode = tier.getTierName() + " (" + tier.getDiscountRate() + "%)";
                    }
                }
            } catch (Exception ignored) {}

            try {
                List<CustomerDiscount> availableDiscounts = entityManager.createQuery(
                    "select cd from CustomerDiscount cd join fetch cd.discount d where cd.customer.id = :cId and cd.status = :st", CustomerDiscount.class)
                    .setParameter("cId", custId)
                    .setParameter("st", CustomerDiscountStatus.AVAILABLE)
                    .getResultList();
                List<ReceptionistDTO.CustomerDiscountOption> vOptions = new ArrayList<>();
                for (CustomerDiscount cd : availableDiscounts) {
                    if (cd.getDiscount() != null) {
                        Discount d = cd.getDiscount();
                        vOptions.add(new ReceptionistDTO.CustomerDiscountOption(
                            cd.getId(),
                            d.getCode(),
                            d.getType() != null ? d.getType().name() : "PERCENTAGE",
                            d.getDiscountValue(),
                            d.getDescription() != null ? d.getDescription() : (d.getCode() + " - " + d.getDiscountValue() + (d.getType() == DiscountType.PERCENTAGE ? "%" : " VND"))
                        ));
                    }
                }
                item.setAvailableVouchers(vOptions);
            } catch (Exception ignored) {}
        }

        totalDiscountAmount = tierDiscountAmount.add(voucherDiscountAmount);
        item.setDiscountAmount(totalDiscountAmount);
        item.setDiscountCode(discountCode != null ? discountCode : "None");
        item.setDiscountAmountStr(formatVnd(totalDiscountAmount));

        BigDecimal netSubtotal = grossSubtotal.subtract(totalDiscountAmount);
        BigDecimal tax = netSubtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalFolio = netSubtotal.add(tax);

        item.setTaxAmount(tax);
        item.setTaxStr(formatVnd(tax));
        item.setTotalFolio(totalFolio);
        item.setTotalFolioStr(formatVnd(totalFolio));

        BigDecimal depositPaid = totalFolio.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        try {
            List<Invoice> invs = invoiceRepository.findByBookingId(b.getId());
            if (invs != null && !invs.isEmpty() && invs.get(0).getPaidAmount() != null) {
                depositPaid = invs.get(0).getPaidAmount();
            }
        } catch (Exception ignored) {}
        item.setDepositPaid(depositPaid);
        item.setDepositPaidStr(formatVnd(depositPaid));

        BigDecimal remaining = totalFolio.subtract(depositPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0 || b.getStatus() == BookingStatus.CHECKED_OUT) {
            remaining = BigDecimal.ZERO;
        }
        item.setNetRemainingPayable(remaining);
        item.setNetRemainingPayableStr(formatVnd(remaining));

        return item;
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0";
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        return fmt.format(amount.longValue());
    }
}