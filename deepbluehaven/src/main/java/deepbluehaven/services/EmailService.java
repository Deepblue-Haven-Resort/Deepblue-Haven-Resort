package deepbluehaven.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@deepbluehaven.com}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmationEmail(String toEmail, String customerName, String bookingCode, String checkInDate, String checkOutDate) {
        if (mailSender == null) {
            System.out.println("[EmailService Simulation] Booking confirmation email sent to " + toEmail + " for booking " + bookingCode);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Booking Confirmation - Deepblue Haven Resort (" + bookingCode + ")");

            String htmlContent = "<h2>Dear " + customerName + ",</h2>"
                    + "<p>Thank you for choosing Deepblue Haven Resort! Your booking has been confirmed.</p>"
                    + "<p><strong>Booking Code:</strong> " + bookingCode + "</p>"
                    + "<p><strong>Check-in:</strong> " + checkInDate + "</p>"
                    + "<p><strong>Check-out:</strong> " + checkOutDate + "</p>"
                    + "<p>We look forward to welcoming you!</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (mailSender == null) {
            System.out.println("[EmailService Simulation] Password reset token sent to " + toEmail + ": " + resetToken);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Deepblue Haven Resort");

            String htmlContent = "<h3>Password Reset Request</h3>"
                    + "<p>Use the following token to reset your password:</p>"
                    + "<h4>" + resetToken + "</h4>"
                    + "<p>This token is valid for 15 minutes.</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send reset email to " + toEmail + ": " + e.getMessage());
        }
    }
}
