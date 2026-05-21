package roomescape.reservation.service.dto;

import java.time.LocalDate;

public record ReservationCommand(Long memberId, LocalDate date, Long timeId, Long themeId) {
}
