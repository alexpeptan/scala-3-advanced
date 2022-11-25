//package com.rockthejvm.part3async
//
//import scala.collection.parallel.*
//import scala.collection.parallel.CollectionConverters.*
//import scala.collection.parallel.immutable.ParVector
//
//object ParallelCollections {
//
//  val aList = (1 to 1000000).toList
//  val anIncrementedList = aList.map(_ + 1)
//  val parList: ParSeq[Int] = aList.par
//  val aParallelizedIncrementedList = parList.map(_ + 1) // map, flatMap, filter, foreach, reduce, fold
//  /*
//    Applicable for
//      - Seq
//      - Vector
//      - Arrays
//      - Maps
//      - Sets
//
//      Use-case: faster processing
//   */
//
//  // parallel collection build explicitely
//  val aParVector = ParVector[Int](1,2,3,4,5,6)
//
//  def measure[A](expression: => A): Long = {
//    val time = System.currentTimeMillis()
//    expression // forcing evaluation
//    System.currentTimeMillis() - time
//  }
//
//  def compareListTransformations(): Unit = {
//    val list = (1 to 10000000).toList
//    println("List creation done")
//
//    val serialTime = measure(list.map((_ + 1)))
//    println(s"serial time: $serialTime")
//
//    val parallelTime = measure(list.par.map(_ + 1))
//    println(s"parallel time: $parallelTime")
//  }
//
//  def main(args: Array[String]): Unit = {
//    compareListTransformations()
//  }
//}
