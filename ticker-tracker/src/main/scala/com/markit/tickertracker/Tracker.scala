package com.markit.tickertracker

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.time.{LocalDate, ZoneOffset}
import java.util.logging.Logger

import cats.data.Validated.Invalid
import cats.data.{NonEmptyList, ValidatedNel}
import com.github.tototoshi.csv.CSVReader
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Status
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import com.outworkers.util.catsparsers._
import com.outworkers.util.validators.dsl._

class TickerSymbol(val value: String)

object TickerSymbol {
  case object GOOG extends TickerSymbol("GOOG")
}

trait Tracker {

  type Out = ValidatedNel[String, TickerValue]

  def logger: Logger = Logger.getLogger("tracker")

  private[this] def host: String = "real-chart.finance.yahoo.com:80"

  def client: Service[http.Request, http.Response] = Http.client.withStreaming(enabled = true).newService(host)

  def fromReader(reader: Reader): AsyncStream[Buf] =
    AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
      case None => AsyncStream.empty
      case Some(a) => a +:: fromReader(reader)
    }

  def pricesURL(
    ticker: TickerSymbol,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[Out]] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"http://$host/table.csv?s=${ticker.value}&a=${businessDate.getMonthValue}&b=${businessDate.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"

    logger.info(s"Making a request to $url")
    val req = http.Request(http.Version.Http11, http.Method.Get, url)
    req.setContentType("application/csv")
    req.headerMap.add("User-Agent", "Finagle 0.0")
    req.headerMap.add("Host", host)

    client(req) flatMap {
      case response if response.status != Status.Ok => Future.exception(new Exception(s"Status code ${response.status}"))
      case resp => resp.reader.read(Int.MaxValue) map {
        case Some(buf) =>
          val arr = Buf.ByteArray.Owned.extract(buf)
          CSVReader.open(new InputStreamReader(new ByteArrayInputStream(arr))).iterator.map { seq =>
            biparse[Seq[String], TickerValue](seq)
          }

        case None => Iterator("Unable to extract a byte buffer from source".invalidNel[TickerValue])
      }
    }
  }

  def dailyPrices(ticker: TickerSymbol): Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def returns(ticker: TickerSymbol) : Future[Iterator[BigDecimal]] = Future.value(Iterator.empty)

  def meanReturn(ticker: TickerSymbol): Future[BigDecimal] = Future.value(BigDecimal(0))

  val googleDailyPrices = dailyPrices(TickerSymbol.GOOG)
  val googleDailyReturns = returns(TickerSymbol.GOOG)
  val googleAverageReturns = meanReturn(TickerSymbol.GOOG)
}

object Tracker extends Tracker