package org.tkit.onecx.product.store.slot.operator.client.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.product.store.slot.operator.SlotSpec;

import gen.org.tkit.onecx.product.store.slot.v1.model.UpdateSlotRequest;

@Mapper
public interface ProductStoreMapper {

    @Mapping(target = "undeployed", constant = "false")
    UpdateSlotRequest map(SlotSpec spec);

}
