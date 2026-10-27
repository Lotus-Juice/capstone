package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.PublisherDTOs;
import com.bookworm.core.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "Publishers", description = "Publisher directories and brand catalogs.")
public class PublisherController {

    private final PublisherService publisherService;

    @Operation(summary = "List publishers", operationId = "listPublishers")
    @GetMapping
    public ResponseEntity<Page<PublisherDTOs.PublisherDTO>> listPublishers(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(publisherService.listPublishers(pageable));
    }

    @Operation(summary = "Get publisher details", operationId = "getPublisherDetails")
    @GetMapping("/{publisherIdOrSlug}")
    public ResponseEntity<PublisherDTOs.PublisherDTO> getPublisher(
        @PathVariable("publisherIdOrSlug") String publisherIdOrSlug
    ) {
        return ResponseEntity.ok(publisherService.getPublisherDetails(publisherIdOrSlug));
    }
}
