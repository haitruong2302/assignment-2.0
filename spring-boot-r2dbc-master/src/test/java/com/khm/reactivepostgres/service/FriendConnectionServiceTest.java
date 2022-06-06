package com.khm.reactivepostgres.service;

import com.khm.reactivepostgres.dto.*;
import com.khm.reactivepostgres.entity.*;
import com.khm.reactivepostgres.mock.FriendConnectionMock;
import com.khm.reactivepostgres.mock.UserMock;
import com.khm.reactivepostgres.repository.FriendConnectionRepository;
import com.khm.reactivepostgres.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.*;
import reactor.test.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
public class FriendConnectionServiceTest {

    @InjectMocks
    @Spy
    private FriendConnectionServiceImpl friendConnectionServiceImpl;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendConnectionRepository friendConnectionRepository;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "userDuplicate", "User is duplicate");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "userInvalid", "User is invalid");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "userBlock", "The block status is active");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "userNotFound", "User not found");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "friendConnectionExisted", "Friend Connection existed");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "friendConnectionExistedOrBlocked", "Friend Connection existed or blocked");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "requestorInvalid", "Please input requestor valid to unblock");
        ReflectionTestUtils.setField(friendConnectionServiceImpl, "emailSubscribed", "The %s email was subscribed to receive information");
    }

    @Test
    public void shouldRetrieveEmailsBySenderEmailNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Mono.empty());
        SenderEmailRequest senderEmailRequest = new SenderEmailRequest();
        StepVerifier
                .create(friendConnectionServiceImpl.retrieveEmailsBySenderEmail(senderEmailRequest))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userInvalid")))
                .verify();
    }

    @Test
    public void shouldRetrieveEmailsBySenderEmailNotFoundMentionedEmails() {
        when(userRepository.findByEmail(any())).thenReturn(Mono.just(UserMock.user1()));
        when(userRepository.findAllByUserIdAndStatus(any(), eq(StatusEnum.ACTIVE))).thenReturn(Flux.just(UserMock.user1()));
        SenderEmailRequest senderEmailRequest = new SenderEmailRequest();
        senderEmailRequest.setText("1");
        RecipientResponse recipientResponse = new RecipientResponse();
        recipientResponse.setRecipients(Arrays.asList(UserMock.user1().getEmail()));
        StepVerifier
                .create(friendConnectionServiceImpl.retrieveEmailsBySenderEmail(senderEmailRequest))
                .expectSubscription()
                .expectNext(recipientResponse)
                .verifyComplete();
    }

    @Test
    public void shouldRetrieveEmailsBySenderEmailHasMentionedEmails() {
        String text = "Hello World! aaa@example.com.vn/bbb@example.com.vn ccc@email.com";
        when(userRepository.findByEmail(any())).thenReturn(Mono.just(UserMock.user1()));
        when(userRepository.findAllByUserIdAndStatus(any(), eq(StatusEnum.ACTIVE))).thenReturn(Flux.just(UserMock.user1()));
        SenderEmailRequest senderEmailRequest = new SenderEmailRequest();
        senderEmailRequest.setText(text);
        RecipientResponse recipientResponse = new RecipientResponse();
        recipientResponse.setRecipients(Arrays.asList(UserMock.user1().getEmail(), "aaa@example.com.vn", "bbb@example.com.vn", "ccc@email.com"));
        StepVerifier
                .create(friendConnectionServiceImpl.retrieveEmailsBySenderEmail(senderEmailRequest))
                .expectSubscription()
                .expectNext(recipientResponse)
                .verifyComplete();
    }

    @Test
    public void shouldBlockFriendUpdatesByEmailsDuplicate() {
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1(), UserMock.user1()));
        FriendUpdatesRequest request = FriendUpdatesRequest.builder().requestor(UserMock.user1().getEmail()).target(UserMock.user1().getEmail()).build();
        StepVerifier
                .create(friendConnectionServiceImpl.blockFriendUpdatesByEmails(request))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userDuplicate")))
                .verify();
    }

    @Test
    public void shouldBlockFriendUpdatesByEmailsInvalid() {
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1()));
        FriendUpdatesRequest request = FriendUpdatesRequest.builder().requestor(UserMock.user1().getEmail()).target(UserMock.user2().getEmail()).build();
        StepVerifier
                .create(friendConnectionServiceImpl.blockFriendUpdatesByEmails(request))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userInvalid")))
                .verify();
    }

    @Test
    public void shouldBlockFriendUpdatesByEmailsBlock() {
        FriendConnection friendConnection = FriendConnectionMock.friendConnection1();
        friendConnection.setActionStatus(ActionStatusEnum.BLOCK);
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(any(), any(), eq(StatusEnum.ACTIVE)))
                .thenReturn(Flux.just(friendConnection));
        FriendUpdatesRequest request = FriendUpdatesRequest.builder().requestor(UserMock.user1().getEmail()).target(UserMock.user2().getEmail()).build();
        StepVerifier
                .create(friendConnectionServiceImpl.blockFriendUpdatesByEmails(request))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userBlock")))
                .verify();
    }

    @Test
    public void shouldBlockFriendUpdatesByEmailsCreateNewTheFriendConnection() {
        FriendConnection friendConnection = FriendConnection.builder().build();
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(any(), any(), eq(StatusEnum.ACTIVE)))
                .thenReturn(Flux.just(FriendConnection.builder().build()));
        FriendUpdatesRequest request = FriendUpdatesRequest.builder().requestor(UserMock.user1().getEmail()).target(UserMock.user2().getEmail()).build();
        when(friendConnectionRepository.saveAll(anyList())).thenReturn(Flux.just(friendConnection));
        StepVerifier
                .create(friendConnectionServiceImpl.blockFriendUpdatesByEmails(request))
                .expectSubscription()
