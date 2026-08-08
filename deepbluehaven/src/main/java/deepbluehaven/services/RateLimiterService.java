package deepbluehaven.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp) {
        long currentTime = System.currentTimeMillis();
        UserRequestInfo info = requestCounts.compute(clientIp, (key, existing) -> {
            if (existing == null || (currentTime - existing.windowStart) > 60000) {
                return new UserRequestInfo(currentTime, 1);
            } else {
                existing.count++;
                return existing;
            }
        });

        return info.count <= MAX_REQUESTS_PER_MINUTE;
    }

    private static class UserRequestInfo {
        long windowStart;
        int count;

        UserRequestInfo(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
