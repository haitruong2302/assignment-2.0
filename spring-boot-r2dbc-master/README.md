# Friends Management Project

An example implementation of Spring Boot R2DBC REST API with PostgreSQL database.

## Tools
* Java
* Maven
* PostgreSQL
* Docker

## Java Dependency
* Spring Boot 2.4.1.RELEASE
* Spring Data R2DBC
* PostgreSQL Driver
* Junit 5

## Setup

1. Run postgresql locally (or from docker)
2. Create two databases
   - postgres (main database)
   - test (dummy database for integration testing)
3. Run Spring Boot App

## APIs to test:

* API 1:
   - code snippet:
     curl --location --request POST 'localhost:8080/friends' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "friends": [
     "andy@example.com",
     "john@example.com"
     ]
     }'
   - Use case: create a friend connection between two email addresses
   - Eligibility: + Users have not a friend connection before
     + User has not blocked updates

* API 2:
   - code snippet:
     curl --location --request POST 'localhost:8080/friends/getFriend' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "email":"kante@example.com"
     }'
   - Use case: retrieve the friends list for an email address
   - Eligibility: + Users have a friend connection

* API 3:
   - code snippet:
     curl --location --request POST 'localhost:8080/friends/getCommonFriend' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "friends": [
     "drogba@example.com",
     "ballack@example.com"
     ]
     }'
   - Use case: retrieve the common friends list between two email addresses
   - Eligibility: + Users have a friend connection
     + Users has not a blocked updates

* API 4:
   - code snippet:
     curl --location --request PUT 'localhost:8080/friends/subscribeUpdates' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "requestor": "ronaldo@example.com",
     "target": "kante@example.com"
     }'
   - Use case: subscribe to updates from an email address
   - Eligibility: + Users have not a friend connection before
     + If user has subscribed updates, user will do not allow do it more
     + If user a has blocked user b, only user a just subscribe to updates to unblocked

* API 5:
   - code snippet:
     curl --location --request PUT 'localhost:8080/friends/block' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "requestor": "kante@example.com",
     "target": "mane@example.com"
     }'
   - Use case: block updates from an email address.
   - Eligibility: + If user a has blocked from user b, two users will no longer receive their notifications

* API 6:
   - code snippet:
     curl --location --request POST 'localhost:8080/friends/retrieveEmails' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "sender": "ballack@example.com",
     "text": "Hello World! aaa@example.com.vn/bbb@example.com.vn ccc@email.com"
     }'
   - Use case: retrieve all email addresses that can receive updates from an email address
   - Eligibility: + has not blocked updates from sender email
     + has subscribed to updates from sender email
     + has a friend connection
     + has been @mentioned in the update