//                .consumeNextWith(response-> {
////                    response.//TODO result
//                })
                .expectNext(Arrays.asList(FriendConnection.builder().build()))
                .verifyComplete();
    }

    @Test
    public void shouldGetCommonFriendByEmailsDuplicateEmails() {
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        StepVerifier
                .create(friendConnectionServiceImpl.getCommonFriendByEmails(Arrays.asList("a@xample.com", "a@xample.com")))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userDuplicate")))
                .verify();
    }

    @Test
    public void shouldGetCommonFriendByEmailsInvalidEmails() {
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1()));
        StepVerifier
                .create(friendConnectionServiceImpl.getCommonFriendByEmails(Arrays.asList("a@xample.com", "b@xample.com")))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userInvalid")))
                .verify();
    }

    @Test
    public void shouldGetCommonFriendByEmailsSuccessfully() {
        when(userRepository.findAllByEmailIn(any())).thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(userRepository.findCommonFriendByEmails(any(), any(), eq(StatusEnum.ACTIVE), eq(ActionStatusEnum.BLOCK)))
                .thenReturn(Flux.just(UserMock.user1()));
        List<String> list = Arrays.asList("a@xample.com", "b@xample.com");
        StepVerifier
                .create(friendConnectionServiceImpl.getCommonFriendByEmails(list))
                .expectSubscription()
                .expectNext(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1))
                .verifyComplete();
    }

    @Test
    public void shouldGetFriendByEmailNotFound() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Mono.empty());
        StepVerifier
                .create(friendConnectionServiceImpl.getFriendByEmail("a@xample.com"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userNotFound")))
                .verify();
    }

    @Test
    public void shouldGetFriendByEmailSuccessfully() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Mono.just(UserMock.user1()));
        when(userRepository.findAllByUserId(any(), eq(StatusEnum.ACTIVE), eq(FriendStatusEnum.FRIEND)))
                .thenReturn(Flux.just(UserMock.user1()));
        StepVerifier
                .create(friendConnectionServiceImpl.getFriendByEmail("a@xample.com"))
                .expectSubscription()
                .expectNext(new UserResponse(Arrays.asList(UserMock.user1().getEmail()), 1))
                .verifyComplete();
    }

    @Test
    public void shouldCreateFriendConnectionDuplicateUsers() {
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1()));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "a@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userDuplicate")))
                .verify();
    }

    @Test
    public void shouldCreateFriendConnectionInvalidUsers() {
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1()));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "b@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userInvalid")))
                .verify();
    }

    @Test
    public void shouldCreateFriendConnectionBeFriendStatus() {
        FriendConnection friendConnection = FriendConnectionMock.friendConnection1();
        friendConnection.setFriendStatus(FriendStatusEnum.FRIEND);
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(any(), any(), eq(StatusEnum.ACTIVE)))
                .thenReturn(Flux.just(friendConnection));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "b@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "friendConnectionExistedOrBlocked")))
                .verify();
    }

    @Test
    public void shouldCreateFriendConnectionSuccessfully() {
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(any(), any(), eq(StatusEnum.ACTIVE)))
                .thenReturn(Flux.just(FriendConnectionMock.friendConnection1(), FriendConnectionMock.friendConnection2()));
        when(friendConnectionRepository.saveAll(anyList()))
                .thenReturn(Flux.just(FriendConnectionMock.friendConnection2()));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "b@xample.com")).build()))
                .expectSubscription()
                .consumeNextWith(response -> {
                    Assertions.assertTrue(response.size() > 0);
                })
                .verifyComplete();
    }

    @Test
    public void shouldCreateFriendConnectionBeBlockStatus() {
        FriendConnection friendConnection = FriendConnectionMock.friendConnection1();
        friendConnection.setFriendStatus(FriendStatusEnum.UNFRIEND);
        friendConnection.setActionStatus(ActionStatusEnum.BLOCK);
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        when(friendConnectionRepository.findAllByUserIdAndFriendIdAndStatus(any(), any(), eq(StatusEnum.ACTIVE)))
                .thenReturn(Flux.just(friendConnection));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "b@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "friendConnectionExistedOrBlocked")))
                .verify();
    }

    @Test
    public void shouldSubscribeFriendUpdatesByEmailsDuplicateEmails() {
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1(), UserMock.user2()));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "a@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userDuplicate")))
                .verify();
    }

    @Test
    public void shouldSubscribeFriendUpdatesByEmailsInvalidEmails() {
        when(userRepository.findAllByEmailIn(any()))
                .thenReturn(Flux.just(UserMock.user1()));
        StepVerifier
                .create(friendConnectionServiceImpl.createFriendConnection(CreateUserRequest.builder().friends(Arrays.asList("a@xample.com", "b@xample.com")).build()))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals(ReflectionTestUtils.getField(friendConnectionServiceImpl, "userInvalid")))
                .verify();
    }

    @Test
    public void shouldGetIdsSuccessfullyForUserId() {
        Map<String, Integer> getIds =
                friendConnectionServiceImpl.getIds("andy@example.com", Arrays.asList(UserMock.user1(), UserMock.user2()));
        Assertions.assertEquals(1, getIds.get("userId"));
        Assertions.assertEquals("andy@example.com", UserMock.user1().getEmail());
    }

    @Test
    public void shouldGetIdsSuccessfullyForFriendId() {
        Map<String, Integer> getIds =
                friendConnectionServiceImpl.getIds("john@example.com", Arrays.asList(UserMock.user1(), UserMock.user2()));
        Assertions.assertEquals(1, getIds.get("friendId"));
        Assertions.assertEquals("john@example.com", UserMock.user2().getEmail());
    }
}
