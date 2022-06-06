package com.khm.reactivepostgres.controller;

import com.khm.reactivepostgres.dto.*;
import com.khm.reactivepostgres.mock.FriendConnectionMock;
import com.khm.reactivepostgres.mock.UserMock;
import com.khm.reactivepostgres.service.FriendConnectionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
public class FriendConnectionControllerTest {

    @InjectMocks
    private FriendConnectionController friendConnectionController;

    @Mock
    private FriendConnectionService friendConnectionService;

    @BeforeEach
    public void setUp() {
        BDDMockito.when(friendConnectionService.createFriendConnection(ArgumentMatchers.any()))
                .thenReturn(Mono.just(Collections.singletonList(FriendConnectionMock.friendConnection1())));
        BDDMockito.when(friendConnectionService.getFriendByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Flux.just(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1)));
        BDDMockito.when(friendConnectionService.getCommonFriendByEmails(ArgumentMatchers.any()))
                .thenReturn(Flux.just(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1)));
        BDDMockito.when(friendConnectionService.subscribeFriendUpdatesByEmails(ArgumentMatchers.any()))
                .thenReturn(Mono.just(Arrays.asList(FriendConnectionMock.friendConnection1())));
        BDDMockito.when(friendConnectionService.blockFriendUpdatesByEmails(ArgumentMatchers.any()))
                .thenReturn(Mono.just(Arrays.asList(FriendConnectionMock.friendConnection1())));
        BDDMockito.when(friendConnectionService.retrieveEmailsBySenderEmail(ArgumentMatchers.any()))
                .thenReturn(Flux.just(new RecipientResponse(Arrays.asList(UserMock.user1().getEmail()))));
    }

    @Test
    public void shouldCreateFriendConnection() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.createFriendConnection(CreateUserRequest.builder().build()).getBody()))
                .expectSubscription()
                .expectNext(Arrays.asList(FriendConnectionMock.friendConnection1()))
                .verifyComplete();
    }

    @Test
    public void shouldGetFriendByEmail() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.getFriendByEmail(GetFriendByEmailRequest.builder().email("a@example.com").build()).getBody()))
                .expectSubscription()
                .expectNext(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1))
                .verifyComplete();
    }

    @Test
    public void shouldGetCommonFriendByEmails() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.getCommonFriendByEmails(CreateUserRequest.builder().build()).getBody()))
                .expectSubscription()
                .expectNext(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1))
                .verifyComplete();
    }

    @Test
    public void shouldSubscribeFriendUpdatesByEmails() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.subscribeFriendUpdatesByEmails(FriendUpdatesRequest.builder().build()).getBody()))
                .expectSubscription()
                .expectNext(Arrays.asList(FriendConnectionMock.friendConnection1()))
                .verifyComplete();
    }

    @Test
    public void shouldBlockFriendUpdatesByEmails() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.blockFriendUpdatesByEmails(FriendUpdatesRequest.builder().build()).getBody()))
                .expectSubscription()
                .expectNext(Arrays.asList(FriendConnectionMock.friendConnection1()))
                .verifyComplete();
    }

    @Test
    public void shouldRetrieveEmailsBySenderEmail() {
        StepVerifier.create(Objects.requireNonNull(friendConnectionController.retrieveEmailsBySenderEmail(new SenderEmailRequest()).getBody()))
                .expectSubscription()
                .expectNext(new RecipientResponse(Arrays.asList(UserMock.user1().getEmail())))
                .verifyComplete();
    }
}
