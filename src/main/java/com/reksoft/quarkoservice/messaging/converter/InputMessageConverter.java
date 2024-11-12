package com.reksoft.quarkoservice.messaging.converter;

import com.reksoft.quarkoservice.dto.InputMessage;
import io.smallrye.reactive.messaging.MessageConverter;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.lang.reflect.Type;
import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
public class InputMessageConverter implements MessageConverter {

    @Override
    public boolean canConvert(Message<?> message, Type type) {
        if (!type.equals(InputMessage.class)) {
            return false;
        }
        if (hasApplicationJsonHeader(message)) {
            return true;
        }
        return message.getPayload().getClass().equals(byte[].class);
    }

    @Override
    public Message<?> convert(Message<?> message, Type type) {
        try {
            if (hasApplicationJsonHeader(message)) {
                return message.withPayload(((JsonObject) message.getPayload()).mapTo(InputMessage.class));
            }
            String jsonString = new String((byte[]) message.getPayload());
            return message.withPayload(new JsonObject(jsonString).mapTo(InputMessage.class));
        } catch (DecodeException | ClassCastException | NullPointerException exc) {
            return message;
        }
    }

    private boolean hasApplicationJsonHeader(Message<?> message) {
        Optional<IncomingRabbitMQMetadata> metadata = message.getMetadata(IncomingRabbitMQMetadata.class);
        if (metadata.isEmpty()) {
            return false;
        }
        Optional<String> contentTypeOptional = metadata.get().getContentType();
        return contentTypeOptional.isPresent()
                && contentTypeOptional.get().equals(APPLICATION_JSON);
    }
}
