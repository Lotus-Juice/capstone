package com.bookworm.core.service;

import com.bookworm.api.v1.dto.BookDTOs;
import com.bookworm.api.v1.dto.RecommendationDTOs;
import com.bookworm.api.v1.dto.SearchDTOs;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.BookMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public Page<BookDTOs.BookSummaryDTO> listBooks(
        String categorySlug,
        String language,
        String formatType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sortBy,
        Pageable pageable
    ) {
        log.info("Listing books - category: {}, format: {}, language: {}, sortBy: {}", categorySlug, formatType, language, sortBy);

        Specification<BookEntity> spec = Specification.where(BookSpecifications.hasCategorySlug(categorySlug))
            .and(BookSpecifications.hasLanguage(language))
            .and(BookSpecifications.hasFormat(formatType))
            .and(BookSpecifications.priceBetween(minPrice, maxPrice));

        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "variants.sellingPrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "variants.sellingPrice");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "publishedDate");
            case "bestseller" -> Sort.by(Sort.Direction.DESC, "copiesSold");
            default -> Sort.by(Sort.Direction.DESC, "averageRating").and(Sort.by(Sort.Direction.DESC, "copiesSold"));
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<BookEntity> books = bookRepository.findAll(spec, sortedPageable);

        return books.map(bookMapper::toSummaryDTO);
    }

    @Transactional(readOnly = true)
    public BookDTOs.BookDetailResponse getBookDetails(String bookIdOrSlug, UUID currentUserId) {
        log.info("Fetching book details for identifier: {}", bookIdOrSlug);

        BookEntity book;
        try {
            UUID id = UUID.fromString(bookIdOrSlug);
            book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        } catch (IllegalArgumentException e) {
            book = bookRepository.findBySlugWithDetails(bookIdOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookIdOrSlug));
        }

        return bookMapper.toDetailResponse(book);
    }

    @Transactional(readOnly = true)
    public SearchDTOs.SearchResultResponse searchBooks(
        String query,
        String category,
        String format,
        String language,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Pageable pageable
    ) {
        log.info("Searching books for query: '{}'", query);

        Specification<BookEntity> spec = Specification.where(BookSpecifications.containsKeyword(query))
            .and(BookSpecifications.hasCategorySlug(category))
            .and(BookSpecifications.hasFormat(format))
            .and(BookSpecifications.hasLanguage(language))
            .and(BookSpecifications.priceBetween(minPrice, maxPrice));

        Page<BookEntity> results = bookRepository.findAll(spec, pageable);
        List<BookDTOs.BookSummaryDTO> summaryDTOs = results.getContent().stream()
            .map(bookMapper::toSummaryDTO)
            .toList();

        // Facets aggregation dummy structure for OpenSearch / SQL aggregation
        SearchDTOs.FacetsDTO facets = SearchDTOs.FacetsDTO.builder()
            .categories(List.of(new SearchDTOs.FacetItemDTO("Self-help", 12), new SearchDTOs.FacetItemDTO("Philosophy", 4)))
            .formats(List.of(new SearchDTOs.FacetItemDTO("Paperback", 14), new SearchDTOs.FacetItemDTO("eBook", 16)))
            .languages(List.of(new SearchDTOs.FacetItemDTO("English", 16)))
            .priceRanges(List.of(new SearchDTOs.PriceRangeFacetDTO("0-199", 8), new SearchDTOs.PriceRangeFacetDTO("200-499", 7)))
            .build();

        return SearchDTOs.SearchResultResponse.builder()
            .content(summaryDTOs)
            .facets(facets)
            .page(results.getNumber() + 1)
            .size(results.getSize())
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .build();
    }

    @Transactional(readOnly = true)
    public SearchDTOs.SearchSuggestionsResponse getSearchSuggestions(String query) {
        log.info("Fetching search autocomplete suggestions for: '{}'", query);

        Page<BookEntity> matches = bookRepository.searchByKeyword(query, PageRequest.of(0, 5));
        List<SearchDTOs.SuggestionItemDTO> suggestions = matches.getContent().stream()
            .map(b -> new SearchDTOs.SuggestionItemDTO("BOOK", b.getId().toString(), b.getTitle()))
            .toList();

        return SearchDTOs.SearchSuggestionsResponse.builder()
            .suggestions(suggestions)
            .build();
    }

    @Transactional(readOnly = true)
    public RecommendationDTOs.HomeRecommendationsResponse getHomeRecommendations(UUID currentUserId) {
        log.info("Generating Home Page recommendation feeds for user ID: {}", currentUserId);

        List<BookEntity> bestsellers = bookRepository.findTop10ByOrderByCopiesSoldDesc();
        List<BookEntity> newLaunches = bookRepository.findTop10ByOrderByPublishedDateDesc();
        List<BookEntity> recommended = bookRepository.findByIsFeaturedTrueOrderByAverageRatingDesc(PageRequest.of(0, 6));

        return RecommendationDTOs.HomeRecommendationsResponse.builder()
            .recommendedForYou(bookMapper.toSummaryDTOList(recommended))
            .bestsellersThisMonth(bookMapper.toSummaryDTOList(bestsellers))
            .newLaunches(bookMapper.toSummaryDTOList(newLaunches))
            .build();
    }

    @Transactional(readOnly = true)
    public List<RecommendationDTOs.RelatedBookDTO> getRelatedReads(UUID bookId, int limit) {
        log.info("Fetching related reads for book ID: {}, limit: {}", bookId, limit);

        List<BookEntity> related = bookRepository.findRelatedBooksByCategory(bookId, PageRequest.of(0, limit));
        return related.stream()
            .map(b -> {
                var summary = bookMapper.toSummaryDTO(b);
                return RecommendationDTOs.RelatedBookDTO.builder()
                    .id(b.getId())
                    .title(b.getTitle())
                    .author(summary.getAuthor() != null ? summary.getAuthor().getName() : "")
                    .format(!summary.getAvailableFormats().isEmpty() ? summary.getAvailableFormats().get(0) : "Paperback")
                    .sellingPrice(summary.getStartingPrice())
                    .coverImageUrl(b.getCoverImageUrl())
                    .build();
            })
            .toList();
    }
}
