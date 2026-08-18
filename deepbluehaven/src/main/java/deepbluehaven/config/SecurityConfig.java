package deepbluehaven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import deepbluehaven.pojo.Customer;
import deepbluehaven.services.AuthService;
import jakarta.servlet.http.HttpSession;

@Configuration
public class SecurityConfig {

    private static final int SESSION_TIMEOUT_SECONDS = 60 * 60 * 8;

    private final AuthService authService;

    public SecurityConfig(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/api/vnpay/**",
                                "/vnpay/**",
                                "/dinio/vnpay/**",
                                "/auth/**",
                                "/api/chat/**",
                                "/api/favorites/**",
                                "/api/customer/**"))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler()));

        return http.build();
    }

    private AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String fullName = oAuth2User.getAttribute("name");
            Customer customer = authService.findOrCreateCustomerFromOAuth(email, fullName);
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInCustomerId", customer.getId());
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

            response.sendRedirect(request.getContextPath() + "/home");
        };
    }
}