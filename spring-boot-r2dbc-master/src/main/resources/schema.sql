ALTER TABLE public.friend_connection DROP CONSTRAINT IF EXISTS friend_connection_friend_id_fkey;
ALTER TABLE public.friend_connection DROP CONSTRAINT IF EXISTS friend_connection_user_id_fkey;

DROP TABLE IF EXISTS public.users;

CREATE TABLE public.users
(
    id    serial4 NOT NULL,
    email text    NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

INSERT INTO public.users (email) VALUES('andy@example.com');
INSERT INTO public.users (email) VALUES('john@example.com');
INSERT INTO public.users (email) VALUES('drogba@example.com');

DROP TABLE IF EXISTS public.friend_connection;

CREATE TABLE public.friend_connection
(
    id            serial4 NOT NULL,
    user_id       int4 NULL,
    friend_id     int4 NULL,
    friend_status text    NOT NULL,
    action_status text    NOT NULL,
    status        text    NOT NULL DEFAULT 'ACTIVE'::text,
    created_by    text NULL,
    created_date  timestamp NULL,
    updated_date  timestamp NULL,
    updated_by    text NULL,
    CONSTRAINT friend_connection_pkey PRIMARY KEY (id)
);


-- public.friend_connection foreign keys
ALTER TABLE public.friend_connection
    ADD CONSTRAINT friend_connection_friend_id_fkey FOREIGN KEY (friend_id) REFERENCES public.users (id);
ALTER TABLE public.friend_connection
    ADD CONSTRAINT friend_connection_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id);