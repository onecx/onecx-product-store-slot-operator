package org.tkit.onecx.product.store.slot.operator;

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.product.store.slot.operator.client.ProductStoreService;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnAddFilter;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;

@ControllerConfiguration(name = "slot", namespaces = WATCH_CURRENT_NAMESPACE, onAddFilter = SlotController.SlotAddFilter.class, onUpdateFilter = SlotController.SlotUpdateFilter.class)
public class SlotController implements Reconciler<Slot>, ErrorStatusHandler<Slot> {

    private static final Logger log = LoggerFactory.getLogger(SlotController.class);

    @Inject
    ProductStoreService service;

    @Override
    public UpdateControl<Slot> reconcile(Slot microservice, Context<Slot> context)
            throws Exception {

        String appId = microservice.getSpec().getAppId();
        String productName = microservice.getSpec().getProductName();
        String name = microservice.getSpec().getName();

        log.info("Reconcile microservice: {} for product: {}, name: {}", appId, productName, name);
        int responseCode = service.updateMicroservice(microservice);

        updateStatusPojo(microservice, responseCode);
        log.info("Microservice '{}' reconciled - updating status", microservice.getMetadata().getName());
        return UpdateControl.updateStatus(microservice);

    }

    @Override
    public ErrorStatusUpdateControl<Slot> updateErrorStatus(Slot slot,
            Context<Slot> context, Exception e) {

        int responseCode = -1;
        if (e.getCause() instanceof WebApplicationException re) {
            responseCode = re.getResponse().getStatus();
        }

        log.error("Error reconcile resource", e);
        var status = new SlotStatus();
        status.setRequestProductName(null);
        status.setRequestAppId(null);
        status.setRequestName(null);
        status.setResponseCode(responseCode);
        status.setStatus(SlotStatus.Status.ERROR);
        status.setMessage(e.getMessage());
        slot.setStatus(status);
        return ErrorStatusUpdateControl.updateStatus(slot);
    }

    private void updateStatusPojo(Slot slot, int responseCode) {
        var result = new SlotStatus();
        var spec = slot.getSpec();
        result.setRequestProductName(spec.getProductName());
        result.setRequestAppId(spec.getAppId());
        result.setRequestName(spec.getName());
        result.setResponseCode(responseCode);
        var status = switch (responseCode) {
            case 201:
                yield SlotStatus.Status.CREATED;
            case 200:
                yield SlotStatus.Status.UPDATED;
            default:
                yield SlotStatus.Status.UNDEFINED;
        };
        result.setStatus(status);
        slot.setStatus(result);
    }

    public static class SlotAddFilter implements OnAddFilter<Slot> {

        @Override
        public boolean accept(Slot resource) {
            return resource.getSpec() != null;
        }
    }

    public static class SlotUpdateFilter implements OnUpdateFilter<Slot> {

        @Override
        public boolean accept(Slot newResource, Slot oldResource) {
            return newResource.getSpec() != null;
        }
    }
}
