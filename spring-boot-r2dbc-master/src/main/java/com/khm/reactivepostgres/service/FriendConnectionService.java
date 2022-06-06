package com.khm.reactivepostgres.service;

import com.khm.reactivepostgres.dto.*;
import com.khm.reactivepostgres.entity.FriendConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FriendConnectionService {
    Mono<List<FriendConnection>> createFriendConnection(CreateUserRequest request);

    Flux<UserResponse> getFriendByEmail(String email);

    Flux<UserResponse> getCommonFriendByEmails(List<String> request);

    Mono<List<FriendConnection>> subscribeFriendUpdatesByEmails(FriendUpdatesRequest request);

    Mono<List<FriendConnection>> blockFriendUpdatesByEmails(FriendUpdatesRequest request);

    Flux<RecipientResponse> retrieveEmailsBySenderEmail(SenderEmailRequest request);

}
