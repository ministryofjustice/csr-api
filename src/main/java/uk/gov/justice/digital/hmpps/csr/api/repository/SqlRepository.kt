package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.DetailTemplate
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

  fun getModified(): List<CmdNotification> =
    jdbcTemplate.query(GET_MODIFIED, modifiedRowMapper)

  fun deleteProcessed(ids: List<Long>) =
    jdbcTemplate.update("delete from CMD_NOTIFICATION where ID in (:ids)", mapOf("ids" to ids))

  fun deleteAll() =
    jdbcTemplate.update("delete from CMD_NOTIFICATION", emptyMap<String, String>())

  fun deleteOld(date: LocalDate) =
    jdbcTemplate.update("delete from CMD_NOTIFICATION where LASTMODIFIED < :date", mapOf("date" to date))

  fun getDetailTemplates(templateNames: Collection<String>): Collection<DetailTemplate> {
    return jdbcTemplate.query(
      GET_DETAIL_TEMPLATES,
      MapSqlParameterSource()
        .addValue("values", templateNames),
      detailsTemplateRowMapper
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
        resultSet.getString("templateName")
      )
    }

    val modifiedRowMapper: RowMapper<CmdNotification> = RowMapper { resultSet: ResultSet, _: Int ->
      CmdNotification(
        id = resultSet.getLong("id"),
        levelId = resultSet.getInt("level_id"),
        onDate = resultSet.getDate("on_date").toLocalDate(),
        quantumId = resultSet.getString("quantumId"),
        lastModified = resultSet.getTimestamp("lastmodified").toLocalDateTime(),
        actionType = resultSet.getInt("action_type"),
        startTimeInSeconds = resultSet.getLong("startTime"),
        endTimeInSeconds = resultSet.getLong("endTime"),
        activity = resultSet.getString("activity"),
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
        null
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
        2,
        null
      )
    }

    val detailsTemplateRowMapper: RowMapper<DetailTemplate> = RowMapper { resultSet: ResultSet, _: Int ->
      DetailTemplate(
        resultSet.getLong("startTime"),
        resultSet.getLong("endTime"),
        resultSet.getBoolean("isRelative"),
        resultSet.getString("activity"),
        resultSet.getString("templateName")
      )
    }

    val GET_DETAILS = """
        SELECT DISTINCT sched.on_date as shiftDate, 
                        DECODE (tk_model.frame_start, NULL, sched.task_start, tk_model.frame_start) as startTime, 
                        DECODE (tk_model.frame_end, NULL, sched.task_end, tk_model.frame_end) as endTime, 
                        DECODE (sched.level_id, 4000, 1, 0) as shiftType,
                        DECODE (tk_model.name, NULL, tk_type.name, tk_model.name) as activity,
                        tk_model.name as templateName
        FROM tw_schedule sched
                INNER JOIN sm_user usr ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0
                LEFT JOIN tk_type ON sched.ref_id = tk_type.tk_type_id 
                LEFT JOIN tk_model ON tk_model.tk_model_id = sched.optional_1 and tk_model.is_deleted = 0
        WHERE sched.st_staff_id = usr.obj_id
        AND   sched.on_date BETWEEN :from AND :to
        AND   sched.layer = -1
        AND   sched.level_id IN (1000, 4000)
        AND   LOWER(usr.name) = LOWER(:quantumId)
    """.trimIndent()

    val GET_MODIFIED_SHIFTS = """
            SELECT DISTINCT pro.st_staff_id,
            
                usr.name AS quantumId, 
                pro.lastmodified AS shiftModified,
                pro.on_date AS shiftDate,
                
                CASE pro.level_id WHEN 4000 THEN 1 ELSE 0 END AS shiftType, 
                CASE WHEN pro.action_type = 47012 THEN 3 -- delete shift 
                     WHEN pro.action_type = 47001 THEN 2 -- edit shift 
                     WHEN pro.action_type IN(47006, 47015) THEN 1 -- add shift 
                ELSE 0 
                END AS actionType 
            FROM tw_protocol pro
                INNER JOIN sm_user usr ON pro.ST_STAFF_ID = usr.OBJ_ID AND usr.OBJ_TYPE = 3 AND usr.IS_DELETED = 0
                         
            -- 1. Staff must be currently allocated to the planning unit
            -- 2. Staff shift must have changed in the date between current date and some date in future
            -- 3. Usage of ST_STAFF_ID IN() instead of a standard join will force DB engine to do an index scan, thus speed up query.
            WHERE  pro.ST_STAFF_ID IN (
                                            SELECT st_staff.st_staff_id 
                                            FROM st_staff 
                                            INNER JOIN st_planunit ON st_staff.st_staff_id = st_planunit.st_staff_id
                                             
                                            -- Staff belonging to all children Planning Unit IDs (no need for a JOIN!) 
                                            -- There are duplicated recs for some PUs, so use is_deleted=0 to search for active ones only! 
                                            WHERE st_planunit.pu_planunit_id IN (
                                                                                    SELECT pu_planunit_id 
                                                                                    FROM pu_planunit 
                                                                                    WHERE is_deleted = 0 
                                                                                    AND pu_planunit.name NOT LIKE '%irtual)' 
                                                                                    AND LOWER(pu_planunit.name) LIKE LOWER(:planUnit) || '%' 
                                                                                ) 
                                            -- must be valid prior to current date (exclude date = 01-JAN-00 because 00 = 1900!) 
                                            AND (st_planunit.valid_from < SYSDATE - 1 AND TO_CHAR(st_planunit.valid_from,'YY') > 0)
                                            AND st_planunit.valid_to > SYSDATE
                                            
                                            AND st_planunit.priority = 1 
                                            AND st_staff.is_deleted = 0
                                        ) 
            AND pro.LAYER = -1 -- TOP LAYER 
            AND pro.level_id IN(1000, 4000) -- shift and time recording lines 
            
            AND pro.lastmodified >= (SYSDATE - 1) 
            AND (pro.on_date BETWEEN (SYSDATE - 1) AND (SYSDATE + 10))
    """.trimIndent()

    val GET_MODIFIED_DETAILS = """
            SELECT DISTINCT sched.on_date as shiftDate,
                            sched.st_staff_id,
                            usr.name AS quantumId, 
                            sched.task_start as startTime, 
                            sched.task_end as endTime, 
                            DECODE (tk_model.name, NULL, tk_type.name, tk_model.name) as activity,
                            (
                                SELECT MAX(tw_protocol.lastmodified)
                                FROM tw_protocol 
                                WHERE st_staff_id = sched.st_staff_id
                                AND level_id = sched.level_id
                                AND layer = -1 
                                AND (tw_protocol.on_date BETWEEN (SYSDATE - 1) AND (SYSDATE + 1))
                            ) as shiftModified,
                             
                            CASE sched.level_id WHEN 4000 THEN 1 ELSE 0 END AS shiftType
                            
            FROM tw_schedule sched 
            
                INNER JOIN sm_user usr ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0 
                LEFT JOIN tk_type ON sched.ref_id = tk_type.tk_type_id 
                LEFT JOIN tk_model ON tk_model.tk_model_id = sched.optional_1 
                
            -- Filter by staff that are currently assigned to Planning Unit 
            WHERE sched.st_staff_id IN (
                                        SELECT st_staff.st_staff_id 
                                        FROM st_staff 
                                        INNER JOIN st_planunit ON st_staff.st_staff_id = st_planunit.st_staff_id 
                                        
                                        -- Staff belonging to all children Planning Unit IDs (no need for a JOIN!) 
                                        -- There are duplicated recs for some PUs, so use is_deleted=0 to search for active ones only! 
                                        WHERE st_planunit.pu_planunit_id IN (
                                                                                SELECT pu_planunit_id 
                                                                                FROM pu_planunit 
                                                                                WHERE is_deleted = 0 
                                                                                AND pu_planunit.name NOT LIKE '%irtual)' 
                                                                                AND LOWER(pu_planunit.name) LIKE LOWER(:planUnit) || '%' 
                                                                             )
                                                                              
                                        -- Staff assignment must be valid prior to current date (exclude date = 01-JAN-00 because 00 = 1900!) 
                                        AND (st_planunit.valid_from < SYSDATE AND TO_CHAR(st_planunit.valid_from,'YY') > 0) 
                                        AND st_planunit.valid_to > SYSDATE
                         
                                        AND st_planunit.priority = 1 
                                        AND st_staff.is_deleted = 0
                                        ) 
                AND sched.pu_planunit_id IN (
                                                SELECT pu_planunit_id 
                                                FROM pu_planunit 
                                                WHERE is_deleted = 0 
                                                AND pu_planunit.name NOT LIKE '%irtual)' 
                                                AND LOWER(pu_planunit.name) LIKE LOWER(:planUnit) || '%'
                                            )
                AND sched.on_date <= (SYSDATE + 1)
                AND (
                        (
                            -- Check timings for current day 
                            sched.on_date between SYSDATE - 1 and SYSDATE 
                            -- Tasks that have not yet started 
                            AND (TO_NUMBER(ROUND(sched.task_start/3600, 0)) >= TO_NUMBER(TO_CHAR(SYSDATE, 'HH24')))
                        )
                        OR ( 
                            -- Check timings for the following day 
                            sched.on_date between SYSDATE and SYSDATE + 1
                            -- task start time must be within x hrs from now
                            AND TO_NUMBER(ROUND(sched.task_start/3600, 0)) <= TO_NUMBER(TO_CHAR(SYSDATE, 'HH24'))
                        )
                    )
                AND sched.sched_lastmodified >= (SYSDATE - 1)

                AND sched.LAYER = -1 -- TOP LAYER
                AND sched.level_id IN(1000, 4000) -- detail and time recording lines
    """.trimIndent()

    val GET_MODIFIED = """
            SELECT 
                n.id,
                n.level_id,
                n.on_date,
                usr.name AS quantumId, 
                CASE WHEN n.action_type is null
                then (
                      SELECT MAX(tw_protocol.lastmodified)
                      FROM tw_protocol
                      WHERE st_staff_id = n.st_staff_id
                      AND level_id = n.level_id
                      AND layer = -1 
                      AND tw_protocol.on_date BETWEEN (SYSDATE - 1) AND (SYSDATE + 2)
                     ) 
                else n.lastmodified
                end as lastmodified,
                n.action_type,
                n.task_start as startTime, 
                n.task_end as endTime, 
                NVL (tk_model.name, tk_type.name) as activity

            FROM CMD_NOTIFICATION n
                LEFT JOIN sm_user usr ON n.ST_STAFF_ID = usr.OBJ_ID 
                    AND usr.OBJ_TYPE = 3 
                    AND usr.IS_DELETED = 0
                LEFT JOIN tk_type  ON n.ref_id     = tk_type.tk_type_id 
                LEFT JOIN tk_model ON n.optional_1 = tk_model.tk_model_id 
            WHERE processed = 0
    """.trimIndent()

    val GET_DETAIL_TEMPLATES = """
            SELECT tk_modelItem.TASK_START AS startTime,
                tk_modelItem.TASK_END AS endTime,
                tk_modelItem.IS_FRAME_RELATIVE AS isRelative,
                tk_type.NAME AS activity,
                tk_model.NAME AS templateName
                FROM tk_modelitem
            JOIN tk_model ON tk_modelitem.TK_MODEL_ID = tk_model.TK_MODEL_ID
            JOIN tk_type on tk_type.TK_TYPE_ID = tk_modelitem.TK_TYPE_ID
            WHERE tk_model.NAME IN (:values)
                AND tk_model.IS_DELETED = 0
                AND tk_modelitem.taskstyle = 0
    """.trimIndent()
  }
}
