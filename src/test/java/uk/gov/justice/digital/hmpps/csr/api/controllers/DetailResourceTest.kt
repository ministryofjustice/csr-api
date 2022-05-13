package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.jdbc.core.RowMapper
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

class DetailResourceTest : ResourceTest() {

  private val ONE_HR = 60 * 60
  private val TWO_HRS = 60 * 60 * 2
  private val NINE_HRS = 60 * 60 * 9
  private val TEN_HRS = 60 * 60 * 10

  companion object {
    private val SYSTEM_ROLE = listOf("ROLE_SYSTEM_USER")
    private val ADMIN_ROLE = listOf("ROLE_CMD_ADMIN")
    private val TODAY_START = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
  }

  @BeforeEach
  fun cleanUp() {
    jdbcTemplate.update("delete from CMD_NOTIFICATION")
    jdbcTemplate.update("delete from R2.CMD_NOTIFICATION")
    jdbcTemplate.update("delete from TK_MODEL")
    jdbcTemplate.update("delete from TK_TYPE")
    jdbcTemplate.update("delete from TK_MODELITEM")
  }

  @Nested
  inner class UserDetails {
    @Test
    fun testUserDetailsOld() {

      jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (11, 'type 11')")
      jdbcTemplate.update("insert into TK_MODEL(TK_MODEL_ID, NAME, FRAME_START, FRAME_END, IS_DELETED) values (12, 'model 12', $ONE_HR, $TWO_HRS, 0)")
      jdbcTemplate.update(
        """insert into TK_MODELITEM(TK_MODELITEM_ID,TK_MODEL_ID,TK_TYPE_ID,TASKSTYLE,IS_FRAME_RELATIVE,TASK_START,TASK_END)
          values (100, 12, 11, 0, 0, $ONE_HR, $TWO_HRS)""".trimMargin()
      )

      jdbcTemplate.update(
        """Insert into TW_SCHEDULE (TW_SCHEDULE_ID, ON_DATE, LEVEL_ID, ST_STAFF_ID, LAYER, PU_PLANUNIT_ID, REF_ID, TASK_START, TASK_END, OPTIONAL_1, SCHED_LASTMODIFIED)
          values (1000001, '2022-03-13', 1000, 1147, -1, 1007, 11, 0, 0, 12, '2022-03-13')"""
      )

      // Note some tables are still populated from flyway SQL

      val response = webTestClient.get()
        .uri {
          it.path("/user/details")
            .queryParam("from", "2022-03-10")
            .queryParam("to", "2022-03-20")
            .build()
        }
        .headers(setAuthorisation())
        .header("X-Region", "1")
        .exchange()
        .expectStatus().isOk
        .expectBodyList(DetailDto::class.java)
        .returnResult()

      assertThat(response.responseBody).containsExactlyInAnyOrder(
        DetailDto(
          quantumId = null,
          shiftModified = null,
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T01:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T02:00:00"),
          activity = "type 11",
          actionType = null
        ),
      )
    }

    @Test
    fun testUserDetails() {

      jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (11, 'type 11')")
      jdbcTemplate.update("insert into TK_MODEL(TK_MODEL_ID, NAME, FRAME_START, FRAME_END, IS_DELETED) values (12, 'model 12', $ONE_HR, $TWO_HRS, 0)")
      jdbcTemplate.update(
        """insert into TK_MODELITEM(TK_MODELITEM_ID,TK_MODEL_ID,TK_TYPE_ID,TASKSTYLE,IS_FRAME_RELATIVE,TASK_START,TASK_END)
          values (100, 12, 11, 0, 0, $ONE_HR, $TWO_HRS)""".trimMargin()
      )

      jdbcTemplate.update(
        """Insert into TW_SCHEDULE (TW_SCHEDULE_ID, ON_DATE, LEVEL_ID, ST_STAFF_ID, LAYER, PU_PLANUNIT_ID, REF_ID, TASK_START, TASK_END, OPTIONAL_1, SCHED_LASTMODIFIED)
          values (1000001, '2022-03-13', 1000, 1147, -1, 1007, 11, 0, 0, 12, '2022-03-13')"""
      )

      // Note some tables are still populated from flyway SQL

      val response = webTestClient.get()
        .uri {
          it.path("/user/details/1")
            .queryParam("from", "2022-03-10")
            .queryParam("to", "2022-03-20")
            .build()
        }
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBodyList(DetailDto::class.java)
        .returnResult()

      assertThat(response.responseBody).containsExactlyInAnyOrder(
        DetailDto(
          quantumId = null,
          shiftModified = null,
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T01:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T02:00:00"),
          activity = "type 11",
          actionType = null
        ),
      )
    }
  }

