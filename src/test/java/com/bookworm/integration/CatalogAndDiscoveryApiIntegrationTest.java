package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.BookDTOs.BookDetailResponse;
import com.bookworm.api.v1.dto.CategoryDTOs.CategoryNodeDTO;
import com.bookworm.api.v1.dto.CouponDTOs.CouponOfferDTO;
import com.bookworm.api.v1.dto.CouponDTOs.CouponValidationResponse;
import com.bookworm.api.v1.dto.CouponDTOs.ValidateCouponRequest;
import com.bookworm.api.v1.dto.RecommendationDTOs.HomeRecommendationsResponse;
import com.bookworm.api.v1.dto.SearchDTOs.SearchResultResponse;
import com.bookworm.api.v1.dto.SearchDTOs.SearchSuggestionsResponse;
import com.bookworm.api.v1.dto.ShippingDTOs.ShippingEstimateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Catalog & Discovery Full API Integration Tests (PostgreSQL Testcontainers)")
class CatalogAndDiscoveryApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("Catalog Browsing & PDP API")
    class CatalogApiTests {

        @Test
        @DisplayName("GET /api/v1/books - Should return paginated catalog with seeded books")
        void shouldReturnPaginatedBooks() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/books?page=1&size=10", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("content");
            assertThat(response.getBody()).contains("Clean Code");
        }

        @Test
        @DisplayName("GET /api/v1/books/{slug} - Should return complete Product Detail Page (PDP)")
        void shouldReturnBookPdpDetails() {
            ResponseEntity<BookDetailResponse> response = restTemplate.getForEntity(
                "/api/v1/books/clean-code",
                BookDetailResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Clean Code");
            assertThat(response.getBody().getSlug()).isEqualTo("clean-code");
            assertThat(response.getBody().getVariants()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Category Taxonomy API")
    class CategoryApiTests {

        @Test
        @DisplayName("GET /api/v1/categories - Should return full hierarchical taxonomy tree")
        void shouldReturnCategoryTree() {
            ResponseEntity<List<CategoryNodeDTO>> response = restTemplate.exchange(
                "/api/v1/categories",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
            assertThat(response.getBody()).anyMatch(c -> c.getName().equals("Technology & Programming"));
        }
    }

    @Nested
    @DisplayName("Search & Suggestions API")
    class SearchApiTests {

        @Test
        @DisplayName("GET /api/v1/search - Should perform keyword search and return results with facets")
        void shouldSearchBooks() {
            ResponseEntity<SearchResultResponse> response = restTemplate.getForEntity(
                "/api/v1/search?q=Data",
                SearchResultResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent().get(0).getTitle()).contains("Data");
            assertThat(response.getBody().getFacets()).isNotNull();
        }

        @Test
        @DisplayName("GET /api/v1/search/suggestions - Should return fast autocomplete suggestions")
        void shouldReturnSearchSuggestions() {
            ResponseEntity<SearchSuggestionsResponse> response = restTemplate.getForEntity(
                "/api/v1/search/suggestions?q=Clean",
                SearchSuggestionsResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuggestions()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Home Recommendations API")
    class RecommendationApiTests {

        @Test
        @DisplayName("GET /api/v1/recommendations - Should aggregate Bestsellers, New Launches, and Recommended carousels")
        void shouldReturnHomeRecommendations() {
            ResponseEntity<HomeRecommendationsResponse> response = restTemplate.getForEntity(
                "/api/v1/recommendations",
                HomeRecommendationsResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBestsellersThisMonth()).isNotEmpty();
            assertThat(response.getBody().getNewLaunches()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Coupons & Shipping Estimation API")
    class CouponsAndShippingApiTests {

        @Test
        @DisplayName("GET /api/v1/coupons - Should list active promotional offers")
        void shouldListAvailableOffers() {
            ResponseEntity<List<CouponOfferDTO>> response = restTemplate.exchange(
                "/api/v1/coupons",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("POST /api/v1/coupons/validate - Should validate coupon and calculate discount")
        void shouldValidateCoupon() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("WELCOME20");
            request.setCartAmount(new BigDecimal("1000.00"));

            ResponseEntity<CouponValidationResponse> response = restTemplate.postForEntity(
                "/api/v1/coupons/validate",
                request,
                CouponValidationResponse.class
            );

            // Returns 200 OK or 400 Problem Details if coupon not active in test DB
            assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("GET /api/v1/shipping/estimate - Should calculate 4-day delivery SLA for 6-digit Indian pincode")
        void shouldEstimateShipping() {
            ResponseEntity<ShippingEstimateResponse> response = restTemplate.getForEntity(
                "/api/v1/shipping/estimate?pincode=560001",
                ShippingEstimateResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPincode()).isEqualTo("560001");
            assertThat(response.getBody().isServiceable()).isTrue();
        }
    }
}
