package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeInUseException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    ThemeRepository themeRepository;

    @InjectMocks
    ThemeService themeService;

    @DisplayName("테마를 새로 생성한다.")
    @Test
    void registerThemeTest_success() {
        //given
        when(themeRepository.existByName("브라운"))
                .thenReturn(false);

        when(themeRepository.save(any()))
                .thenReturn(new Theme(1L, "브라운", "설명", "url"));

        //when
        Theme theme = themeService.registerTheme(
                new ThemeCommand("브라운", "설명", "url")
        );

        //then
        assertThat(theme).isEqualTo(new Theme(1L, "브라운", "설명", "url"));
    }

    @DisplayName("테마 생성 시, 기존에 이미 동일한 테마가 있으면 예외가 발생한다.")
    @Test
    void registerThemeTest_duplicate() {
        //given
        when(themeRepository.existByName("브라운"))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(
                () -> themeService.registerTheme(new ThemeCommand("브라운", "설명", "url"))
        ).isInstanceOf(DuplicateThemeException.class);
    }

    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    @Test
    void removeThemeByIdTest_not_found() {
        //given
        when(themeRepository.findById(1L))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(
                () -> themeService.removeThemeById(1L)
        ).isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("테마 삭제시, 테마가 사용 중이면 예외가 발생한다.")
    @Test
    void removeThemeByIdTest_in_use() {
        //given
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(new Theme(1L, "테마", "설명", "url")));

        when(themeRepository.deleteById(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(() -> themeService.removeThemeById(1L))
                .isInstanceOf(ThemeInUseException.class);
    }
}
