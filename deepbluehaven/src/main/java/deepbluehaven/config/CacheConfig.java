package deepbluehaven.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, String> otpCache() {
        return Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build();
    }

    @Bean
    public Cache<String, Boolean> verifiedOtpCache() {
        return Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    }
}