package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.sql.ResultSet
import java.time.LocalDate

@Repository
class DetailRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun getDetails(from: LocalDate, to: LocalDate, quantumId: String): Collection<Detail> {
        return jdbcTemplate.query(
                GET_DETAILS,
                MapSqlParameterSource()
                        .addValue("from", from)
                        .addValue("to", to)
                        .addValue("quantumId", quantumId),
                rowMapper
        )
    }

    companion object {

        val rowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
            Detail(
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("startTime"),
                    resultSet.getLong("endTime"),
                    resultSet.getInt("shiftType"),
                    resultSet.getString("activity")
            )
        }

        val GET_DETAILS = """
        SELECT DISTINCT tw_schedule.on_date as date, 
                        tw_schedule.task_start as startTime, 
                        tw_schedule.task_end as endTime,
                        CASE tw_schedule.level_id  WHEN 4000 THEN 1 ELSE 0 END AS shiftType,
                        DECODE (tk_model.NAME, NULL, tk_type.NAME, tk_model.NAME) as activity 
        FROM tw_schedule
                INNER JOIN sm_user usr ON tw_schedule.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0
                LEFT JOIN tk_type ON tw_schedule.ref_id = tk_type.tk_type_id 
                LEFT JOIN tk_model ON tk_model.tk_model_id = tw_schedule.optional_1 
        WHERE TW_SCHEDULE.ST_STAFF_ID = SM_USER.OBJ_ID
        AND   TW_SCHEDULE.ON_DATE BETWEEN :from AND :to
        AND   TW_SCHEDULE.LAYER = -1        -- TOP LAYER
        AND   TW_SCHEDULE.LEVEL_ID = 4000   -- Time Recording line only
        AND   LOWER(SM_USER.NAME) = LOWER(:quantumId)
        ORDER BY date;
        """.trimIndent()

    }
}
