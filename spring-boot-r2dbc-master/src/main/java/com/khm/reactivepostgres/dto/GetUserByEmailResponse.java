package com.khm.reactivepostgres.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetUserByEmailResponse {
    private List<String> friends;
    private Integer count;
}
