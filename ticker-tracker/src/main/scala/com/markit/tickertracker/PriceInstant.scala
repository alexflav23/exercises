package com.markit.tickertracker

import java.time.{LocalDate, ZoneOffset}

case class PriceRequest(
  tickerSymbol: TickerSymbol,
  businessDate: LocalDate,
  today: LocalDate
)

object PriceRequest {
  def apply(tickerSymbol: TickerSymbol): PriceRequest = {
    val now = LocalDate.now(ZoneOffset.UTC)
    PriceRequest(tickerSymbol, now, now)
  }
}

case class PriceInstant(
  date: LocalDate,
  value: BigDecimal
)

trait PriceComputation extends (DailyValue => BigDecimal)

object Computation {
  object MedianPrice extends PriceComputation {
    override def apply(v1: DailyValue): BigDecimal = (v1.high + v1.low) / 2
  }
}
