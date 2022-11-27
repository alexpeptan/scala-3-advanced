package com.rockthejvm.part4context

import scala.concurrent.{ExecutionContext, Future}

object ContextFunctions {

  val aList = List(1,2,3,4)
  val sortedList = aList.sorted

  // defs can take using clauses
  def methodWithoutContextArguments(nonContextArg: Int)(nonContextArg2: String): String = ???
  def methodWithContextArguments(nonContextArg: Int)(using nonContextArg2: String): String = ???

  // eta-expansion
  val functionWithoutContextArguments = methodWithoutContextArguments
//  val func2 = methodWithContextArguments // doesn't work

  // context function
  val functionWithContextArguments: Int => String ?=> String = methodWithContextArguments

//  val sameResult = functionWithContextArguments(2)/*(using "Scala")*/

  /*
    - convert methods with using clauses to function values
    - HOF with function values taking given instances as arguments
    - requiring given instances at CALL SITE, not at DEFINITION
  */

  // execution context here
  // but we'd like to pass the execution context where the function is being called
  // NOT where it is being defined...
  // For doing that decoupling we have the concept of CONTEXT FUNCTION
//  val incrementAsync: Int => Future[Int] = x => Future(x + 1) // doesn't work without an execution context in scope
  // but in order to instantiate a Future we need a given execution context in the scope
  // in the scope of what?
  // in the scope where the Future consgtructor is being involved

  val incrementAsync: ExecutionContext ?=> Int => Future[Int] = x => Future(x + 1)

  def main(args: Array[String]): Unit = {

  }
}
