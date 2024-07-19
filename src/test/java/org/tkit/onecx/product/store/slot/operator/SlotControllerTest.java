package org.tkit.onecx.product.store.slot.operator;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SlotControllerTest extends AbstractTest {

    static final Logger log = LoggerFactory.getLogger(SlotControllerTest.class);

    @Inject
    Operator operator;

    @Inject
    KubernetesClient client;

    @BeforeAll
    public static void init() {
        Awaitility.setDefaultPollDelay(2, SECONDS);
        Awaitility.setDefaultPollInterval(2, SECONDS);
        Awaitility.setDefaultTimeout(10, SECONDS);
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void SlotTest(String name, SlotSpec spec, SlotStatus.Status status) {

        operator.start();

        Slot slot = new Slot();
        slot.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(client.getNamespace()).build());
        slot.setSpec(spec);

        client.resource(slot).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            SlotStatus mfeStatus = client.resource(slot).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(status);
        });
    }

    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of("test-1", createSpec("test-1", "product-test", "test1"), SlotStatus.Status.CREATED),
                Arguments.of("test-2", createSpec("test-2", "product-test-2", "test2"), SlotStatus.Status.CREATED),
                Arguments.of("test-3", createSpec("test-3", "product-test-2", "test3"), SlotStatus.Status.UPDATED),
                Arguments.of("test-error-1", createSpec("test-error-1", "product-test-2", "test2"),
                        SlotStatus.Status.ERROR),
                Arguments.of("test-error-2", createSpec("test-error-2", "product-test-2", "test2"),
                        SlotStatus.Status.ERROR));
    }

    private static SlotSpec createSpec(String appId, String productName, String name) {
        SlotSpec spec = new SlotSpec();
        spec.setAppId(appId);
        spec.setProductName(productName);
        spec.setName(name);
        spec.setDescription("description");
        return spec;
    }

    @Test
    void SlotEmptySpecTest() {

        operator.start();

        Slot slot = new Slot();
        slot.setMetadata(new ObjectMetaBuilder().withName("empty-spec").withNamespace(client.getNamespace()).build());
        slot.setSpec(new SlotSpec());

        client.resource(slot).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            SlotStatus mfeStatus = client.resource(slot).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(SlotStatus.Status.ERROR);
        });
    }

    @Test
    void SlotNullSpecTest() {

        operator.start();

        Slot slot = new Slot();
        slot.setMetadata(new ObjectMetaBuilder().withName("null-spec").withNamespace(client.getNamespace()).build());
        slot.setSpec(null);

        client.resource(slot).serverSideApply();

        await().pollDelay(4, SECONDS).untilAsserted(() -> {
            SlotStatus mfeStatus = client.resource(slot).get().getStatus();
            assertThat(mfeStatus).isNull();
        });

    }

    @Test
    void SlotUpdateEmptySpecTest() {

        operator.start();

        var m = new SlotSpec();
        m.setAppId("test-1");
        m.setProductName("product-test");

        Slot slot = new Slot();
        slot
                .setMetadata(new ObjectMetaBuilder().withName("to-update-spec").withNamespace(client.getNamespace()).build());
        slot.setSpec(m);

        client.resource(slot).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            SlotStatus mfeStatus = client.resource(slot).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(SlotStatus.Status.CREATED);
        });

        client.resource(slot).inNamespace(client.getNamespace())
                .edit(s -> {
                    s.setSpec(null);
                    return s;
                });

        await().pollDelay(4, SECONDS).untilAsserted(() -> {
            SlotStatus mfeStatus = client.resource(slot).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(SlotStatus.Status.CREATED);
        });
    }
}
