package com.kenmcgowan.narrativeio.analytics.integrationtests

import com.kenmcgowan.narrativeio.analytics._
import scalikejdbc._

class PostgresqlWebAnalyticsRepositoryTestFixture extends TestFixtureModule with WebAnalyticsRepositoryModule {
  import PostgresqlWebAnalyticsRepositoryTestFixture._

  val webAnalyticsRepository = new PostgresqlWebAnalyticsRepository

  behavior of "WebAnalyticsRepository"

  it should "insert a click" in {
    Given("there are no clicks or impressions in the store")
    removeAllClicks
    removeAllImpressions

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
    removeAllClicks
    removeAllImpressions

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

  it should "allow two clicks with the same values" in {
    Given("there are no clicks or impressions in the store")
    removeAllClicks
    removeAllImpressions

    When("two subsequent clicks with the same parameters are stored")
    val epochHour: Int = 2468
    val userId: String = "some user"
    webAnalyticsRepository.storeClick(epochHour, userId)
    webAnalyticsRepository.storeClick(epochHour, userId)

    Then("the store should contain both clicks")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT c.epoch_hour, c.user_id FROM web.clicks AS c"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).list.apply()
    } shouldEqual List((epochHour, userId), (epochHour, userId))
  }

  it should "allow two impressions with the same values" in {
    Given("there are no clicks or impressions in the store")
    removeAllClicks
    removeAllImpressions

    When("two subsequent impressions with the same parameters are stored")
    val epochHour: Int = 2468
    val userId: String = "some user"
    webAnalyticsRepository.storeImpression(epochHour, userId)
    webAnalyticsRepository.storeImpression(epochHour, userId)

    Then("the store should contain both impressions")
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT i.epoch_hour, i.user_id FROM web.impressions AS i"
        .map(rs => rs.int("epoch_hour") -> rs.string("user_id")).list.apply()
    } shouldEqual List((epochHour, userId), (epochHour, userId))
  }

  it should "retrieve the correct measurements based on clicks, impressions, and users" in {
    Given("the store contains some number of clicks and impressions")
    val uniqueUsers = "user1" :: "user2" :: "user3" :: "user4" :: "user5" :: "user6" :: "user7" :: Nil
    removeAllClicks
    removeAllImpressions
    storeRandomClicks(
      n = 100,
      minEpochHourInclusive = 0,
      maxEpochHourExclusive = 100,
      userIds = uniqueUsers)
    storeRandomImpressions(
      n = 100,
      minEpochHourInclusive = 0,
      maxEpochHourExclusive = 100,
      userIds = uniqueUsers)

    And("the store contains a known numbers of clicks, impressions, and unique users for a given epoch hour")
    val clickCount = 237
    val impressionCount = 492
    val epochHour = 1000
    storeRandomClicks(
      n = clickCount,
      minEpochHourInclusive = epochHour,
      maxEpochHourExclusive = epochHour + 1,
      userIds = uniqueUsers
    )
    storeRandomImpressions(
      n = impressionCount,
      minEpochHourInclusive = epochHour,
      maxEpochHourExclusive = epochHour + 1,
      userIds = uniqueUsers
    )

    When("measurements are retrieved")
    val measurements = webAnalyticsRepository.retrieveMeasurements(epochHour).map(m => m.name -> m).toMap

    Then("the measurements indicates the correct number of clicks for the given epoch hour")
    measurements("clicks").value shouldEqual (clickCount)

    And("the measurements indicate the correct number of impressions for the given epoch hour")
    measurements("impressions").value shouldEqual (impressionCount)

    And("the measurements should indicate the correct number of unique users")
    measurements("unique_users").value shouldEqual(uniqueUsers.size)
  }
}

object PostgresqlWebAnalyticsRepositoryTestFixture extends TestFixtureModule {
  import scala.util.Random

  def removeAllClicks(): Unit = {
    NamedDB('analytics) localTx { implicit session => sql"DELETE FROM web.clicks".update.apply() }
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT COUNT(*) AS rowcount FROM web.clicks"
      .map(rs => rs.int("rowcount")).single.apply()
    } getOrElse(-1) shouldEqual (0)
  }

  def removeAllImpressions(): Unit = {
    NamedDB('analytics) localTx { implicit session => sql"DELETE FROM web.impressions".update.apply() }
    NamedDB('analytics) readOnly { implicit session =>
      sql"SELECT COUNT(*) AS rowcount FROM web.impressions"
      .map(rs => rs.int("rowcount")).single.apply()
    } getOrElse(-1) shouldEqual (0)
  }

  def storeRandomClicks(
    n: Int,
    minEpochHourInclusive: Int,
    maxEpochHourExclusive: Int,
    userIds: Seq[String]) : Unit = {
      val random = Random
      Stream.continually(userIds.toStream).flatten.take(n).foreach { (userId: String) => {
        NamedDB('analytics) localTx { implicit session =>
          sql"INSERT INTO web.clicks ( epoch_hour, user_id ) VALUES ( ${minEpochHourInclusive + random.nextInt(maxEpochHourExclusive - minEpochHourInclusive)}, ${userId})"
          .update.apply()
        }
      }
    }
  }

  def storeRandomImpressions(
    n: Int,
    minEpochHourInclusive: Int,
    maxEpochHourExclusive: Int,
    userIds: Seq[String]) : Unit = {
      val random = Random
      Stream.continually(userIds.toStream).flatten.take(n).foreach { (userId: String) => {
        NamedDB('analytics) localTx { implicit session =>
          sql"INSERT INTO web.impressions ( epoch_hour, user_id ) VALUES ( ${minEpochHourInclusive + random.nextInt(maxEpochHourExclusive - minEpochHourInclusive)}, ${userId})"
          .update.apply()
        }
      }
    }
  }
}
