package com.rockthejvm.part1as

import scala.util.Try

class DarkSugars {

  // 1 - sugar for methods with one argument
  def singleArgMethod(arg: Int): Int = arg + 1

  val aMethodCall = singleArgMethod({
    // long code
    42
  })

  val aMethodCall_v2 = singleArgMethod {
    // long code
    42
  }

  // example: Try, Future
  val aTryInstance = Try{
    throw new RuntimeException
  }

  // with HOFs
  val anIncrementedList = List(1,2,3).map { x =>
    // code block for the whole lambda
    x + 1
  }

  // 2 - single abstract method pattern (since Scala 2.12)
  trait Action {
    def act(x: Int): Int
  }

  val anAction = new Action {
    // can also have other implemented fields/methods here
    override def act(x: Int) = x + 1
  }

  val anotherAction: Action = (x: Int) => x + 1 // new Action { def act(x: Int) = x + 1 }

  // example: Runnable
  val aThread = new Runnable {
    override def run(): Unit = println("Hi Scala, from another thread")
  }

  val aSweeterThread = new Thread(() => println("Hi, Scala"))

  // 3 - methods ending in a : are RIGHT-ASSOCIATIVE
  // the only condition for an operator or a method to be right associative
  // is that it must be non-alphanumerical and ending in a :
  val aList = List(1,2,3)
  val aPrependedList = 0 :: aList // 0.::(aList)
  val aThing = aList.::(0) //0.::(aList) -> gets rewritten on compilation
  val aBigList = 0 :: 1 :: 2 :: List(3,4) // List(3,4).::(2).::(1).::(0)

  class MyStream[T] {
    infix def -->:(value: T): MyStream[T] = this // impl not important
  }

  val myStream = 1 -->: 2 -->: 3 -->: 4 -->: MyStream[Int]

  // 4 - multi-word identifiers
  class Talker(name: String) {
    infix def `and then said`(gossip: String) = println(s"$name said $gossip")
  }

  val daniel = new Talker("Daniel")
  val danielsStatement = daniel `and then said` "I love Scala"

  // example: HTTP libraries
  object `Content-Type` {
    val `application/json` = "application/JSON"
  }

  // 5 - infix types
  import scala.annotation.targetName
  @targetName("Arrow") // for more readable bytecode + Java interop
  infix class -->[A, B]
  val compositType: Int --> String = new -->[Int, String]

  // 6 - update()
  val anArray = Array(1,2,3,4)
  anArray.update(2, 45)
  anArray(2) = 45 // same anArray[2] = 45 -> in Java -> very similar

  // 7 - mutable fields
  class Mutable {
    private var internalMember: Int = 0
    def member = internalMember // "getter"
    def member_=(value: Int): Unit =
      internalMember = value // "setter"
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // aMutableContainer.member_=(42)

  // 8 - variable arguments (varargs)
  def methodWithVarargs(args: Int*): Int = {
    // return the number of arguments supplied
    return args.length
  }

  val callWithZeroArgs = methodWithVarargs()
  val callWithOneArgs = methodWithVarargs(48)
  val callWithTwoArgs = methodWithVarargs(48, 12)

  val aCollection = List(1,2,3,4)
  val callWithDynamicArgs = methodWithVarargs(aCollection*)




  def main(args: Array[String]): Unit = {

  }
}
