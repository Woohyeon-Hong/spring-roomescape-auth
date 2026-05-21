package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.exception.MissingAuthorizationHeaderException;
import roomescape.member.repository.MemberRepository;

public class AuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final String AUTH_PRINCIPAL_ATTRIBUTE = "AuthPrincipal";

    private final MemberRepository memberRepository;

    public AuthPrincipalArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean isAuthPrincipalType = AuthPrincipal.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isAuthPrincipalType;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory)
            throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        AuthPrincipal authPrincipal = (AuthPrincipal) request.getAttribute(AUTH_PRINCIPAL_ATTRIBUTE);

        if (authPrincipal == null) {
            throw new MissingAuthorizationHeaderException();
        }

        return memberRepository.getById(authPrincipal.memberId());
    }
}
