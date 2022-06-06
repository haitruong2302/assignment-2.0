package com.khm.reactivepostgres.service;

import com.khm.reactivepostgres.dto.*;
import com.khm.reactivepostgres.entity.*;
import com.khm.reactivepostgres.repository.FriendConnectionRepository;
import com.khm.reactivepostgres.repository.UserRepository;
import com.khm.reactivepostgres.util.ValidationUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class FriendConnectionServiceImpl implements FriendConnectionService {

    @Value("${user.notfound}")
    private String userNotFound;

    @Value("${user.invalid}")
    private String userInvalid;

    @Value("${user.duplicate}")
    private String userDuplicate;

    @Value("${friend-connection.existed}")
    private String friendConnectionExisted;

    @Value("${friend-connection.existed-or-blocked}")
    private String friendConnectionExistedOrBlocked;

    @Value("${requestor.invalid}")
    private String requestorInvalid;

    @Value("${email.subscribed}")
    private String emailSubscribed;

    @Value("${user.block}")
    private String userBlock;

    private final FriendConnectionRepository friendConnectionRepository;

    private final UserRepository userRepository;

    @Transactional
    @Override
    public Mono<List<FriendConnection>> createFriendConnection(CreateUserRequest request) {
        return userRepository.findAllByEmailIn(request.getFriends())
                .collectList()
                .filter(items -> !request.getFriends().get(0).equals(request.getFriends().get(1)))
                .switchIfEmpty(Mono.error(new RuntimeException(userDuplicate)))
                .filter(items -> items.size() == 2)
                .switchIfEmpty(Mono.error(new RuntimeException(userInvalid)))
                .map(items -> {
                    Map<String, Integer> ids = getIds(request.getFriends().get(0), items);
                    Integer userId = ids.get("userId");
                    Integer friendId = ids.get("friendId");
                    return friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(userId, friendId, StatusEnum.ACTIVE).switchIfEmpty(Flux.just(FriendConnection.builder().build()))
                            .collectList()
                            .filter(data -> data.stream().filter(item -> Objects.nonNull(item.getId())
                                    && ((FriendStatusEnum.FRIEND.equals(item.getFriendStatus()))
                                    || (FriendStatusEnum.UNFRIEND.equals(item.getFriendStatus())
                                    && ActionStatusEnum.BLOCK.equals(item.getActionStatus())))).count() != 1)
                            .switchIfEmpty(Mono.error(new RuntimeException(friendConnectionExistedOrBlocked)))
                            .map(data -> {
                                String requestorEmail = request.getFriends().get(0);
                                List<FriendConnection> friendConnections = new ArrayList<>();
                                data.forEach(item -> {
                                    if (Objects.nonNull(item.getId())) {
                                        item.setStatus(StatusEnum.INACTIVE);
                                        item.setUpdatedBy(requestorEmail);
                                        item.setUpdatedDate(LocalDateTime.now());
                                        friendConnections.add(item);
                                    }
                                });

                                friendConnections.add(FriendConnection.builder()
                                        .userId(userId)
                                        .friendId(friendId)
                                        .friendStatus(FriendStatusEnum.FRIEND)
                                        .actionStatus(ActionStatusEnum.RECEIVED_UPDATES)
                                        .status(StatusEnum.ACTIVE)
                                        .createdBy(requestorEmail)
                                        .createdDate(LocalDateTime.now())
                                        .build());

                                return friendConnections;
                            });
                }).flatMap(item -> item.flatMap(item1 -> friendConnectionRepository.saveAll(item1).collectList()));
    }

    @Override
    public Flux<UserResponse> getFriendByEmail(String email) {
        return userRepository.findByEmail(email).filter(Objects::nonNull).map(User::getId).flatMapMany(item ->
                        userRepository.findAllByUserId(item, StatusEnum.ACTIVE, FriendStatusEnum.FRIEND).collectList()
                                .map(itemDTO -> new UserResponse(itemDTO.stream().map(object -> Objects.toString(object.getEmail(), null))
                                        .collect(Collectors.toList()), itemDTO.size())))
                .switchIfEmpty(Mono.error(new RuntimeException(userNotFound)));
    }

    @Override
    public Flux<UserResponse> getCommonFriendByEmails(List<String> request) {
        return userRepository.findAllByEmailIn(request)
                .collectList()
                .filter(items -> !request.get(0).equals(request.get(1)))
                .switchIfEmpty(Mono.error(new RuntimeException(userDuplicate)))
                .filter(items -> items.size() == 2)
                .switchIfEmpty(Mono.error(new RuntimeException(userInvalid)))
                .flatMapMany(items -> userRepository.findCommonFriendByEmails(items.get(0).getId(), items.get(1).getId(), StatusEnum.ACTIVE, ActionStatusEnum.BLOCK).collectList()
                        .map(itemDTO -> new UserResponse(itemDTO.stream().map(object -> Objects.toString(object.getEmail(), null)).collect(Collectors.toList()), itemDTO.size())));
    }

    //TODO transactional doesn't work
    @Transactional
    @Override
    public Mono<List<FriendConnection>> subscribeFriendUpdatesByEmails(FriendUpdatesRequest request) {
        return userRepository.findAllByEmailIn(Arrays.asList(request.getRequestor(), request.getTarget()))
                .collectList()
                .filter(items -> !request.getRequestor().equals(request.getTarget()))
                .switchIfEmpty(Mono.error(new RuntimeException(userDuplicate)))
                .filter(items -> items.size() == 2)
                .switchIfEmpty(Mono.error(new RuntimeException(userInvalid)))
                .map(items -> {
                    Map<String, Integer> ids = getIds(request.getRequestor(), items);
                    Integer userId = ids.get("userId");
                    Integer friendId = ids.get("friendId");
                    return friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(userId, friendId, StatusEnum.ACTIVE).switchIfEmpty(Flux.just(FriendConnection.builder().build())).collectList()
                            .map(item -> {
                                List<FriendConnection> friendConnections = new ArrayList<>();
                                AtomicReference<FriendStatusEnum> friendStatusEnum = new AtomicReference<>(FriendStatusEnum.UNFRIEND);
                                String requestorEmail = request.getRequestor();
                                item.forEach(data -> {
                                    if (Objects.nonNull(data.getId()) && StatusEnum.ACTIVE.equals(data.getStatus())) {
                                        boolean allowActiveStatus = false;
                                        switch (data.getActionStatus()) {
                                            case RECEIVED_UPDATES:
                                                if (FriendStatusEnum.FRIEND.equals(data.getFriendStatus())) {
                                                    throw new RuntimeException(friendConnectionExisted);
                                                } else if (FriendStatusEnum.UNFRIEND.equals(data.getFriendStatus())) {
                                                    if (data.getUserId().equals(userId) && data.getFriendId().equals(friendId))
                                                        throw new RuntimeException(String.format(emailSubscribed, request.getRequestor()));
                                                    else
                                                        allowActiveStatus = true;
                                                }
                                                break;
                                            case BLOCK:
                                                if (data.getUserId().equals(friendId) && data.getFriendId().equals(userId))
                                                    throw new RuntimeException(requestorInvalid);
                                                break;
                                            default:
                                                break;
                                        }
                                        friendStatusEnum.set((data.getFriendStatus()));
                                        data.setStatus(allowActiveStatus ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);
                                        data.setUpdatedBy(requestorEmail);
                                        data.setUpdatedDate(LocalDateTime.now());
                                        friendConnections.add(data);
                                    }
                                });

                                friendConnections.add(FriendConnection.builder()
                                        .userId(userId)
                                        .friendId(friendId)
                                        .actionStatus(ActionStatusEnum.RECEIVED_UPDATES)
                                        .status(StatusEnum.ACTIVE)
                                        .friendStatus(friendStatusEnum.get())
                                        .createdBy(requestorEmail)
                                        .createdDate(LocalDateTime.now())
                                        .build());

                                return friendConnections;
                            })
                            .switchIfEmpty(Mono.error(new RuntimeException(friendConnectionExisted)));
                }).flatMap(items -> items.flatMap(item -> friendConnectionRepository.saveAll(item).collectList()));
    }

    @Transactional
    @Override
    public Mono<List<FriendConnection>> blockFriendUpdatesByEmails(FriendUpdatesRequest request) {
        return userRepository.findAllByEmailIn(Arrays.asList(request.getRequestor(), request.getTarget()))
                .collectList()
                .filter(items -> !request.getRequestor().equals(request.getTarget()))
                .switchIfEmpty(Mono.error(new RuntimeException(userDuplicate)))
                .filter(items -> items.size() == 2)
                .switchIfEmpty(Mono.error(new RuntimeException(userInvalid)))
                .map(items -> {
                    Map<String, Integer> ids = getIds(request.getRequestor(), items);
                    Integer userId = ids.get("userId");
                    Integer friendId = ids.get("friendId");
                    return friendConnectionRepository
                            .findAllByUserIdAndFriendIdAndStatus(userId, friendId, StatusEnum.ACTIVE)
                            .switchIfEmpty(Flux.just(FriendConnection.builder().build()))
                            .collectList()
                            .filter(data -> data.stream().filter(item -> Objects.nonNull(item.getId())
                                    && item.getActionStatus().equals(ActionStatusEnum.BLOCK)
                                    && item.getStatus().equals(StatusEnum.ACTIVE)).count() != 1)
                            .switchIfEmpty(Mono.error(new RuntimeException(userBlock)))
                            .map(item -> {
                                List<FriendConnection> friendConnections = new ArrayList<>();
                                AtomicReference<FriendStatusEnum> friendStatusEnum = new AtomicReference<>(FriendStatusEnum.UNFRIEND);
                                String requestorEmail = request.getRequestor();
                                item.forEach(item1 -> {
                                    if (Objects.nonNull(item1.getId()) && StatusEnum.ACTIVE.equals(item1.getStatus())) {
                                        friendStatusEnum.set(item1.getFriendStatus());
                                    }
                                    if (Objects.nonNull(item1.getId())) {
                                        item1.setStatus(StatusEnum.INACTIVE);
                                        item1.setUpdatedBy(requestorEmail);
                                        item1.setUpdatedDate(LocalDateTime.now());
                                        friendConnections.add(item1);
                                    }
                                });
                                friendConnections.add(FriendConnection.builder()
                                        .userId(userId)
                                        .friendId(friendId)
                                        .actionStatus(ActionStatusEnum.BLOCK)
                                        .status(StatusEnum.ACTIVE)
                                        .friendStatus(friendStatusEnum.get())
                                        .createdBy(requestorEmail)
                                        .createdDate(LocalDateTime.now())
                                        .build());
                                return friendConnections;
                            });
                }).flatMap(items ->
                        items.flatMap(item -> friendConnectionRepository.saveAll(item).collectList()));
    }

    public Flux<RecipientResponse> retrieveEmailsBySenderEmail(SenderEmailRequest request) {
        return userRepository.findByEmail(request.getSender())
                .filter(item -> Objects.nonNull(item.getId()))
                .switchIfEmpty(Mono.error(new RuntimeException(userInvalid)))
                .flatMapMany(item -> userRepository.findAllByUserIdAndStatus(item.getId(), StatusEnum.ACTIVE).collectList()
                        .map(itemDTO -> {
                            List<String> recipients = itemDTO.stream().map(object -> Objects.toString(object.getEmail(), null))
                                    .collect(Collectors.toList());
                            List<String> mentionedEmails = ValidationUtility.convertEmails(request.getText());
                            if (mentionedEmails.size() > 0) {
                                recipients.addAll(mentionedEmails);
                            }
                            return new RecipientResponse(recipients);
                        }));
    }

    public Map<String, Integer> getIds(String requestor, List<User> users) {
        Map<String, Integer> ids = new HashMap<>();
        Integer firstId = users.get(0).getId();
        Integer secondId = users.get(1).getId();
        Integer userId = requestor.equals(users.get(0).getEmail()) ? firstId : secondId;
        ids.put("userId", userId);
        ids.put("friendId", userId.equals(firstId) ? secondId : firstId);
        return ids;
    }
}
