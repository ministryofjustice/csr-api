package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.TemplateDetail
import java.sql.ResultSet
import java.time.LocalDate

@Repository
class SqlRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun getDetails(from: LocalDate, to: LocalDate, quantumId: String): Collection<Detail> {
        return jdbcTemplate.query(
                GET_DETAILS,
                MapSqlParameterSource()
                        .addValue("from", from)
                        .addValue("to", to)
                        .addValue("quantumId", quantumId),
                detailsRowMapper
        )
    }

    fun getModifiedShifts(planUnit: String): Collection<Detail> {
        return jdbcTemplate.query(
                GET_MODIFIED_SHIFTS,
                MapSqlParameterSource()
                        .addValue("planUnit", planUnit),
                modifiedShiftsRowMapper
        )
    }

    fun getModifiedDetails(planUnit: String): Collection<Detail> {
        return jdbcTemplate.query(
                GET_MODIFIED_DETAILS,
                MapSqlParameterSource()
                        .addValue("planUnit", planUnit),
                modifiedDetailsRowMapper
        )
    }

    fun getTemplateDetails(modelNames: List<String>): Collection<TemplateDetail> {
        val values = modelNames.joinToString()
        return jdbcTemplate.query(
                GET_TEMPLATE_DETAILS,
                MapSqlParameterSource()
                        .addValue("values", values),
                templateDetailsRowMapper
        )
    }

    companion object {

        val detailsRowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
            Detail(
                    null,
                    null,
                    resultSet.getDate("shiftDate").toLocalDate(),
                    resultSet.getInt("shiftType"),
                    resultSet.getLong("startTime"),
                    resultSet.getLong("endTime"),
                    resultSet.getString("activity"),
                    null,
                    resultSet.getString("modelName")
            )
        }

        val modifiedShiftsRowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
            Detail(
                    resultSet.getString("quantumId"),
                    resultSet.getTimestamp("shiftModified").toLocalDateTime(),
                    resultSet.getDate("shiftDate").toLocalDate(),
                    resultSet.getInt("shiftType"),
                    null,
                    null,
                    null,
                    resultSet.getInt("actionType"),
                    resultSet.getString("modelName")
            )
        }

        val modifiedDetailsRowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
            Detail(
                    resultSet.getString("quantumId"),
                    resultSet.getTimestamp("shiftModified").toLocalDateTime(),
                    resultSet.getDate("shiftDate").toLocalDate(),
                    resultSet.getInt("shiftType"),
                    resultSet.getLong("startTime"),
                    resultSet.getLong("endTime"),
                    resultSet.getString("activity"),
                    resultSet.getInt("actionType"),
                    resultSet.getString("modelName")
            )
        }

        val templateDetailsRowMapper: RowMapper<TemplateDetail> = RowMapper { resultSet: ResultSet, _: Int ->
            TemplateDetail(
                    resultSet.getLong("detailStart"),
                    resultSet.getLong("detailEnd"),
                    resultSet.getBoolean("isRelative"),
                    resultSet.getString("activity"),
                    resultSet.getString("modelName")
            )
        }

        val GET_DETAILS = """
        SELECT DISTINCT sched.on_date as shiftDate, 
                        DECODE (tk_model.frame_start, NULL, sched.task_start, tk_model.frame_start) as startTime, 
                        DECODE (tk_model.frame_end, NULL, sched.task_end, tk_model.frame_end) as endTime, 
                        CASE sched.level_id  WHEN 4000 THEN 1 ELSE 0 END AS shiftType,
                        DECODE (tk_model.name, NULL, tk_type.name, tk_model.name) as activity,
                        tk_model.name as modelName
        FROM tw_schedule sched
                INNER JOIN sm_user usr ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0
                LEFT JOIN tk_type ON sched.ref_id = tk_type.tk_type_id 
                LEFT JOIN tk_model ON tk_model.tk_model_id = sched.optional_1 and tk_model.is_deleted = 0
        WHERE sched.st_staff_id = usr.obj_id
        AND   sched.on_date BETWEEN :from AND :to
        AND   sched.layer = -1
        AND   sched.level_id IN (1000, 4000)
        AND   LOWER(usr.name) = LOWER(:quantumId)
        ORDER BY shiftDate
        """.trimIndent()

        val GET_MODIFIED_SHIFTS = """
            SELECT DISTINCT usr.name AS quantumId, 
                            pro.lastmodified as shiftModified,
                            sched.on_date as shiftDate,  
                            CASE sched.level_id WHEN 4000 THEN 1 ELSE 0 END AS shiftType, 
                            CASE 
                                WHEN pro.action_type = 47012 THEN 3 -- delete shift 
                                WHEN pro.action_type = 47001 THEN 2 -- edit shift 
                                WHEN pro.action_type IN(47006, 47015) THEN 1 -- add shift 
                                ELSE 0 
                            END AS actionType 
            FROM tw_schedule sched 
                INNER JOIN sm_user usr ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0 
                INNER JOIN tw_protocol pro ON pro.st_staff_id = sched.st_staff_id AND pro.LAYER = sched.LAYER AND pro.level_id = sched.level_id 
            -- Filter by staff that are currently assigned to Planning Unit 
            WHERE sched.st_staff_id IN 
                (SELECT st_staff.st_staff_id 
                    FROM st_staff 
                        INNER JOIN st_planunit ON st_staff.st_staff_id = st_planunit.st_staff_id 
                    -- Staff belonging to all children Planning Unit IDs (no need for a JOIN!) 
                    -- There are duplicated recs for some PUs, so use is_deleted=0 to search for active ones only! 
                    WHERE st_planunit.pu_planunit_id IN
                        (SELECT pu_planunit_id 
                            FROM pu_planunit 
                            WHERE is_deleted = 0 
                            AND lower(:planUnit) NOT LIKE '%virtual%' 
                            AND lower(:planUnit) LIKE lower(p_planunit) || '%' ) 
                            -- Staff assignment must be valid prior to current date (exclude date = 01-JAN-00 because 00 = 1900!) 
                        AND (floor(st_planunit.valid_from - (SYSDATE - 1)) <= 0 
                            AND to_char(st_planunit.valid_from,'YY') > 0) 
                        AND st_planunit.valid_to > SYSDATE 
                        AND st_planunit.priority = 1 
                        AND st_staff.is_deleted = 0) 
                    AND sched.pu_planunit_id IN 
                        (SELECT pu_planunit_id 
                            FROM pu_planunit 
                            WHERE is_deleted = 0 
                            AND lower(:planUnit) NOT LIKE '%virtual%' 
                            AND lower(:planUnit) LIKE lower(p_planunit) || '%' ) 
                    AND sched.LAYER = -1 -- TOP LAYER 
                    AND sched.level_id IN(1000, 4000) -- detail and time recording lines 
                    AND pro.lastmodified >= (SYSDATE - 1) 
                    AND (pro.on_date BETWEEN (SYSDATE - 1) 
                        AND (SYSDATE + 130)
                );""".trimIndent()

        val GET_MODIFIED_DETAILS = """
            SELECT DISTINCT usr.name AS quantumId, 
                            (SELECT MAX(tw_protocol.lastmodified)
                                FROM tw_protocol 
                                WHERE st_staff_id = sched.st_staff_id
                                AND layer = -1 
                                AND (tw_protocol.on_date BETWEEN (SYSDATE - 1) AND (SYSDATE + 1))) as shiftModified,
                            sched.on_date as date, 
                            CASE sched.level_id WHEN 4000 THEN 1 ELSE 0 END AS shiftType,
                            sched.task_start as startTime, 
                            sched.task_end as endTime, 
                            DECODE (tk_model.name, NULL, tk_type.name, tk_model.name) as activity,
                             2 AS actionType, -- edit shift 
            FROM tw_schedule sched 
                INNER JOIN sm_user usr ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0 
                LEFT JOIN tk_type ON sched.ref_id = tk_type.tk_type_id 
                LEFT JOIN tk_model ON tk_model.tk_model_id = sched.optional_1 
                        -- Filter by staff that are currently assigned to Planning Unit 
            WHERE sched.st_staff_id IN 
                (SELECT st_staff.st_staff_id 
                    FROM st_staff 
                        INNER JOIN st_planunit ON st_staff.st_staff_id = st_planunit.st_staff_id 
                    -- Staff belonging to all children Planning Unit IDs (no need for a JOIN!) 
                    -- There are duplicated recs for some PUs, so use is_deleted=0 to search for active ones only! 
                    WHERE st_planunit.pu_planunit_id IN
                        (SELECT pu_planunit_id 
                            FROM pu_planunit 
                            WHERE is_deleted = 0 
                            AND lower(name) NOT LIKE '%virtual%' 
                            AND lower(name) LIKE LOWER(p_planunit) || '%' ) 
                            -- Staff assignment must be valid prior to current date (exclude date = 01-JAN-00 because 00 = 1900!) 
                        AND (FLOOR(st_planunit.valid_from - (SYSDATE - 1)) <= 0 
                            AND TO_CHAR(st_planunit.valid_from,'YY') > 0) 
                        AND st_planunit.valid_to > SYSDATE 
                        AND st_planunit.priority = 1 
                        AND st_staff.is_deleted = 0) 
                    AND sched.pu_planunit_id IN 
                        (SELECT pu_planunit_id 
                            FROM pu_planunit 
                            WHERE is_deleted = 0 
                            AND lower(name) NOT LIKE '%virtual%' 
                            AND lower(name) LIKE lower(p_planunit) || '%' ) 
                    AND sched.LAYER = -1 -- TOP LAYER 
                    AND sched.level_id IN(1000, 4000) -- detail and time recording lines 
                    AND sched.sched_lastmodified >= (SYSDATE - 1)
                    AND sched.on_date <= (SYSDATE + 1)
                        AND (
                            -- Check timings for current day 
                            TO_NUMBER(TRUNC((SYSDATE + 1) - sched.on_date) * (86400 / 3600)) = (86400 / 3600)
                            -- Tasks that have not yet started 
                            AND (TO_NUMBER(ROUND(sched.task_start/3600, 0)) >= TO_NUMBER(TO_CHAR(SYSDATE, 'HH24'))
                        )
                        OR ( 
                            -- Check timings for the following day 
                            TO_NUMBER(FLOOR((SYSDATE + 1) - sched.on_date)) = 0
                            -- task start time must be within x hrs from now
                            AND TO_NUMBER(ROUND(sched.task_start/3600, 0)) <= TO_NUMBER(TO_CHAR(SYSDATE, 'HH24'))
                        )
            );""".trimIndent()

        val GET_TEMPLATE_DETAILS = """
            SELECT tk_modelItem.TASK_START AS startTime,
                tk_modelItem.TASK_END AS endTime,
                tk_modelItem.IS_FRAME_RELATIVE AS isRelative,
                tk_type.NAME AS activity,
                tk_model.NAME AS modelName
                FROM tk_modelitem
            JOIN tk_model ON tk_modelitem.TK_MODEL_ID = tk_model.TK_MODEL_ID
            JOIN tk_type on tk_type.TK_TYPE_ID = tk_modelitem.TK_TYPE_ID
            WHERE tk_model.NAME IN (:values)
                AND tk_model.IS_DELETED = 0
                AND tk_modelitem.taskstyle = 0;
            """.trimIndent()
    }
}
