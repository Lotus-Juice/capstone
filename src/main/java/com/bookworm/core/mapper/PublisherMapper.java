package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.PublisherDTOs;
import com.bookworm.infra.persistence.entity.PublisherEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PublisherMapper {

    @Mapping(target = "booksCount", expression = "java(entity.getBooks() != null ? entity.getBooks().size() : 0)")
    PublisherDTOs.PublisherDTO toDTO(PublisherEntity entity);

    List<PublisherDTOs.PublisherDTO> toDTOList(List<PublisherEntity> entities);
}
