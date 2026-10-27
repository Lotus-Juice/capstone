package com.bookworm.core.service;

import com.bookworm.api.v1.dto.AuthorDTOs;
import com.bookworm.api.v1.dto.BookDTOs;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.AuthorMapper;
import com.bookworm.core.mapper.BookMapper;
import com.bookworm.infra.persistence.entity.AuthorEntity;
import com.bookworm.infra.persistence.entity.AuthorFollowEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.AuthorFollowRepository;
import com.bookworm.infra.persistence.repository.AuthorRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorFollowRepository authorFollowRepository;
    private final UserRepository userRepository;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public Page<AuthorDTOs.AuthorDTO> listAuthors(String search, Pageable pageable) {
        log.info("Listing authors with search filter: '{}'", search);
        Page<AuthorEntity> authors = (search != null && !search.isBlank())
            ? authorRepository.findByNameContainingIgnoreCase(search, pageable)
            : authorRepository.findAll(pageable);

        return authors.map(authorMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public AuthorDTOs.AuthorDetailResponse getAuthorDetails(String authorIdOrSlug, UUID currentUserId) {
        log.info("Fetching author profile: {}", authorIdOrSlug);

        AuthorEntity author;
        try {
            UUID id = UUID.fromString(authorIdOrSlug);
            author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        } catch (IllegalArgumentException e) {
            author = authorRepository.findBySlug(authorIdOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorIdOrSlug));
        }

        AuthorDTOs.AuthorDetailResponse response = authorMapper.toDetailResponse(author);
        if (author.getBookAuthors() != null) {
            List<BookDTOs.BookSummaryDTO> books = author.getBookAuthors().stream()
                .map(ba -> bookMapper.toSummaryDTO(ba.getBook()))
                .toList();
            response.setBooks(books);
        }

        if (currentUserId != null) {
            response.setFollowed(authorFollowRepository.existsByUserIdAndAuthorId(currentUserId, author.getId()));
        }

        return response;
    }

    @Transactional
    public AuthorDTOs.FollowAuthorResponse followAuthor(UUID userId, UUID authorId) {
        log.info("User ID: {} following author ID: {}", userId, authorId);

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        AuthorEntity author = authorRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));

        if (!authorFollowRepository.existsByUserIdAndAuthorId(userId, authorId)) {
            AuthorFollowEntity follow = AuthorFollowEntity.builder()
                .user(user)
                .author(author)
                .build();
            authorFollowRepository.save(follow);
        }

        return AuthorDTOs.FollowAuthorResponse.builder()
            .authorId(authorId)
            .isFollowed(true)
            .followedAt(Instant.now())
            .build();
    }

    @Transactional
    public void unfollowAuthor(UUID userId, UUID authorId) {
        log.info("User ID: {} unfollowing author ID: {}", userId, authorId);
        authorFollowRepository.deleteByUserIdAndAuthorId(userId, authorId);
    }

    @Transactional(readOnly = true)
    public List<AuthorDTOs.FollowedAuthorDTO> getFollowedAuthors(UUID userId) {
        log.info("Listing followed authors for user ID: {}", userId);
        List<AuthorEntity> authors = authorRepository.findFollowedAuthorsByUserId(userId);

        return authors.stream()
            .map(a -> {
                AuthorDTOs.LatestReleaseDTO latest = null;
                if (a.getBookAuthors() != null && !a.getBookAuthors().isEmpty()) {
                    var latestBook = a.getBookAuthors().get(0).getBook();
                    latest = AuthorDTOs.LatestReleaseDTO.builder()
                        .bookId(latestBook.getId())
                        .title(latestBook.getTitle())
                        .publishedDate(latestBook.getPublishedDate())
                        .build();
                }

                return AuthorDTOs.FollowedAuthorDTO.builder()
                    .authorId(a.getId())
                    .name(a.getName())
                    .slug(a.getSlug())
                    .profileImageUrl(a.getProfileImageUrl())
                    .latestRelease(latest)
                    .build();
            })
            .toList();
    }
}
