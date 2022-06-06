package com.khm.reactivepostgres.controller;

import com.khm.reactivepostgres.dto.*;
import com.khm.reactivepostgres.entity.FriendConnection;
import com.khm.reactivepostgres.service.FriendConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendConnectionController {

  private final FriendConnectionService friendConnectionService;

  @PostMapping
  public ResponseEntity<Mono<List<FriendConnection>>> createFriendConnection(@RequestBody CreateUserRequest request) {
    return new ResponseEntity<>(friendConnectionService.createFriendConnection(request), HttpStatus.OK);
  }

  @PostMapping("/getFriend")
  public ResponseEntity<Flux<UserResponse>> getFriendByEmail(@RequestBody GetFriendByEmailRequest request) {
    return new ResponseEntity<>(friendConnectionService.getFriendByEmail(request.getEmail()), HttpStatus.OK);
  }

  @PostMapping("/getCommonFriend")
  public ResponseEntity<Flux<UserResponse>> getCommonFriendByEmails(@RequestBody CreateUserRequest request) {
    return new ResponseEntity<>(friendConnectionService.getCommonFriendByEmails(request.getFriends()), HttpStatus.OK);
  }

  @PutMapping("/subscribeUpdates")
  public ResponseEntity<Mono<List<FriendConnection>>> subscribeFriendUpdatesByEmails(@RequestBody FriendUpdatesRequest request) {
    return new ResponseEntity<>(friendConnectionService.subscribeFriendUpdatesByEmails(request), HttpStatus.OK);
  }

  @PutMapping("/block")
  public ResponseEntity<Mono<List<FriendConnection>>> blockFriendUpdatesByEmails(@RequestBody FriendUpdatesRequest request) {
    return new ResponseEntity<>(friendConnectionService.blockFriendUpdatesByEmails(request), HttpStatus.OK);
  }

  @PostMapping("/retrieveEmails")
  public ResponseEntity<Flux<RecipientResponse>> retrieveEmailsBySenderEmail(@RequestBody SenderEmailRequest request) {
    return new ResponseEntity<>(friendConnectionService.retrieveEmailsBySenderEmail(request), HttpStatus.OK);
  }
}

