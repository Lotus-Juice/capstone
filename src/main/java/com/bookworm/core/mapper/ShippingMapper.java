package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.ShippingDTOs;
import com.bookworm.infra.persistence.entity.ShipmentCheckpointEntity;
import com.bookworm.infra.persistence.entity.ShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingMapper {

    @Mapping(target = "checkpoints", source = "checkpoints")
    @Mapping(target = "estimatedDeliveryDate", expression = "java(entity.getOrder() != null ? entity.getOrder().getEstimatedDeliveryDate() : null)")
    ShippingDTOs.ShipmentTrackingResponse toTrackingResponse(ShipmentEntity entity);

    @Mapping(target = "location", source = "locationCity")
    @Mapping(target = "description", source = "activityDescription")
    ShippingDTOs.TrackingCheckpointDTO toCheckpointDTO(ShipmentCheckpointEntity entity);

    List<ShippingDTOs.TrackingCheckpointDTO> toCheckpointDTOList(List<ShipmentCheckpointEntity> entities);
}
