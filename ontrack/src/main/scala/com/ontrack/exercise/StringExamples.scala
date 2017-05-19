package com.ontrack.exercise

object StringExamples {

  val myStringOption = Some("test")

  def transform1(str: Option[String]): String = {
    str match {
      case Some(value) => value + "test2"
      case None => "test2"
    }
  }

  def transform2(str: Option[String]): String = {
    str.fold("")(_ + "test2")
  }


  def transform3(str: Option[String], str2: Option[String]): Option[String] = {
    for {
      s1 <- str
      s2 <- str2
    } yield s1 + s2
  }
}