package com.reksoft.quarkoservice.service;

import com.reksoft.quarkoservice.dto.InputMessage;
import com.reksoft.quarkoservice.dto.OutputMessage;
import com.reksoft.quarkoservice.mapper.InputMessageMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class InputMessageService {

    private final InputMessageMapper mapper;

    public Uni<OutputMessage> processMessage(InputMessage message) {
        return Uni.createFrom().item(mapper.toOutputMessage(message))
                .map(outputMessage -> {
                    outputMessage.setMessage("message: %s".formatted(outputMessage.getMessage()));
                    return outputMessage;
                });
    }
}
