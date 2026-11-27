--
-- PostgreSQL database dump
--

\restrict PQhNfNmPxTrPDENpjcYxYFBmRMEA5mi5i4mHZ9EBZMuGXti2BUU7acusMNlMTUo

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

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

--
-- Data for Name: admin_entitlements; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admin_entitlements (id, created_at, expires_at, starts_at, status, updated_at, user_id) FROM stdin;
941	2026-09-26 11:15:10.121327+05:30	\N	2026-09-26 11:15:10.121327+05:30	ACTIVE	2026-09-26 11:15:10.121327+05:30	2167
3151	2026-10-24 13:30:15.432335+05:30	\N	2026-10-24 13:30:15.432335+05:30	ACTIVE	2026-10-24 13:30:15.432335+05:30	10480
705	2026-09-24 11:55:03.312498+05:30	\N	2026-11-07 21:29:01.455798+05:30	ACTIVE	2026-11-07 21:29:01.815691+05:30	1
\.


--
-- PostgreSQL database dump complete
--

\unrestrict PQhNfNmPxTrPDENpjcYxYFBmRMEA5mi5i4mHZ9EBZMuGXti2BUU7acusMNlMTUo

