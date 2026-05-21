package roomescape.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AuthPrincipalArgumentResolver;
import roomescape.auth.interceptor.AuthenticationInterceptor;
import roomescape.auth.interceptor.ReservationOwnerAuthorizationInterceptor;
import roomescape.global.exception.support.BusinessExceptionMappingJackson2HttpMessageConverter;
import roomescape.member.repository.MemberRepository;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final AuthenticationInterceptor authenticationInterceptor;
    private final ReservationOwnerAuthorizationInterceptor reservationOwnerAuthorizationInterceptor;
    private final MemberRepository memberRepository;

    public WebMvcConfig(ObjectMapper objectMapper, AuthenticationInterceptor authenticationInterceptor,
                        ReservationOwnerAuthorizationInterceptor reservationOwnerAuthorizationInterceptor,
                        MemberRepository memberRepository) {
        this.objectMapper = objectMapper;
        this.authenticationInterceptor = authenticationInterceptor;
        this.reservationOwnerAuthorizationInterceptor = reservationOwnerAuthorizationInterceptor;
        this.memberRepository = memberRepository;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addFirst(new BusinessExceptionMappingJackson2HttpMessageConverter(objectMapper));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
        registry.addInterceptor(reservationOwnerAuthorizationInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addFirst(new AuthPrincipalArgumentResolver(memberRepository));
    }
}
