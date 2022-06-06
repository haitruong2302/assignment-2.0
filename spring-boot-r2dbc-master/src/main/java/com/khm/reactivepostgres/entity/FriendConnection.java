package com.khm.reactivepostgres.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class FriendConnection {

    @Id
    private Integer id;

    private Integer userId;

    private Integer friendId;

    private FriendStatusEnum friendStatus;

    private ActionStatusEnum actionStatus;

    private StatusEnum status;

    private String createdBy;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;

    private String updatedBy;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedDate;

}
