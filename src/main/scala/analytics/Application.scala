package com.kenmcgowan.narrativeio.analytics

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object Application extends App
  with WebAnalyticsRestServerModule
  with WebAnalyticsRestEndpointsModule
  with WebAnalyticsServiceModule
  with WebAnalyticsRepositoryModule {

  val webAnalyticsRepository: WebAnalyticsRepository = new PostgresqlWebAnalyticsRepository()
  val webAnalyticsService: WebAnalyticsService = new WebAnalyticsService()
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  val config = ConfigFactory.load()
  val logger = Logging(actorSystem, getClass)

  start()
}
