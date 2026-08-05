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
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
import deepbluehaven.pojo.enums.PermissionTag;
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

    @PersistenceContext
    private EntityManager entityManager;

    public SeedDataRunner(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (isSeeded()) {
            LOGGER.info("Seed data  already exists. Skipping SeedDataRunner.");
            return;
        }

        LOGGER.info("Starting SeedDataRunner...");

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
        seedInventoryTransactions(inventoryItems, workers);

        seedCustomerLoyaltyLogs(customers, bookings, serviceOrders);
        seedAuthAccessLogs(customers, workers);
        seedNotifications(customers, workers);
        seedSystemLogs(bookings, rooms, tasks, inventoryItems, invoices, customers, workers);

        entityManager.flush();
        LOGGER.info("Successfully seeded dataset (~100 rows per entity, 2-week window, English).");
    }

    private boolean isSeeded() {
        Long count = entityManager.createQuery(
                "select count(w) from Worker w where w.username = :username", Long.class)
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
            tier.setDescription("Membership Tier: " + statuses[i].name());
            persist(tier);
            tiers.add(tier);
        }
        entityManager.flush();
        return tiers;
    }

    private List<Resort> seedResorts() {
        String[][] data = {
                { "Deep Blue Haven Danang Beach Resort", "Vo Nguyen Giap Coastal Highway, Danang City" },
                { "Deep Blue Haven Nha Trang Bay Resort", "Tran Phu Ocean Boulevard, Nha Trang City" },
                { "Deep Blue Haven Phu Quoc Island Resort", "Bai Truong Sunset Coast, Phu Quoc Island" },
                { "Deep Blue Haven Quy Nhon Cliff Resort", "Ghenh Rang Seaside, Quy Nhon City" },
                { "Deep Blue Haven Halong Luxury Resort", "Bai Chay Coastal Area, Halong City" }
        };

        List<Resort> resorts = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            Resort resort = new Resort();
            resort.setName(data[i][0]);
            resort.setLocation(data[i][1]);
            resort.setScript("Welcome and VIP guest experience protocol for " + data[i][0]);
            persist(resort);
            resorts.add(resort);
        }
        entityManager.flush();
        return resorts;
    }

    private List<Supplier> seedSuppliers() {
        String[] names = {
                "Oceanic Amenity Supplies",
                "Blue Velvet Linen & Fabric Co.",
                "Fresh Harbor Food & Beverage Services",
                "EcoGreen Housekeeping Essentials",
                "Apex Resort Equipment & Maintenance"
        };

        List<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(names[i]);
            supplier.setPhoneNumber(String.format("+1-800-555-%04d", i + 101));
            supplier.setEmail("contact@supplier" + (i + 1) + ".deepbluehaven.com");
            supplier.setAddress("Industrial Zone " + (i + 1) + ", Supply Logistics Park");
            persist(supplier);
            suppliers.add(supplier);
        }
        entityManager.flush();
        return suppliers;
    }

    private List<Worker> seedWorkers() {
        List<Worker> workers = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            WorkerStatus status = (i == 23) ? WorkerStatus.LOCKED : WorkerStatus.ACTIVE;

            Worker worker = new Worker();
            worker.setEmployeeCode(String.format("SEED-EMP-%03d", i + 1));
            worker.setUsername(
                    i == 0
                            ? SEED_SENTINEL
                            : String.format("seed_worker_%03d", i + 1));

            worker.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            worker.setStatus(status);
            worker.setLocked(status == WorkerStatus.LOCKED);
            worker.setForceChangePassword(false);
            worker.setCreatedAt(LocalDateTime.now().minusDays(30 - i));

            persist(worker);
            workers.add(worker);
        }

        entityManager.flush();
        return workers;
    }

    private void seedWorkerProfiles(List<Worker> workers) {
        String[] firstNames = { "John", "Sarah", "Michael", "Emily", "David", "Jessica", "James", "Laura", "Robert",
                "Emma", "Daniel", "Olivia", "William", "Sophia", "Alexander", "Isabella", "Ethan", "Mia", "Matthew",
                "Charlotte", "Joseph", "Amelia", "Henry", "Harper", "Andrew" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor",
                "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White", "Harris" };

        Role[] roles = Role.values();
        Gender[] genders = Gender.values();

        for (int i = 0; i < workers.size(); i++) {
            WorkerProfile profile = new WorkerProfile();
            profile.setWorker(workers.get(i));
            profile.setFullName(firstNames[i] + " " + lastNames[i % lastNames.length]);
            Role role = (i == 0 || i == 1) ? Role.ADMIN
                    : (i < 5 ? Role.MANAGER : (i < 12 ? Role.RECEPTIONIST : Role.HOUSEKEEPER));
            profile.setRole(role);
            profile.setRoleLevel((i % 4) + 1);
            profile.setPhoneNumber(String.format("+1-555-%03d-%04d", (i * 17) % 900 + 100, (i * 31) % 9000 + 1000));
            profile.setEmail(firstNames[i].toLowerCase() + "." + lastNames[i % lastNames.length].toLowerCase()
                    + "@deepbluehaven.com");
            profile.setDepartment(defaultDepartment(role));
            profile.setGender(genders[i % genders.length]);
            profile.setDateOfBirth(LocalDate.of(1985 + (i % 15), (i % 12) + 1, (i % 25) + 1));
            profile.setAddress(100 + i * 12 + " Ocean Boulevard, Suite " + (i + 1));
            profile.setPerformanceScore(75.0 + (i % 24) * 0.9);
            persist(profile);
        }
        entityManager.flush();
    }

    private Department defaultDepartment(Role role) {
        if (role == null)
            return Department.RECEPTION;
        return switch (role) {
            case ADMIN, MANAGER -> Department.MANAGEMENT;
            case RECEPTIONIST -> Department.RECEPTION;
            case HOUSEKEEPER -> Department.HOUSEKEEPING;
        };
    }

    private void seedWorkerRoleTags(List<Worker> workers) {
        PermissionTag[] tags = PermissionTag.values();
        for (int i = 0; i < workers.size(); i++) {
            Worker worker = workers.get(i);
            WorkerRoleTag mainTag = new WorkerRoleTag(worker, tags[i % tags.length],
                    "Granted system role permission tag");
            persist(mainTag);
        }
        entityManager.flush();
    }

    private List<Customer> seedCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Customer customer = new Customer();
            customer.setUsername(String.format("seed_customer_%03d", i + 1));
            customer.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            customer.setCreatedAt(LocalDateTime.now().minusDays(60 - i));

            persist(customer);
            customers.add(customer);
        }
        entityManager.flush();
        return customers;
    }

    private void seedCustomerProfiles(List<Customer> customers, List<MembershipTier> tiers) {
        String[] firstNames = { "Oliver", "Charlotte", "Liam", "Amelia", "Noah", "Harper", "Elijah", "Evelyn", "Lucas",
                "Abigail", "Mason", "Emily", "Logan", "Elizabeth", "Ethan", "Mila", "Jacob", "Ella", "Jack", "Avery",
                "Michael", "Sofia", "Benjamin", "Camila", "William", "Aria", "James", "Scarlett", "Alexander",
                "Victoria", "Sebastian", "Madison", "Henry", "Luna", "Samuel", "Grace", "Jackson", "Chloe", "Levi",
                "Penelope" };
        String[] lastNames = { "Clark", "Lewis", "Robinson", "Walker", "Young", "Allen", "King", "Wright", "Scott",
                "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell",
                "Mitchell", "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker", "Cruz",
                "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy", "Cook", "Rogers",
                "Gutierrez" };

        for (int i = 0; i < customers.size(); i++) {
            CustomerProfile profile = new CustomerProfile();
            profile.setCustomer(customers.get(i));
            profile.setFullName(firstNames[i] + " " + lastNames[i]);
            profile.setPhoneNumber(String.format("+1-555-888-%04d", i + 1000));
            profile.setEmail(firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@example.com");
            profile.setBirthDay(LocalDate.of(1980 + (i % 20), (i % 12) + 1, (i % 25) + 1));
            profile.setTotalBookings(i % 5 + 1);
            profile.setTotalSpent(BigDecimal.valueOf(i * 1500000L));
            profile.setTotalPoints(i * 450);
            profile.setSegment(i % 2 == 0 ? "LEISURE" : "BUSINESS");
            profile.setMembershipTier(tiers.get(i % tiers.size()));
            persist(profile);
        }
        entityManager.flush();
    }

    private List<Room> seedRooms(List<Resort> resorts) {
        List<Room> rooms = new ArrayList<>();
        RoomType[] roomTypes = RoomType.values();

        int roomCounter = 0;
        for (int floor = 1; floor <= 5; floor++) {
            for (int r = 1; r <= 14; r++) {
                roomCounter++;
                String roomNum = String.format("%d%02d", floor, r);
                Room room = new Room();
                room.setResort(resorts.get(roomCounter % resorts.size()));
                room.setRoomNumber(roomNum);
                RoomType type = roomTypes[roomCounter % roomTypes.length];
                room.setRoomType(type);
                room.setDescription("Luxurious " + type.name()
                        + " with panoramic ocean views, private balcony, and king-size bedding.");
                room.setCapacity((floor % 3) + 2);
                room.setBasePrice(BigDecimal.valueOf(1500000L + (floor * 500000L) + (r * 100000L)));
                room.setArea(35 + floor * 10);
                room.setPlanUrl("/assets/images/rooms/plan-" + type.name().toLowerCase() + ".png");

                RoomStatus status;
                if (roomCounter % 7 == 0)
                    status = RoomStatus.MAINTENANCE;
                else if (roomCounter % 5 == 0)
                    status = RoomStatus.CLEANING;
                else if (roomCounter % 2 == 0)
                    status = RoomStatus.OCCUPIED;
                else
                    status = RoomStatus.AVAILABLE;

                room.setStatus(status);
                room.getTags().add(RoomTag.OCEAN_VIEW);
                room.getImages().add("/assets/images/rooms/room-" + (roomCounter % 5 + 1) + ".jpg");

                for (Amenity a : Amenity.values()) {
                    if (roomCounter % 2 == 0)
                        room.getAmenities().add(a);
                }

                persist(room);
                rooms.add(room);
            }
        }
        entityManager.flush();
        return rooms;
    }

    private void seedRoomHighlights(List<Room> rooms) {
        String[] titles = { "Panoramic Ocean View", "Private Infinity Jacuzzi", "King Size Master Bed",
                "Executive Lounge Access", "High-Speed Wi-Fi" };
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            for (int h = 0; h < 2; h++) {
                RoomHighlight highlight = new RoomHighlight();
                highlight.setRoom(r);
                highlight.setTitle(titles[(i + h) % titles.length]);
                highlight.setDescription("Premium feature provided in room " + r.getRoomNumber());
                persist(highlight);
            }
        }
        entityManager.flush();
    }

    private void seedRoomStatusLogs(List<Room> rooms, List<Worker> workers) {
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            RoomStatusLog log = new RoomStatusLog();
            log.setRoom(r);
            log.setWorker(workers.get(i % workers.size()));
            log.setPreviousStatus(RoomStatus.CLEANING);
            log.setCurrentStatus(r.getStatus());
            log.setTimestamp(LocalDateTime.now().minusDays(i % 14).minusHours(i % 12));
            persist(log);
        }
        entityManager.flush();
    }

    private void seedWorkerRoomAssignmentLogs(List<Room> rooms, List<Worker> workers) {
        for (int i = 0; i < rooms.size(); i++) {
            WorkerRoomAssignmentLog log = new WorkerRoomAssignmentLog();
            log.setRoom(rooms.get(i));
            log.setWorker(workers.get(i % workers.size()));
            log.setUpdatedBy(workers.get(0));
            log.setAction("Assigned housekeeping inspection for Room " + rooms.get(i).getRoomNumber());
            log.setTimestamp(LocalDateTime.now().minusDays(i % 14));
            persist(log);
        }
        entityManager.flush();
    }

    private List<TaskType> seedTaskTypes() {
        String[][] types = {
                { "Routine Housekeeping", "Clean & sanitize room after guest check-out" },
                { "Deep Sanitation & Turnover", "Full deep cleaning, linen change, and disinfection" },
                { "AC Maintenance & Filter Check", "HVAC unit maintenance, air filter cleaning" },
                { "Plumbing & Fixture Repair", "Inspect bathroom fixtures and piping" },
                { "Minibar Stocking & Check", "Restock minibar items and verify inventory" }
        };

        List<TaskType> list = new ArrayList<>();
        for (String[] t : types) {
            TaskType tt = new TaskType();
            tt.setName(t[0]);
            tt.setDescription(t[1]);
            tt.setRequiredLevel(1);
            tt.setIsActive(true);
            persist(tt);
            list.add(tt);
        }
        entityManager.flush();
        return list;
    }

    private List<Task> seedTasks(List<Room> rooms, List<TaskType> taskTypes, List<Worker> workers) {
        List<Task> tasks = new ArrayList<>();
        TaskStatus[] statuses = TaskStatus.values();

        for (int i = 0; i < 90; i++) {
            Task task = new Task();
            Room room = rooms.get(i % rooms.size());
            task.setRoom(room);
            task.setTaskType(taskTypes.get(i % taskTypes.size()));
            task.setAssignedBy(workers.get(1));
            task.setAssignedTo(workers.get(5 + (i % 15)));
            TaskStatus status = statuses[i % statuses.length];
            task.setStatus(status);
            task.setAction("Perform " + task.getTaskType().getName() + " in Room " + room.getRoomNumber());

            LocalDateTime baseTime = LocalDateTime.now().minusDays(14 - (i % 14)).withHour(8 + (i % 10)).withMinute(0);
            if (i == 5) {
                task.setDueTime(LocalDateTime.now().minusMinutes(25));
                task.setStatus(TaskStatus.CLEANING);
                task.setAction("Clean Room 410 delayed by 25 minutes");
            } else {
                task.setDueTime(baseTime.plusMinutes(60));
            }

            task.setTimestamp(baseTime);

            persist(task);
            tasks.add(task);
        }
        entityManager.flush();
        return tasks;
    }

    private void seedPricingRules() {
        RoomType[] roomTypes = RoomType.values();
        for (int i = 0; i < roomTypes.length; i++) {
            PricingRule rule = new PricingRule();
            rule.setRoomType(roomTypes[i]);
            rule.setMultiplier(new BigDecimal("1.15"));
            rule.setStartDate(LocalDate.now().minusDays(30));
            rule.setEndDate(LocalDate.now().plusDays(90));
            persist(rule);
        }
        entityManager.flush();
    }

    private List<Service> seedServices(List<Resort> resorts) {
        String[][] servicesData = {
                { "Gourmet Breakfast Buffet", "Full continental and Asian breakfast spread", "350000",
                        "FOOD_BEVERAGE", "F&B Buffet", "Per Person" },
                { "In-Room Dining F&B", "24/7 dining served to room", "500000", "FOOD_BEVERAGE", "In-Room Order",
                        "Per Order" },
                { "Aroma Essential Spa Session", "60-minute relaxing full body aromatherapy massage", "850000",
                        "SPA", "Wellness Massage", "Per Session" },
                { "Express Laundry Service", "Wash, press, and fold laundry service", "200000", "LAUNDRY",
                        "Laundry Care", "Per Basket" },
                { "Airport Luxury Shuttle", "Private airport pickup and dropoff service", "450000", "TRANSPORT",
                        "Airport Transfer", "Per Trip" },
                { "Scuba Diving Experience", "Guided coral reef scuba diving excursion", "1200000", "SPORT",
                        "Outdoor Tour", "Per Person" }
        };

        List<Service> services = new ArrayList<>();
        for (int i = 0; i < servicesData.length; i++) {
            Service service = new Service();
            service.setResort(resorts.get(i % resorts.size()));
            service.setName(servicesData[i][0]);
            service.setDescription(servicesData[i][1]);
            service.setBasePrice(new BigDecimal(servicesData[i][2]));
            service.setCategory(ServiceCategory.valueOf(servicesData[i][3]));
            service.setType(servicesData[i][4]);
            service.setUnit(servicesData[i][5]);
            service.setStatus(ServiceStatus.ACTIVE);
            service.getImages().add("/assets/images/services/service-" + (i + 1) + ".jpg");
            persist(service);
            services.add(service);
        }
        entityManager.flush();
        return services;
    }

    private void seedServicePoints() {
        ServiceCategory[] categories = ServiceCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ServicePoint point = new ServicePoint();
            point.setServiceCategory(categories[i]);
            point.setCalculationType(CalculationType.PERCENTAGE);
            point.setFixedPoints(100 + i * 50);
            point.setRewardPercentage(new BigDecimal("5.00"));
            point.setIsActive(true);
            persist(point);
        }
        entityManager.flush();
    }

    private List<Discount> seedDiscounts(List<MembershipTier> tiers, List<Worker> workers) {
        List<Discount> list = new ArrayList<>();
        String[][] discounts = {
                { "WELCOME2026", "Summer Welcome Voucher", "15.00", "500000", "PERCENTAGE" },
                { "VIPLUXURY", "VIP Executive Discount", "25.00", "1500000", "PERCENTAGE" },
                { "SPARELAX", "Spa Special Offer", "100000", "300000", "FIXED_AMOUNT" }
        };

        for (int i = 0; i < discounts.length; i++) {
            Discount d = new Discount();
            d.setCode(discounts[i][0]);
            d.setDescription(discounts[i][1] + " valid for all guests");
            d.setType(DiscountType.valueOf(discounts[i][4]));
            d.setDiscountValue(new BigDecimal(discounts[i][2]));
            d.setMinValueService(new BigDecimal(discounts[i][3]));
            d.setUsageLimit(500);
            d.setUsageCount(i * 12);
            d.setLimitPerUser(2);
            d.setStartDate(LocalDate.now().minusDays(30));
            d.setEndDate(LocalDate.now().plusDays(60));
            d.setIsActive(true);
            d.setCreatedBy(workers.get(0));
            d.setMembershipTier(tiers.get(i % tiers.size()));
            persist(d);
            list.add(d);
        }
        entityManager.flush();
        return list;
    }

    private List<CustomerDiscount> seedCustomerDiscounts(List<Customer> customers, List<Discount> discounts) {
        List<CustomerDiscount> list = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            CustomerDiscount cd = new CustomerDiscount();
            cd.setCustomer(customers.get(i));
            cd.setDiscount(discounts.get(i % discounts.size()));
            cd.setStatus(i % 3 == 0 ? CustomerDiscountStatus.USED : CustomerDiscountStatus.AVAILABLE);
            if (cd.getStatus() == CustomerDiscountStatus.USED) {
                cd.setUsedAt(LocalDateTime.now().minusDays(i % 7));
            }
            persist(cd);
            list.add(cd);
        }
        entityManager.flush();
        return list;
    }

    private List<Booking> seedBookings(List<Customer> customers, List<Worker> workers, List<Room> rooms) {
        List<Booking> bookings = new ArrayList<>();
        BookingStatus[] statuses = BookingStatus.values();

        for (int i = 0; i < 100; i++) {
            Booking b = new Booking();
            b.setCustomer(customers.get(i % customers.size()));
            b.setCreatedBy(workers.get(i % 5));
            BookingStatus status = statuses[i % statuses.length];
            b.setStatus(status);
            LocalDateTime bookingTime = LocalDateTime.now().minusDays(14 - (i % 14)).withHour(9 + (i % 10))
                    .withMinute((i * 7) % 60);
            b.setBookingTime(bookingTime);
            b.setTotalAmount(BigDecimal.valueOf(3500000L + (i % 10) * 1200000L));
            b.setNote(
                    "Guest requested " + (i % 2 == 0 ? "high floor ocean view room" : "quiet room away from elevator"));

            persist(b);
            bookings.add(b);
        }
        entityManager.flush();
        return bookings;
    }

    private void seedBookingDetails(List<Booking> bookings, List<Room> rooms) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            Room r = rooms.get(i % rooms.size());

            BookingDetail detail = new BookingDetail();
            detail.setBooking(b);
            detail.setRoom(r);
            detail.setRoomType(r.getRoomType());
            LocalDate checkIn = b.getBookingTime().toLocalDate();
            detail.setCheckIn(checkIn);
            detail.setCheckOut(checkIn.plusDays(2 + (i % 4)));
            detail.setPricePerNight(r.getBasePrice());
            detail.setSubTotal(b.getTotalAmount());
            detail.setStatus(b.getStatus());
            detail.setAction("Room reserved for booking #" + b.getId());
            detail.setTimestamp(b.getBookingTime());
            persist(detail);
        }
        entityManager.flush();
    }

    private void seedBookingLogs(List<Booking> bookings, List<Worker> workers) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            BookingLog log = new BookingLog();
            log.setBooking(b);
            log.setActorId(workers.get(i % workers.size()).getId());
            log.setPreviousStatus(BookingStatus.PENDING);
            log.setCurrentStatus(b.getStatus());
            log.setNote("Booking status updated to " + b.getStatus());
            log.setTimestamp(b.getBookingTime().plusMinutes(15));
            persist(log);
        }
        entityManager.flush();
    }

    private List<Invoice> seedInvoices(List<Booking> bookings, List<Customer> customers, List<Worker> workers) {
        List<Invoice> invoices = new ArrayList<>();
        InvoiceStatus[] statuses = InvoiceStatus.values();

        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            Invoice inv = new Invoice();
            inv.setBooking(b);
            inv.setCustomer(b.getCustomer());
            inv.setWorker(workers.get(i % 5));
            inv.setTotalAmount(b.getTotalAmount());

            InvoiceStatus status = (b.getStatus() == BookingStatus.CANCELLED) ? InvoiceStatus.CANCELLED
                    : statuses[i % statuses.length];
            inv.setStatus(status);
            inv.setPaidAmount(status == InvoiceStatus.PAID ? b.getTotalAmount() : BigDecimal.ZERO);
            inv.setTimestamp(b.getBookingTime().plusHours(1));

            persist(inv);
            invoices.add(inv);
        }
        entityManager.flush();
        return invoices;
    }

    private void attachInvoicesToCustomerDiscounts(List<CustomerDiscount> customerDiscounts, List<Invoice> invoices) {
        for (int i = 0; i < customerDiscounts.size() && i < invoices.size(); i++) {
            CustomerDiscount cd = customerDiscounts.get(i);
            if (cd.getStatus() == CustomerDiscountStatus.USED) {
                cd.setReferenceInvoiceId(invoices.get(i).getId());
            }
        }
        entityManager.flush();
    }

    private void seedInvoiceStatusLogs(List<Invoice> invoices, List<Worker> workers) {
        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            InvoiceStatusLog log = new InvoiceStatusLog();
            log.setInvoice(inv);
            log.setWorker(workers.get(i % workers.size()));
            log.setPreviousStatus(InvoiceStatus.UNPAID);
            log.setCurrentStatus(inv.getStatus());
            log.setTimestamp(inv.getTimestamp().plusMinutes(10));
            persist(log);
        }
        entityManager.flush();
    }

    private void seedPaymentTransactions(List<Invoice> invoices) {
        PaymentMethod[] methods = PaymentMethod.values();
        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            PaymentTransaction tx = new PaymentTransaction();
            tx.setInvoice(inv);
            tx.setAmount(inv.getPaidAmount());
            tx.setPaymentMethod(methods[i % methods.length]);
            tx.setPaymentType(PaymentType.FINAL);
            tx.setTransactionRef(String.format("TX-2026-%05d", i + 1000));
            tx.setAction(inv.getStatus() == InvoiceStatus.PAID ? "Payment Success" : "Payment Pending");
            tx.setTimestamp(inv.getTimestamp().plusMinutes(5));
            persist(tx);
        }
        entityManager.flush();
    }

    private List<ServiceOrder> seedServiceOrders(List<Service> services, List<Booking> bookings,
            List<Customer> customers, List<Worker> workers) {
        List<ServiceOrder> serviceOrders = new ArrayList<>();
        ServiceOrderStatus[] statuses = ServiceOrderStatus.values();

        for (int i = 0; i < 80; i++) {
            ServiceOrder so = new ServiceOrder();
            Booking b = bookings.get(i % bookings.size());
            Service s = services.get(i % services.size());

            so.setBooking(b);
            so.setCustomer(b.getCustomer());
            so.setGuestPhone(b.getCustomer().getProfile() != null ? b.getCustomer().getProfile().getPhoneNumber()
                    : "+1-555-000-0000");
            so.setGuestEmail("guest" + ((i % 40) + 1) + "@example.com");
            so.setService(s);
            so.setQuantity((i % 3) + 1);
            so.setTotalPrice(s.getBasePrice().multiply(BigDecimal.valueOf(so.getQuantity())));
            so.setNote("Service requested for " + s.getName() + " at Room " + (101 + i % 50));

            ServiceOrderStatus status = statuses[i % statuses.length];
            so.setStatus(status);
            so.setProcessedBy(workers.get(5 + (i % 10)));
            LocalDateTime orderTime = LocalDateTime.now().minusDays(14 - (i % 14)).withHour(10 + (i % 8))
                    .withMinute((i * 11) % 60);
            so.setOrderTime(orderTime);
            if (status == ServiceOrderStatus.DELIVERED) {
                so.setCompletedTime(orderTime.plusMinutes(45));
            }
            so.setAction("Order #" + (i + 1) + " processed by service staff");
            so.setTimestamp(orderTime);

            persist(so);
            serviceOrders.add(so);
        }
        entityManager.flush();
        return serviceOrders;
    }

    private void seedComments(List<Customer> customers, List<Resort> resorts, List<Room> rooms, List<Service> services,
            List<Worker> workers) {
        String[] highReviews = {
                "Exceptional resort experience! The ocean view suite was breathtaking and service staff were incredibly attentive.",
                "Top-notch hospitality! From check-in to spa session, everything was flawless. Will definitely return!",
                "Amazing gourmet breakfast and pristine infinity pool. Deep Blue Haven exceeded all our expectations.",
                "Beautiful beachfront property with luxurious amenities. Highly recommended for couples and families alike!"
        };

        String[] lowReviews = {
                "Air conditioning in Room 305 was making noise and took time to inspect.",
                "Service delivery for room dining was slightly delayed during peak dinner hours.",
                "Minibar water supply was low upon check-in, though staff resolved it quickly."
        };

        for (int i = 0; i < 60; i++) {
            Comment comment = new Comment();
            comment.setCustomer(customers.get(i % customers.size()));
            comment.setResort(resorts.get(i % resorts.size()));
            comment.setRoom(rooms.get(i % rooms.size()));
            comment.setService(services.get(i % services.size()));
            comment.setWorker(workers.get(i % workers.size()));

            if (i % 6 == 0) {
                comment.setRating(1 + (i % 2));
                comment.setContent(lowReviews[i % lowReviews.length]);
                comment.setIsComplaint(true);
            } else {
                comment.setRating(4 + (i % 2));
                comment.setContent(highReviews[i % highReviews.length]);
                comment.setIsComplaint(false);
            }

            comment.setCreatedAt(LocalDateTime.now().minusDays(14 - (i % 14)).minusHours(i % 10));
            persist(comment);
        }
        entityManager.flush();
    }

    private List<ChatSession> seedChatSessions(List<Customer> customers, List<Worker> workers) {
        List<ChatSession> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ChatSession cs = new ChatSession();
            cs.setCustomer(customers.get(i));
            cs.setCurrentAssigneeId(workers.get(2 + (i % 5)).getId());
            cs.setStatus(ChatStatus.RESOLVED);
            persist(cs);
            list.add(cs);
        }
        entityManager.flush();
        return list;
    }

    private void seedChatMessages(List<ChatSession> sessions, List<Customer> customers, List<Worker> workers) {
        for (int i = 0; i < sessions.size(); i++) {
            ChatSession cs = sessions.get(i);
            ChatMessage m1 = new ChatMessage();
            m1.setChatSession(cs);
            m1.setSenderType(SenderType.CUSTOMER);
            m1.setSenderId(cs.getCustomer().getId());
            m1.setMessageType(MessageType.TEXT);
            m1.setContent("Hello, I would like to inquire about late check-out options for my booking.");
            persist(m1);

            ChatMessage m2 = new ChatMessage();
            m2.setChatSession(cs);
            m2.setSenderType(SenderType.STAFF);
            m2.setSenderId(cs.getCurrentAssigneeId());
            m2.setMessageType(MessageType.TEXT);
            m2.setContent("Good day! Late check-out until 2:00 PM is complimentary for VIP tier guests.");
            persist(m2);
        }
        entityManager.flush();
    }

    private List<InventoryItem> seedInventoryItems(List<Resort> resorts, List<Supplier> suppliers) {
        String[][] itemsData = {
                { "Minibar Premium Water 500ml", "150", "bottles", "100" },
                { "Luxury Bath Towel 100% Cotton", "85", "pieces", "50" },
                { "Aroma Essential Lavender Massage Oil", "8", "bottles", "15" },
                { "Organic Espresso Coffee Pods", "400", "capsules", "100" },
                { "King Bed Linen Sets White", "12", "sets", "20" },
                { "Guest Slipper Pairs Premium", "250", "pairs", "60" }
        };

        List<InventoryItem> items = new ArrayList<>();
        for (int i = 0; i < itemsData.length; i++) {
            InventoryItem item = new InventoryItem();
            item.setResort(resorts.get(i % resorts.size()));
            item.setName(itemsData[i][0]);
            item.setQuantity(Integer.parseInt(itemsData[i][1]));
            item.setUnit(itemsData[i][2]);
            item.setMinThreshold(Integer.parseInt(itemsData[i][3]));
            item.setSupplier(suppliers.get(i % suppliers.size()));
            persist(item);
            items.add(item);
        }
        entityManager.flush();
        return items;
    }

    private void seedInventoryTransactions(List<InventoryItem> items, List<Worker> workers) {
        for (int i = 0; i < items.size(); i++) {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setInventoryItem(items.get(i));
            tx.setChangeAmount(-5);
            tx.setReason("Routine inventory restock transaction");
            persist(tx);
        }
        entityManager.flush();
    }

    private void seedCustomerLoyaltyLogs(List<Customer> customers, List<Booking> bookings,
            List<ServiceOrder> serviceOrders) {
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            CustomerLoyaltyLog log = new CustomerLoyaltyLog();
            log.setCustomer(c);
            log.setPointsChanged(250 + i * 50);
            log.setReason("Reward points earned from completed stay #" + (bookings.get(i % bookings.size()).getId()));
            log.setReferenceType(ReferenceType.BOOKING);
            log.setReferenceId(bookings.get(i % bookings.size()).getId());
            persist(log);
        }
        entityManager.flush();
    }

    private void seedAuthAccessLogs(List<Customer> customers, List<Worker> workers) {
        for (int i = 0; i < 30; i++) {
            AuthAccessLog log = new AuthAccessLog();
            if (i % 2 == 0) {
                log.setAccountId(customers.get(i % customers.size()).getId());
                log.setAccountType("CUSTOMER");
            } else {
                log.setAccountId(workers.get(i % workers.size()).getId());
                log.setAccountType("WORKER");
            }
            log.setAction("LOGIN_SUCCESS");
            log.setIpAddress("192.168.1." + (10 + i));
            log.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0.0.0");
            persist(log);
        }
        entityManager.flush();
    }

    private void seedNotifications(List<Customer> customers, List<Worker> workers) {
        if (customers != null && !customers.isEmpty()) {
            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);

                Notification n1 = new Notification(c, "Booking Confirmed",
                        "Your reservation #DBH-2026-00" + (i + 1) + " has been successfully confirmed.",
                        NotificationType.BOOKING);
                n1.setLink("/booking/history");
                persist(n1);

                Notification n2 = new Notification(c, "Special Member Privilege",
                        "Enjoy an exclusive 15% discount on Spa & Wellness services this week.",
                        NotificationType.PROMOTION);
                n2.setLink("/offers");
                persist(n2);

                Notification n3 = new Notification(c, "Welcome to Deep Blue Haven",
                        "Thank you for choosing Deep Blue Haven Luxury Resort.", NotificationType.SYSTEM);
                n3.setIsRead(true);
                persist(n3);
            }
        }

        if (workers != null && !workers.isEmpty()) {
            for (int i = 0; i < workers.size(); i++) {
                Worker w = workers.get(i);

                Notification n1 = new Notification(w, "New Housekeeping Assignment",
                        "You have been assigned to inspect and clean Room #" + (101 + i) + ".", NotificationType.TASK);
                n1.setLink("/housekeeper/tasks");
                persist(n1);

                Notification n2 = new Notification(w, "System Operations Announcement",
                        "Weekly resort operational schedule has been updated.", NotificationType.SYSTEM);
                persist(n2);
            }
        }
        entityManager.flush();
    }

    private void seedSystemLogs(List<Booking> bookings, List<Room> rooms, List<Task> tasks,
            List<InventoryItem> inventoryItems, List<Invoice> invoices, List<Customer> customers,
            List<Worker> workers) {
        for (int i = 0; i < 50; i++) {
            Log log = new Log();
            log.setObjectType(i % 2 == 0 ? ObjectType.BOOKING : ObjectType.ROOM);
            log.setObjectId((long) (i + 1));
            log.setCorrelationId("CORR-2026-" + (i + 100));
            log.setActionCode(i % 2 == 0 ? ActionCode.CHECK_IN : ActionCode.START_CLEANING);
            log.setWorkerId(workers.get(i % workers.size()).getId());
            log.setPreviousStatus(i % 2 == 0 ? "PENDING" : "OCCUPIED");
            log.setCurrentStatus(i % 2 == 0 ? "CONFIRMED" : "CLEANING");
            log.setMetadata("{\"source\":\"SeedDataRunner\",\"executor\":\"SystemSeeder\",\"step\":" + i + "}");
            persist(log);
        }
        entityManager.flush();
    }

    private void persist(Object entity) {
        entityManager.persist(entity);
    }
}
