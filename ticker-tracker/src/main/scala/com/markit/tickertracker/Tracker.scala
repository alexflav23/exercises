package com.markit.tickertracker

import java.time.{LocalDate, ZoneOffset}
import java.util.logging.Logger

import com.twitter.finagle.http.Response
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.Future

class Ticker(val value: String)

object Ticker {
  case object GOOG extends Ticker("GOOG")
}

trait Tracker {

  def logger: Logger = Logger.getLogger("tracker")

  private[this] def rootUrl: String = "real-chart.finance.yahoo.com:80"

  def client: Service[http.Request, http.Response] = Http.newService(rootUrl)

  def pricesURL(
    ticker: Ticker,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Response] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"""
         |$rootUrl/table.csv
         |?s=${ticker.value}&a=${lastYear.getMonthValue}
         |&b=${lastYear.getDayOfMonth}
         |&c=${lastYear.getYear}
         |&d=${today.getMonthValue}
         |&e=${today.getDayOfMonth}
         |&f=${today.getYear}
         |&g=d&ignore=.csv"
       """.stripMargin

    logger.info()
    val req = http.Request(http.Method.Get, url)
    client.apply(req)
  }

  def dailyPrices(ticker: String): Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def returns(ticker:String) : Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def meanReturn(ticker:String): Future[BigDecimal] = Future.value(BigDecimal(0))

  val googleDailyPrices = dailyPrices("GOOG")
  val googleDailyReturns = returns("GOOG")
  val googleAverageReturns = meanReturn("GOOG")
}

object Tracker extends Tracker