package com.kenmcgowan.narrativeio.analytics.tests

import com.kenmcgowan.narrativeio.analytics._

trait TestContext extends WebAnalyticsServiceModule with WebAnalyticsRepositoryModule {
  def op(sut: WebAnalyticsService): Unit
  def assert(epochHour: Int): Unit

  object MockRepository extends WebAnalyticsRepository {
    def storeClick(epochHour: Int, userId: String): Unit = assert(epochHour)
    def storeImpression(epochHour: Int, userId: String): Unit = assert(epochHour)
    def retrieveMeasurements(epochHour: Int): Iterable[Measurement] = {
      assert(epochHour)
      None
    }
  }

  val webAnalyticsRepository = MockRepository
  val webAnalyticsService = new WebAnalyticsService()

  def test(): Unit = op(webAnalyticsService)
}

class WebAnalyticsServiceTests
  extends TestFixtureModule {

  behavior of "WebAnalyticsService"

  it should "convert epoch milliseconds to epoch hours before storing clicks" in {
    Given("a click occurs at a given time measured in epoch milliseconds")
    val epochMillis: Long = (60L * 60L * 1000L)
    val expectedEpochHours: Int = 1
    val irrelevantUserId = "don't care"

    When("the click is registered")
    new TestContext() {
      def op(sut: WebAnalyticsService): Unit = sut.registerClick(epochMillis, irrelevantUserId)

      Then("the time value is converted to epoch hours in the call to the repository")
      def assert(epochHour: Int): Unit = { epochHour shouldEqual (expectedEpochHours) }
    }.test
  }

  it should "convert epoch milliseconds to epoch hours before storing impressions" in {
    Given("an impression occurs at a given time measured in epoch milliseconds")
    val epochMillis: Long = (60L * 60L * 1000L)
    val expectedEpochHours: Int = 1
    val irrelevantUserId = "don't care"

    When("the impression is registered")
    new TestContext() {
      def op(sut: WebAnalyticsService): Unit = sut.registerImpression(epochMillis, irrelevantUserId)

      Then("the time value is converted to epoch hours in the call to the repository")
      def assert(epochHour: Int): Unit = { epochHour shouldEqual (expectedEpochHours) }
    }.test
  }

  it should "convert epoch milliseconds to epoch hours before retrieving measurements" in {
    Given("measurements for a given time measured in epoch milliseconds are requested")
    val epochMillis: Long = (60L * 60L * 1000L)
    val expectedEpochHours: Int = 1

    When("the measurements are requested")
    new TestContext() {
      def op(sut: WebAnalyticsService): Unit = sut.getMeasurementsForHour(epochMillis)

      Then("the time value is converted to epoch hours in the call to the repository")
      def assert(epochHour: Int): Unit = { epochHour shouldEqual (expectedEpochHours) }
    }.test
  }
}
