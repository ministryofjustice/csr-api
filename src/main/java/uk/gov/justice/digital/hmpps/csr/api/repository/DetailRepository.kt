package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.sql.ResultSet
import java.time.LocalDate

@Repository
class DetailRepository(val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun getDetails(from: LocalDate, to: LocalDate, quantumId: String): Collection<Detail> {
        return jdbcTemplate.query(
                GET_OVERTIME_DETAILS,
                MapSqlParameterSource()
                        .addValue("from", from)
                        .addValue("to", to)
                        .addValue("quantum_id", quantumId),
                rowMapper
        )
    }

    companion object {

        val rowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
            Detail(
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("startTime"),
                    resultSet.getLong("endTime"),
                    resultSet.getString("activity")
            )
        }

        val GET_OVERTIME_DETAILS = """
        SELECT DISTINCT 
          CASE
            WHEN TW_SCHEDULE.TASK_START < 0 THEN TW_SCHEDULE.ON_DATE-1
            ELSE TW_SCHEDULE.ON_DATE
          END as date,
          
          CASE 
            WHEN TW_SCHEDULE.TASK_START = -2147483648 THEN 0
            WHEN TW_SCHEDULE.TASK_START < 0 THEN (TW_SCHEDULE.TASK_START + 86400)
            ELSE TW_SCHEDULE.TASK_START
          END as startTime,
          
          CASE 
            WHEN TW_SCHEDULE.TASK_END = -2147483648 THEN 0
            WHEN TW_SCHEDULE.TASK_END < 0 THEN (TW_SCHEDULE.TASK_END + 86400)
            ELSE TW_SCHEDULE.TASK_END
          END as endTime,
                     
          DECODE (TK_MODEL.NAME, NULL, TK_TYPE.NAME, TK_MODEL.NAME) as activity
                      
        FROM TW_SCHEDULE 
            INNER JOIN SM_USER ON TW_SCHEDULE.ST_STAFF_ID = SM_USER.OBJ_ID
            LEFT JOIN TK_TYPE ON TW_SCHEDULE.REF_ID = TK_TYPE.TK_TYPE_ID
            LEFT JOIN TK_MODEL ON TK_MODEL.TK_MODEL_ID = TW_SCHEDULE.OPTIONAL_1
                 
        -- Filter records for a period of time
        WHERE TW_SCHEDULE.ST_STAFF_ID = SM_USER.OBJ_ID
        
        AND   TW_SCHEDULE.ON_DATE BETWEEN :from AND :to
        AND   TW_SCHEDULE.LAYER = -1        -- TOP LAYER
        AND   TW_SCHEDULE.LEVEL_ID = 4000   -- Time Recording line only
        AND   LOWER(SM_USER.NAME) = LOWER(:quantum_id)
        
        ORDER BY date, startTime;
        """.trimIndent()

    }
}
