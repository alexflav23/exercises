package com.markit.tickertracker

import java.time.{LocalDate, Period, ZoneOffset}
import java.util.concurrent.TimeUnit

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import com.outworkers.util.testing._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.{Millis, Seconds, Span}

class TrackerTest extends FlatSpec with Matchers with OptionValues {

  protected[this] val defaultScalaTimeoutSeconds = 25

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout = scala.concurrent.duration.Duration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  implicit val patienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  it should "retrieve a basic set of information from Yahoo" in {
    val request = Tracker.daily(PriceRequest(TickerSymbol.GOOG))

    request.successful { prices =>

    }
  }

  it should "retrieve a set of daily values for the GOOG symbol" in {
    val googleDailyPrices = Tracker.dailyPrices(TickerSymbol.GOOG)

    googleDailyPrices.successful { res =>
      res.foreach(r => println(r.trace()))
    }
  }

  it should "retrieve the median value of a year for the GOOG symbol" in {
    val googMedianPrice = Tracker.medianReturn(TickerSymbol.GOOG)

    googMedianPrice.successful { res =>
      info(res.toString())
    }
  }
}
