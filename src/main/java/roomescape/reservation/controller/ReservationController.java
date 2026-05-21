package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.exception.InvalidRequestFormatException;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservationsByName(@RequestParam("name") String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestFormatException();
        }

        List<ReservationResponse> responses = reservationService.findReservationsByName(name)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
