package com.markit

import java.time.LocalDate

package object tickertracker {
  implicit class TraversableIndex[T](val col: Seq[T]) extends AnyVal {
    def option(index: Int): Option[T] = if (col.isDefinedAt(index)) Some(col(index)) else None
  }

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }
}
