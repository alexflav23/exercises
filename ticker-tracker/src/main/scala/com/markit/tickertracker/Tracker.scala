package com.markit.tickertracker

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.time.{LocalDate, Period, ZoneOffset}
import java.util.concurrent.ConcurrentHashMap
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

  def cache: ConcurrentHashMap[PriceRequest, Iterator[DailyValue]] = new ConcurrentHashMap()

  private[this] def host: String = "real-chart.finance.yahoo.com:80"

  def client: Service[http.Request, http.Response] = Http.client.newService(host)

  protected[tickertracker] def parseResponse(
    httpResponse: http.Response
  ): Future[Iterator[ValidatedNel[String, DailyValue]]] = httpResponse match {
    case response if response.status != Status.Ok => Future.exception(new Exception(s"Invalid status code ${response.status}"))
    case resp => resp.reader.read(Int.MaxValue) map {
      case Some(buf) if !buf.isEmpty =>
        val arr = Buf.ByteArray.Owned.extract(buf)
        CSVReader.open(new InputStreamReader(new ByteArrayInputStream(arr))).iterator.drop(1).map { seq =>
          biparse[Seq[String], DailyValue](seq)
        }
      case None => Iterator("Unable to extract a byte buffer from source".invalidNel[DailyValue])
    }
  }

  protected[this] def prices(
    ticker: TickerSymbol,
    businessDate: LocalDate,
    today: LocalDate = LocalDate.now(ZoneOffset.UTC)
  ): Future[Iterator[ValidatedNel[String, DailyValue]]] = {
    val lastYear = businessDate.minusYears(1)
    val url = f"http://$host/table.csv?s=${ticker.value}&a=${businessDate.getMonthValue}&b=${businessDate.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"

    logger.info(s"Making a request to $url")
    val req = http.Request(http.Method.Get, url)
    req.setContentType("application/csv")
    req.headerMap.add("User-Agent", "Finagle 0.0")
    req.headerMap.add("Host", host)

    client(req) flatMap parseResponse
  }

  protected[this] def prices(req: PriceRequest): Future[Iterator[ValidatedNel[String, DailyValue]]] = {
    prices(req.tickerSymbol, req.businessDate, req.today)
  }

  def daily(
    req: PriceRequest
  ): Future[Iterator[DailyValue]] = {
    if (cache.containsKey(req)) {
      Future.value(cache.get(req))
    } else {
      val f = prices(req) map (_.flatMap(_.toOption))
      f onSuccess (data => cache.put(req, data))
      f
    }
  }

  /**
    * Returns a list of historical prices for one year ending with today.
    * @param ticker The symbol to retrieve the daily returns for.
    * @return An iterator of price instants, a price associated with a [[java.time.LocalDate]].
    */
  def dailyPrices(ticker: TickerSymbol): Future[Seq[PriceInstant]] = {
    daily(PriceRequest(ticker)) map { col =>
      col.map(daily => PriceInstant(daily.date, daily.adjClose)).toSeq.sortBy(_.date)
    }
  }

  /**
    * Computes the returns of a symbol using the (PRICE_TODAY - PRICE_YESTERDAY) / PRICE_YESTERDAY
    * @param ticker The symbol to compute the returns for.
    * @return An iterator of price instants, a price associated with a [[java.time.LocalDate]].
    */
  def returns(ticker: TickerSymbol) : Future[List[PriceInstant]] = {
    daily(PriceRequest(ticker)) map {
      iterator => iterator.toList.sortBy(_.date).sliding(2).foldLeft(List.empty[PriceInstant]) { case (acc, el) =>
        el match {
          case yesterday :: today :: Nil => PriceInstant(
            today.date,
            (today.adjClose - yesterday.adjClose).abs / yesterday.adjClose
          ) :: acc

          case _ => acc
        }
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
    daily(PriceRequest(ticker)) map (_.map(Computation.MedianPrice).sum)
  }
}

object Tracker extends Tracker