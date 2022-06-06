package com.khm.reactivepostgres.mock;

import com.khm.reactivepostgres.entity.FriendConnection;
import com.khm.reactivepostgres.entity.*;
public class FriendConnectionMock {

    public static FriendConnection friendConnection1(){
        return FriendConnection.builder()
                .id(1)
                .userId(1)
                .friendId(2)
                .friendStatus(FriendStatusEnum.UNFRIEND)
                .actionStatus(ActionStatusEnum.RECEIVED_UPDATES)
                .status(StatusEnum.ACTIVE)
                .build();
    }

    public static FriendConnection friendConnection2(){
        return FriendConnection.builder()
                .id(2)
                .userId(2)
                .friendId(1)
                .friendStatus(FriendStatusEnum.UNFRIEND)
                .actionStatus(ActionStatusEnum.RECEIVED_UPDATES)
                .status(StatusEnum.ACTIVE)
                .build();
    }
}
