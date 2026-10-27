package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.AuthorDTOs;
import com.bookworm.core.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author profiles, published works, and 'My Writers' subscriptions.")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "List and search authors", operationId = "listAuthors")
    @GetMapping
    public ResponseEntity<Page<AuthorDTOs.AuthorDTO>> listAuthors(
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(authorService.listAuthors(search, pageable));
    }

    @Operation(summary = "Get author details and bibliography", operationId = "getAuthorDetails")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Author profile details"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{authorIdOrSlug}")
    public ResponseEntity<AuthorDTOs.AuthorDetailResponse> getAuthor(
        @PathVariable("authorIdOrSlug") String authorIdOrSlug,
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(authorService.getAuthorDetails(authorIdOrSlug, userId));
    }

    @Operation(summary = "Follow author (Add to 'My Writers')", operationId = "followAuthor")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{authorId}/follow")
    public ResponseEntity<AuthorDTOs.FollowAuthorResponse> follow(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("authorId") UUID authorId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(authorService.followAuthor(effectiveUserId, authorId));
    }

    @Operation(summary = "Unfollow author", operationId = "unfollowAuthor")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{authorId}/follow")
    public ResponseEntity<Void> unfollow(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("authorId") UUID authorId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        authorService.unfollowAuthor(effectiveUserId, authorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List followed authors ('My Writers')", operationId = "listFollowedAuthors")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/followed")
    public ResponseEntity<List<AuthorDTOs.FollowedAuthorDTO>> listFollowed(
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(authorService.getFollowedAuthors(effectiveUserId));
    }
}
