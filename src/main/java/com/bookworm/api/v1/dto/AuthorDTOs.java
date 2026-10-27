package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AuthorDTOs {

    private AuthorDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDTO {
        private UUID id;
        private String name;
        private String slug;
        private String biography;
        private String profileImageUrl;
        private int booksCount;
        private int followersCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDetailResponse {
        private UUID id;
        private String name;
        private String slug;
        private String biography;
        private String profileImageUrl;
        private boolean isFollowed;
        private List<BookDTOs.BookSummaryDTO> books;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowAuthorResponse {
        private UUID authorId;
        private boolean isFollowed;
        private Instant followedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowedAuthorDTO {
        private UUID authorId;
        private String name;
        private String slug;
        private String profileImageUrl;
        private LatestReleaseDTO latestRelease;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LatestReleaseDTO {
        private UUID bookId;
        private String title;
        private LocalDate publishedDate;
    }
}
