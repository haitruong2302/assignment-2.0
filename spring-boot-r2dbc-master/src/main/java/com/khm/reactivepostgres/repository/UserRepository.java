package com.khm.reactivepostgres.repository;

import com.khm.reactivepostgres.entity.*;
import com.khm.reactivepostgres.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository extends R2dbcRepository<User, Long> {

    Flux<User> findAllByEmailIn(List<String> emails);

    Mono<User> findByEmail(String email);

    @Query("select email from users u where id in (SELECT CASE " +
            "WHEN user_id = :userId then friend_id " +
            "ELSE user_id " +
            "end FROM friend_connection " +
            "where " +
            "(user_id = :userId or friend_id = :userId) " +
            "and status = :status " +
            "and friend_status = :friendStatusEnum)")
    Flux<User> findAllByUserId(Integer userId, StatusEnum status, FriendStatusEnum friendStatusEnum);

    @Query("select u.* from users u where email in (select common_friend.email from ((select email from users u where \n" +
            "id in (select fc.friend_id from friend_connection fc  \n" +
            "where fc.user_id = :firstUserId and status = :status and friend_status = 'FRIEND' and action_status <> :actionStatusEnum)\n" +
            "union \n" +
            "select email from users u where\n" +
            "id in (select fc.user_id from friend_connection fc  \n" +
            "where fc.friend_id = :firstUserId and status = :status and friend_status = 'FRIEND' and action_status <> :actionStatusEnum))\n" +
            "union all \n" +
            "(select email from users u where \n" +
            "id in (select fc.friend_id from friend_connection fc  \n" +
            "where fc.user_id = :secondUserId and status = :status and friend_status = 'FRIEND' and action_status <> :actionStatusEnum)\n" +
            "union \n" +
            "select email from users u where\n" +
            "id in (select fc.user_id from friend_connection fc  \n" +
            "where fc.friend_id = :secondUserId and status = :status and friend_status = 'FRIEND' and action_status <> :actionStatusEnum))) as common_friend\n" +
            "group by common_friend.email\n" +
            "having count(common_friend.email) > 1)")
    Flux<User> findCommonFriendByEmails(Integer firstUserId, Integer secondUserId, StatusEnum status, ActionStatusEnum actionStatusEnum);

    @Query("SELECT email FROM users u WHERE id IN (SELECT CASE " +
            "WHEN user_id = :userId THEN friend_id " +
            "ELSE user_id " +
            "END FROM friend_connection " +
            "WHERE " +
            "(user_id = :userId OR friend_id = :userId) " +
            "AND status = :status  " +
            "AND (friend_status = 'FRIEND' OR (friend_id = :userId AND friend_status='UNFRIEND' AND action_status = 'RECEIVED_UPDATES')))")
    Flux<User> findAllByUserIdAndStatus(Integer userId, StatusEnum status);
}
