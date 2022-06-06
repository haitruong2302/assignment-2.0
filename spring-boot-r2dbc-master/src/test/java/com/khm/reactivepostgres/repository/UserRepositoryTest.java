package com.khm.reactivepostgres.repository;

import com.khm.reactivepostgres.entity.User;
import com.khm.reactivepostgres.mock.UserMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.Objects;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class UserRepositoryTest {
    @MockBean
    private UserRepository userRepository;

    @Test
    public void a() {
        BDDMockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(UserMock.user1()));
        Assertions.assertTrue(Objects.nonNull(userRepository.findByEmail("a")));
    }
}
