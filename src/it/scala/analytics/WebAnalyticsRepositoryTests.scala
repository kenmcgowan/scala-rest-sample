package com.kenmcgowan.narrativeio.analytics

import com.kenmcgowan.narrativeio._
import scalikejdbc._

class PostgresqlWebAnalyticsRepositoryTestFixture extends TestFixtureModule with WebAnalyticsRepositoryModule {

  val webAnalyticsRepository = new PostgresqlWebAnalyticsRepository

  behavior of "WebAnalyticsRepository"

  it should "insert a click" in {
    Given("there are no clicks or impressions in the store")
    NamedDB('analytics) localTx { implicit session => sql"DELETE FROM web.clicks; DELETE FROM web.impressions;".update.apply() }

    When("a click is stored at some epoch Hour for some user")
    val epochHour: Int = 7421
    val userId: String = "49276D20-4B65-6E20-4D63-476F77616E2E"
    webAnalyticsRepository.storeClick(epochHour, userId)

    Then("the analytics store should contain a single click with the given epoch hour and user ID")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT c.epoch_hour, c.user_id FROM web.clicks AS c"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).single.apply()
    } getOrElse (0 -> "") shouldEqual (epochHour -> userId)

    And("the analytics store should contain no impressions")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT i.epoch_hour, i.user_id FROM web.impressions AS i"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).single.apply()
    } shouldEqual None
  }

  it should "insert an impression" in {
    Given("there are no clicks or impressions in the store")
    NamedDB('analytics) localTx { implicit session => sql"DELETE FROM web.clicks; DELETE FROM web.impressions;".update.apply() }

    When("an impression is stored at some epoch Hour for some user")
    val epochHour: Int = 7421
    val userId: String = "49276D20-4B65-6E20-4D63-476F77616E2E"
    webAnalyticsRepository.storeImpression(epochHour, userId)

    Then("the analytics store should contain a single impression with the given epoch hour and user ID")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT i.epoch_hour, i.user_id FROM web.impressions AS i"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).single.apply()
    } getOrElse (0 -> "") shouldEqual (epochHour -> userId)

    And("the analytics store should contain no clicks")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT c.epoch_hour, c.user_id FROM web.clicks AS c"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).single.apply()
    } shouldEqual None
  }
}
