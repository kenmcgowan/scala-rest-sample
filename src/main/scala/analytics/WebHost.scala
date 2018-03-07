package com.kenmcgowan.narrativeio.analytics

import scala.io.StdIn

object App {
  def main(args: Array[String]): Unit = {
    object WebServer
      extends WebAnalyticsEndpointsModule
      with WebAnalyticsServiceModule
      with WebAnalyticsRepositoryModule {

      val webAnalyticsRepository: WebAnalyticsRepository = new PostgresqlWebAnalyticsRepository()
      val webAnalyticsService: WebAnalyticsService = new WebAnalyticsService()
    }

    val bindingFuture = WebServer.startup()

    println("Server online, press RETURN to stop…")
    StdIn.readLine()

    WebServer.shutdown(bindingFuture)
  }
}
