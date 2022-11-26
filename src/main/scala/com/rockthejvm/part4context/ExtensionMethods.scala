package com.rockthejvm.part4context

object ExtensionMethods {

  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name. Nice meeting you."
  }

  extension (string: String)
    def greetAsPerson: String = Person(string).greet

  val danielGreeting = "Daniel".greetAsPerson

  // generic extension methods
  extension [A](list: List[A])
    def ends: (A, A) = (list.head, list.last)

  val aList = List(1,2,3,4)
  val firstLast = aList.ends

  // reason: make APIs very expressive
  // reason 2: enhance CERTAIN types with new capabilities
  // => super-powerful code
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }

  extension [A](list: List[A])
    def combineAll(using combinator: Combinator[A]): A =
      list.reduce(combinator.combine)

  given intCombinator: Combinator[Int] with
    override def combine(x: Int, y: Int) = x + y

  val firstSum = aList.combineAll // works, sum is 10
  val someStrings = List("I", "love", "Scala")
//  val stringsSum = someStrings.combineAll // does not compile - no given Combinator[Strings] in scope

  // grouping extensions
  object GroupedExtensions {
    extension[A] (list: List[A]) {
      def ends: (A, A) = (list.head, list.last)
      def combineAll(using combinator: Combinator[A]): A =
        list.reduce(combinator.combine)
    }
  }

  // call extension methods directly
  val firstLast_v2 = ends(aList) // same as aList.ends

  /**
   * Exercises
   * 1. Add a isPrime method to the Int type
   *  You should be able to type 7.isPrime
   * 2. Add extensions to Tree:
   *  - map(f: A => B) : Tree[B]
   *  - forall(predicate: A => Boolean): Boolean
   *  - sum => sum of all elements of the tree
   *    -> just for Tree[Int]
   *    -> if brave use combineALl for all... :)
   */

  // "library code" = cannot change
  sealed abstract class Tree[A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  // 1
  extension (n: Int)
    def isPrime: Boolean = {
      def isPrimeUntil(t: Int): Boolean =
        if (t <= 1) true
        else n % t != 0 && isPrimeUntil(t - 1)

      isPrimeUntil(n / 2)
    }

  // 2
//  object TreeExtensions {
  extension [A](tree: Tree[A]) {
    def map[B](f: A => B) : Tree[B] =
      tree match
        case Leaf(value: A) => Leaf(f(value))
        case Branch(left/*: Tree[A]*/, right/*: Tree[A]*/)
          => Branch(left.map(f), right.map(f))

    def forall(predicate: A => Boolean): Boolean =
      tree match
        case Leaf(value: A) => predicate(value)
        case Branch(left/*: Tree[A]*/, right/*: Tree[A]*/)
          => left.forall(predicate) && right.forall(predicate)

    def combineAll(using combinator: Combinator[A]): A = tree match
      case Leaf(value) => value
      case Branch(left, right) => combinator.combine(left.combineAll, right.combineAll)
  }

  extension /*[Int]*/(tree: Tree[Int]) {
    def sum: Int = {
      tree match
        case Leaf(value: Int) => value
        case Branch(left/*: Tree[A]*/, right/*: Tree[A]*/)
        => (left.sum + right.sum)
    }
  }
//  }

  val aTree: Tree[Int] = Branch(Leaf(1), Branch(Leaf(12), Leaf(22)))
  val isEven: Int => Boolean = _ % 2 == 0
  val mappedTreeElementsParity: Tree[Boolean] = aTree.map(isEven)

  def main(args: Array[String]): Unit = {
//    println(danielGreeting)
//    println(firstLast)
//    println(firstSum)
    println(7.isPrime)
    println(711.isPrime)

    println(aTree)
    println(aTree.map(_ + 1))
    println(aTree.forall(_ % 2 == 0))
    println(aTree.sum)
    println(aTree.combineAll)
  }
}
