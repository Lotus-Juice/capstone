package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CategoryDTOs.CategoryDetailResponse;
import com.bookworm.api.v1.dto.CategoryDTOs.CategoryNodeDTO;
import com.bookworm.core.service.CategoryService;
import com.bookworm.web.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Web Unit Tests")
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetCategoryTreeTests {

        @Test
        @DisplayName("Should return hierarchical category taxonomy tree with 200 OK")
        void shouldReturnCategoryTree() throws Exception {
            CategoryNodeDTO node = CategoryNodeDTO.builder()
                .id(UUID.randomUUID())
                .name("Technology")
                .slug("technology")
                .subcategories(List.of(
                    CategoryNodeDTO.builder()
                        .id(UUID.randomUUID())
                        .name("Software Engineering")
                        .slug("software-engineering")
                        .build()
                ))
                .build();

            given(categoryService.getCategoryTree()).willReturn(List.of(node));

            mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Technology"))
                .andExpect(jsonPath("$[0].subcategories[0].name").value("Software Engineering"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{categoryIdOrSlug}")
    class GetCategoryDetailsTests {

        @Test
        @DisplayName("Should return category details with 200 OK")
        void shouldReturnCategoryDetails() throws Exception {
            CategoryDetailResponse detail = CategoryDetailResponse.builder()
                .id(UUID.randomUUID())
                .name("Science Fiction")
                .slug("sci-fi")
                .description("Futuristic speculative fiction")
                .bookCount(25)
                .build();

            given(categoryService.getCategoryDetails("sci-fi")).willReturn(detail);

            mockMvc.perform(get("/api/v1/categories/sci-fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Science Fiction"))
                .andExpect(jsonPath("$.slug").value("sci-fi"))
                .andExpect(jsonPath("$.bookCount").value(25));
        }
    }
}
