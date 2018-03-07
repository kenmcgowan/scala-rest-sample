package com.kenmcgowan.narrativeio.analytics

trait WebAnalyticsServiceModule {
  this: WebAnalyticsRepositoryModule =>

  import WebAnalyticsServiceModule._

  val webAnalyticsService : WebAnalyticsService

  class WebAnalyticsService {
    def registerClick(epochMillis: Long, userId: String): Unit = {
      webAnalyticsRepository.storeClick(epochMillis.toEpochHours, userId)
    }

    def registerImpression(epochMillis: Long, userId: String): Unit = {
      webAnalyticsRepository.storeImpression(epochMillis.toEpochHours, userId)
    }

    def getMeasurementsForHour(epochMillis: Long): Iterable[Measurement] = {
      webAnalyticsRepository.retrieveMeasurements(epochMillis.toEpochHours)
    }
  }
}

object WebAnalyticsServiceModule {
  implicit class EpochMSConverter(epochMillis: Long) {
    def toEpochHours(): Int = { (epochMillis / (60L * 60L * 1000L)).toInt }
  }
}
