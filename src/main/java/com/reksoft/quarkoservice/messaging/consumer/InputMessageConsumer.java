package com.reksoft.quarkoservice.messaging.consumer;

import com.reksoft.quarkoservice.dto.InputMessage;
import com.reksoft.quarkoservice.dto.OutputMessage;
import com.reksoft.quarkoservice.service.InputMessageService;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

@ApplicationScoped
public class InputMessageConsumer {

    private final InputMessageService service;
    private final Emitter<OutputMessage> emitter;

    @Inject
    public InputMessageConsumer(InputMessageService service,
                                @Channel("output-exchange") Emitter<OutputMessage> emitter) {
        this.service = service;
        this.emitter = emitter;
    }

    @Incoming("input-queue")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public Uni<Void> consume(Message<InputMessage> message) {
        return service.processMessage(message.getPayload())
                .invoke(outputMessagePayload ->
                        emitter.send(Message.of(outputMessagePayload)
                                .withMetadata(Metadata.of(new OutgoingRabbitMQMetadata.Builder()
                                        .withHeader("custom-header", "some-value")
                                        .build())))
                )
                .replaceWithVoid();
    }
}
