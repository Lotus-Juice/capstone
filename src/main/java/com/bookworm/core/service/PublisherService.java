package com.bookworm.core.service;

import com.bookworm.api.v1.dto.PublisherDTOs;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.PublisherMapper;
import com.bookworm.infra.persistence.entity.PublisherEntity;
import com.bookworm.infra.persistence.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Transactional(readOnly = true)
    public Page<PublisherDTOs.PublisherDTO> listPublishers(Pageable pageable) {
        log.info("Listing publishers with pagination");
        return publisherRepository.findAll(pageable).map(publisherMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PublisherDTOs.PublisherDTO getPublisherDetails(String publisherIdOrSlug) {
        log.info("Fetching publisher details for: {}", publisherIdOrSlug);

        PublisherEntity publisher;
        try {
            UUID id = UUID.fromString(publisherIdOrSlug);
            publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", id));
        } catch (IllegalArgumentException e) {
            publisher = publisherRepository.findBySlug(publisherIdOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", publisherIdOrSlug));
        }

        return publisherMapper.toDTO(publisher);
    }
}
