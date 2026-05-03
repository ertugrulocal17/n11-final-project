package com.n11bootcamp.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void from_mapsSpringPageFields() {
        var pageable = PageRequest.of(2, 10);
        var springPage = new PageImpl<>(List.of("x", "y"), pageable, 45);

        PageResponse<String> r = PageResponse.from(springPage);

        assertThat(r.content()).containsExactly("x", "y");
        assertThat(r.page()).isEqualTo(2);
        assertThat(r.size()).isEqualTo(10);
        assertThat(r.totalElements()).isEqualTo(45);
        assertThat(r.totalPages()).isEqualTo(5);
    }
}
