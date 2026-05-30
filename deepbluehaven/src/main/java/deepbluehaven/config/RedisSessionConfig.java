package deepbluehaven.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(
        redisNamespace = "deepbluehaven:session",
        maxInactiveIntervalInSeconds = 28800
)
public class RedisSessionConfig {
}