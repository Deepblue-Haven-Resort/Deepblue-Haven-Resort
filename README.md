# Deepblue-Haven-Resort
**Deepblue Haven Resort** là một Hệ thống Quản lý Khách sạn / Khu nghỉ dưỡng toàn diện được thiết kế với kiến trúc mở rộng và chuẩn hóa cao. Hệ thống số hóa toàn bộ quy trình vận hành của một resort quy mô lớn: từ nền tảng đặt phòng trực tuyến cho khách hàng, quản lý kho bãi, đến hệ thống CRM tích điểm và phân tích dữ liệu log tập trung.  
Hệ thống được phát triển dựa trên Java **(Spring Boot)** kết hợp với **Redis** để tối ưu hóa quản lý phiên và hiệu suất.

---
## **Actors** 
- **Customer** *(Khách hàng)* - **Receptionist** *(Lễ tân)* - **Housekeeper** *(Buồng phòng)* - **Manager** *(Quản lý)* - **Admin** *(Quản trị viên)* - **System** *(Hệ thống tự động)* ---

## **1. Chức năng cho Khách hàng (Customer)** 

### a. Tìm kiếm & Đặt phòng  
- Tìm kiếm phòng trống theo **tiêu chí mong muốn** - Thực hiện đặt phòng (**Book room**) cho một hoặc nhiều phòng cùng lúc (**BookingDetail**)  
- Xem lịch sử và chi tiết các **đơn đặt phòng** - Hủy đặt phòng theo **chính sách của khách sạn** ### b. Sử dụng dịch vụ & Hỗ trợ  
- Trò chuyện với **Chatbot (ChatSession)** để nhận hỗ trợ giải đáp tự động  
- Đặt các dịch vụ nội khu *(F&B, Spa, Transport, Laundry...)* - Theo dõi trạng thái đơn đặt dịch vụ (**Service Order**):  
  `PENDING → PROCESSING → DELIVERED`  

### c. Quản lý tài khoản & Khách hàng thân thiết  
- Quản lý hồ sơ cá nhân (**CustomerProfile**)  
- Theo dõi điểm tích lũy (**Loyalty Points**) và tiến trình thăng hạng (**MembershipTier**)  
- Theo dõi và sử dụng các mã ưu đãi / Voucher hiện có (**CustomerDiscount**)  

---

## **2. Chức năng cho Khối Vận hành (Receptionist, Housekeeper)** 

### a. Tiền sảnh & Lễ tân (Receptionist)  
- Thực hiện thủ tục **Check-in / Check-out** cho khách  
- Phân bổ phòng (**Assign room**) trực tiếp cho khách hàng  
- Tạo đơn đặt phòng (**Create booking**) cho khách đến trực tiếp hoặc qua hotline  
- Khởi tạo và xử lý đơn đặt dịch vụ (**Service Order**) cho khách lưu trú hoặc khách vãng lai  
- Quản lý hóa đơn (**Invoice**) và ghi nhận các đợt thanh toán *(Tiền cọc, Thanh toán cuối, Hoàn tiền)* qua nhiều hình thức *(Tiền mặt, Thẻ, Chuyển khoản...)* ### b. Nghiệp vụ Buồng phòng (Housekeeper)  
- Xem danh sách các công việc **dọn dẹp hoặc bảo trì phòng** được phân công  
- Cập nhật trạng thái công việc (**Task**):  
  `PENDING → ASSIGNED → CLEANING → INSPECTED`  

---

## **3. Chức năng cho Khối Quản trị (Manager, Admin)** 

### a. Quản lý Cơ sở vật chất & Định giá  
- Cấu hình thông tin khách sạn và thiết lập phòng ốc (**Room**)  
- Thiết lập quy tắc tính giá (**PricingRule**) theo loại phòng, hệ số nhân và mùa vụ  
- Quản lý danh mục các dịch vụ nội khu (**Service**)  

### b. Quản lý Chuỗi cung ứng  
- Quản lý danh sách nhà cung cấp (**Supplier**)  
- Quản lý tồn kho hàng hóa, vật tư, phụ tùng (**InventoryItem**)  
- Theo dõi lịch sử xuất/nhập kho và lý do biến động (**InventoryTransaction**)  

### c. Quản lý CRM & Khuyến mãi  
- Thiết lập các hạng thành viên và điều kiện thăng hạng (**MembershipTier**)  
- Cấu hình quy tắc cộng điểm dịch vụ (**ServicePoint**) *(theo % hoặc điểm cố định)* - Quản lý và phát hành các chiến dịch Mã giảm giá (**Discount**)  

### d. Báo cáo & Nhân sự  
- Quản lý tài khoản nhân viên và phân quyền hệ thống (**WorkerProfile**)  
- Xem các báo cáo tổng quan về **doanh thu, công suất phòng và hiệu quả vận hành** ---

