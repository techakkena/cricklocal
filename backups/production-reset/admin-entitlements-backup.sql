--
-- PostgreSQL database dump
--

\restrict bORjQZKk3he1F4X6KFFq0KuBpnKyuTwKR3uvXFdw1OmPIZlkyfIWLsiLngPSlET

-- Dumped from database version 18.6 (Debian 18.6-1.pgdg12+2)
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
-- Data for Name: admin_entitlements; Type: TABLE DATA; Schema: public; Owner: cricklocal_user
--

COPY public.admin_entitlements (id, created_at, expires_at, starts_at, status, updated_at, user_id) FROM stdin;
1	2026-10-03 08:07:15.947689+00	\N	2026-10-03 08:07:15.947689+00	ACTIVE	2026-10-03 08:07:15.947689+00	1
\.


--
-- PostgreSQL database dump complete
--

\unrestrict bORjQZKk3he1F4X6KFFq0KuBpnKyuTwKR3uvXFdw1OmPIZlkyfIWLsiLngPSlET

