package com.markit.tickertracker

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.time.{LocalDate, Period, ZoneOffset}
import java.util.logging.Logger

import cats.data.ValidatedNel
import com.github.tototoshi.csv.CSVReader
import com.outworkers.util.catsparsers._
import com.twitter.finagle.http.Status
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.Buf
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
  ): Future[Iterator[ValidatedNel[String, DailyValue]]] = httpResponse match {
    case response if response.status != Status.Ok => Future.exception(new Exception(s"Status code ${response.status}"))
    case resp => resp.reader.read(Int.MaxValue) map {
      case Some(buf) if !buf.isEmpty =>
        val arr = Buf.ByteArray.Owned.extract(buf)
        CSVReader.open(new InputStreamReader(new ByteArrayInputStream(arr))).iterator.drop(1).map { seq =>
          biparse[Seq[String], DailyValue](seq)
        }

      case None => Iterator("Unable to extract a byte buffer from source".invalidNel[DailyValue])
    }
  }

  def pricesURL(
    ticker: TickerSymbol,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[ValidatedNel[String, DailyValue]]] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"http://$host/table.csv?s=${ticker.value}&a=${businessDate.getMonthValue}&b=${businessDate.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"

    logger.info(s"Making a request to $url")
    val req = http.Request(http.Version.Http11, http.Method.Get, url)
    req.setContentType("application/csv")
    req.headerMap.add("User-Agent", "Finagle 0.0")
    req.headerMap.add("Host", host)

    client(req) flatMap parseResponse
  }

  def tickerPrices(
    tk: TickerSymbol,
    bd: LocalDate,
    td: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[DailyValue]] = {
    pricesURL(tk, bd, td) map (_.flatMap(_.toOption))
  }

  /**
    * Returns a list of historical prices for one year ending with today.
    * @param ticker The symbol to retrieve the daily returns for.
    * @return An iterator of price instants, a price associated with a [[java.time.LocalDate]].
    */
  def dailyPrices(ticker: TickerSymbol): Future[Seq[PriceInstant]] = {
    tickerPrices(ticker, LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(1))) map { col =>
      col.map(daily => PriceInstant(daily.date, daily.adjClose)).toSeq.sortBy(_.date)
    }
  }

  /**
    * Computes the returns of a symbol using the (PRICE_TODAY - PRICE_YESTERDAY) / PRICE_YESTERDAY
    * @param ticker The symbol to compute the returns for.
    * @return An iterator of price instants, a price associated with a [[java.time.LocalDate]].
    */
  def returns(ticker: TickerSymbol) : Future[Iterator[PriceInstant]] = {
    tickerPrices(ticker, LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(1))) map {
      iterator => iterator.sliding(2) map {
        case Seq(yesterday, today) => PriceInstant(
          today.date,
          (today.adjClose - yesterday.adjClose).abs / yesterday.adjClose
        )
      }
    }
  }

  /**
    * Returns a MEDIAN value based on the daily high low price of a symbol.
    * We cannot from the data provided by YAHOO finance infer the mean.
    * @param ticker The symbol to compute the median return for.
    * @return A future wrapping the total number.
    */
  def medianReturn(ticker: TickerSymbol): Future[BigDecimal] = {
    tickerPrices(ticker, LocalDate.now(ZoneOffset.UTC)) map (_.map(Computation.MedianPrice).sum)
  }
}

object Tracker extends Tracker