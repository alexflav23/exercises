package com.markit.tickertracker

import java.time.LocalDate

case class PriceInstant(
  date: LocalDate,
  value: BigDecimal
)



trait PriceComputation extends (TickerValue => BigDecimal)

object Computation {
  case object MedianPrice extends PriceComputation
}