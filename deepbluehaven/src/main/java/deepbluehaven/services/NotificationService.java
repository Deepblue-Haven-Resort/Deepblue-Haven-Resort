package deepbluehaven.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Deep Blue Haven - OTP Verification");
        message.setText(
                "Your OTP code is: "
                        + otp
                        + ".\nThis code will expire in 3 minutes."
        );

        mailSender.send(message);
    }
}