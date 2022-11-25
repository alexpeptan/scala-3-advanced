package com.rockthejvm.part4context

object Givens {

  // list sorting
  val aList = List(4,2,3,1)
  val anOrderedList = aList.sorted//(descendingOrdering)

  given descendingOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
//  given ascendingOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _)
  val anInverseOrderedList = aList.sorted(descendingOrdering)

  // custom sorting
  case class Person(name: String, age: Int)
  val people: List[Person] = List(Person("Alice", 29), Person("Sarah", 34), Person("Jim", 23))

  given personOrdering: Ordering[Person] = new Ordering[Person]:
    override def compare(x: Person, y: Person): Int =
      x.name.compareTo(y.name)

  val sortedPeople = people.sorted//(personOrdering) <--- automatically passed by the compiler

  object PersonAltSyntax {
    given personOrdering: Ordering[Person] with {
      override def compare(x: Person, y: Person): Int =
        x.name.compareTo(y.name)
    }
  }

  // using clause
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }

  def combineAll[A](list: List[A])(using combinator: Combinator[A]): A =
    list.reduce(combinator.combine)

  /*
    combineAll(List(1,2,3,4))
    combineAll(people)
  */

  given intCombinator: Combinator[Int] with {
    override def combine(x: Int, y: Int) = x + y
  }

  val firstSome = combineAll(List(1,2,3,4))//(someCombinator[Int]
//  val combineAllPeople = combineAll(people) // does not compile - no Combinator[Person] in scope

  // context bound
  def combineInGroupsOf3[A](list: List[A])(using combinator: Combinator[A]): List[A] =
    list.grouped(3).map(group => combineAll(group)/*(combinator) passed by the compiler*/).toList

  def combineInGroupsOf3_v2[A: Combinator](list: List[A]): List[A] = // A: Combinator => there is a given Combinator[A] in scope
    list.grouped(3).map(group => combineAll(group) /*(combinator) passed by the compiler*/).toList

  // synthesize new given instances based on existing ones
  given listOrdering(using intOrdering: Ordering[Int]): Ordering[List[Int]] with {
    override def compare(x: List[Int], y: List[Int]) =
      x.sum - y.sum
  }

  val listsOfLists = List(List(1,2), List(1,1), List(3,4,5))
  val nestedListsOrdered = listsOfLists.sorted

  // ... with generics
  given listOrderingBasedOnCombinator[A](using ord: Ordering[A])(using combinator: Combinator[A]): Ordering[List[A]] with {
    override def compare(x: List[A], y: List[A]) =
      ord.compare(combineAll(x), combineAll(y))
  }

  // pass a regular value instead of a given
  val myCombinator = new Combinator[Int] {
    override def combine(x: Int, y: Int) = x * y
  }

  val listProduct = combineAll(List(1,2,3,4))(using myCombinator)

  /**
   * Exercises:
   * 1 - create a given for ordering Option[A] if you can order A
   * 2 - create a summoning method that fetches the given value of your particular type
   */

//  given optionOrdering[A] (using normalOrdering: Ordering[A]): Ordering[Option[A]] with {
//    override def compare(x: Option[A], y: Option[A]): Int =
////      if(x.isEmpty) 1
////      else if(y.isEmpty) -1
////      else ordA.compare(x.get, y.get)
//      (x, y) match {
//        case (None, None) => 0
//        case (None, _) => -1
//        case (_, None) => 1
//        case (Some(a), Some(b)) => normalOrdering.compare(a, b)
//      }
//  }

  given optionOrdering_short[A: Ordering]: Ordering[Option[A]] with {
    override def compare(x: Option[A], y: Option[A]): Int =
      (x, y) match {
        case (None, None) => 0
        case (None, _) => -1
        case (_, None) => 1
        case (Some(a), Some(b)) => fetchGivenValue[Ordering[A]].compare(a, b)
//        case (Some(a), Some(b)) => summon[Ordering[A]].compare(a, b) // identical
      }
  }

  def fetchGivenValue[A](using theValue: A): A = theValue

  val firstIntOption: Option[Int] = Option(3)
  val secondIntOption: Option[Int] = Option(33)
  val optionList: List[Option[Int]] = List(Some(4), Some(2), None, Some(33), Option(2))
  val optionListSorted = optionList.sorted

  def main(args: Array[String]): Unit = {
//    println(anOrderedList) // [1,2,3,4]
//    println(anInverseOrderedList) // [4,3,2,1]
//    println(people)
//    println(sortedPeople)
//
//    println(nestedListsOrdered)
//    print(listProduct)

    println(optionListSorted)
  }
}
