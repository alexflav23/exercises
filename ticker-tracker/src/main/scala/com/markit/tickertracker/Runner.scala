package com.markit.tickertracker

import java.time.{Duration, LocalDate, Period, ZoneOffset}

import com.twitter.util.Await
import com.twitter.conversions.time._

object Runner extends App {

  val now = LocalDate.now(ZoneOffset.UTC)
  val request = Tracker
    .pricesURL(Ticker.GOOG, now.minus(Period.ofDays(30)), now)

  val res = Await.result(request, 30.seconds)

  Console.println(res.getStatusCode())
  Console.println(res.contentString)
}
