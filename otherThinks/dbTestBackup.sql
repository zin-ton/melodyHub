--
-- PostgreSQL database dump
--

-- Dumped from database version 15.10 (Debian 15.10-0+deb12u1)
-- Dumped by pg_dump version 17.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category (
                                 id integer NOT NULL,
                                 name text NOT NULL
);


ALTER TABLE public.category OWNER TO postgres;

--
-- Name: category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.category_id_seq OWNER TO postgres;

--
-- Name: category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;


--
-- Name: comment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comment (
                                id integer NOT NULL,
                                user_id integer NOT NULL,
                                post_id integer NOT NULL,
                                content text NOT NULL,
                                reply_to integer,
                                date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.comment OWNER TO postgres;

--
-- Name: COLUMN comment.date_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.comment.date_time IS 'generated automatically';


--
-- Name: comment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.comment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.comment_id_seq OWNER TO postgres;

--
-- Name: comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.comment_id_seq OWNED BY public.comment.id;


--
-- Name: like; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."like" (
                               id integer NOT NULL,
                               user_id integer NOT NULL,
                               post_id integer NOT NULL
);


ALTER TABLE public."like" OWNER TO postgres;

--
-- Name: like_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.like_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.like_id_seq OWNER TO postgres;

--
-- Name: like_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.like_id_seq OWNED BY public."like".id;


--
-- Name: post; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post (
                             id integer NOT NULL,
                             user_id integer NOT NULL,
                             description text,
                             name character varying(255) NOT NULL,
                             leadsheet bytea,
                             date_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                             s3_key character varying(255) NOT NULL
);


ALTER TABLE public.post OWNER TO postgres;

--
-- Name: post_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.post_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.post_id_seq OWNER TO postgres;

--
-- Name: post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.post_id_seq OWNED BY public.post.id;


--
-- Name: post_to_categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_to_categories (
                                           id integer NOT NULL,
                                           post_id integer NOT NULL,
                                           category_id integer NOT NULL
);


ALTER TABLE public.post_to_categories OWNER TO postgres;

--
-- Name: posttocategories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.posttocategories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.posttocategories_id_seq OWNER TO postgres;

--
-- Name: posttocategories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.posttocategories_id_seq OWNED BY public.post_to_categories.id;


--
-- Name: saved; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.saved (
                              id integer NOT NULL,
                              post_id integer NOT NULL,
                              user_id integer NOT NULL
);


ALTER TABLE public.saved OWNER TO postgres;

--
-- Name: saved_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.saved_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.saved_id_seq OWNER TO postgres;

--
-- Name: saved_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.saved_id_seq OWNED BY public.saved.id;


--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
                               id integer NOT NULL,
                               email character varying(255) NOT NULL,
                               first_name character varying(255) NOT NULL,
                               last_name character varying(255) NOT NULL,
                               password character varying(255) NOT NULL,
                               login character varying(255) NOT NULL
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public."user" ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: category id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category ALTER COLUMN id SET DEFAULT nextval('public.category_id_seq'::regclass);


--
-- Name: comment id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comment ALTER COLUMN id SET DEFAULT nextval('public.comment_id_seq'::regclass);


--
-- Name: like id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."like" ALTER COLUMN id SET DEFAULT nextval('public.like_id_seq'::regclass);


--
-- Name: post id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post ALTER COLUMN id SET DEFAULT nextval('public.post_id_seq'::regclass);


--
-- Name: saved id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved ALTER COLUMN id SET DEFAULT nextval('public.saved_id_seq'::regclass);


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.category (id, name) FROM stdin;
1	Other
3	Piano
2	Guitar
\.


--
-- Data for Name: comment; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.comment (id, user_id, post_id, content, reply_to, date_time) FROM stdin;
1	1	1	This arrangement is so impressive! The fast runs and arpeggios are just insane!	\N	2025-03-15 14:39:32.86684
2	2	1	I agree! The transitions between sections are amazing. The speed is mind-blowing!	\N	2025-03-15 14:39:32.86684
3	3	2	Finally! I’ve been waiting for this one for so long. The piano arrangement is beautiful!	\N	2025-03-15 14:39:32.86684
4	1	3	Such a classic! I love how you made this your own. Amazing cover!	\N	2025-03-15 14:39:32.86684
5	2	4	This is my favorite anime opening! You did such a great job with the piano arrangement!	\N	2025-03-15 14:39:32.86684
6	3	5	I love how relaxed and smooth this cover is! Very chill vibes!	\N	2025-03-15 14:39:32.86684
7	1	1	Yes, exactly! The way he incorporates the guitar riff is amazing.	1	2025-03-15 14:39:32.86684
8	2	2	Thanks for the kind words! It really means a lot to me.	3	2025-03-15 14:39:32.86684
9	3	4	Thank you! I’ve always loved this anime and its music, so I had to do it justice.	2	2025-03-15 14:39:32.86684
10	1	5	Glad you liked it! I wanted to keep it relaxed and simple.	5	2025-03-15 14:39:32.86684
18	46	1	123	\N	2025-05-08 18:09:07.722271
19	46	1	312	\N	2025-05-08 18:12:49.129577
20	46	1	Yes, that was me	\N	2025-05-08 18:13:35.446286
21	46	2	Yes sure bro i understand u u play very good instrument called guitar i guess	\N	2025-05-09 12:34:07.524668
22	46	2	123	\N	2025-05-09 12:35:25.35843
\.


--
-- Data for Name: like; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."like" (id, user_id, post_id) FROM stdin;
1	1	1
2	1	2
3	2	1
4	2	3
5	3	4
6	3	5
7	1	3
8	2	4
9	3	2
14	38	1
\.


--
-- Data for Name: post; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.post (id, user_id, description, name, leadsheet, date_time, s3_key) FROM stdin;
2	1	This is probably the most anticipated piano covers of this anime season, because countless people have requested a piano cover of this song. Now, the time has finally come! Here's my piano cover of the full version of "This Game" - the opening song to No Game No Life! 	This game (2014 ver.) - No Game No Life OP [Piano]	\N	2025-03-15 14:34:50.362934	0102a4eb-bde2-4342-8b1d-c1cd1bebf5b7test3.mp4
3	1	I hope you enjoyed it.Thanks for watching!	Nirvana - Smells Like Teen Spirit - Guitar Cover	\N	2025-03-15 14:34:50.362934	36d7a4ca-5c78-43d6-9bfe-ddccbaf0a4e5test5.mp4
5	2	Cześć Wszystkim! Taka mała spontaniczna wersja Quebonafide - Jesień z nowej płytki Romantic Psycho.	Quebonafide - Jesień | Guitar Chill Cover	\N	2025-03-15 14:34:50.362934	72f156e9-1b22-4dd3-b058-8352318aa4b7test1.mp4
1	1	My piano cover of "unravel" - the opening song of Tokyo Ghoul (2014). It was created while I was enrolled in a music conservatory in Germany and I was practicing many advanced classical pieces at that time. This might explain the extreme difficulty level of this piano arrangement, compared to my other arrangements. It is basically full with very risky jumps and super fast runs. Especially the particular "guitar riff" arpeggio at 02:21 is probably the fastest arpeggio I have ever attempted.	Unravel - Tokyo Ghoul OP [Piano]	\N	2025-03-15 14:34:50.362934	8bc981a2-f8c9-4032-b2ec-2679e9bda772test2.mp4
4	2	The long awaited piano cover is finally here! Hikaru Nara by Goose House! Shigatsu wa Kimi no Uso (your lie in April) is definitely one of the best anime series of this fall anime season 2014, and of course, I am not missing out the awesome OP song as well	Hikaru Nara - Your Lie in April OP1 [Piano]	\N	2025-03-15 14:34:50.362934	d494eacb-adf0-4714-a68c-188ebf58bb83test4.mp4
\.


--
-- Data for Name: post_to_categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.post_to_categories (id, post_id, category_id) FROM stdin;
3	3	2
4	4	3
5	5	2
2	2	3
1	1	3
\.


--
-- Data for Name: saved; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.saved (id, post_id, user_id) FROM stdin;
1	1	1
2	3	2
3	4	3
4	5	1
5	2	2
6	1	3
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, email, first_name, last_name, password, login) FROM stdin;
1	john.doe@example.com	John	Doe	password123	johndoe
2	jane.smith@example.com	Jane	Smith	password456	janesmith
3	alice.jones@example.com	Alice	Jones	password789	alicejones
4	test@gmail.com	test	test	$2a$10$hCQ5kU5jfm6wVJdqyZvxluxN/2tNFtLU6ccHnvO1TuV0arMhLFuzK	test
5	qwerty@gmail.com	Test	Test2	$2a$10$B9O6KqoHehXR/FSsHcblh.1tQavQRIQ0z81Db9U2GuzzfC312Ic2y	testtest
36	string@gmail.com	QHyOGxWpNhInutMsGLzAwDdItoaNfkMeYIwbplQYyjLMyzIaBMHwnOMTghdjPxjjilbenErckSAwiTVNtnbPBzIf	eILjDJGhJllazvLqHRtgFFOOqIQePdrcvBkvEooDvLVCRToSoGvGsvbVtISnmAVXwUCJJHvzdpPzyzRVFL	$2a$10$J0g7MX6fXhFsUCTrmHevyeKZrFZ5PXcZKVNiYNXUgpmFMhdYtD6mG	qwe
38	test3@gmail.com	Test	Test	$2a$10$93l4NFQIsZtx9h7R.PctRu6z0X/gIh9JwyKW05ApIEGKC2u.Ey0.W	test3
39	verba777@gmail.com	Ivan	Verba	$2a$10$pxG0tfUwSm4u3aSUUQy0wOvA/30PKj9jMP8hhPlbiBoOP4nGoqeXC	ivaninja
40	verba@gmail.com	Ivan	Verba	$2a$10$A6OFDtWGtSNDAg0uzqD6gOZuLZWXx7.Rd7QDN/TNr8f6w9WcVZap2	ivaninja123
43	314440@office.umcs.pl	Deleted	user	$2a$10$La8SJFJV19cMQdoo4ZyY3.g.H3nfYlSnPigjn6yYSKTiW7Gsw/wzS	deleted_user
44	verba+1@gmail.com	Ivan	Verba	$2a$10$o09p12U4XTqw0QUHW.XBLe4dwtMgwitG81F5F7ruCfCvY8kjSu9vC	ivano
45	verba_email@email.com	Ivan	Verbovetskyi	$2a$10$1diIe41wuaxsNQbPmPCKTOX1YxhZzUCY1n.kiORCl5MQeCUYC6P9e	login
46	zaba@gmail.com	zaba	zaba	$2a$10$0hRFh3ic8nJPJHzHLPtp5eBG29zGLlA2uXjtFzSoX8nOxWfMrkkTa	zaba
47	testlol@test.com	Nazar	Test	$2a$10$UJ9lxiofGxtuSjnx8xyc/.whcMVIgyniUdgt5eRGuilrmCF9guH0e	testlol
42	n.kuziv2007@gmail.com	Nazarko	Kuziv	$2a$10$X6eHxu23VoizUUeEBfKD4OhscRwWlO8pmwXVqrCyOIMD02Xg9gmiq	llutsefer
\.


