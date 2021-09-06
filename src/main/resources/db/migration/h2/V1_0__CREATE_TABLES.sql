
CREATE TABLE IF NOT EXISTS tw_protocol
(
    st_staff_id  VARCHAR   NOT NULL,
    on_date      TIMESTAMP NOT NULL,
    lastmodified TIMESTAMP NOT NULL,
    level_id     INT       NOT NULL,
    layer        INT       NOT NULL,
    action_type  INT       NOT NULL
);

CREATE TABLE IF NOT EXISTS tw_schedule
(
    TW_SCHEDULE_ID     INT       NOT NULL,
    st_staff_id        VARCHAR   NOT NULL,
    on_date            TIMESTAMP NOT NULL,
    sched_lastmodified TIMESTAMP NOT NULL,
    task_start         INT       NOT NULL,
    task_end           INT       NOT NULL,
    level_id           INT       NOT NULL,
    layer              INT       NOT NULL,
    ref_id             VARCHAR   NOT NULL,
    optional_1         VARCHAR   NOT NULL,
    pu_planunit_id     VARCHAR   NOT NULL
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
    SM_USER_ID INT     NOT NULL,
    obj_id     VARCHAR NOT NULL,
    obj_type   VARCHAR NOT NULL,
    name       VARCHAR NOT NULL,
    is_deleted INT     NOT NULL
);

CREATE TABLE IF NOT EXISTS st_staff
(
    st_staff_id    VARCHAR NOT NULL,
    is_deleted     INT     NOT NULL
);

CREATE TABLE IF NOT EXISTS pu_planunit
(
    pu_planunit_id VARCHAR NOT NULL,
    is_deleted     INT     NOT NULL,
    name           VARCHAR NOT NULL
--     SHORTNAME      VARCHAR NOT NULL,
--     COLOR          INT     NOT NULL,
--     RASTER         INT     NOT NULL,
--     PARENT_ID      INT     NOT NULL,
--     NUM_WORKPLACES INT     NOT NULL,
--     LO_TIMEZONE_ID INT     NOT NULL
);

CREATE TABLE IF NOT EXISTS st_planunit
(
    st_planunit_id VARCHAR   NOT NULL,
    st_staff_id    VARCHAR   NOT NULL,
    pu_planunit_id VARCHAR   NOT NULL,
    valid_from     TIMESTAMP NOT NULL,
    valid_to       TIMESTAMP NOT NULL,
    priority       INT       NOT NULL
);

