package com.ontrack.exercise

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Futures {

  val f1: Future[Unit] = Future.successful(())
  val f2: Future[Unit] = Future.successful(())
  val f3: Future[Unit] = Future.successful(())
  val f4: Future[Unit] = Future.successful(())

  // no dependencies
  Future.sequence(Seq(f1, f2, f3, f4))

  // linear dependency
  val chain = for {
    _ <- f1
    _ <- f2
    _ <- f3
    _ <- f4
  } yield ()

  // complex dependnency
  val chain2 = for {
    _ <- f1
    _ <- Future.sequence(List(f2, f3))
    _ <- f4
  } yield ()

}
