package deepbluehaven.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

import deepbluehaven.services.RememberMeService;

@Component
public class AuthFilter implements Filter {

    private final RememberMeService rememberMeService;

    public AuthFilter(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Thử tự động khôi phục đăng nhập từ Persistent Remember-Me Cookie nếu chưa có Session
        rememberMeService.tryAutoLoginFromCookie(request, response);

        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        String ctx = request.getContextPath();

        boolean isPublicRoute = uri.equals(ctx + "/") ||
                uri.equals(ctx + "/login") ||
                uri.equals(ctx + "/logout") ||
                uri.startsWith(ctx + "/assets/") ||
                uri.startsWith(ctx + "/css/") ||
                uri.startsWith(ctx + "/js/") ||
                uri.startsWith(ctx + "/images/") ||
                uri.startsWith(ctx + "/img/") ||
                uri.startsWith(ctx + "/webjars/") ||
                uri.equals(ctx + "/favicon.ico") ||
                uri.equals(ctx + "/error") ||
                uri.equals(ctx + "/401") ||
                uri.equals(ctx + "/403") ||
                uri.equals(ctx + "/404") ||
                uri.equals(ctx + "/500");

        if (isPublicRoute) {
            chain.doFilter(req, res);
            return;
        }

        boolean isCustomerRoute = uri.equals(ctx + "/home") ||
                uri.equals(ctx + "/dashboard") ||
                uri.startsWith(ctx + "/customer/");

        if (isCustomerRoute) {
            if (session == null || session.getAttribute("loggedInCustomerId") == null) {
                response.sendRedirect(ctx + "/login");
                return;
            }

            chain.doFilter(req, res);
            return;
        }

        boolean isStaffRoute = uri.startsWith(ctx + "/admin/") ||
                uri.startsWith(ctx + "/manager/") ||
                uri.startsWith(ctx + "/receptionist/") ||
                uri.startsWith(ctx + "/housekeeper/");

        if (isStaffRoute) {
            if (session == null || session.getAttribute("loggedInWorkerId") == null) {
                response.sendRedirect(ctx + "/login");
                return;
            }

            String role = (String) session.getAttribute("workerRole");

            boolean allowed = (uri.startsWith(ctx + "/admin/") && "ADMIN".equals(role)) ||
                    (uri.startsWith(ctx + "/manager/") && "MANAGER".equals(role)) ||
                    (uri.startsWith(ctx + "/receptionist/") && "RECEPTIONIST".equals(role)) ||
                    (uri.startsWith(ctx + "/housekeeper/") && "HOUSEKEEPER".equals(role));

            if (!allowed) {
                response.sendRedirect(ctx + "/403");
                return;
            }

            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(req, res);
    }
}