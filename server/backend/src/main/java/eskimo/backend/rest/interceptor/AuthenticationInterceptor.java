package eskimo.backend.rest.interceptor;

import eskimo.backend.entity.User;
import eskimo.backend.entity.UserSession;
import eskimo.backend.entity.enums.Role;
import eskimo.backend.rest.AdminApiController;
import eskimo.backend.rest.UserApiController;
import eskimo.backend.rest.holder.AuthenticationHolder;
import eskimo.backend.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.math.NumberUtils.createLong;

/**
 * Intercepts all requests to backend controllers. Decides can user do this request or not according to user role.
 * Anonymous user can call methods only from {@link eskimo.backend.rest.PublicApiController}.
 * User = anonymous + {@link UserApiController} methods.
 * Admin = user + {@link AdminApiController} methods.
 */
@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    public static final String ESKIMO_UID_COOKIE_NAME = "eskimoUid";
    public static final String ESKIMO_TOKEN_COOKIE_NAME = "eskimoToken";

    private static final Set<String> ESKIMO_COOKIES = Collections.unmodifiableSet(new HashSet<>(asList(
            ESKIMO_UID_COOKIE_NAME, ESKIMO_TOKEN_COOKIE_NAME
    )));

    private final UserService userService;

    @Autowired
    private AuthenticationHolder authenticationHolder;

    @Autowired
    public AuthenticationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String servletPath = request.getServletPath();
        if (servletPath.startsWith("/api/invoker")) {
            authenticateInvoker();
        } else {
            Map<String, String> cookies = getAuthorizationCookies(
                    Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}));

            Long userId = createLong(cookies.get(ESKIMO_UID_COOKIE_NAME));
            User user = userId == null ? new User() : userService.getUserById(userId);
            UserSession userSession = getUserSession(request, cookies, user);
            authenticationHolder.setUserSession(userSession);
            if (userSession == null) {
                if (user == null) {
                    user = new User();
                }
                user.setId(null);
                user.setUsername("");
                user.setRole(Role.ANONYMOUS);
            }
            authenticationHolder.setUser(user);
            Class<?> controllerType = handlerMethod.getBeanType();
            boolean badUserRequest = user.getRole() == Role.USER && controllerType.equals(AdminApiController.class);
            boolean badAnonymousRequest = user.getRole() == Role.ANONYMOUS
                    && (controllerType.equals(AdminApiController.class) || controllerType.equals(UserApiController.class));
            if (badUserRequest || badAnonymousRequest) {
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }
        return true;
    }

    /**
     * Gets user session, if exists and not up-to-date or null.
     * If user session exists:
     * - if its last request time is out of date - removes session from db
     * - if last request time is OK - updates it on current time
     */
    private UserSession getUserSession(HttpServletRequest request, Map<String, String> cookies, User user) {
        if (user == null) {
            return null;
        }
        String token = cookies.get(ESKIMO_TOKEN_COOKIE_NAME);
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        if (token == null || userAgent == null || ip == null) {
            return null;
        }
        UserSession userSession = userService.getUserSession(user.getId(), token, userAgent, ip);
        if (userSession == null) {
            return null;
        } else {
            userService.updateLastRequestTime(userSession);
            return userSession;
        }
    }

    private void authenticateInvoker() {
        //todo check allowed hosts
    }

    /**
     * Returns eskimo cookies. See {@link AuthenticationInterceptor#ESKIMO_COOKIES}
     */
    private Map<String, String> getAuthorizationCookies(Cookie[] cookies) {
        return Arrays.stream(cookies).filter(c -> ESKIMO_COOKIES.contains(c.getName()))
                .collect(toMap(Cookie::getName, Cookie::getValue));
    }
}