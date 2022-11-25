package com.rockthejvm.part3async

import scala.collection.parallel.*
import scala.collection.parallel.CollectionConverters.*
import scala.collection.parallel.immutable.ParVector

object ParallelCollections {

  val aList = (1 to 1000000).toList
  val anIncrementedList = aList.map(_ + 1)
  val parList: ParSeq[Int] = aList.par
  val aParallelizedIncrementedList = parList.map(_ + 1) // map, flatMap, filter, foreach, reduce, fold
  /*
    Applicable for
      - Seq
      - Vector
      - Arrays
      - Maps
      - Sets

      Use-case: faster processing
   */

  // parallel collection build explicitely
  val aParVector = ParVector[Int](1,2,3,4,5,6)

  def measure[A](expression: => A): Long = {
    val time = System.currentTimeMillis()
    expression // forcing evaluation
    System.currentTimeMillis() - time
  }

  def compareListTransformations(): Unit = {
    val list = (1 to 30000000).toList
    println("List creation done")

    val serialTime = measure(list.map((_ + 1)))
    println(s"serial time: $serialTime")

    val parallelTime = measure(list.par.map(_ + 1))
    println(s"parallel time: $parallelTime")
  }

  def demoUndefinedOrder(): Unit = {
    val list = (1 to 1000).toList
    val reduction = aList.reduce(_ - _) // usually a bad idea to use non-associative operators

    val parallelReduction = list.par.reduce(_ - _)

    println(s"Sequential reduction: $reduction")
    println(s"Sequential reduction: $parallelReduction")
  }

  // for associative ops, result is deterministic
  def demoDefinedOrder(): Unit = {
    val strings = "I love parallel collections but I must be careful".split(" ").toList
    val concatenation = strings.reduce(_ + " " + _)
    val parallelConcatenation = strings.par.reduce(_ + " " + _)

    println(s"Sequential concatenation: $concatenation")
    println(s"Parallel concatenation: $parallelConcatenation")
  }

  // be careful with imperative programming on parallel collections
  def demoRaceConditions(): Unit = {
    var sum = 0
    (1 to 1000).toList.par.foreach(elem => sum += elem)
    println(sum)
  }

  def main(args: Array[String]): Unit = {
//    compareListTransformations()
//    demoUndefinedOrder()
//    demoDefinedOrder()
    demoRaceConditions()
  }
}
