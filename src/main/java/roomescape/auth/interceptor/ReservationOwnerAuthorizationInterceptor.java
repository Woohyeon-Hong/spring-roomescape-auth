package roomescape.auth.interceptor;

import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.AuthPrincipal;
import roomescape.auth.annotation.RequireReservationOwner;
import roomescape.auth.exception.AuthorizationException;
import roomescape.auth.exception.MissingAuthorizationHeaderException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;

@Component
public class ReservationOwnerAuthorizationInterceptor implements HandlerInterceptor {

    private final String AUTH_PRINCIPAL_ATTRIBUTE = "AuthPrincipal";

    private final ReservationRepository reservationRepository;

    public ReservationOwnerAuthorizationInterceptor(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        boolean needAuthenticated =
                hm.hasMethodAnnotation(RequireReservationOwner.class)
                        || hm.getBeanType().isAnnotationPresent(RequireReservationOwner.class);

        if (!needAuthenticated) {
            return true;
        }

        AuthPrincipal authPrincipal = (AuthPrincipal) request.getAttribute(AUTH_PRINCIPAL_ATTRIBUTE);

        if (authPrincipal == null) {
            throw new MissingAuthorizationHeaderException();
        }

        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVars == null || !uriVars.containsKey("id")) {
            throw new ReservationNotFoundException();
        }

        Long reservationId;
        try {
            reservationId = Long.valueOf(uriVars.get("id"));
        } catch (NumberFormatException e) {
            throw new InvalidReservationRequestFormatException();
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.isMadeBy(authPrincipal.memberId())) {
            throw new AuthorizationException();
        }

        return true;
    }
}
