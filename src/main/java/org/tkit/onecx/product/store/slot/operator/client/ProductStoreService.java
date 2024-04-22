package org.tkit.onecx.product.store.slot.operator.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.product.store.slot.operator.Slot;
import org.tkit.onecx.product.store.slot.operator.SlotSpec;
import org.tkit.onecx.product.store.slot.operator.client.mappers.ProductStoreMapper;

import gen.org.tkit.onecx.product.store.slot.v1.api.OperatorSlotApi;
import gen.org.tkit.onecx.product.store.slot.v1.model.UpdateSlotRequest;

@ApplicationScoped
public class ProductStoreService {

    private static final Logger log = LoggerFactory.getLogger(ProductStoreService.class);

    @Inject
    @RestClient
    OperatorSlotApi client;

    @Inject
    ProductStoreMapper mapper;

    public int updateMicroservice(Slot microfrontend) {
        SlotSpec spec = microfrontend.getSpec();
        UpdateSlotRequest dto = mapper.map(spec);
        try (var response = client.createOrUpdateSlot(spec.getProductName(), spec.getAppId(), dto)) {
            log.info("Update micro-fronted response {}", response.getStatus());
            return response.getStatus();
        }
    }
}
