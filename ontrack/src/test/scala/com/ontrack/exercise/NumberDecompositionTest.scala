package com.ontrack.exercise

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class NumberDecompositionTest extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 300)

  it should "compute a value of 0 for Nil" in {
    val input = Nil
    SeqNumericOps.addN(input) shouldBe empty
  }

  it should "compute a value of 0 for an empty sequence" in {
    val input = Seq.empty[Int]
    SeqNumericOps.addN(input) shouldBe empty
  }

  it should "compute a value of Seq(1) when the input is Seq(0)" in {
    val input = Seq(0)
    SeqNumericOps.addN(input) shouldBe Seq(1)
  }

  it should "compute a value of Seq(1, 2, 4) when the input is Seq(1, 2, 3)" in {
    val input = Seq(1, 2, 3)
    SeqNumericOps.addN(input) shouldBe Seq(1, 2, 4)
  }

  it should "compute a value of Seq(1, 0, 0, 0) when the input is Seq(9, 9, 9)" in {
    val input = Seq(9, 9, 9)
    SeqNumericOps.addN(input) shouldBe Seq(1, 0, 0, 0)
  }
}
