package com.joprice

import shapeless._
import syntax.std.function._
import ops.function._
import ops.hlist._
import poly._
import syntax.singleton._

trait LowPriorityWidenCases extends Poly1 {
  implicit def default[T] = at[T](identity)
}

// widen singleton-typed literals
object widen extends Poly1 with LowPriorityWidenCases {
  implicit def widenable[T](implicit w: Widen[T]): Case.Aux[T, w.Out] = at[T](w(_))
}

abstract class Overload extends ProductArgs {
  trait Case[T <: HList] {
    type Out
    def apply(t: T): Out
  }

  object Case {
    def apply[F, L <: HList, R](f: F)(implicit ftp: FnToProduct.Aux[F, L => R]) = new Case[L] {
      type Out = R
      def apply(t: L): R = f.toProduct(t)
    }
  }

  def applyProduct[In <: HList, Out <: HList](args: In)(
    implicit
    mapper: Mapper.Aux[widen.type, In, Out],
    c: Case[Out]
  ): c.Out = c(args.map(widen))
}


object Overload {

  object overloaded extends Overload {
    implicit val intIntCase = Case((a: Int, b: Int) => a + b)
    implicit val stringCase = Case((b: String, i: Int) => s"$b: $i")
    implicit val optStringCase = Case((t: Option[String]) => t.get)
    implicit val optIntCase = Case((t: Option[Int]) => t.get)
  }

  def main(args: Array[String]): Unit = {
    println(overloaded(1, 2): Int)
    println(overloaded(1: Int, 2: Int): Int)
    println(overloaded(1, 2: Int): Int)
    println(overloaded("a", 2: Int): String)
    println(overloaded("result": String, 2: Int): String)
  }
}
