package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeInUseException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간을 새로 등록한다.")
    @Test
    void registerReservationTimeTest_success() {
        //given
        when(reservationTimeRepository.existByStartAt(LocalTime.of(10, 0)))
                .thenReturn(false);

        when(reservationTimeRepository.save(
                        ReservationTime.of(LocalTime.of(10, 0))
                )
        ).thenReturn(new ReservationTime(1L, LocalTime.of(10, 0)));

        //when
        ReservationTime saved = reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );

        //then
        assertThat(saved.getId()).isNotNull();
    }

    @DisplayName("예약 시간 생성 시, 기존에 이미 동일한 시간이 있으면 예외가 발생한다.")
    @Test
    void registerReservationTimeTest_duplicate() {
        //given
        when(reservationTimeRepository.existByStartAt(LocalTime.of(10, 0)))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(
                () -> reservationTimeService.registerReservationTime(
                        new ReservationTimeCommand(LocalTime.of(10, 0))
                )
        ).isInstanceOf(DuplicateTimeException.class);
    }

    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    @Test
    void removeThemeByIdTest_not_found() {
        //given
        when(reservationTimeRepository.findById(1L))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(
                () -> reservationTimeService.removeReservationTimeById(1L)
        ).isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약 시간 삭제시, 예약 시간이 사용 중이면 예외가 발생한다.")
    @Test
    void removeThemeByIdTest_in_use() {
        //given
        when(reservationTimeRepository.findById(1L))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(reservationTimeRepository.deleteById(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(
                () -> reservationTimeService.removeReservationTimeById(1L)
        ).isInstanceOf(TimeInUseException.class);
    }
}
