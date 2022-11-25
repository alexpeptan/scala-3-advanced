package com.rockthejvm.practice

import scala.annotation.tailrec

abstract class FSet[A] extends (A => Boolean) {
  // main api
  def contains(elem: A): Boolean
  def apply(elem: A): Boolean = contains(elem)

  infix def +(elem: A): FSet[A]
  infix def ++(anotherSet: FSet[A]): FSet[A]

  // "classics"
  def map[B](f: A => B): FSet[B]
  def flatMap[B](f: A => FSet[B]): FSet[B]
  def filter(predicate: A => Boolean): FSet[A]
  def foreach(f: A => Unit): Unit

  // methods -> infix because they are gonna be used as math operators
  infix def -(elem: A): FSet[A]
  infix def --(anotherSet: FSet[A]): FSet[A]
  infix def &(anotherSet: FSet[A]): FSet[A]

  // "negation" = all the elements of type A EXCEPT the elements in this set
  def unary_! : FSet[A] = new PBSet(x => !contains(x))
//  def unary_! : FSet[A] = new PBSet(!contains) // DOES NOT WORK -> asking on slack
//  def unary_~ : FSet[A] = new PBSet(!contains)
}

case class Empty[A]() extends FSet[A] { // PBSet(_ => false)
  override def contains(elem: A) = false
  override infix def +(elem: A): FSet[A] = Cons(elem, this)
  override infix def ++(anotherSet: FSet[A]) = anotherSet

  override def map[B](f: A => B) = Empty()
  override def flatMap[B](f: A => FSet[B]): FSet[B] = Empty()
  override def filter(predicate: A => Boolean): FSet[A] = this
  override def foreach(f: A => Unit): Unit = ()

  override infix def -(elem: A): FSet[A] = this
  override infix def --(anotherSet: FSet[A]): FSet[A] = this
  override infix def &(anotherSet: FSet[A]): FSet[A] = this
}

case class Cons[A](head: A, tail: FSet[A]) extends FSet[A] {
  override def contains(elem: A): Boolean = {
    if (head == Empty) false
    else head == elem || tail.contains(elem)
  }
  override infix def +(elem: A) =
    if this.contains(elem) then this
    else Cons(elem, this)

  override infix def ++(anotherSet: FSet[A]) = {
    if (this == Empty()) anotherSet
    else this.tail ++ (anotherSet + this.head)
  }

  override def map[B](f: A => B): FSet[B] = {
//    def map_elem[B](remainingElements: Cons[A], accumulator: Cons[B]): FSet[B] = {
//      if (remainingElements == Empty()) accumulator
//      else map_elem(remainingElements.tail, Cons(f(remainingElements.head), accumulator))
//    }
//
//    map_elem(this, Empty())
    tail.map(f) + f(head)
  }

  override def flatMap[B](f: A => FSet[B]): FSet[B] = {
//    def map_elem[B](remainingElements: Cons[A], accumulator: FSet[B]): FSet[B] = {
//      if (remainingElements == Empty () ) accumulator
//      else map_elem(remainingElements.tail, f(remainingElements.head) ++ accumulator)
//    }
//
//    map_elem(this, Empty())
    tail.flatMap(f) ++ f(head)
  }

  override def filter(predicate: A => Boolean): FSet[A] = {
//    if (predicate(head)) Cons(head, filter(tail))
//    else filter(tail)
    val filteredTail = tail.filter(predicate)
    if predicate(head) then filteredTail + head
    else filteredTail
  }

  override def foreach(f: A => Unit): Unit = {
    f(head)
//    foreach(tail)
    tail.foreach(f)
  }

//  override infix def -(elem: A): FSet[A] = {
//    this.filter(_ != elem)
//  }

  // Daniel's implementation:
  override infix def -(elem: A): FSet[A] = {
    if (head == elem) tail
    else tail - elem + head
  }

//  override infix def --(anotherSet: FSet[A]): FSet[A] = {
//    if anotherSet == Empty() then this
////    else (this - anotherSet.head) -- anotherSet.tail
//    // I do not have anotherSet.tail -> because I need to work with the interface of FSet -> more restrictive
//    // not with the one of Cons -> which allows anotherSet.tail
//    else (this - anotherSet.head) -- (anotherSet.tail - anotherSet.head) // need to see Daniel's approach
//  }

  // Daniel's implementation(using the FSet apply function, nice!):
  override infix def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)//filter(x => !anotherSet(x))
  // x => !anotherSet(x) ... this is an expression of a set that contains everything BUT a set
  // filter(!anotherSet) ... possible only if we implement the unary operator !


