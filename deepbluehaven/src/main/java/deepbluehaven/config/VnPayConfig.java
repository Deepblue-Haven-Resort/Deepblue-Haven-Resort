package deepbluehaven.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class VnPayConfig {

    @Value("${vnpay.tmnCode:${VNPAY_TMN_CODE:${VNP_TMN_CODE:SUEJODFE}}}")
    private String tmnCode;

    @Value("${vnpay.hashSecret:${VNPAY_HASH_SECRET:${VNP_HASH_SECRET:}}}")
    private String hashSecret;

    @Value("${vnpay.url:${VNPAY_URL:${VNP_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}}}")
    private String vnpayUrl;

    @Value("${vnpay.returnUrl:${VNPAY_RETURN_URL:${VNP_RETURN_URL:http://localhost:8080/deepbluehaven/api/vnpay/payment-return}}}")
    private String returnUrl;

    @Value("${vnpay.version:2.1.0}")
    private String version;

    @Value("${vnpay.command:pay}")
    private String command;

    @Value("${vnpay.orderType:250000}")
    private String orderType;

    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getVnpayUrl() {
        return vnpayUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getVersion() {
        return version;
    }

    public String getCommand() {
        return command;
    }

    public String getOrderType() {
        return orderType;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if (ipAddress != null && (ipAddress.contains(":") || "0:0:0:0:0:0:0:1".equals(ipAddress))) {
                ipAddress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress != null ? ipAddress : "127.0.0.1";
    }
}
