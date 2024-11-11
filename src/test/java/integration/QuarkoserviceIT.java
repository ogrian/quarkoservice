package integration;

import com.reksoft.quarkoservice.dto.Customer;
import com.reksoft.quarkoservice.dto.InputMessage;
import com.reksoft.quarkoservice.dto.OutputMessage;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;
import java.util.Optional;

@QuarkusTest
// Аналог @DirtiesContext. При наличии одного теста эта аннотация не нужна.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuarkoserviceIT {

    private static final int OPERATION_TIMEOUT = 10;
    private static final String TEST_FIRST_NAME = "testFirstName";
    private static final String TEST_LAST_NAME = "testLastName";
    private static final String TEST_EMAIL = "test@test.com";

    private final Emitter<InputMessage> inputQueueEmitter;
    private final Multi<Message<JsonObject>> outputExchange;

    public QuarkoserviceIT(@Channel("input-queue-test") Emitter<InputMessage> inputQueueEmitter,
                           @Channel("output-exchange-test") Multi<Message<JsonObject>> outputExchange) {
        this.inputQueueEmitter = inputQueueEmitter;
        this.outputExchange = outputExchange;
    }

    @Test
    void shouldSendOutputMessage_whenInputMessageIsConsumed() {
        // Подписку нужно сделать раньше отправки сообщения
        AssertSubscriber<Message<JsonObject>> subscriber = outputExchange.subscribe()
                .withSubscriber(AssertSubscriber.create(1));

        // Отправка сообщения
        InputMessage inputMessage = InputMessage.builder()
                .customer(Customer.builder()
                        .email(TEST_EMAIL)
                        .firstName(TEST_FIRST_NAME)
                        .lastName(TEST_LAST_NAME)
                        .build())
                .build();

        inputQueueEmitter.send(Message.of(inputMessage));

        // Получение результата обработки сообщения
        Message<JsonObject> responseMessage = subscriber.awaitItems(1,
                Duration.ofSeconds(OPERATION_TIMEOUT)).getItems().get(0);

        // Отмена подписки: в случае наличия нескольких тестов это поможет избежать "интересных" побочных эффектов
        subscriber.cancel();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertNotNull(responseMessage.getMetadata());

        Optional<IncomingRabbitMQMetadata> metadataOptional =
                responseMessage.getMetadata().get(IncomingRabbitMQMetadata.class);
        Assertions.assertTrue(metadataOptional.isPresent());

        String headerValue = (String) metadataOptional.get().getHeaders().get("custom-header");
        Assertions.assertEquals("some-value", headerValue);

        OutputMessage result = responseMessage.getPayload().mapTo(OutputMessage.class);

        Assertions.assertEquals("message: " + TEST_LAST_NAME, result.getMessage());
    }
}
