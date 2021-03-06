package com.kenmcgowan.narrativeio.analytics.tests

import com.kenmcgowan.narrativeio.analytics._
import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

class WebAnalyticsRestEndpointsTests
  extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with WebAnalyticsRestEndpointsModule
  with WebAnalyticsServiceModule
  with WebAnalyticsRepositoryModule {

  object DummyService extends WebAnalyticsService {
    val dummyMeasurements = List(
      Measurement("clicks", 2468),
      Measurement("impressions", 13579),
      Measurement("unique_users", 149))

    override def registerClick(epochMillis: Long, userId: String): Unit = {}
    override def registerImpression(epochMillis: Long, userId: String): Unit = {}
    override def getMeasurementsForHour(epochMillis: Long): Iterable[Measurement] = dummyMeasurements
  }

  val webAnalyticsService = DummyService
  val webAnalyticsRepository = null // Not needed since the dummy service does nothing.

  "WebAnalyticsRestEndpointsModule" should {

    "Respond to a click POST with status 202" in {
      Post("/analytics?timestamp=123&user=someone&event=click") ~> route ~> check {
          status shouldEqual StatusCodes.NoContent
      }
    }

    "Respond to an impression POST with status 202" in {
      Post("/analytics?timestamp=123&user=someone&event=impression") ~> route ~> check {
          status shouldEqual StatusCodes.NoContent
      }
    }

    "Respond to a GET with status 200 and the correct response text" in {
      Get("/analytics?timestamp=112358") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "clicks,2468\nimpressions,13579\nunique_users,149\n"
      }
    }

    "Reject a GET without the correct parameters" in {
      Get("/analytics") ~> route ~> check {
        handled shouldEqual false
      }
    }

    "Reject a POST without the correct parameters" in {
      Post("/analytics") ~> route ~> check {
        handled shouldEqual false
      }
    }

    "Reject a GET on the root URL" in {
      Get() ~> route ~> check {
        handled shouldEqual false
      }
    }

    "Reject a POST on the root URL" in {
      Post() ~> route ~> check {
        handled shouldEqual false
      }
    }

    "Handle a GET with unknown additional parameters" in {
      Get("/analytics?timestamp=1925&unknown_extra_param=true") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "clicks,2468\nimpressions,13579\nunique_users,149\n"
      }
    }

    "Handle a POST with unknown additional parameters" in {
      Post("/analytics?timestamp=1925&user=someone&event=click&unknown_extra_param=true") ~> route ~> check {
        status shouldEqual StatusCodes.NoContent
      }
    }
  }
}
