package com.khm.reactivepostgres.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendUpdatesRequest {
    private String requestor;
    private String target;
}
