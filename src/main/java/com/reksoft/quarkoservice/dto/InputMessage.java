package com.reksoft.quarkoservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InputMessage {

    private Customer customer;
    private List<BookingOption> options;
}
