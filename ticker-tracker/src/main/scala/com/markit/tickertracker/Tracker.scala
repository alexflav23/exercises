package com.markit.tickertracker

import java.time.{LocalDate, ZoneOffset}
import java.util.logging.Logger

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future

class TickerSymbol(val value: String)

object TickerSymbol {
  case object GOOG extends TickerSymbol("GOOG")
}

trait Tracker {

  def logger: Logger = Logger.getLogger("tracker")

  private[this] def host: String = "real-chart.finance.yahoo.com:80"

  def client: Service[http.Request, http.Response] = Http.newService(host)


  def fromReader(reader: Reader): AsyncStream[Buf] =
    AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
      case None => AsyncStream.empty
      case Some(a) => a +:: fromReader(reader)
    }

  def pricesURL(
    ticker: TickerSymbol,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[TickerValue]] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"http://$host/table.csv?s=${ticker.value}&a=${businessDate.getMonthValue}&b=${businessDate.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"

    logger.info(s"Making a request to $url")
    val req = http.Request(http.Version.Http11, http.Method.Get, url)
    req.setContentType("application/csv")
    req.headerMap.add("User-Agent", "Finagle 0.0")
    req.headerMap.add("Host", host)


    client(req) flatMap {
      case response if response.status != Status.Ok => Future.exception(new Exception(s"Status code ${response.status}"))
      case resp => resp.contentString
    }
  }

  def dailyPrices(ticker: String): Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def returns(ticker:String) : Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def meanReturn(ticker:String): Future[BigDecimal] = Future.value(BigDecimal(0))

  val googleDailyPrices = dailyPrices("GOOG")
  val googleDailyReturns = returns("GOOG")
  val googleAverageReturns = meanReturn("GOOG")
}

object Tracker extends Tracker