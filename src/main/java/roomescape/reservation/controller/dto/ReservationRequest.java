package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
import roomescape.reservation.service.dto.ReservationCommand;

public record ReservationRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        if (memberId == null || date == null || timeId == null || themeId == null) {
            throw new InvalidReservationRequestFormatException();
        }
    }

    public ReservationCommand toCommand() {
        return new ReservationCommand(
                memberId,
                date,
                timeId,
                themeId
        );
    }
}
