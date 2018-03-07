package com.kenmcgowan.narrativeio.analytics

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Application extends App
  with WebAnalyticsEndpointsModule
  with WebAnalyticsServiceModule
  with WebAnalyticsRepositoryModule {

  val webAnalyticsRepository: WebAnalyticsRepository = new PostgresqlWebAnalyticsRepository()
  val webAnalyticsService: WebAnalyticsService = new WebAnalyticsService()
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load()

  val bindingFuture = startup()

  println("Server online, press RETURN to stop…")
  StdIn.readLine()

  shutdown(bindingFuture)
}
