package com.ontrack.exercise

object SeqNumericOps {

  /**
    * Adds a natural number to a sequence of digits.
    * @param s The input sequence of digits.
    * @param n The number representing the sum of the digits in the sequence.
    * @param num The number of add to the sequence.
    * @param steps The number of recursion steps undertaken so far, used to give us an exit condition.
    * @return A sequence of digits where the number num has been added according to the rules.
    */
  @annotation.tailrec
  final def addN(s: Seq[Int], n: Int = 1, num: Long = 0, steps: Int = 0): Seq[Int] = {
    s match {
      case Nil => if (steps > 0) num2Seq(num + n) else Seq.empty
      case h :: t => addN(
        t,
        n,
        h * math.pow(10, s.length - 1).toInt + num,
        steps + 1
      )
    }
  }

  /**
    * We use the fact that [[scala.collection.immutable.List]] extends [[scala.collection.immutable.Seq]]
    * to avoid having to call [[scala.collection.immutable.Seq.reverse]] at the end, because we can only
    * deconstruct a number from the last digit towards the first.
    *
    * Lists prepend by default in constant time, which means we can be more efficient.
    * @param num A source number.
    * @param col The final collection to build recursively.
    * @return
    */
  @annotation.tailrec
  def num2Seq(num: Long, col: List[Int] = List.empty): Seq[Int] = {
    if (num <= 0) {
      col
    } else {
      num2Seq(num / 10, (num % 10).toInt :: col)
    }
  }
}
