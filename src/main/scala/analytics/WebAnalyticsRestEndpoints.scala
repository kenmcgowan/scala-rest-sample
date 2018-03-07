package com.kenmcgowan.narrativeio.analytics

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._

trait WebAnalyticsRestEndpointsModule {
  this: WebAnalyticsServiceModule with WebAnalyticsRepositoryModule =>

  val route = path ("analytics") {
    post {
      parameters('timestamp.as[Long], 'user) { (epochMillis, userId) =>
        parameters('event ! "click") {
          webAnalyticsService.registerClick(epochMillis, userId)
          complete(StatusCodes.NoContent)
        } ~
        parameters('event ! "impression") {
          webAnalyticsService.registerImpression(epochMillis, userId)
          complete(StatusCodes.NoContent)
        }
      }
    } ~
    get {
      parameters('timestamp.as[Long]) { (epochMillis) =>
        complete(webAnalyticsService.getMeasurementsForHour(epochMillis)
          .foldLeft("") { (s: String, m: Measurement) => s"$s${m.name},${m.value}\n" })
      }
    }
  }
}
