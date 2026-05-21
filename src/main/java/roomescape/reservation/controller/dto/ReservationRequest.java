package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
import roomescape.reservation.service.dto.ReservationCommand;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        if (date == null || timeId == null || themeId == null) {
            throw new InvalidReservationRequestFormatException();
        }
    }

    public ReservationCommand toCommand(Long memberId) {
        return new ReservationCommand(
                memberId,
                date,
                timeId,
                themeId
        );
    }
}
