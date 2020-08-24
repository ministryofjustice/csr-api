
CREATE TABLE IF NOT EXISTS tw_protocol
(
    st_staff_id     VARCHAR     NOT NULL,
    on_date         DATE        NOT NULL,
    lastmodified    TIMESTAMP   NOT NULL,
    level_id        INT         NOT NULL,
    layer           INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS tw_schedule
(
    st_staff_id         VARCHAR     NOT NULL,
    on_date             DATE        NOT NULL,
    sched_lastmodified  TIMESTAMP   NOT NULL,
    task_start          INT         NOT NULL,
    task_end            INT         NOT NULL,
    level_id            INT         NOT NULL,
    layer               INT         NOT NULL,
    ref_id              VARCHAR     NOT NULL,
    optional_1          VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS tk_model
(
    tk_model_id  VARCHAR     NOT NULL,
    name         VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS tk_type
(
    tk_type_id   VARCHAR     NOT NULL,
    name         VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS sm_user
(
    obj_id      VARCHAR     NOT NULL,
    obj_type    VARCHAR     NOT NULL,
    name        VARCHAR     NOT NULL,
    is_deleted  INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS st_staff
(
    st_staff_id VARCHAR     NOT NULL,
    is_deleted  INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS pu_planunit
(
    pu_planunit_id  VARCHAR     NOT NULL,
    is_deleted      INT         NOT NULL,
    name            VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS st_planunit
(
    st_staff_id     VARCHAR     NOT NULL,
    is_deleted      INT         NOT NULL,
    p_planunit      VARCHAR     NOT NULL,
    valid_from      TIMESTAMP   NOT NULL,
    valid_to        TIMESTAMP   NOT NULL,
    priority        INT         NOT NULL
);

