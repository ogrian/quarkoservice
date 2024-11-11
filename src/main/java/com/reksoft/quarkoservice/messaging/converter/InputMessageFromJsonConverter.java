package com.reksoft.quarkoservice.messaging.converter;

import com.reksoft.quarkoservice.dto.InputMessage;
import io.smallrye.reactive.messaging.MessageConverter;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.lang.reflect.Type;

@ApplicationScoped
public class InputMessageFromJsonConverter implements MessageConverter {

    @Override
    public boolean canConvert(Message<?> message, Type type) {
        return message.getPayload().getClass().equals(JsonObject.class)
                && type.equals(InputMessage.class);
    }

    @Override
    public Message<?> convert(Message<?> message, Type type) {
        try {
            return message.withPayload(((JsonObject) message.getPayload()).mapTo(InputMessage.class));
        } catch (Exception exc) {
            return message;
        }
    }
}