//  override infix def &(anotherSet: FSet[A]): FSet[A] = {
//    val diff1 = this -- anotherSet
//    val diff2 = anotherSet -- this
//    val reunion = this ++ anotherSet
//
//    reunion -- diff1 -- diff2
//  }

  // However: Daniel: "A set is a function from Int to Bookean[same with FSet]" -> WHY? -> because of the apply function :)
  override infix def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet) // intersection = filtering

}

object FSet {
  def apply[A](values: A*): FSet[A] = {
    @tailrec
    def buildSet(valuesSeq: Seq[A], acc: FSet[A]): FSet[A] = {
      if (valuesSeq.isEmpty) acc
      else buildSet(valuesSeq.tail, acc + valuesSeq.head)
    }

    buildSet(values, Empty())
  }
}

class PBSet[A](property: A => Boolean) extends FSet[A] {
  // main api
  def contains(elem: A): Boolean = property(elem)

  infix def +(elem: A): FSet[A] = new PBSet(x => property(x) || x == elem)
  infix def ++(anotherSet: FSet[A]): FSet[A] = new PBSet(x => anotherSet(x) || property(x))

  // "classics"
  def map[B](f: A => B): FSet[B] = politelyFail()
  def flatMap[B](f: A => FSet[B]): FSet[B] = politelyFail()
  def filter(predicate: A => Boolean): FSet[A] =
    new PBSet(x => property(x) || predicate(x))
  def foreach(f: A => Unit): Unit = politelyFail()

  // methods -> infix because they are gonna be used as math operators
  infix def -(elem: A): FSet[A] = filter(x => x != elem)
  infix def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)
  infix def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet)

  // extra utilities (internal)
  private def politelyFail() = throw new RuntimeException("I don't know if this set is iterable")

}

object FunctionalSetPlayground {
  val aSet = FSet(1,2,3)
  def main(args: Array[String]): Unit = {
    val myFSet: FSet[Int] = Empty()
    println(myFSet)

    val first5 = FSet(1,2,3,4,5)
    val someNumbers = FSet(4,5,6,7,8)

    println(first5.contains(5)) // true
    println(first5.contains(6)) // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1)) // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7))

    println((first5.contains(3)))
    println((first5 -- FSet(3)).contains(3))

    println(s"first5 contain 2: ${first5.contains(2)}")
    val first5Minus2 = first5 - 2
    println(first5Minus2.contains(2))

    println((aSet & first5Minus2).contains(3))
    println((aSet & first5Minus2).contains(33))

    println((first5).contains(3)) // true
    println((first5 - 3).contains(3)) // false

    println((first5 -- someNumbers).contains(4)) // false
    println((first5 & someNumbers).contains(5)) // true

    println("PBSet testing")
    val naturals = new PBSet[Int](_ => true)
    println(naturals.contains(23432424)) // true
    println(!naturals.contains(0)) // false
    println((!naturals + 1 + 2 + 3).contains(3)) // true
    println((!naturals).map(_ + 1)) // will throw exception

//    println(~naturals.contains(0)) // false
//    println((~naturals + 1 + 2 + 3).contains(3)) // true
//    println((~naturals).map(_ + 1)) // will throw exception
  }
}
