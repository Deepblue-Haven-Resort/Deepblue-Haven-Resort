package deepbluehaven.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import deepbluehaven.pojo.Worker;
import deepbluehaven.repositories.WorkerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final WorkerRepository workerRepository;

    public AuthInterceptor(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        HttpSession session = request.getSession(false);
        Long workerId = (session != null) ? (Long) session.getAttribute("loggedInWorkerId") : null;
        String workerRole = (session != null) ? (String) session.getAttribute("workerRole") : null;
        Long customerId = (session != null) ? (Long) session.getAttribute("loggedInCustomerId") : null;

        if (workerId != null && workerRole == null && session != null) {
            try {
                Worker worker = workerRepository.findById(workerId).orElse(null);
                if (worker != null && worker.getProfile() != null && worker.getProfile().getRole() != null) {
                    workerRole = worker.getProfile().getRole().name();
                    session.setAttribute("workerRole", workerRole);
                    session.setAttribute("workerName", worker.getProfile().getFullName());
                }
            } catch (Exception ignored) {
            }
        }

        if (uri.startsWith("/admin")) {
            if (workerId == null) {
                response.sendRedirect(contextPath + "/staff-login?redirect=" + uri);
                return false;
            }
            if (!"ADMIN".equalsIgnoreCase(workerRole)) {
                response.sendRedirect(contextPath + "/staff-login?unauthorized=true");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/manager")) {
            if (workerId == null) {
                response.sendRedirect(contextPath + "/staff-login?redirect=" + uri);
                return false;
            }
            if (!"MANAGER".equalsIgnoreCase(workerRole) && !"ADMIN".equalsIgnoreCase(workerRole)) {
                response.sendRedirect(contextPath + "/staff-login?unauthorized=true");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/receptionist")) {
            if (workerId == null) {
                response.sendRedirect(contextPath + "/staff-login?redirect=" + uri);
                return false;
            }
            if (!"RECEPTIONIST".equalsIgnoreCase(workerRole) && !"MANAGER".equalsIgnoreCase(workerRole) && !"ADMIN".equalsIgnoreCase(workerRole)) {
                response.sendRedirect(contextPath + "/staff-login?unauthorized=true");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/housekeeper")) {
            if (workerId == null) {
                response.sendRedirect(contextPath + "/staff-login?redirect=" + uri);
                return false;
            }
            if (!"HOUSEKEEPER".equalsIgnoreCase(workerRole) && !"MANAGER".equalsIgnoreCase(workerRole) && !"ADMIN".equalsIgnoreCase(workerRole)) {
                response.sendRedirect(contextPath + "/staff-login?unauthorized=true");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/booking/history") || uri.startsWith("/profile") || uri.startsWith("/favorites")) {
            if (customerId == null) {
                response.sendRedirect(contextPath + "/login?required=true");
                return false;
            }
            return true;
        }

        return true;
    }
}
