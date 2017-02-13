package com.markit

package object tickertracker {
  implicit class TraversableIndex[T](val col: Seq[T]) extends AnyVal {
    def option(index: Int): Option[T] = if (col.isDefinedAt(index)) Some(col(index)) else None
  }
}
