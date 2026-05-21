package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.AuthPrincipal;
import roomescape.auth.JwtProvider;
import roomescape.auth.annotation.RequireAuth;
import roomescape.auth.exception.MissingAuthorizationHeaderException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String BEARER_PREFIX = "Bearer ";
    private final String AUTH_PRINCIPAL_ATTRIBUTE = "AuthPrincipal";

    private final JwtProvider jwtProvider;

    public AuthenticationInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        boolean needAuthenticated =
                AnnotatedElementUtils.hasAnnotation(hm.getMethod(), RequireAuth.class)
                        || AnnotatedElementUtils.hasAnnotation(hm.getBeanType(), RequireAuth.class);

        if (!needAuthenticated) {
            return true;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MissingAuthorizationHeaderException();
        }

        String accessToken = authorization.trim().substring(BEARER_PREFIX.length());
        Long memberId = jwtProvider.extractSub(accessToken);

        request.setAttribute(AUTH_PRINCIPAL_ATTRIBUTE, new AuthPrincipal(memberId));

        return true;
    }
}
