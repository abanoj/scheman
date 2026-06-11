--
-- PostgreSQL database dump
--

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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
-- Name: employee_store; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.employee_store (
    preference_order integer NOT NULL,
    employee_id uuid NOT NULL,
    store_id uuid NOT NULL
);


ALTER TABLE public.employee_store OWNER TO root;

--
-- Name: employees; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.employees (
    weekly_contracted_hours integer,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    dni character varying(255),
    preferred_shift character varying(255),
    CONSTRAINT employees_preferred_shift_check CHECK (((preferred_shift)::text = ANY ((ARRAY['MORNING'::character varying, 'AFTERNOON'::character varying, 'NIGHT'::character varying])::text[])))
);


ALTER TABLE public.employees OWNER TO root;


--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    revoked boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expiry_date timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO root;

--
-- Name: shift_assignments; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.shift_assignments (
    date date NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    employee_id uuid NOT NULL,
    id uuid NOT NULL,
    shift_id uuid NOT NULL
);


ALTER TABLE public.shift_assignments OWNER TO root;

--
-- Name: shift_available_days; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.shift_available_days (
    shift_id uuid NOT NULL,
    available_days character varying(255),
    CONSTRAINT shift_available_days_available_days_check CHECK (((available_days)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);


ALTER TABLE public.shift_available_days OWNER TO root;

--
-- Name: shifts; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.shifts (
    deleted boolean NOT NULL,
    effective_from date,
    effective_to date,
    end_time time(6) without time zone NOT NULL,
    start_time time(6) without time zone NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    group_id uuid,
    id uuid NOT NULL,
    store_id uuid,
    name character varying(255),
    shift_type character varying(255),
    CONSTRAINT shifts_shift_type_check CHECK (((shift_type)::text = ANY ((ARRAY['MORNING'::character varying, 'AFTERNOON'::character varying, 'NIGHT'::character varying])::text[])))
);


ALTER TABLE public.shifts OWNER TO root;

--
-- Name: stores; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.stores (
    deleted boolean NOT NULL,
    is24h boolean,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    address character varying(255),
    name character varying(255) NOT NULL,
    phone character varying(255)
);


ALTER TABLE public.stores OWNER TO root;

--
-- Name: users; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE IF NOT EXISTS public.users (
    enabled boolean NOT NULL,
    must_change_password boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['EMPLOYEE'::character varying, 'MANAGER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO root;

--
-- Name: employee_store employee_store_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employee_store
    ADD CONSTRAINT employee_store_pkey PRIMARY KEY (preference_order, employee_id);


--
-- Name: employees employees_dni_key; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_dni_key UNIQUE (dni);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (user_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_key UNIQUE (token);


--
-- Name: shift_assignments shift_assignments_employee_id_date_key; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shift_assignments
    ADD CONSTRAINT shift_assignments_employee_id_date_key UNIQUE (employee_id, date);


--
-- Name: shift_assignments shift_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shift_assignments
    ADD CONSTRAINT shift_assignments_pkey PRIMARY KEY (id);


--
-- Name: shifts shifts_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT shifts_pkey PRIMARY KEY (id);


--
-- Name: stores stores_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.stores
    ADD CONSTRAINT stores_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_refresh_token_user_id; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_refresh_token_user_id ON public.refresh_tokens USING btree (user_id);


--
-- Name: idx_shift_assignments_shift_id; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_shift_assignments_shift_id ON public.shift_assignments USING btree (shift_id);


--
-- Name: idx_shifts_group_id; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_shifts_group_id ON public.shifts USING btree (group_id);


--
-- Name: idx_shifts_store_id; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_shifts_store_id ON public.shifts USING btree (store_id);


--
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: shift_assignments fk21yymh6e7f9jgjt80yb8s98r; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shift_assignments
    ADD CONSTRAINT fk21yymh6e7f9jgjt80yb8s98r FOREIGN KEY (shift_id) REFERENCES public.shifts(id);


--
-- Name: employee_store fk4bw5cy9uaeed15m7w7f5ece28; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employee_store
    ADD CONSTRAINT fk4bw5cy9uaeed15m7w7f5ece28 FOREIGN KEY (employee_id) REFERENCES public.employees(user_id);


--
-- Name: shift_available_days fk4ji8svjerjblth2dos4vs51wl; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shift_available_days
    ADD CONSTRAINT fk4ji8svjerjblth2dos4vs51wl FOREIGN KEY (shift_id) REFERENCES public.shifts(id);


--
-- Name: employees fk69x3vjuy1t5p18a5llb8h2fjx; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fk69x3vjuy1t5p18a5llb8h2fjx FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: shifts fk9xe5xhf0o3ix0r76oyutlo693; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT fk9xe5xhf0o3ix0r76oyutlo693 FOREIGN KEY (store_id) REFERENCES public.stores(id);


--
-- Name: shift_assignments fkbfmt35sngf827e1fldbpvdi8a; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.shift_assignments
    ADD CONSTRAINT fkbfmt35sngf827e1fldbpvdi8a FOREIGN KEY (employee_id) REFERENCES public.employees(user_id);


--
-- Name: employee_store fksqj3gnwc57tyapwgavfd7gpao; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.employee_store
    ADD CONSTRAINT fksqj3gnwc57tyapwgavfd7gpao FOREIGN KEY (store_id) REFERENCES public.stores(id);


--
-- PostgreSQL database dump complete
--

