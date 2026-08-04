package deepbluehaven;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.pojo.AuthAccessLog;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;
import deepbluehaven.pojo.BookingLog;
import deepbluehaven.pojo.ChatMessage;
import deepbluehaven.pojo.ChatSession;
import deepbluehaven.pojo.Comment;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerDiscount;
import deepbluehaven.pojo.CustomerLoyaltyLog;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Discount;
import deepbluehaven.pojo.InventoryItem;
import deepbluehaven.pojo.InventoryTransaction;
import deepbluehaven.pojo.Invoice;
import deepbluehaven.pojo.InvoiceStatusLog;
import deepbluehaven.pojo.Log;
import deepbluehaven.pojo.MembershipTier;
import deepbluehaven.pojo.Notification;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.PaymentTransaction;
import deepbluehaven.pojo.PricingRule;
import deepbluehaven.pojo.Resort;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.RoomHighlight;
import deepbluehaven.pojo.RoomStatusLog;
import deepbluehaven.pojo.Service;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.ServicePoint;
import deepbluehaven.pojo.Supplier;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.TaskType;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.WorkerProfile;
import deepbluehaven.pojo.WorkerRoleTag;
import deepbluehaven.pojo.WorkerRoomAssignmentLog;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.Amenity;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.CalculationType;
import deepbluehaven.pojo.enums.ChatStatus;
import deepbluehaven.pojo.enums.CustomerDiscountStatus;
import deepbluehaven.pojo.enums.Department;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.pojo.enums.Gender;
import deepbluehaven.pojo.enums.InvoiceStatus;
import deepbluehaven.pojo.enums.MessageType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
import deepbluehaven.pojo.enums.ReferenceType;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomTag;
import deepbluehaven.pojo.enums.RoomType;
import deepbluehaven.pojo.enums.SenderType;
import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.pojo.enums.TierStatus;
import deepbluehaven.pojo.enums.WorkerStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedDataRunner.class);

    private static final String SEED_SENTINEL = "seed_admin_001";

    private static final String DEFAULT_PASSWORD = "password";

    private final BCryptPasswordEncoder passwordEncoder;

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 21, 8, 0);

    @PersistenceContext
    private EntityManager entityManager;

    public SeedDataRunner(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (isSeeded()) {
            seedNotificationsIfEmpty();
            LOGGER.info("Seed data đã tồn tại. Bỏ qua SeedDataRunner.");
            return;
        }

        List<MembershipTier> membershipTiers = seedMembershipTiers();
        List<Resort> resorts = seedResorts();
        List<Supplier> suppliers = seedSuppliers();

        List<Worker> workers = seedWorkers();
        seedWorkerProfiles(workers);
        seedWorkerRoleTags(workers);

        List<Customer> customers = seedCustomers();
        seedCustomerProfiles(customers, membershipTiers);

        List<Room> rooms = seedRooms(resorts);
        seedRoomHighlights(rooms);
        seedRoomStatusLogs(rooms, workers);
        seedWorkerRoomAssignmentLogs(rooms, workers);

        List<TaskType> taskTypes = seedTaskTypes();
        List<Task> tasks = seedTasks(rooms, taskTypes, workers);
        seedPricingRules();

        List<Service> services = seedServices(resorts);
        seedServicePoints();

        List<Discount> discounts = seedDiscounts(membershipTiers, workers);
        List<CustomerDiscount> customerDiscounts = seedCustomerDiscounts(customers, discounts);

        List<Booking> bookings = seedBookings(customers, workers, rooms);
        seedBookingDetails(bookings, rooms);
        seedBookingLogs(bookings, workers);

        List<Invoice> invoices = seedInvoices(bookings, customers, workers);
        attachInvoicesToCustomerDiscounts(customerDiscounts, invoices);
        seedInvoiceStatusLogs(invoices, workers);
        seedPaymentTransactions(invoices);

        List<ServiceOrder> serviceOrders = seedServiceOrders(services, bookings, customers, workers);

        seedComments(customers, resorts, rooms, services, workers);

        List<ChatSession> chatSessions = seedChatSessions(customers, workers);
        seedChatMessages(chatSessions, customers, workers);

        List<InventoryItem> inventoryItems = seedInventoryItems(resorts, suppliers);
        seedInventoryTransactions(inventoryItems);

        seedCustomerLoyaltyLogs(customers, bookings, serviceOrders);
        seedAuthAccessLogs(customers, workers);
        seedNotifications(customers, workers);
        seedSystemLogs(bookings, rooms, tasks, inventoryItems, invoices, customers, workers);

        entityManager.flush();
        LOGGER.info("Đã seed thành công dữ liệu cho 34 entity và các bảng ElementCollection.");
    }

    private boolean isSeeded() {
        Long count = entityManager.createQuery(
                "select count(w) from Worker w where w.username = :username",
                Long.class)
                .setParameter("username", SEED_SENTINEL)
                .getSingleResult();
        return count > 0;
    }

    private List<MembershipTier> seedMembershipTiers() {
        TierStatus[] statuses = TierStatus.values();
        int[] minimumPoints = { 0, 1_000, 5_000, 15_000, 30_000 };
        String[] multipliers = { "1.00", "1.10", "1.25", "1.50", "2.00" };
        String[] discountRates = { "0.00", "3.00", "5.00", "8.00", "12.00" };
        int[] priorityDurations = { 0, 15, 30, 45, 60 };

        List<MembershipTier> tiers = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            MembershipTier tier = new MembershipTier();
            tier.setTierName(statuses[i]);
            tier.setMinPoints(minimumPoints[i]);
            tier.setPointMultiplier(new BigDecimal(multipliers[i]));
            tier.setDiscountRate(new BigDecimal(discountRates[i]));
            tier.setPriorityDuration(priorityDurations[i]);
            tier.setDescription("Hạng thành viên " + statuses[i].name());
            persist(tier);
            tiers.add(tier);
        }
        entityManager.flush();
        return tiers;
    }

    private List<Resort> seedResorts() {
        String[][] data = {
                { "Deep Blue Haven Đà Nẵng", "Võ Nguyên Giáp, Đà Nẵng" },
                { "Deep Blue Haven Nha Trang", "Trần Phú, Nha Trang" },
                { "Deep Blue Haven Phú Quốc", "Bãi Trường, Phú Quốc" },
                { "Deep Blue Haven Quy Nhơn", "Ghềnh Ráng, Quy Nhơn" },
                { "Deep Blue Haven Hạ Long", "Bãi Cháy, Hạ Long" }
        };

        List<Resort> resorts = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            Resort resort = new Resort();
            resort.setName(data[i][0]);
            resort.setLocation(data[i][1]);
            resort.setScript("Kịch bản giới thiệu và chăm sóc khách hàng tại " + data[i][0]);
            persist(resort);
            resorts.add(resort);
        }
        entityManager.flush();
        return resorts;
    }

    private List<Supplier> seedSuppliers() {
        String[] names = {
                "Ocean Supply",
                "Blue Linen",
                "Fresh Food Partner",
                "Green Amenities",
                "Premium Equipment"
        };

        List<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(names[i]);
            supplier.setPhoneNumber(String.format("09080000%02d", i + 1));
            supplier.setEmail("supplier" + (i + 1) + "@deepbluehaven.test");
            supplier.setAddress("Địa chỉ nhà cung cấp số " + (i + 1));
            persist(supplier);
            suppliers.add(supplier);
        }
        entityManager.flush();
        return suppliers;
    }

    private List<Worker> seedWorkers() {
        List<Worker> workers = new ArrayList<>();

        WorkerStatus[] statuses = WorkerStatus.values();

        for (int i = 0; i < 5; i++) {
            WorkerStatus status = statuses[i % statuses.length];

            Worker worker = new Worker();
            worker.setEmployeeCode(
                    String.format("SEED-EMP-%03d", i + 1));

            worker.setUsername(
                    i == 0
                            ? SEED_SENTINEL
                            : String.format(
                                    "seed_worker_%03d",
                                    i + 1));

            worker.setPasswordHash(
                    passwordEncoder.encode(DEFAULT_PASSWORD));

            worker.setStatus(status);
            worker.setLocked(status == WorkerStatus.LOCKED);
            worker.setForceChangePassword(false);
            worker.setCreatedAt(
                    BASE_TIME.minusDays(30L - i));

            persist(worker);
            workers.add(worker);
        }

        entityManager.flush();
        return workers;
    }

    private void seedWorkerProfiles(List<Worker> workers) {
        String[] names = {
                "Nguyễn Minh Quản Trị",
                "Trần Hải Quản Lý",
                "Lê Thu Lễ Tân",
                "Phạm An Buồng Phòng",
                "Võ Bình Lễ Tân"
        };

        Role[] roles = Role.values();
        Gender[] genders = Gender.values();
        for (int i = 0; i < workers.size(); i++) {
            WorkerProfile profile = new WorkerProfile();
            profile.setWorker(workers.get(i));
            profile.setFullName(names[i]);
            profile.setRole(roles[i % roles.length]);
            profile.setRoleLevel((i % 4) + 1);
            profile.setPhoneNumber(String.format("09110000%02d", i + 1));
            profile.setEmail("worker" + (i + 1) + "@deepbluehaven.test");
            profile.setDepartment(defaultDepartment(roles[i % roles.length]));
            profile.setGender(genders[i % genders.length]);
            profile.setDateOfBirth(
                    LocalDate.of(
                            1985 + i,
                            1 + i,
                            10 + i));
            profile.setAddress(
                    "Địa chỉ nhân viên seed số " + (i + 1));

            workers.get(i).setProfile(profile);
            persist(profile);
        }
        entityManager.flush();
    }

    private int defaultRoleLevel(Role role) {
        switch (role) {
            case ADMIN:
                return 4;
            case MANAGER:
                return 3;
            case RECEPTIONIST:
                return 2;
            case HOUSEKEEPER:
                return 1;
            default:
                throw new IllegalArgumentException(
                        "Role không được hỗ trợ: " + role);
        }
    }

    private Department defaultDepartment(Role role) {
        switch (role) {
            case ADMIN:
                return Department.ADMINISTRATION;
            case MANAGER:
                return Department.MANAGEMENT;
            case RECEPTIONIST:
                return Department.RECEPTION;
            case HOUSEKEEPER:
                return Department.HOUSEKEEPING;
            default:
                throw new IllegalArgumentException(
                        "Role không được hỗ trợ: " + role);
        }
    }

    private void seedWorkerRoleTags(List<Worker> workers) {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách worker không được null hoặc rỗng");
        }

        for (Worker worker : workers) {
            if (worker == null) {
                throw new IllegalArgumentException(
                        "Worker trong danh sách không được null");
            }

            WorkerProfile profile = worker.getProfile();

            if (profile == null) {
                throw new IllegalStateException(
                        "Worker " + worker.getUsername()
                                + " chưa có WorkerProfile");
            }

            Role role = profile.getRole();

            if (role == null) {
                throw new IllegalStateException(
                        "Worker " + worker.getUsername()
                                + " chưa được gán Role");
            }

            worker.applyDefaultPermissions(role);

            for (WorkerRoleTag roleTag : worker.getRoleTags()) {
                if (roleTag.getWorker() == null) {
                    roleTag.setWorker(worker);
                }

                if (roleTag.getPermissionTag() == null) {
                    throw new IllegalStateException(
                            "Permission của worker "
                                    + worker.getUsername()
                                    + " không được null");
                }

                if (roleTag.getId() == null) {
                    persist(roleTag);
                }
            }
        }

        entityManager.flush();
    }

    private List<Customer> seedCustomers() {
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Customer customer = new Customer();

            customer.setUsername(
                    String.format(
                            "seed_customer_%03d",
                            i + 1));

            customer.setPasswordHash(
                    passwordEncoder.encode(DEFAULT_PASSWORD));

            customer.setCreatedAt(
                    BASE_TIME.minusDays(20L - i));

            persist(customer);
            customers.add(customer);
        }

        entityManager.flush();
        return customers;
    }

    private void seedCustomerProfiles(
            List<Customer> customers,
            List<MembershipTier> membershipTiers) {

        String[] names = {
                "Nguyễn An",
                "Trần Bình",
                "Lê Chi",
                "Phạm Dũng",
                "Võ Giang"
        };
        String[] segments = {
                "NEW",
                "REGULAR",
                "LOYAL",
                "VIP",
                "PREMIUM"
        };

        for (int i = 0; i < customers.size(); i++) {
            CustomerProfile profile = new CustomerProfile();
            profile.setCustomer(customers.get(i));
            profile.setBirthDay(LocalDate.of(1988 + i, 2 + i, 10 + i));
            profile.setFullName(names[i]);
            profile.setPhoneNumber(String.format("09220000%02d", i + 1));
            profile.setEmail("customer" + (i + 1) + "@deepbluehaven.test");
            profile.setTotalBookings(i * 3);
            profile.setTotalSpent(BigDecimal.valueOf((long) i * 8_500_000L));
            profile.setTotalPoints(i * 7_500);
            profile.setSegment(segments[i]);
            profile.setMembershipTier(membershipTiers.get(i));

            customers.get(i).setProfile(profile);
            persist(profile);
        }
        entityManager.flush();
    }

    private List<Room> seedRooms(List<Resort> resorts) {
        RoomType[] roomTypes = RoomType.values();
        RoomStatus[] roomStatuses = RoomStatus.values();
        RoomTag[] roomTags = RoomTag.values();
        Amenity[] amenities = Amenity.values();

        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Room room = new Room();
            room.setResort(resorts.get(i));
            room.setRoomNumber(String.format("%d01", i + 1));
            room.setRoomType(roomTypes[i % roomTypes.length]);
            room.setTags(List.of(roomTags[i]));
            room.setDescription("Phòng mẫu số " + (i + 1) + " thuộc " + resorts.get(i).getName());
            room.setImages(List.of(
                    "/images/rooms/seed-room-" + (i + 1) + "-1.jpg",
                    "/images/rooms/seed-room-" + (i + 1) + "-2.jpg"));
            room.setAmenities(distributeAmenities(amenities, i, 5));
            room.setPlanUrl("/plans/seed-room-" + (i + 1) + ".pdf");
            room.setArea(32 + i * 12);
            room.setStatus(roomStatuses[i % roomStatuses.length]);
            room.setCapacity(2 + (i % 4));
            room.setBasePrice(BigDecimal.valueOf(1_500_000L + i * 1_000_000L));
            persist(room);
            rooms.add(room);
        }

        entityManager.flush();
        return rooms;
    }

    private List<Amenity> distributeAmenities(
            Amenity[] amenities,
            int roomIndex,
            int roomCount) {

        List<Amenity> result = new ArrayList<>();
        for (int i = roomIndex; i < amenities.length; i += roomCount) {
            result.add(amenities[i]);
        }
        return result;
    }

    private void seedRoomHighlights(List<Room> rooms) {
        String[] titles = {
                "Không gian yên tĩnh",
                "Tầm nhìn đẹp",
                "Thiết kế hiện đại",
                "Gần tiện ích",
                "Dịch vụ cao cấp"
        };

        for (int i = 0; i < rooms.size(); i++) {
            RoomHighlight highlight = new RoomHighlight();
            highlight.setRoom(rooms.get(i));
            highlight.setTitle(titles[i]);
            highlight.setDescription("Điểm nổi bật của phòng " + rooms.get(i).getRoomNumber());
            persist(highlight);
        }
        entityManager.flush();
    }

    private void seedRoomStatusLogs(List<Room> rooms, List<Worker> workers) {
        for (int i = 0; i < rooms.size(); i++) {
            RoomStatusLog statusLog = new RoomStatusLog();
            statusLog.setRoom(rooms.get(i));
            statusLog.setWorker(workers.get(i));
            statusLog.setPreviousStatus(i == 0 ? null : RoomStatus.AVAILABLE);
            statusLog.setCurrentStatus(rooms.get(i).getStatus());
            statusLog.setTimestamp(BASE_TIME.minusHours(10 - i));
            persist(statusLog);
        }
        entityManager.flush();
    }

    private void seedWorkerRoomAssignmentLogs(List<Room> rooms, List<Worker> workers) {
        for (int i = 0; i < rooms.size(); i++) {
            WorkerRoomAssignmentLog assignment = new WorkerRoomAssignmentLog();
            assignment.setWorker(workers.get((i + 1) % workers.size()));
            assignment.setRoom(rooms.get(i));
            assignment.setAction(i % 2 == 0 ? "ASSIGNED" : "REASSIGNED");
            assignment.setUpdatedBy(workers.get(0));
            assignment.setTimestamp(BASE_TIME.minusHours(5 - i));
            persist(assignment);
        }
        entityManager.flush();
    }

    private List<TaskType> seedTaskTypes() {
        String[][] data = {
                { "Dọn phòng", "Vệ sinh và chuẩn bị phòng" },
                { "Kiểm tra phòng", "Kiểm tra phòng trước khi đón khách" },
                { "Bảo trì điện", "Kiểm tra hệ thống điện" },
                { "Bổ sung minibar", "Bổ sung hàng hóa minibar" },
                { "Xử lý yêu cầu khách", "Xử lý yêu cầu phát sinh" }
        };

        List<TaskType> taskTypes = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            TaskType taskType = new TaskType();
            taskType.setName(data[i][0]);
            taskType.setDescription(data[i][1]);
            taskType.setRequiredLevel((i % 4) + 1);
            taskType.setIsActive(i != 4);
            persist(taskType);
            taskTypes.add(taskType);
        }
        entityManager.flush();
        return taskTypes;
    }

    private List<Task> seedTasks(
            List<Room> rooms,
            List<TaskType> taskTypes,
            List<Worker> workers) {

        TaskStatus[] statuses = TaskStatus.values();
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            Task task = new Task();
            task.setRoom(rooms.get(i % rooms.size()));
            task.setTaskType(taskTypes.get(i % taskTypes.size()));
            task.setAssignedBy(workers.get(0));
            // Gán cho Pham An (index 3) để hiển thị trên dashboard của housekeeper
            task.setAssignedTo(workers.size() > 3 ? workers.get(3) : workers.get(i % workers.size()));
            task.setStatus(statuses[i % statuses.length]);
            
            String[] actions = {
                "Bed Linen Change & Routine",
                "Checkout Room Cleaning",
                "Towel & Minibar Restock",
                "Low Shower Pressure Inspection",
                "Deep Clean After Maintenance",
                "Air Conditioner Filter Wash"
            };
            task.setAction(actions[i % actions.length]);
            task.setTimestamp(BASE_TIME.plusHours(i));
            persist(task);
            tasks.add(task);
        }

        entityManager.flush();
        return tasks;
    }

    private void seedPricingRules() {
        RoomType[] roomTypes = RoomType.values();
        String[] multipliers = { "1.00", "1.15", "1.30", "1.50", "0.90" };

        for (int i = 0; i < 5; i++) {
            PricingRule rule = new PricingRule();
            rule.setRoomType(roomTypes[i % roomTypes.length]);
            rule.setMultiplier(new BigDecimal(multipliers[i]));
            rule.setStartDate(BASE_DATE.plusMonths(i));
            rule.setEndDate(BASE_DATE.plusMonths(i + 1).minusDays(1));
            persist(rule);
        }
        entityManager.flush();
    }

    private List<Service> seedServices(List<Resort> resorts) {
        ServiceCategory[] categories = ServiceCategory.values();
        ServiceStatus[] statuses = ServiceStatus.values();

        String[] names = {
                "Bữa sáng tại phòng",
                "Giặt ủi nhanh",
                "Massage thư giãn",
                "Đưa đón sân bay",
                "Minibar cao cấp",
                "Thuê dụng cụ thể thao",
                "Trang trí phòng"
        };
        String[] units = {
                "suất",
                "kg",
                "lượt",
                "chuyến",
                "sản phẩm",
                "giờ",
                "gói"
        };

        List<Service> services = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            Service service = new Service();
            service.setResort(resorts.get(i % resorts.size()));
            service.setName(names[i]);
            service.setDescription("Dịch vụ mẫu thuộc nhóm " + categories[i].name());
            service.setImages(List.of("/images/services/seed-service-" + (i + 1) + ".jpg"));
            service.setType("SEED_" + categories[i].name());
            service.setCategory(categories[i]);
            service.setBasePrice(BigDecimal.valueOf(150_000L + i * 175_000L));
            service.setUnit(units[i]);
            service.setStatus(statuses[i % statuses.length]);
            persist(service);
            services.add(service);
        }

        entityManager.flush();
        return services;
    }

    private void seedServicePoints() {
        ServiceCategory[] categories = ServiceCategory.values();
        CalculationType[] calculationTypes = CalculationType.values();

        for (int i = 0; i < categories.length; i++) {
            ServicePoint servicePoint = new ServicePoint();
            servicePoint.setServiceCategory(categories[i]);
            servicePoint.setCalculationType(calculationTypes[i % calculationTypes.length]);

            if (servicePoint.getCalculationType() == CalculationType.FIXED_AMOUNT) {
                servicePoint.setFixedPoints(25 + i * 5);
                servicePoint.setRewardPercentage(null);
            } else {
                servicePoint.setFixedPoints(null);
                servicePoint.setRewardPercentage(new BigDecimal("2.50").add(BigDecimal.valueOf(i)));
            }

            servicePoint.setIsActive(i != categories.length - 1);
            persist(servicePoint);
        }
        entityManager.flush();
    }

    private List<Discount> seedDiscounts(
            List<MembershipTier> membershipTiers,
            List<Worker> workers) {

        DiscountType[] discountTypes = DiscountType.values();
        List<Discount> discounts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Discount discount = new Discount();
            discount.setCode(String.format("SEED%02d", i + 1));
            discount.setType(discountTypes[i % discountTypes.length]);
            discount.setDiscountValue(
                    discount.getType() == DiscountType.PERCENTAGE
                            ? BigDecimal.valueOf(5 + i * 2L)
                            : BigDecimal.valueOf(100_000L + i * 50_000L));
            discount.setDescription("Mã giảm giá seed số " + (i + 1));
            discount.setStartDate(BASE_DATE.minusMonths(1));
            discount.setEndDate(BASE_DATE.plusMonths(6 + i));
            discount.setMinValueService(BigDecimal.valueOf(500_000L + i * 250_000L));
            discount.setUsageLimit(100 + i * 20);
            discount.setUsageCount(i * 3);
            discount.setLimitPerUser(1 + (i % 2));
            discount.setMembershipTier(membershipTiers.get(i));
            discount.setRoomType(RoomType.values()[i % RoomType.values().length].name());
            discount.setIsActive(i != 4);
            discount.setIsStackable(i % 2 == 0);
            discount.setCreatedAt(BASE_TIME.minusDays(30 - i));
            discount.setUpdatedAt(BASE_TIME.minusDays(i));
            discount.setCreatedBy(workers.get(i));
            persist(discount);
            discounts.add(discount);
        }

        entityManager.flush();
        return discounts;
    }

    private List<CustomerDiscount> seedCustomerDiscounts(
            List<Customer> customers,
            List<Discount> discounts) {

        CustomerDiscountStatus[] statuses = CustomerDiscountStatus.values();
        List<CustomerDiscount> customerDiscounts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            CustomerDiscount customerDiscount = new CustomerDiscount();
            customerDiscount.setCustomer(customers.get(i));
            customerDiscount.setDiscount(discounts.get(i));
            customerDiscount.setStatus(statuses[i % statuses.length]);
            customerDiscount.setAcquiredAt(BASE_TIME.minusDays(20 - i));

            if (customerDiscount.getStatus() == CustomerDiscountStatus.USED) {
                customerDiscount.setUsedAt(BASE_TIME.minusDays(2));
            }

            persist(customerDiscount);
            customerDiscounts.add(customerDiscount);
        }

        entityManager.flush();
        return customerDiscounts;
    }

    private List<Booking> seedBookings(
            List<Customer> customers,
            List<Worker> workers,
            List<Room> rooms) {

        BookingStatus[] statuses = BookingStatus.values();
        List<Booking> bookings = new ArrayList<>();

        for (int i = 0; i < statuses.length; i++) {
            Room room = rooms.get(i % rooms.size());
            int nights = 2 + (i % 3);
            BigDecimal total = room.getBasePrice().multiply(BigDecimal.valueOf(nights));

            Booking booking = new Booking();
            booking.setCustomer(customers.get(i % customers.size()));
            booking.setCreatedBy(workers.get(i % workers.size()));
            booking.setStatus(statuses[i]);
            booking.setBookingTime(BASE_TIME.minusDays(12 - i));
            booking.setTotalAmount(total);
            booking.setNote("Đặt phòng seed với trạng thái " + statuses[i].name());
            persist(booking);
            bookings.add(booking);
        }

        entityManager.flush();
        return bookings;
    }

    private void seedBookingDetails(List<Booking> bookings, List<Room> rooms) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            Room room = rooms.get(i % rooms.size());
            LocalDate checkIn = BASE_DATE.plusDays(i * 3L);
            LocalDate checkOut = checkIn.plusDays(2 + (i % 3));
            long nights = checkOut.toEpochDay() - checkIn.toEpochDay();

            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setRoom(room);
            detail.setRoomType(room.getRoomType());
            detail.setCheckIn(checkIn);
            detail.setCheckOut(checkOut);
            detail.setPricePerNight(room.getBasePrice());
            detail.setSubTotal(room.getBasePrice().multiply(BigDecimal.valueOf(nights)));
            detail.setStatus(booking.getStatus());
            detail.setAction("CREATE_BOOKING_DETAIL");
            detail.setTimestamp(BASE_TIME.minusDays(10 - i));
            persist(detail);
        }
        entityManager.flush();
    }

    private void seedBookingLogs(List<Booking> bookings, List<Worker> workers) {
        BookingStatus[] statuses = BookingStatus.values();

        for (int i = 0; i < bookings.size(); i++) {
            BookingLog bookingLog = new BookingLog();
            bookingLog.setBooking(bookings.get(i));
            bookingLog.setActorId(workers.get(i % workers.size()).getId());
            bookingLog.setPreviousStatus(i == 0 ? null : statuses[i - 1]);
            bookingLog.setCurrentStatus(bookings.get(i).getStatus());
            bookingLog.setNote("Cập nhật booking sang " + bookings.get(i).getStatus().name());
            bookingLog.setTimestamp(BASE_TIME.minusDays(6 - i));
            persist(bookingLog);
        }
        entityManager.flush();
    }

    private List<Invoice> seedInvoices(
            List<Booking> bookings,
            List<Customer> customers,
            List<Worker> workers) {

        InvoiceStatus[] statuses = InvoiceStatus.values();
        List<Invoice> invoices = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Booking booking = bookings.get(i);
            InvoiceStatus status = statuses[i % statuses.length];
            BigDecimal total = booking.getTotalAmount();

            Invoice invoice = new Invoice();
            invoice.setBooking(booking);
            invoice.setCustomer(customers.get(i));
            invoice.setWorker(workers.get(i));
            invoice.setTotalAmount(total);
            invoice.setPaidAmount(paidAmountFor(status, total));
            invoice.setStatus(status);
            invoice.setTimestamp(BASE_TIME.minusDays(5 - i));
            persist(invoice);
            invoices.add(invoice);
        }

        entityManager.flush();
        return invoices;
    }

    private BigDecimal paidAmountFor(InvoiceStatus status, BigDecimal total) {
        if (status == InvoiceStatus.PAID) {
            return total;
        }
        if (status == InvoiceStatus.PARTIAL_PAID) {
            return total.divide(BigDecimal.valueOf(2));
        }
        return BigDecimal.ZERO;
    }

    private void attachInvoicesToCustomerDiscounts(
            List<CustomerDiscount> customerDiscounts,
            List<Invoice> invoices) {

        for (int i = 0; i < customerDiscounts.size(); i++) {
            customerDiscounts.get(i).setReferenceInvoiceId(invoices.get(i).getId());
        }
    }

    private void seedInvoiceStatusLogs(List<Invoice> invoices, List<Worker> workers) {
        for (int i = 0; i < invoices.size(); i++) {
            InvoiceStatusLog statusLog = new InvoiceStatusLog();
            statusLog.setInvoice(invoices.get(i));
            statusLog.setWorker(workers.get(i));
            statusLog.setPreviousStatus(i == 0 ? null : InvoiceStatus.UNPAID);
            statusLog.setCurrentStatus(invoices.get(i).getStatus());
            statusLog.setTimestamp(BASE_TIME.minusDays(4 - i));
            persist(statusLog);
        }
        entityManager.flush();
    }

    private void seedPaymentTransactions(List<Invoice> invoices) {
        PaymentMethod[] methods = PaymentMethod.values();
        PaymentType[] types = PaymentType.values();

        for (int i = 0; i < 5; i++) {
            Invoice invoice = invoices.get(i);
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setInvoice(invoice);
            transaction.setAmount(
                    invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0
                            ? invoice.getPaidAmount()
                            : BigDecimal.valueOf(500_000L + i * 100_000L));
            transaction.setPaymentMethod(methods[i % methods.length]);
            transaction.setPaymentType(types[i % types.length]);
            transaction.setTransactionRef(String.format("SEED-TXN-%04d", i + 1));
            transaction.setAction(
                    transaction.getPaymentType() == PaymentType.REFUND
                            ? "REFUND_PROCESSED"
                            : "PAYMENT_SUCCESS");
            transaction.setTimestamp(BASE_TIME.minusDays(3 - i));
            persist(transaction);
        }
        entityManager.flush();
    }

    private List<ServiceOrder> seedServiceOrders(
            List<Service> services,
            List<Booking> bookings,
            List<Customer> customers,
            List<Worker> workers) {

        ServiceOrderStatus[] statuses = ServiceOrderStatus.values();
        List<ServiceOrder> orders = new ArrayList<>();

        for (int i = 0; i < services.size(); i++) {
            int quantity = 1 + (i % 3);
            ServiceOrderStatus status = statuses[i % statuses.length];

            ServiceOrder order = new ServiceOrder();
            order.setBooking(bookings.get(i % bookings.size()));
            order.setCustomer(customers.get(i % customers.size()));
            order.setGuestPhone(null);
            order.setGuestEmail(null);
            order.setService(services.get(i));
            order.setQuantity(quantity);
            order.setTotalPrice(
                    services.get(i).getBasePrice().multiply(BigDecimal.valueOf(quantity)));
            order.setNote("Yêu cầu dịch vụ seed số " + (i + 1));
            order.setStatus(status);
            order.setProcessedBy(workers.get(i % workers.size()));
            order.setOrderTime(BASE_TIME.plusHours(i));
            if (status == ServiceOrderStatus.DELIVERED
                    || status == ServiceOrderStatus.COMPLETED) {
                order.setCompletedTime(BASE_TIME.plusHours(i + 2L));
            }
            order.setAction("SERVICE_ORDER_" + status.name());
            order.setTimestamp(BASE_TIME.plusHours(i));
            persist(order);
            orders.add(order);
        }

        entityManager.flush();
        return orders;
    }

    private void seedComments(
            List<Customer> customers,
            List<Resort> resorts,
            List<Room> rooms,
            List<Service> services,
            List<Worker> workers) {

        for (int i = 0; i < 5; i++) {
            Comment comment = new Comment();
            comment.setCustomer(customers.get(i));

            switch (i) {
                case 0:
                    comment.setResort(resorts.get(i));
                    break;
                case 1:
                    comment.setRoom(rooms.get(i));
                    break;
                case 2:
                    comment.setService(services.get(i));
                    break;
                case 3:
                    comment.setWorker(workers.get(i));
                    break;
                default:
                    comment.setResort(resorts.get(i));
                    comment.setRoom(rooms.get(i));
                    break;
            }

            comment.setContent("Đánh giá seed số " + (i + 1));
            comment.setRating(i + 1);
            comment.setImages(List.of("/images/comments/seed-comment-" + (i + 1) + ".jpg"));
            comment.setCreatedAt(BASE_TIME.minusDays(5 - i));
            persist(comment);
        }
        entityManager.flush();
    }

    private List<ChatSession> seedChatSessions(
            List<Customer> customers,
            List<Worker> workers) {

        ChatStatus[] statuses = ChatStatus.values();
        List<ChatSession> sessions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ChatSession session = new ChatSession();
            session.setCustomer(customers.get(i));
            session.setCurrentAssigneeId(workers.get(i).getId());
            session.setStatus(statuses[i % statuses.length]);
            session.setIsread(i % 2 == 0);
            session.setStartTime(BASE_TIME.minusHours(10 - i));
            session.setUpdatedAt(BASE_TIME.minusHours(5 - i));
            persist(session);
            sessions.add(session);
        }

        entityManager.flush();
        return sessions;
    }

    private void seedChatMessages(
            List<ChatSession> sessions,
            List<Customer> customers,
            List<Worker> workers) {

        MessageType[] messageTypes = MessageType.values();
        SenderType[] senderTypes = SenderType.values();

        // 12 rows để bao phủ đầy đủ 4 MessageType và 3 SenderType.
        for (int i = 0; i < 12; i++) {
            SenderType senderType = senderTypes[i % senderTypes.length];

            ChatMessage message = new ChatMessage();
            message.setChatSession(sessions.get(i % sessions.size()));
            message.setSenderType(senderType);
            message.setMessageType(messageTypes[i % messageTypes.length]);
            message.setContent(
                    "Tin nhắn seed " + (i + 1)
                            + " - " + senderType.name()
                            + " - " + message.getMessageType().name());
            message.setTimestamp(BASE_TIME.minusMinutes(60L - i * 3L));

            if (senderType == SenderType.CUSTOMER) {
                message.setSenderId(customers.get(i % customers.size()).getId());
            } else if (senderType == SenderType.STAFF) {
                message.setSenderId(workers.get(i % workers.size()).getId());
            } else {
                message.setSenderId(null);
            }

            persist(message);
        }
        entityManager.flush();
    }

    private List<InventoryItem> seedInventoryItems(
            List<Resort> resorts,
            List<Supplier> suppliers) {

        String[] names = {
                "Khăn tắm",
                "Ga giường",
                "Nước suối",
                "Bộ amenities",
                "Cà phê minibar"
        };
        String[] units = {
                "cái",
                "bộ",
                "chai",
                "bộ",
                "gói"
        };

        List<InventoryItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            InventoryItem item = new InventoryItem();
            item.setResort(resorts.get(i));
            item.setName(names[i]);
            item.setQuantity(100 + i * 50);
            item.setUnit(units[i]);
            item.setSupplier(suppliers.get(i));
            persist(item);
            items.add(item);
        }

        entityManager.flush();
        return items;
    }

    private void seedInventoryTransactions(List<InventoryItem> inventoryItems) {
        int[] changes = { 50, -10, 100, -5, 75 };

        for (int i = 0; i < inventoryItems.size(); i++) {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryItem(inventoryItems.get(i));
            transaction.setChangeAmount(changes[i]);
            transaction.setReason(changes[i] > 0 ? "Nhập kho seed" : "Xuất kho seed");
            persist(transaction);
        }
        entityManager.flush();
    }

    private void seedCustomerLoyaltyLogs(
            List<Customer> customers,
            List<Booking> bookings,
            List<ServiceOrder> serviceOrders) {

        ReferenceType[] referenceTypes = ReferenceType.values();

        for (int i = 0; i < 5; i++) {
            ReferenceType referenceType = referenceTypes[i % referenceTypes.length];

            CustomerLoyaltyLog loyaltyLog = new CustomerLoyaltyLog();
            loyaltyLog.setReferenceType(referenceType);
            loyaltyLog.setCustomer(customers.get(i));
            loyaltyLog.setReferenceId(
                    referenceType == ReferenceType.BOOKING
                            ? bookings.get(i % bookings.size()).getId()
                            : serviceOrders.get(i % serviceOrders.size()).getId());
            loyaltyLog.setPointsChanged((i + 1) * 100);
            loyaltyLog.setReason("Tích điểm từ " + referenceType.name());
            loyaltyLog.setTimestamp(BASE_TIME.plusDays(i));
            persist(loyaltyLog);
        }
        entityManager.flush();
    }

    private void seedAuthAccessLogs(
            List<Customer> customers,
            List<Worker> workers) {

        String[] actions = {
                "LOGIN_SUCCESS",
                "LOGIN_FAILED",
                "LOGOUT",
                "PASSWORD_CHANGED",
                "SESSION_REFRESHED"
        };

        for (int i = 0; i < 5; i++) {
            boolean customerAccount = i % 2 == 0;

            AuthAccessLog accessLog = new AuthAccessLog();
            accessLog.setAccountId(
                    customerAccount
                            ? customers.get(i % customers.size()).getId()
                            : workers.get(i % workers.size()).getId());
            accessLog.setAccountType(customerAccount ? "CUSTOMER" : "WORKER");
            accessLog.setAction(actions[i]);
            accessLog.setIpAddress("192.168.10." + (20 + i));
            accessLog.setUserAgent("SeedDataRunner/1.0");
            accessLog.setTimestamp(BASE_TIME.plusMinutes(i));
            persist(accessLog);
        }
        entityManager.flush();
    }

    private void seedSystemLogs(
            List<Booking> bookings,
            List<Room> rooms,
            List<Task> tasks,
            List<InventoryItem> inventoryItems,
            List<Invoice> invoices,
            List<Customer> customers,
            List<Worker> workers) {

        ActionCode[] actionCodes = ActionCode.values();
        ObjectType[] objectTypes = ObjectType.values();

        // 7 rows để bao phủ đầy đủ toàn bộ ActionCode.
        for (int i = 0; i < actionCodes.length; i++) {
            ObjectType objectType = objectTypes[i % objectTypes.length];

            Log log = new Log();
            log.setObjectType(objectType);
            log.setObjectId(resolveObjectId(
                    objectType,
                    i,
                    bookings,
                    rooms,
                    tasks,
                    inventoryItems,
                    invoices,
                    customers));
            log.setCorrelationId(String.format("SEED-CORR-%04d", i + 1));
            log.setActionCode(actionCodes[i]);
            log.setTimestamp(BASE_TIME.plusMinutes(i * 5L));
            log.setWorkerId(workers.get(i % workers.size()).getId());
            log.setPreviousStatus(i == 0 ? null : "PREVIOUS");
            log.setCurrentStatus("CURRENT_" + actionCodes[i].name());
            log.setMetadata(
                    "{\"source\":\"SeedDataRunner\",\"index\":" + (i + 1) + "}");
            persist(log);
        }
        entityManager.flush();
    }

    private Long resolveObjectId(
            ObjectType objectType,
            int index,
            List<Booking> bookings,
            List<Room> rooms,
            List<Task> tasks,
            List<InventoryItem> inventoryItems,
            List<Invoice> invoices,
            List<Customer> customers) {

        switch (objectType) {
            case BOOKING:
                return bookings.get(index % bookings.size()).getId();
            case ROOM:
                return rooms.get(index % rooms.size()).getId();
            case TASK:
                return tasks.get(index % tasks.size()).getId();
            case INVENTORY:
                return inventoryItems.get(index % inventoryItems.size()).getId();
            case PAYMENT:
                return invoices.get(index % invoices.size()).getId();
            case USER:
                return customers.get(index % customers.size()).getId();
            default:
                throw new IllegalArgumentException("ObjectType không được hỗ trợ: " + objectType);
        }
    }

    private void seedNotifications(List<Customer> customers, List<Worker> workers) {
        if (customers != null && !customers.isEmpty()) {
            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);

                Notification n1 = new Notification(c, "Đặt phòng thành công", "Đơn đặt phòng #DBH-2026-00" + (i + 1) + " đã được xác nhận thành công.", NotificationType.BOOKING);
                n1.setLink("/booking/history");
                persist(n1);

                Notification n2 = new Notification(c, "Ưu đãi thành viên đặc biệt", "Nhận ngay giảm giá 15% cho dịch vụ Spa & Wellness trong tháng này.", NotificationType.PROMOTION);
                n2.setLink("/offers");
                persist(n2);

                Notification n3 = new Notification(c, "Chào mừng tới Deep Blue Haven", "Cảm ơn bạn đã lựa chọn khu nghỉ dưỡng cao cấp Deep Blue Haven Resort.", NotificationType.SYSTEM);
                n3.setIsRead(true);
                persist(n3);
            }
        }

        if (workers != null && !workers.isEmpty()) {
            for (int i = 0; i < workers.size(); i++) {
                Worker w = workers.get(i);

                Notification n1 = new Notification(w, "Nhiệm vụ dọn dẹp mới", "Bạn được phân công dọn dẹp phòng #" + (101 + i) + ".", NotificationType.TASK);
                n1.setLink("/housekeeper/tasks");
                persist(n1);

                Notification n2 = new Notification(w, "Thông báo hệ thống", "Lịch làm việc tuần tới đã được cập nhật trên trang quản lý.", NotificationType.SYSTEM);
                persist(n2);
            }
        }
    }

    private void seedNotificationsIfEmpty() {
        Long count = entityManager.createQuery("select count(n) from Notification n", Long.class).getSingleResult();
        if (count == 0) {
            List<Customer> customers = entityManager.createQuery("select c from Customer c", Customer.class).getResultList();
            List<Worker> workers = entityManager.createQuery("select w from Worker w", Worker.class).getResultList();
            seedNotifications(customers, workers);
            LOGGER.info("Đã tự động seed bổ sung thông báo cho {} customer và {} worker.", customers.size(), workers.size());
        }
    }

    private void persist(Object entity) {
        entityManager.persist(entity);
    }
}
