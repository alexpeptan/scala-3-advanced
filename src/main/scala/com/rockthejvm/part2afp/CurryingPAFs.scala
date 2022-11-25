package com.rockthejvm.part2afp

object CurryingPAFs {

  // currying
  val supperAdder: Int => Int => Int =
    x => y => x + y

  val add3: Int => Int = supperAdder(3) // y => 3 + y
  val eight = add3(5) // 8
  val eight_v2 = supperAdder(3)(5)

  // curried methods
  def curriedAdder(x: Int)(y: Int): Int =
    x + y

  // methods != function values
  // converting methods to functions
  val add4 = curriedAdder(4)
  // eta-expansion -> Partially Applied Funtion
  // converting a Partial Function Application to a value through eta-expansion
  // when you invoke a method with fewer argument lists you'll get
  // a lambda that receive the rest of the argument lists to invoke later
  val nine = add4(5) // 9

  def increment(x: Int): Int = x + 1
  val aList = List(1,2,3)
  val anIncrementedList = aList.map(increment) // through eta-expansion automatically converts the method to a lambda
  // eta-expansion is the internal process that the compiler works to convert a method to the function values that you need

  // underscores are powerful: allow you to decide the shape of lambdas obtained from methods
  def concatenator(a: String, b: String, c: String): String = a + b + c
  val insertName = concatenator(
    "Hello, my name is ",
    _: String,
    ", I'm going to show you a nice Scala trick."
  ) // x => concatenator("...", x, "...")

  val danielsGreeting = insertName("Daniel") // concatenator("...", "Daniel", "...")

  val fillInTheBlanks = concatenator(_:String, "Daniel", _:String) // (x, y) => concatenator(x, "Daniel", y)
  val danielsGreeting_v2 = fillInTheBlanks("Hi,", "how are you?")

  /**
   * Exercises:
   * 1.
   */
  // FUNCTION VALUE, NOT method
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedMethod(x: Int)(y: Int) = x + y

  // 1 - obtain an add7 function: x => x + 7 out of these 3 definitions
  def add7_1: Int => Int = x => x + 7
  def add7_2 = (x: Int) => simpleAddFunction(x, 7)
  def add7_3 = (x: Int) => simpleAddFunction(7, x)
  val add7_4 = (x: Int) => simpleAddMethod(7, x:Int)
  val add7_5 = (x: Int) => simpleAddMethod(x:Int, 7)
  val add7_5_daniel = (x: Int) => simpleAddMethod(x, 7)
  val add7_4_1 = (x: Int) => simpleAddMethod(7, _: Int) // x ignored
  val add7_4_1_daniel = (x: Int) => simpleAddMethod(7, _) // x ignored
  val add7_5_1 = (x: Int) => simpleAddMethod(_: Int, 7) // x ignored
  val add7_5_2 = simpleAddMethod(_: Int, 7)
  val add7_5_3 = (x: Int) => simpleAddMethod(x: Int, 7)
  val add7_6 = (x: Int) => curriedMethod(7)(x)
  val add7_6_2_daniel = curriedMethod(7)
  def add7_7 = (x: Int) => curriedMethod(x)(7)
  val add7_8_daniel = simpleAddFunction.curried(7)

  // 2 - process a list of numbers and return their string representations under different formats
  // step 1: create a curried formatted method with a formatted string and a value
  // step 2: process a list of numbers with various formats
  val piWith2Dec = "%4.2f".format(Math.PI) // 3.14
  val piWith2Dec_2 = "%8.6f".format(Math.PI) // 3.14

  def formatList(list: List[Double])(format: String): String = {
    if (list.isEmpty) ""
    else format.format(list.head) + " " + formatList(list.tail)(format)
  }

  def curriedFormatter(fmt: String)(number: Double): String = fmt.format(number)
  def formatListWithFormatter(frmt: String)(list: List[Double]): String = {
    if (list.isEmpty) ""
    else curriedFormatter(frmt)(list.head) + " "
      + formatListWithFormatter(frmt)(list.tail)
  }

  // methods vs functions + by-name vs 0-lambdas
  def byName(n: => Int) = n + 1
  def byLambda(f: () => Int) = f() + 1

  def method: Int = 42
  def parenMethod(): Int = 42

  byName(23) // ok
  byName(method) // 43. is this eta-expanded? NO - methid is INVOKED here
  byName(parenMethod()) // simple
//  byName(parenMethod) // not ok
  byName((() => 42)()) // ok
//  byName(() => 42) // not ok
//  byLambda(23) // not ok
//  byLambda(method) // the method cannot be automatically
  // eta-expanded into a 0 argument lambda because
  // the method has no arguments at all
  byLambda(parenMethod) // eta-expansion is done -> because
  // parenMethod is directly transformable into the 0-arg lambda
  byLambda(() => 42)
  byLambda(() => parenMethod()) // ok

  def main(args: Array[String]): Unit = {
    println(add7_1(1))
    println(add7_2(1))
    println(add7_3(1))
    println(add7_4(1))
    println(add7_5(1))
    println(add7_4_1(123)(10))
    println(add7_5_1(123)(100))
    println(add7_5_2(1000))
    println(add7_5_3(10000))
    println(add7_6(1))
    println(add7_7(1))

    println(piWith2Dec)
    println(piWith2Dec_2)

    val aDoublesList = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)
    val formattedList_v1 = formatList(aDoublesList)("%4.2f")
    val formattedList_v1_2 = formatListWithFormatter("%4.2f")(aDoublesList)
    println(formattedList_v1)
    println(formattedList_v1_2)

    val formattedList_v2 = formatList(aDoublesList)("%4.4f")
    val formattedList_v2_2 = formatListWithFormatter("%4.4f")(aDoublesList)
    println(formattedList_v2)
    println(formattedList_v2_2)

    println(add7_5_daniel(2))
    println(add7_4_1_daniel(1234)(2))
    println(add7_6_2_daniel(2))
    println(add7_8_daniel(2))

    println(aDoublesList.map(curriedFormatter("%4.2f")))
    println(aDoublesList.map(curriedFormatter("%8.6f")))
    println(aDoublesList.map(curriedFormatter("%14.12f")))
    println(aDoublesList.map(curriedFormatter("%14.14f")))
  }
}

