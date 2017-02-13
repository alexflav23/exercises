package com.markit.tickertracker

import java.time.LocalDate

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