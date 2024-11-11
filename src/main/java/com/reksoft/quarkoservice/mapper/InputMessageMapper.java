package com.reksoft.quarkoservice.mapper;

import com.reksoft.quarkoservice.dto.InputMessage;
import com.reksoft.quarkoservice.dto.OutputMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi")
public interface InputMessageMapper {

    @Mapping(source = "customer.lastName", target = "message")
    OutputMessage toOutputMessage(InputMessage inputMessage);
}
