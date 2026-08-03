package deepbluehaven.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    
    @Value("${esms.api.key:}")
    private String apiKey;

    @Value("${esms.api.secret:}")
    private String secretKey;

    @Value("${esms.api.brandname:}")
    private String brandname;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Deep Blue Haven - OTP Verification");
        message.setText("Your OTP code is: " + otp + ".\nThis code will expire in 3 minutes.");
        mailSender.send(message);
    }

    // public void sendSmsOtp(String toPhone, String otp) {
    //     RestTemplate restTemplate = new RestTemplate();
    //     String url = "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/";

    //     Map<String, Object> body = new HashMap<>();
    //     body.put("ApiKey", apiKey);
    //     body.put("SecretKey", secretKey);
    //     body.put("Content", "Deep Blue Haven - Ma xac nhan OTP cua ban la: " + otp);
    //     body.put("Phone", toPhone);
    //     body.put("SmsType", "2");
    //     body.put("IsUnicode", "0");
        
    //     if (brandname != null && !brandname.trim().isEmpty()) {
    //         body.put("Brandname", brandname);
    //     } else {
    //         body.put("Brandname", "Baotrixemay"); // Hoặc brandname test của bạn
    //     }
        
    //     body.put("Sandbox", "2"); 

    //     try {
    //         ObjectMapper objectMapper = new ObjectMapper();
    //         String jsonBody = objectMapper.writeValueAsString(body);

    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.APPLICATION_JSON);
    //         headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

    //         HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

    //         String responseBody = restTemplate.postForObject(url, request, String.class);
    //         System.out.println("====== ESMS RESPONSE BODY ======");
    //         System.out.println(responseBody);
    //         System.out.println("================================");
            
    //     } catch (Exception e) {
    //         System.err.println("eSMS Error: " + e.getMessage());
    //         throw new RuntimeException("Failed to send SMS via eSMS.");
    //     }
    // }
}