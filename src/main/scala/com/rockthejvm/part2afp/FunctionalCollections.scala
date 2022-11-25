package com.rockthejvm.part2afp

object FunctionalCollections {
  // sets are functions
  val aSet: Set[String] = Set("I", "love", "Scala")
  val setContainsScala = aSet("Scala") // true

  // Seq - are PartialFunction[Int, A]
  val aSeq = Seq[Int](1,2,3,4)
  val anElement = aSeq(2) // 3
//  val aNonExistentElement = aSeq(100) // throw OOBException - is not total function

  // Map[K, V] "extends" PartialFunction[K, V]
  val aPhonebook = Map[String, Int](
    "Alice" -> 123456,
    "Bob" -> 987654
  )
  val alicesPhoneNo = aPhonebook("Alice")
//  val danielsPhone = aPhonebook("Daniel") // throws a NoSuchElementException



  def main(args: Array[String]): Unit = {

  }
}
