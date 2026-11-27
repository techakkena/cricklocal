--
-- PostgreSQL database dump
--

\restrict 1JbRTSyo9HrUlKlvExWpLcTaN55dyUJZYjoHBiWY0k6v9QMM2VPyAIwv9s1VkHC

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    display_name character varying(100) NOT NULL,
    email character varying(150),
    phone_number character varying(20),
    role character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    account_status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'PLAYER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- PostgreSQL database dump complete
--

\unrestrict 1JbRTSyo9HrUlKlvExWpLcTaN55dyUJZYjoHBiWY0k6v9QMM2VPyAIwv9s1VkHC

