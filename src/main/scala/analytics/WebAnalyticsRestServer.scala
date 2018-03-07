package com.kenmcgowan.narrativeio.analytics

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.event.LoggingAdapter
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import com.typesafe.config.Config

trait WebAnalyticsRestServerModule {
  this: WebAnalyticsRestEndpointsModule =>

  implicit val actorSystem: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContextExecutor
  val config: Config
  val logger: LoggingAdapter

  //shutdown Hook
  scala.sys.addShutdownHook {
    logger.info("Shutting down...")
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 10.seconds)
    logger.info("Shutdown complete.")
  }

  def start(): Unit = {
    val address: String = config.getString("http.address")
    val port:Int = config.getInt("http.port")

    logger.info(s"Starting HTTP server on ${address} port ${port}")
    Http().bindAndHandle(route, address, port)
  }
}
