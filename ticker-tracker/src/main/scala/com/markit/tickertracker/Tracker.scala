package com.markit.tickertracker

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.time.{LocalDate, ZoneOffset}
import java.util.logging.Logger

import cats.data.ValidatedNel
import com.github.tototoshi.csv.CSVReader
import com.outworkers.util.catsparsers._
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Status
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

  def client: Service[http.Request, http.Response] = Http.client.withStreaming(enabled = true).newService(host)


  protected[tickertracker] def parseResponse(
    httpResponse: http.Response
  ): Future[Iterator[ValidatedNel[String, TickerValue]]] = httpResponse match {
    case response if response.status != Status.Ok => Future.exception(new Exception(s"Status code ${response.status}"))
    case resp => resp.reader.read(Int.MaxValue) map {
      case Some(buf) if buf.length > 100 =>
        val arr = Buf.ByteArray.Owned.extract(buf)
        CSVReader.open(new InputStreamReader(new ByteArrayInputStream(arr))).iterator.drop(1).map { seq =>
          biparse[Seq[String], TickerValue](seq)
        }

      case None => Iterator("Unable to extract a byte buffer from source".invalidNel[TickerValue])
    }
  }

  def pricesURL(
    ticker: TickerSymbol,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[ValidatedNel[String, TickerValue]]] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"http://$host/table.csv?s=${ticker.value}&a=${businessDate.getMonthValue}&b=${businessDate.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"

    logger.info(s"Making a request to $url")
    val req = http.Request(http.Version.Http11, http.Method.Get, url)
    req.setContentType("application/csv")
    req.headerMap.add("User-Agent", "Finagle 0.0")
    req.headerMap.add("Host", host)

    client(req) flatMap parseResponse
  }

  def correctPrices(
    tk: TickerSymbol,
    bd: LocalDate,
    td: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[TickerValue]] = {
    pricesURL(tk, bd, td) map (_.flatMap(_.toOption))
  }

  def dailyPrices(ticker: TickerSymbol): Future[Iterator[PriceInstant]] = {

  }

  def returns(ticker: TickerSymbol) : Future[Iterator[PriceInstant]] = {

  }

  def meanReturn(ticker: TickerSymbol): Future[BigDecimal] = {
    correctPrices(ticker, LocalDate.now(ZoneOffset.UTC)) map { values =>
      values.map(_.adjClose).sum
    }
  }

  val googleDailyPrices = dailyPrices(TickerSymbol.GOOG)
  val googleDailyReturns = returns(TickerSymbol.GOOG)
  val googleAverageReturns = meanReturn(TickerSymbol.GOOG)
}

object Tracker extends Tracker