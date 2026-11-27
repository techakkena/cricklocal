--
-- PostgreSQL database dump
--

\restrict LnBeDI2TZQjUo0c49ag0IoCtkQOj3Q4k8CMdGeqJkLXd3XSyJSv6HOZJb2r2xTE

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
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, active, created_at, display_name, email, phone_number, role, updated_at, account_status) FROM stdin;
1	t	2026-09-21 20:01:58.788604+05:30	GYANA BHASKAR AKKENA	techvizag2017@gmail.com	\N	PLAYER	2026-09-21 20:01:58.788604+05:30	ACTIVE
5050	t	2026-09-29 19:57:52.046481+05:30	PROTECH AUTOMOTIVE SOLUTIONS	protechautomotive99@gmail.com	\N	PLAYER	2026-09-29 19:57:52.046481+05:30	ACTIVE
10480	t	2026-10-24 13:25:55.737978+05:30	Simha	simha1569@gmail.com	\N	ADMIN	2026-10-24 13:25:55.737978+05:30	ACTIVE
2167	t	2026-09-24 12:24:17.516241+05:30	gyana bhaskar	cnu210812@gmail.com	\N	ADMIN	2026-09-24 12:24:17.516241+05:30	ACTIVE
2346	t	2026-09-24 16:51:42.654784+05:30	AKKENA NAIDU	iamakkena@gmail.com	\N	PLAYER	2026-09-24 16:51:42.654784+05:30	ACTIVE
\.


--
-- PostgreSQL database dump complete
--

\unrestrict LnBeDI2TZQjUo0c49ag0IoCtkQOj3Q4k8CMdGeqJkLXd3XSyJSv6HOZJb2r2xTE

