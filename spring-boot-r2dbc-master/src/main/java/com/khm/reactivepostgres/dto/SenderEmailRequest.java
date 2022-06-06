package com.khm.reactivepostgres.dto;

import lombok.Data;

@Data
public class SenderEmailRequest {
    private String sender;
    private String text;
}
