package com.kenmcgowan.narrativeio.analytics

import scalikejdbc._
import scalikejdbc.config._
import scalaz._
import Scalaz._

trait WebAnalyticsRepositoryModule {

  val webAnalyticsRepository: WebAnalyticsRepository

  case class Measure(name: String, value: Int)

  abstract class WebAnalyticsRepository {
    def storeClick(epochHour: Int, userId: String): Unit
    def storeImpression(epochHour: Int, userId: String): Unit
    def retrieveMeasures(epochHour: Int): Iterable[Measure]
  }

  class PostgresqlWebAnalyticsRepository extends WebAnalyticsRepository {

    DBs.setup('analytics)

    def storeClick(epochHour: Int, userId: String): Unit =
      NamedDB('analytics) localTx { implicit session =>
        sql"INSERT INTO web.clicks ( epoch_hour, user_id ) VALUES ( ${epochHour}, ${userId} )"
          .update
          .apply()
      }

    def storeImpression(epochHour: Int, userId: String): Unit =
      NamedDB('analytics) localTx { implicit session =>
        sql"INSERT INTO web.impressions ( epoch_hour, user_id ) VALUES ( ${epochHour}, ${userId} )"
          .update
          .apply()
      }

    def retrieveMeasures(epochHour: Int): Iterable[Measure] = {
      NamedDB('analytics) readOnly (implicit session =>
        sql"SELECT m.measure_name, m.measure_value FROM web.measures AS m WHERE m.epoch_hour = ${epochHour}"
          .map(rs => (rs.string("measure_name"), rs.int("measure_value")))
          .list.apply()
          .toMap |+| Map("clicks" -> 0, "impressions" -> 0, "unique_users" -> 0)
          map { case (k,v) => Measure(k, v) }
        )
    }
  }
}
