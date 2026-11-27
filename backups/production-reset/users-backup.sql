--
-- PostgreSQL database dump
--

\restrict q9HTIMM02fcM6NxO2scJvhRlDR83NjkwBPcGbbUuJVpChK6cu3k9VnccbgjUg4a

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
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: cricklocal_user
--

COPY public.users (id, active, created_at, display_name, email, phone_number, role, updated_at) FROM stdin;
1	t	2026-10-03 05:06:41.840151+00	gyana bhaskar	cnu210812@gmail.com	\N	PLAYER	2026-10-03 05:06:41.840151+00
2	t	2026-10-03 05:53:27.326541+00	PROTECH AUTOMOTIVE SOLUTIONS	protechautomotive99@gmail.com	\N	PLAYER	2026-10-03 05:53:27.326541+00
3	t	2026-10-10 10:02:59.108279+00	gedela g	rynaidu1569@gmail.com	\N	PLAYER	2026-10-10 10:02:59.108279+00
4	t	2026-10-24 07:36:30.818465+00	GYANA BHASKAR AKKENA	techvizag2017@gmail.com	\N	PLAYER	2026-10-24 07:36:30.818465+00
5	t	2026-10-24 07:39:07.119497+00	AKKENA NAIDU	iamakkena@gmail.com	\N	PLAYER	2026-10-24 07:39:07.119497+00
6	t	2026-10-24 16:09:06.101132+00	RONGALI SIMHACHALAM	simha1569@gmail.com	\N	PLAYER	2026-10-24 16:09:06.101132+00
7	t	2026-10-31 08:13:42.913478+00	Simhachalam Rongali	simha156916@gmail.com	\N	PLAYER	2026-10-31 08:13:42.913478+00
8	t	2026-10-31 08:18:17.911246+00	CMR GREEN FIELDS LAYOUT WELFARE SOCIETY	cmrsocietyvizianagaram@gmail.com	\N	PLAYER	2026-10-31 08:18:17.911246+00
\.


--
-- PostgreSQL database dump complete
--

\unrestrict q9HTIMM02fcM6NxO2scJvhRlDR83NjkwBPcGbbUuJVpChK6cu3k9VnccbgjUg4a

