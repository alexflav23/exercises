package com.markit.tickertracker

import java.time.{LocalDate, Period, ZoneOffset}
import java.util.concurrent.TimeUnit

import cats.data.Validated.Valid
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
    val now = LocalDate.now(ZoneOffset.UTC)
    val request = Tracker
      .pricesURL(TickerSymbol.GOOG, now.minus(Period.ofDays(30)), now)

    request.successful { csvs =>
      csvs.foreach { el =>
        Console.println(el)
        el.isValid shouldEqual true
        el.toOption.value.trace()
      }
    }

  }
}
