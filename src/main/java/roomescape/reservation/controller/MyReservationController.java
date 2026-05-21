package roomescape.reservation.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.RequireAuth;
import roomescape.auth.annotation.RequireReservationOwner;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/members/me/reservations")
public class MyReservationController {

    private final ReservationService reservationService;

    public MyReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @RequireAuth
    @PostMapping
    public ResponseEntity<Void> createReservation(@RequestBody ReservationRequest requestDto) {
        Reservation reservation = reservationService.makeReservation(requestDto.toCommand());
        ReservationResponse response = ReservationResponse.from(reservation);

        return ResponseEntity
                .created(URI.create("/reservations/" + response.id()))
                .build();
    }

    @RequireReservationOwner
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateMyReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request
    ) {
        reservationService.updateReservation(request.toCommand(), id);
        return ResponseEntity.noContent().build();
    }
    
    @RequireReservationOwner
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyReservation(
            @PathVariable Long id
    ) {
        reservationService.validateReservationNotExpired(id);
        reservationService.deleteReservationById(id);

        return ResponseEntity.noContent().build();
    }
}
