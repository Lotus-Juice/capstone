package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.BookDTOs.BookDetailResponse;
import com.bookworm.api.v1.dto.BookDTOs.BookSummaryDTO;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.service.CatalogService;
import com.bookworm.web.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Web Unit Tests")
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/books")
    class ListBooksEndpointTests {

        @Test
        @DisplayName("Should return paginated list of books with 200 OK")
        void shouldReturnPaginatedBooks() throws Exception {
            BookSummaryDTO book = BookSummaryDTO.builder()
                .id(UUID.randomUUID())
                .title("Clean Code")
                .slug("clean-code")
                .authors(List.of("Robert C. Martin"))
                .sellingPrice(new BigDecimal("450.00"))
                .averageRating(new BigDecimal("4.80"))
                .build();

            Page<BookSummaryDTO> page = new PageImpl<>(List.of(book));

            given(catalogService.listBooks(any(), any(), any(), any(), any(), eq("relevance"), any(Pageable.class)))
                .willReturn(page);

            mockMvc.perform(get("/api/v1/books")
                    .param("page", "1")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.content[0].slug").value("clean-code"))
                .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/{bookIdOrSlug}")
    class GetBookDetailsEndpointTests {

        @Test
        @DisplayName("Should return book detail PDP for valid slug")
        void shouldReturnBookDetails() throws Exception {
            BookDetailResponse bookDetail = BookDetailResponse.builder()
                .id(UUID.randomUUID())
                .title("Design Patterns")
                .slug("design-patterns")
                .synopsis("Elements of Reusable Object-Oriented Software")
                .build();

            given(catalogService.getBookDetails(eq("design-patterns"), any()))
                .willReturn(bookDetail);

            mockMvc.perform(get("/api/v1/books/design-patterns"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Design Patterns"))
                .andExpect(jsonPath("$.slug").value("design-patterns"));
        }

        @Test
        @DisplayName("Should return 404 Problem Details when book is not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(catalogService.getBookDetails(eq("non-existent-book"), any()))
                .willThrow(new ResourceNotFoundException("Book with identifier non-existent-book not found"));

            mockMvc.perform(get("/api/v1/books/non-existent-book"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }
}
