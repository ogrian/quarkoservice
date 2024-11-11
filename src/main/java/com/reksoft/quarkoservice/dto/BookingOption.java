package com.reksoft.quarkoservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingOption {

    private String type;
    private String value;
}
