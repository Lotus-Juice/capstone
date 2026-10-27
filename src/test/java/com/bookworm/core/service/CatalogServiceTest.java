package com.bookworm.core.service;

import com.bookworm.api.v1.dto.BookDTOs.BookDetailResponse;
import com.bookworm.api.v1.dto.BookDTOs.BookSummaryDTO;
import com.bookworm.api.v1.dto.RecommendationDTOs.HomeRecommendationsResponse;
import com.bookworm.api.v1.dto.SearchDTOs.SearchResultResponse;
import com.bookworm.api.v1.dto.SearchDTOs.SearchSuggestionsResponse;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.BookMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService Unit Tests")
class CatalogServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private CatalogService catalogService;

    private BookEntity sampleBook;
    private BookSummaryDTO sampleSummaryDto;
    private BookDetailResponse sampleDetailDto;

    @BeforeEach
    void setUp() {
        sampleBook = BookEntity.builder()
            .id(UUID.randomUUID())
            .title("Clean Architecture")
            .slug("clean-architecture")
            .synopsis("A Craftsman's Guide to Software Structure and Design")
            .language("English")
            .coverImageUrl("https://images.example.com/clean-arch.jpg")
            .copiesSold(5000)
            .averageRating(new BigDecimal("4.85"))
            .ratingsCount(120)
            .isFeatured(true)
            .bookAuthors(new ArrayList<>())
            .variants(new ArrayList<>())
            .build();

        sampleSummaryDto = BookSummaryDTO.builder()
            .id(sampleBook.getId())
            .title(sampleBook.getTitle())
            .slug(sampleBook.getSlug())
            .averageRating(sampleBook.getAverageRating())
            .ratingsCount(sampleBook.getRatingsCount())
            .build();

        sampleDetailDto = BookDetailResponse.builder()
            .id(sampleBook.getId())
            .title(sampleBook.getTitle())
            .slug(sampleBook.getSlug())
            .synopsis(sampleBook.getSynopsis())
            .build();
    }

    @Nested
    @DisplayName("List and Filter Books")
    class ListBooksTests {

        @Test
        @DisplayName("Should return paginated books with sorting and filters")
        void shouldListBooksWithFilters() {
            Page<BookEntity> page = new PageImpl<>(List.of(sampleBook));
            given(bookRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
            given(bookMapper.toSummaryDTO(sampleBook)).willReturn(sampleSummaryDto);

            Page<BookSummaryDTO> result = catalogService.listBooks(
                "software-engineering",
                "English",
                "PAPERBACK",
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                "rating",
                PageRequest.of(0, 20)
            );

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Architecture");
        }
    }

    @Nested
    @DisplayName("Get Book Details")
    class BookDetailsTests {

        @Test
        @DisplayName("Should fetch book details by slug successfully")
        void shouldFetchBookDetailsBySlug() {
            given(bookRepository.findBySlugWithDetails("clean-architecture")).willReturn(Optional.of(sampleBook));
            given(bookMapper.toDetailResponse(sampleBook)).willReturn(sampleDetailDto);

            BookDetailResponse response = catalogService.getBookDetails("clean-architecture", null);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Clean Architecture");
        }

        @Test
        @DisplayName("Should fetch book details by UUID successfully")
        void shouldFetchBookDetailsByUUID() {
            UUID id = sampleBook.getId();
            given(bookRepository.findByIdWithDetails(id)).willReturn(Optional.of(sampleBook));
            given(bookMapper.toDetailResponse(sampleBook)).willReturn(sampleDetailDto);

            BookDetailResponse response = catalogService.getBookDetails(id.toString(), null);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when slug does not exist")
        void shouldThrowExceptionWhenNotFound() {
            given(bookRepository.findBySlugWithDetails("unknown-book")).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalogService.getBookDetails("unknown-book", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book with identifier unknown-book not found");
        }
    }

    @Nested
    @DisplayName("Search & Autocomplete Suggestions")
    class SearchTests {

        @Test
        @DisplayName("Should return search suggestions for given prefix keyword")
        void shouldReturnSearchSuggestions() {
            Page<BookEntity> matches = new PageImpl<>(List.of(sampleBook));
            given(bookRepository.searchByKeyword(eq("Clean"), any(Pageable.class))).willReturn(matches);

            SearchSuggestionsResponse response = catalogService.getSearchSuggestions("Clean");

            assertThat(response).isNotNull();
            assertThat(response.getSuggestions()).hasSize(1);
            assertThat(response.getSuggestions().get(0).getTitle()).isEqualTo("Clean Architecture");
        }

        @Test
        @DisplayName("Should return search results with facets")
        void shouldReturnSearchResultsWithFacets() {
            Page<BookEntity> results = new PageImpl<>(List.of(sampleBook));
            given(bookRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(results);
            given(bookMapper.toSummaryDTO(sampleBook)).willReturn(sampleSummaryDto);

            SearchResultResponse response = catalogService.searchBooks(
                "Clean", null, null, null, null, null, PageRequest.of(0, 20)
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getFacets()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Home Recommendations")
    class HomeRecommendationsTests {

        @Test
        @DisplayName("Should aggregate bestsellers, new launches, and recommended carousels")
        void shouldAggregateHomeRecommendations() {
            given(bookRepository.findTop10ByOrderByCopiesSoldDesc()).willReturn(List.of(sampleBook));
            given(bookRepository.findTop10ByOrderByPublishedDateDesc()).willReturn(List.of(sampleBook));
            given(bookRepository.findByIsFeaturedTrueOrderByAverageRatingDesc(any(Pageable.class))).willReturn(List.of(sampleBook));
            given(bookMapper.toSummaryDTOList(any())).willReturn(List.of(sampleSummaryDto));

            HomeRecommendationsResponse response = catalogService.getHomeRecommendations(null);

            assertThat(response).isNotNull();
            assertThat(response.getBestsellersThisMonth()).isNotEmpty();
            assertThat(response.getNewLaunches()).isNotEmpty();
            assertThat(response.getRecommendedForYou()).isNotEmpty();
        }
    }
}
