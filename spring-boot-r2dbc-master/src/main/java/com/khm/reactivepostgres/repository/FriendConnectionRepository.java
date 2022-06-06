package com.khm.reactivepostgres.repository;

import com.khm.reactivepostgres.entity.*;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FriendConnectionRepository extends R2dbcRepository<FriendConnection, Integer> {

    @Query("SELECT * FROM friend_connection " +
            "WHERE ((user_id = :userId AND friend_id = :friendId) " +
            "or (user_id = :friendId AND friend_id = :userId)) and status = :status")
    Flux<FriendConnection> findAllByUserIdAndFriendIdAndStatus(Integer userId, Integer friendId, StatusEnum status);

}
