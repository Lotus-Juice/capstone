package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.AddressDTOs;
import com.bookworm.api.v1.dto.AuthDTOs;
import com.bookworm.infra.persistence.entity.UserAddressEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "active", constant = "true")
    UserEntity toEntity(AuthDTOs.RegisterRequest request);

    AuthDTOs.UserProfileResponse toProfileResponse(UserEntity entity);

    AddressDTOs.AddressDTO toAddressDTO(UserAddressEntity entity);

    List<AddressDTOs.AddressDTO> toAddressDTOList(List<UserAddressEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserAddressEntity toAddressEntity(AddressDTOs.CreateAddressRequest request);
}
