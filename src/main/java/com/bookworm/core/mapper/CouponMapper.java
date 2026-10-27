package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.CouponDTOs;
import com.bookworm.infra.persistence.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {

    CouponDTOs.CouponOfferDTO toOfferDTO(CouponEntity entity);

    List<CouponDTOs.CouponOfferDTO> toOfferDTOList(List<CouponEntity> entities);
}
