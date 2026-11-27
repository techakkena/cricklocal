--
-- PostgreSQL database dump
--

\restrict exvhKnPDkMiBUo0tfmq2FH1lIoSj5x2yUDqm1V5JY3KfZ3CwS826pTz1Q8EqGtV

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
-- Data for Name: user_authentications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_authentications (id, created_at, last_login_at, provider, provider_user_id, user_id) FROM stdin;
1186	2026-09-29 19:57:52.478589+05:30	2026-09-29 19:57:52.478589+05:30	GOOGLE	116502359650684802017	5050
393	2026-09-24 12:24:17.644429+05:30	2026-10-12 18:31:31.804294+05:30	GOOGLE	108511163048289005445	2167
1	2026-09-21 20:01:59.256152+05:30	2026-09-26 13:29:51.02179+05:30	GOOGLE	117171113789142546829	1
443	2026-09-24 16:51:42.804981+05:30	2026-09-29 09:44:33.823643+05:30	GOOGLE	111409463735667563305	2346
\.


--
-- PostgreSQL database dump complete
--

\unrestrict exvhKnPDkMiBUo0tfmq2FH1lIoSj5x2yUDqm1V5JY3KfZ3CwS826pTz1Q8EqGtV

