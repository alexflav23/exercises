package com.markit.tickertracker

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.data.ValidatedNel
import com.outworkers.util.catsparsers.{parse => cparse, _}
import com.outworkers.util.validators.dsl._

import scala.util.Try

// Date,Open,High,Low,Close,Volume,Adj Close
// 2017-02-08,807.00,811.840027,803.190002,808.380005,1155300,808.380005
case class TickerValue(
  open: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  close: BigDecimal,
  volume: Long,
  adjClose: BigDecimal,
  date: LocalDate
)

object TickerValue {
  val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit object BigDecimalParser extends Parser[BigDecimal] {
    override def parse(str: String): ValidatedNel[String, BigDecimal] = {
      Try(BigDecimal(str)).asValidation
    }
  }

  implicit object LocalDateParser extends Parser[LocalDate] {
    override def parse(str: String): ValidatedNel[String, LocalDate] = {
      Try(LocalDate.parse(str, dateFormat)).asValidation
    }
  }

  implicit object TickerValueParser extends BiParser[Seq[String], TickerValue] {
    override def parse(source: Seq[String]): Nel[TickerValue] = {
      source(5)
      cparse[LocalDate](source.option(0)).prop("date") and
        cparse[BigDecimal](source.option(1)).prop("open") and
        cparse[BigDecimal](source.option(1)).prop("high") and
        cparse[BigDecimal](source.option(1)).prop("low") and
        cparse[BigDecimal](source.option(1)).prop("close") and
        cparse[Long](source.option(1)).prop("volume") and
        cparse[BigDecimal](source.option(1)).prop("adj_close") map {
          case (dt, open, high, low, close, volume, adjClose) => TickerValue(open, high, low, close, volume, adjClose, dt)
        }
    }
  }
}