  @Test
  fun testShifts() {
    val response = webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/shifts/updated")
          .build("Frankland")
      }
      .headers(setAuthorisation(roles = SYSTEM_ROLE))
      .exchange()
      .expectStatus().isOk
      .expectBodyList(DetailDto::class.java)
      .returnResult()

    assertThat(response.responseBody).containsExactlyInAnyOrder(
      DetailDto(
        quantumId = "a_1152",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.ADD
      ),
      DetailDto(
        quantumId = "a_1152",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START.plusDays(1),
        detailEnd = TODAY_START.plusDays(1),
        activity = null,
        actionType = ActionType.ADD
      ),
      DetailDto(
        quantumId = "a_1154",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
      DetailDto(
        quantumId = "a_1155",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.EDIT
      ),
      DetailDto(
        quantumId = "a_1156",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
      DetailDto(
        quantumId = "a_1157",
        shiftModified = LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
    )
  }

  @Test
  fun testDetails() {
    webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/details/updated")
          .build("Frankland")
      }
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isOk
      .expectBodyList(DetailDto::class.java)
      .hasSize(0)
  }

  @Test
  fun test_unauthorised() {
    webTestClient.get()
      .uri("/somewhere")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Nested
  inner class Notification {

    private val testRowMapper: RowMapper<Long> = RowMapper { rs: ResultSet, _: Int -> rs.getLong(1) }

    private val INSERT =
      "insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, ACTION_TYPE, TASK_START, TASK_END, REF_ID, OPTIONAL_1) values"
    private val INSERTR2 =
      "insert into R2.CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, ACTION_TYPE, TASK_START, TASK_END, REF_ID, OPTIONAL_1) values"

    @Test
    fun testGetNotifications() {
      jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (11, 'type 11')")
      jdbcTemplate.update("insert into TK_MODEL(TK_MODEL_ID, NAME, FRAME_START, FRAME_END, IS_DELETED) values (12, 'model 12', $ONE_HR, $TWO_HRS, 0)")

      jdbcTemplate.update("$INSERT (101, 1147, 1000, '2022-03-21', CURRENT_DATE,     null,  $NINE_HRS, $TEN_HRS, 11,null)")
      jdbcTemplate.update("$INSERT (102, 1148, 1000, '2022-03-22', CURRENT_DATE + 1, null,  $NINE_HRS, $TEN_HRS, null,12)")
      jdbcTemplate.update("$INSERT (103, 1148, 1000, '2022-03-22', CURRENT_DATE + 1, 47015, null, null, null, null)")
      jdbcTemplate.update("$INSERT (104, 1148, 4000, '2022-03-22', CURRENT_DATE + 1, 47012, null, null, null, null)")
      jdbcTemplate.update("$INSERT (105, 1148, 4000, '2022-03-22', CURRENT_DATE + 1, 47999, null, null, null, null)")

      val response = webTestClient.get()
        .uri("/updates/1")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBodyList(DetailDto::class.java)
        .returnResult()

      assertThat(response.responseBody).containsExactlyInAnyOrder(
        DetailDto(
          id = 101,
          quantumId = "TEST-USER",
          shiftModified = LocalDateTime.parse("2099-08-21T00:00:00"),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-21T09:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-21T10:00:00"),
          activity = "type 11",
          actionType = ActionType.EDIT
        ),
        DetailDto(
          id = 102,
          quantumId = "a_1148",
          shiftModified = LocalDateTime.parse("2099-08-21T00:00:00"),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-22T09:00:00"), // not overridden by tk_model
          detailEnd = LocalDateTime.parse("2022-03-22T10:00:00"),
          activity = "model 12",
          actionType = ActionType.EDIT,
        ),
        DetailDto(
          id = 103,
          quantumId = "a_1148",
          shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
          activity = null,
          actionType = ActionType.ADD,
        ),
        DetailDto(
          id = 104,
          quantumId = "a_1148",
          shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
          shiftType = ShiftType.OVERTIME,
          detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
          activity = null,
          actionType = ActionType.DELETE,
        ),
        DetailDto(
          id = 105,
          quantumId = "a_1148",
          shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
          shiftType = ShiftType.OVERTIME,
          detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
          activity = null,
          actionType = ActionType.UNCHANGED,
        ),
      )
    }

    @Test
    fun testDeleteNotifications() {
      jdbcTemplate.update("$INSERT (101, 1147, 1000, '2022-03-21', SYSDATE,     47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (102, 1148, 4000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (103, 1149, 1000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")

      webTestClient.put()
        .uri("/updates/1")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""[101,103,999]""")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .consumeWith { c -> assertThat(c.responseBody!!.contains("Received 3 ids, time taken ")).isTrue() }

      assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList().containsExactly(102L)
    }

    @Test
    fun testDeleteAllNotifications() {
      jdbcTemplate.update("$INSERT (101, 1147, 1000, '2022-03-21', SYSDATE,     47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (102, 1148, 4000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")

      webTestClient.put()
        .uri("/updates/delete-all/1")
        .headers(setAuthorisation(roles = ADMIN_ROLE))
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .consumeWith { c -> assertThat(c.responseBody!!.contains("Deleted 2 rows, time taken ")).isTrue() }

      assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList().hasSize(0)
    }

    @Test
    fun testDeleteAllNotificationsRegion2() {
      jdbcTemplate.update("$INSERT (101, 1147, 1000, '2022-03-21', SYSDATE,     47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERTR2 (101, 1147, 1000, '2022-03-21', SYSDATE,     47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERTR2 (102, 1148, 4000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")

      webTestClient.put()
        .uri("/updates/delete-all/2")
        .headers(setAuthorisation(roles = ADMIN_ROLE))
        .exchange()
        .expectStatus().isOk

      assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList().hasSize(1)
      assertThat(jdbcTemplate.query("SELECT ID FROM R2.CMD_NOTIFICATION", testRowMapper)).asList().hasSize(0)
    }

    @Test
    fun testDeleteOld() {
      jdbcTemplate.update("$INSERT (101, 1147, 1000, '2022-01-01', to_date('2022-03-01 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (102, 1147, 1000, '2022-01-01', to_date('2022-03-02 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (103, 1147, 1000, '2022-01-01', to_date('2022-03-03 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
      jdbcTemplate.update("$INSERT (104, 1148, 4000, '2022-01-01', to_date('2022-03-04 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")

      webTestClient.put()
        .uri("/updates/delete-old/1?date=2022-03-03")
        .headers(setAuthorisation(roles = ADMIN_ROLE))
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .consumeWith { c -> assertThat(c.responseBody!!.contains("Deleted 2 rows up to 2022-03-03, time taken ")).isTrue() }

      assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList()
        .containsExactly(103L, 104L)
    }

    @Test
    fun testDeleteOldInvalid() {
      webTestClient.put()
        .uri("/updates/delete-old/1")
        .headers(setAuthorisation(roles = ADMIN_ROLE))
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun testDeleteOldNoRole() {
      webTestClient.put()
        .uri("/updates/delete-old/1?date=2022-03-03")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testDeleteAllNoRole() {
      webTestClient.put()
        .uri("/updates/delete-all/1")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
    }
  }
}