## **4. Chức năng của Hệ thống tự động (System Automation)** 
### a. Đồng bộ Trạng thái & Ràng buộc logic (Sync & Constraints)  
- **Kiểm soát Overlap:** Xử lý thuật toán chống trùng lặp lịch khi đặt phòng:  
  `newCheckIn < existingCheckOut AND newCheckOut > existingCheckIn`  
- **Đồng bộ vòng đời phòng:** Tự động chuyển trạng thái phòng:  
  `Booking CHECKED_IN → Room OCCUPIED`  
  `Booking COMPLETED → Room CLEANING`  
  `Cleaning DONE → Room AVAILABLE`  
- Ngăn chặn **Check-out** nếu hóa đơn chưa thanh toán đủ hoặc còn đơn dịch vụ đang xử lý  

### b. Tự động hóa CRM & Inventory  
- Tự động cộng điểm vào ví khách hàng ngay khi **thanh toán hóa đơn hoàn tất** - Liên tục quét điểm tích lũy để tự động nâng/hạ hạng thành viên (**Auto-Tiering**)  
- Nhận diện hạng thành viên để tự động áp dụng **% giảm giá** khi khách hàng tạo Booking  
- Tự động tạo giao dịch trừ kho (**InventoryTransaction**) khi nhân viên giao thành công một đơn hàng thuộc danh mục vật lý *(VD: MINIBAR)* ### c. Giám sát hệ thống (Centralized Logging)  
- Lưu vết mọi hành động làm thay đổi dữ liệu (**SystemActivityLog**)  
- Sử dụng **CorrelationId (BookingId)** để móc nối toàn bộ lịch sử tương tác của 1 khách hàng *(từ lúc tạo booking → nhận phòng → gọi dịch vụ → thanh toán)* ---
---
### Không gian làm việc (Project Workspace)
- [Google Drive - Tài liệu & Quản lý dự án Deepblue Haven Resort] (https://drive.google.com/drive/folders/1x8s5vtqAUUAso_9DfijcbGcXfuh9LznF?usp=drive_link)
---

# Cấu trúc thư mục (Project Structure)
Dự án Deepblue Haven Resort được phát triển dựa trên kiến trúc Spring Boot (MVC) kết hợp với hệ thống Template Engine (Thymeleaf). Cấu trúc mã nguồn được tổ chức như sau:
```plaintext
Deepblue-Haven-Resort
├── .github/                 # Cấu hình CI/CD (GitHub Actions)
├── src/
│   ├── main/
│   │   ├── java/com/deepbluehaven/   # Chứa toàn bộ mã nguồn Java Backend
│   │   │   ├── config/               # Cấu hình hệ thống (Security, MVC, Bean...)
│   │   │   ├── controllers/          # Nhận và điều hướng các HTTP Request
│   │   │   ├── pojo/                 # Các class thực thể (Entities/Models) tương tác với Database
│   │   │   ├── repositories/         # Các Interface giao tiếp trực tiếp với Database (JPA)
│   │   │   ├── service/              # Xử lý logic nghiệp vụ cốt lõi (Business Logic)
│   │   │   ├── DeepblueHavenApplication.java  # File khởi chạy ứng dụng Spring Boot
│   │   │   └── SeedDataRunner.java            # Khởi tạo dữ liệu mẫu (mock data) khi chạy app
│   │   │
│   │   └── resources/        # Chứa tài nguyên tĩnh và views
│   │       ├── static/assets/
│   │       │   ├── css/      # Stylesheet phân chia theo role (admin, auth, customer...) & base CSS
│   │       │   ├── js/       # JavaScript phân chia theo role & các script dùng chung (toast, sidebar...)
│   │       │   └── pic/      # Thư mục lưu trữ hình ảnh (Banners, Rooms, Services...)
│   │       │
│   │       ├── templates/    # Chứa các file giao diện HTML (Thymeleaf views)
│   │       │   ├── admin/        # Giao diện cho Quản lý / Admin
│   │       │   ├── auth/         # Giao diện Đăng nhập / Đăng ký / Quên mật khẩu
│   │       │   ├── cashier/      # Giao diện cho Lễ tân / Thu ngân (Booking, Thanh toán)
│   │       │   ├── component/    # Các thành phần UI nhỏ dùng chung
│   │       │   ├── customer/     # Giao diện cho Khách hàng (Tìm phòng, Đặt phòng, Profile)
│   │       │   ├── error/        # Giao diện báo lỗi (404, 500)
│   │       │   ├── fragments/    # Các phần giao diện dùng chung (Header, Footer, Sidebar)
│   │       │   ├── kitchen/      # Giao diện cho Bếp / Dịch vụ ăn uống
│   │       │   └── waiter/       # Giao diện cho Buồng phòng / Phục vụ
│   │       │
│   │       └── application.properties # File cấu hình môi trường, database, port của hệ thống
│   │
│   └── test/                 # Chứa các Unit Test và Integration Test
│
├── pom.xml                   # Quản lý thư viện phụ thuộc (Dependencies) của Maven
└── README.md                 # Tài liệu hướng dẫn dự án
```
