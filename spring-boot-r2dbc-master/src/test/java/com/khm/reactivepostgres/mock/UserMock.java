package com.khm.reactivepostgres.mock;

import com.khm.reactivepostgres.entity.User;

public class UserMock {

    public static User user1(){
        return User.builder()
                .id(1)
                .email("andy@example.com")
                .build();
    }

    public static User user2(){
        return User.builder()
                .id(2)
                .email("john@example.com")
                .build();
    }
}
