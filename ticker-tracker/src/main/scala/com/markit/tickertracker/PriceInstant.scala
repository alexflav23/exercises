package com.markit.tickertracker

import java.time.LocalDate

case class PriceInstant(
  date: LocalDate,
  value: BigDecimal
)

