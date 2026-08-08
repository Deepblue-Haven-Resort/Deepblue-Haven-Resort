package deepbluehaven.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Service;

import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportBookingsToCsv(List<Booking> bookings) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Booking ID,Customer Name,Booking Time,Check-In Date,Check-Out Date,Status,Total Amount");

            for (Booking booking : bookings) {
                String customerName = (booking.getCustomer() != null && booking.getCustomer().getProfile() != null) ? booking.getCustomer().getProfile().getFullName(): "N/A";

                String checkInDate = "N/A";
                String checkOutDate = "N/A";

                if (booking.getDetails() != null && !booking.getDetails().isEmpty()) {
                    BookingDetail firstDetail = booking.getDetails().get(0);
                    if (firstDetail.getCheckIn() != null) {
                        checkInDate = firstDetail.getCheckIn().toString();
                    }
                    if (firstDetail.getCheckOut() != null) {
                        checkOutDate = firstDetail.getCheckOut().toString();
                    }
                }

                writer.printf("%d,\"%s\",%s,%s,%s,%s,%s%n",
                        booking.getId(),
                        customerName,
                        booking.getBookingTime() != null ? booking.getBookingTime().toString() : "N/A",
                        checkInDate,
                        checkOutDate,
                        booking.getStatus(),
                        booking.getTotalAmount() != null ? booking.getTotalAmount().toString() : "0");
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}