--
-- Name: category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.category_id_seq', 3, true);


--
-- Name: comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.comment_id_seq', 22, true);


--
-- Name: like_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.like_id_seq', 24, true);


--
-- Name: post_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.post_id_seq', 12, true);


--
-- Name: posttocategories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.posttocategories_id_seq', 1, false);


--
-- Name: saved_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.saved_id_seq', 7, true);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_id_seq', 47, true);


--
-- Name: category category_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_name_key UNIQUE (name);


--
-- Name: category category_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pk PRIMARY KEY (id);


--
-- Name: comment comment_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT comment_pk PRIMARY KEY (id);


--
-- Name: user id; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT id PRIMARY KEY (id);


--
-- Name: like like_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."like"
    ADD CONSTRAINT like_pk PRIMARY KEY (id);


--
-- Name: post post_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pk PRIMARY KEY (id);


--
-- Name: post_to_categories post_to_categories_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_to_categories
    ADD CONSTRAINT post_to_categories_pk PRIMARY KEY (id);


--
-- Name: saved saved_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved
    ADD CONSTRAINT saved_pk PRIMARY KEY (id);


--
-- Name: user user_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_email_key UNIQUE (email);


--
-- Name: user user_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_login_key UNIQUE (login);


--
-- Name: post_to_categories categoryid___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_to_categories
    ADD CONSTRAINT categoryid___fk FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE CASCADE;


--
-- Name: like post_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."like"
    ADD CONSTRAINT post_id___fk FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE CASCADE;


--
-- Name: comment post_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT post_id___fk FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE CASCADE;


--
-- Name: saved post_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved
    ADD CONSTRAINT post_id___fk FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE CASCADE;


--
-- Name: post_to_categories postid___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_to_categories
    ADD CONSTRAINT postid___fk FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE CASCADE;


--
-- Name: post postuserid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT postuserid FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: comment reply_to___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT reply_to___fk FOREIGN KEY (reply_to) REFERENCES public.comment(id) ON DELETE CASCADE;


--
-- Name: like user_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."like"
    ADD CONSTRAINT user_id___fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: comment user_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT user_id___fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: saved user_id___fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved
    ADD CONSTRAINT user_id___fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

