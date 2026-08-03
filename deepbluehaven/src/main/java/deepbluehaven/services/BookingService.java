package deepbluehaven.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.BookingHistoryDTO;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.repositories.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private static final BigDecimal EXCHANGE_RATE_USD = new BigDecimal("26200");

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryDTO.Response> getBookingHistoryByCustomer(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        List<BookingHistoryDTO.Response> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Booking booking : bookings) {
            result.add(toHistoryResponse(booking, today));
        }

        return result;
    }

    private BookingHistoryDTO.Response toHistoryResponse(Booking booking, LocalDate today) {
        BookingHistoryDTO.Response dto = new BookingHistoryDTO.Response();

        int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : today.getYear();
        String bookingCode = String.format("DBH-%d-%03d", year, booking.getId());
        dto.setBookingCode(bookingCode);
        dto.setId(booking.getId());

        BigDecimal amountVnd = (booking.getTotalAmount() != null) ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal amountUsd = amountVnd.divide(EXCHANGE_RATE_USD, 0, RoundingMode.HALF_UP);
        dto.setTotalAmountVnd(amountVnd);
        dto.setTotalAmountUsd(amountUsd);

        BookingDetail firstDetail = (booking.getDetails() != null && !booking.getDetails().isEmpty())? booking.getDetails().get(0): null;

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

        dto.setServiceChargeVnd(BigDecimal.ZERO);
        dto.setTaxVnd(BigDecimal.ZERO);
        dto.setDiscountVnd(BigDecimal.ZERO);
        dto.setSpecialRequest(booking.getNote());

        dto.setBookedOn(booking.getBookingTime());
        dto.setRawStatus(booking.getStatus());

        BookingStatus status = booking.getStatus();
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

        return dto;
    }
}