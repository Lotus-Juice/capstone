package com.bookworm.api.v1.dto;

import lombok.*;

import java.util.UUID;

public final class PublisherDTOs {

    private PublisherDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublisherDTO {
        private UUID id;
        private String name;
        private String slug;
        private String description;
        private String website;
        private int booksCount;
    }
}
