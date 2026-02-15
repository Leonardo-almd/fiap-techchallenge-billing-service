package br.com.techchallenge.fiap.billingservice.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageDto - Unit Tests")
class PageDtoTest {

    @Test
    @DisplayName("Should create page with correct metadata")
    void shouldCreatePageWithCorrectMetadata() {
        List<String> content = List.of("a", "b", "c");
        PageDto<String> page = PageDto.of(content, 0, 10, 25);

        assertThat(page.content()).hasSize(3);
        assertThat(page.pageNumber()).isEqualTo(0);
        assertThat(page.pageSize()).isEqualTo(10);
        assertThat(page.totalElements()).isEqualTo(25);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isFalse();
    }

    @Test
    @DisplayName("Should set first true only when page is 0")
    void shouldSetFirstTrueOnlyWhenPageIs0() {
        PageDto<String> firstPage = PageDto.of(List.of("x"), 0, 10, 100);
        assertThat(firstPage.first()).isTrue();

        PageDto<String> secondPage = PageDto.of(List.of("x"), 1, 10, 100);
        assertThat(secondPage.first()).isFalse();
    }

    @Test
    @DisplayName("Should set last true on last page")
    void shouldSetLastTrueOnLastPage() {
        PageDto<String> lastPage = PageDto.of(List.of("x"), 2, 10, 25);
        assertThat(lastPage.last()).isTrue();
        assertThat(lastPage.totalPages()).isEqualTo(3);
    }
}
