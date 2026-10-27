package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.AuthorDTOs;
import com.bookworm.infra.persistence.entity.AuthorEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {BookMapper.class})
public interface AuthorMapper {

    @Mapping(target = "booksCount", expression = "java(entity.getBookAuthors() != null ? entity.getBookAuthors().size() : 0)")
    @Mapping(target = "followersCount", expression = "java(entity.getFollowers() != null ? entity.getFollowers().size() : 0)")
    AuthorDTOs.AuthorDTO toDTO(AuthorEntity entity);

    List<AuthorDTOs.AuthorDTO> toDTOList(List<AuthorEntity> entities);

    @Mapping(target = "isFollowed", constant = "false")
    @Mapping(target = "books", ignore = true)
    AuthorDTOs.AuthorDetailResponse toDetailResponse(AuthorEntity entity);
}
