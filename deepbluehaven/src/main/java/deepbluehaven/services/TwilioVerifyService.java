package deepbluehaven.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import jakarta.annotation.PostConstruct;

@Service
public class TwilioVerifyService {

    private final String accountSid;
    private final String authToken;
    private final String verifyServiceSid;

    public TwilioVerifyService(
            @Value("${twilio.account-sid:}")
            String accountSid,

            @Value("${twilio.auth-token:}")
            String authToken,

            @Value("${twilio.verify-service-sid:}")
            String verifyServiceSid
    ) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.verifyServiceSid = verifyServiceSid;
    }

    @PostConstruct
    private void initializeTwilio() {
        validateConfiguration();

        Twilio.init(accountSid, authToken);
    }


    public void sendSmsOtp(String phoneE164) {
        try {
            Verification verification =
                    Verification.creator(
                            verifyServiceSid,
                            phoneE164,
                            "sms"
                    ).create();

            String status = verification.getStatus();

            if (!"pending".equalsIgnoreCase(status)) {
                throw new RuntimeException(
                        "Twilio did not start OTP verification. "
                                + "Status: "
                                + status
                );
            }
        } catch (ApiException e) {
            throw new RuntimeException(
                    "Twilio could not send OTP: "
                            + e.getMessage(),
                    e
            );
        }
    }

    public boolean verifySmsOtp(
            String phoneE164,
            String otpCode
    ) {
        try {
            VerificationCheck check =
                    VerificationCheck.creator(
                            verifyServiceSid
                    )
                    .setTo(phoneE164)
                    .setCode(otpCode)
                    .create();

            return "approved".equalsIgnoreCase(
                    check.getStatus()
            );

        } catch (ApiException e) {
            return false;
        }
    }

    private void validateConfiguration() {
        if (accountSid == null || accountSid.isBlank()) {
            throw new IllegalStateException(
                    "Missing TWILIO_ACCOUNT_SID."
            );
        }

        if (!accountSid.startsWith("AC")) {
            throw new IllegalStateException(
                    "TWILIO_ACCOUNT_SID must start with AC."
            );
        }

        if (authToken == null || authToken.isBlank()) {
            throw new IllegalStateException(
                    "Missing TWILIO_AUTH_TOKEN."
            );
        }

        if (verifyServiceSid == null
                || verifyServiceSid.isBlank()) {
            throw new IllegalStateException(
                    "Missing TWILIO_VERIFY_SERVICE_SID."
            );
        }

        if (!verifyServiceSid.startsWith("VA")) {
            throw new IllegalStateException(
                    "TWILIO_VERIFY_SERVICE_SID "
                            + "must start with VA."
            );
        }
    }
}