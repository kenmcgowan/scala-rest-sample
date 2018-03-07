package com.kenmcgowan.narrativeio.analytics

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import com.typesafe.config.Config

trait WebAnalyticsEndpointsModule {
  this: WebAnalyticsServiceModule with WebAnalyticsRepositoryModule =>

  val route = path ("analytics") {
    put {
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

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContextExecutor
  val config: Config

  def startup(): Future[Http.ServerBinding] = {
    Http().bindAndHandle(route, config.getString("http.address"), config.getInt("http.port"))
  }

  def shutdown(bindingFuture: Future[Http.ServerBinding]) {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
