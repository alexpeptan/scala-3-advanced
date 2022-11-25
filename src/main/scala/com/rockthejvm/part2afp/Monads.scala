package com.rockthejvm.part2afp

import scala.util.Random
import scala.annotation.targetName

object Monads {

  def listStory(): Unit = {
    val aList = List(1, 2, 3)
    val listMultiply = for {
      x <- List(1, 2, 3)
      y <- List(4, 5, 6)
    } yield x * y

    // for comprehensions = chains of map + flatMap

    val listMultiply_v2 = List(1, 2, 3).flatMap(x => List(4, 5, 6).map(y => x * y))

    val f = (x: Int) => List(x, x + 1)
    val g = (x: Int) => List(x, 2 * x)
    val pure = (x: Int) => List(x) // same as the List "constructor"

    // prop 1: left identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for every x, for every f

    // prop 2: right identity
    val rightIdentity = aList.flatMap(pure) == aList // for every list

    // prop 3: associativity
    val associativity = aList.flatMap(f).flatMap(g) == aList.flatMap(x => f(x).flatMap(g))
  }

  def optionsStory(): Unit = {
    val anOption = Option(42)
    val optionString = for {
      lang <- Option("Scala")
      ver <- Option(3)
    } yield s"$lang-$ver"
    // identical
    val optionString_v2 = Option("Scala").flatMap(lang => Option(3).map(ver => s"$lang-$ver"))

    val f = (x: Int) => Option(x + 1)
    val g = (x: Int) => Option(2 * x)
    val pure = (x: Int) => Option(x)

    // prop 1: left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for any x, for any f

    // prop 2: right-identity
    val rightIdentity = anOption.flatMap(pure) == anOption // for any Option

    // prop 3: associativity
    val associativity = anOption.flatMap(f).flatMap(g) == anOption.flatMap(x => f(x).flatMap(g)) // for any Option, f and g
  }

  // MONADS = chain dependent computations

  // exercise: IS THIS A MONAD?
  // answer: IT IS A MONAD!
  // interpretation: ANY computation that might perform side effects
  // these side-effects - and everything that we pass to it is not being
  // performed at construction phase!
  case class IO[A](unsafeRun: () => A) {
    def map[B](f: A => B): IO[B] =
      IO(() => f(unsafeRun()))

    def flatMap[B](f: A => IO[B]): IO[B] =
//      PossiblyMonad(() => unsafeRun())
//      PossiblyMonad(() => f(unsafeRun()))
      IO(() => f(unsafeRun()).unsafeRun())
  }

  object IO {
    // name collision here
    // PossiblyMonad is a case class
    // We already have a companion object with an apply method that takes a "zero lambda" -> "() => A"
    // And in JVM bytecode, by name methods ("value: => A") and methods that take "zero argument lambdas" (the one above)
    // are erased to the same type -> addressing the name collision with @targetName annotation
    @targetName("pure")
    def apply[A](value: => A): IO[A] =
      IO(() => value)
  }

  def possiblyMonadStory(): Unit = {
//    val rand: Random = Random()
//    val unsafeFunc: () => Int = rand.nextInt(10) % rand.nextInt(3)
//    val aMonad = PossiblyMonad[Int](unsafeFunc)

    val aPossiblyMonad = IO(42)

    // why writing listMultiply / optionString with for comprehension and then with map & flatmap function calls?
//    val monadString = for {
//      x <- PossiblyMonad[Int](() => "Ana are mere")
//      y <- PossiblyMonad[Int](() => " foarte dulci")
//    } yield s"$x$y"

    // don't know what I'm doing :))
//    val monadString_v2 = PossiblyMonad[Int](() => "Ana are mere").flatMap(PossiblyMonad[Int](() => " foarte dulci").map(identity))

    // how would f, g and pure functions look like?
//    val f = (x: Int) => PossiblyMonad[Int](() => s"Ana are $x mere")
//    val g = (x: Int) => PossiblyMonad[Int](() => s"de nota $x")
//    val pure = (x: Int) => PossiblyMonad[Int](() => s"$x")
    val f = (x: Int) => IO(x + 1)
    val g = (x: Int) => IO(2 * x)
    val pure = (x: Int) => IO(x)

    // prop 1:  left-identity property
    val leftIdentity = pure(42).flatMap(f) == f(42)

    // prop 2: right-identity property
    val rightIdentity = aPossiblyMonad.flatMap(pure) == aPossiblyMonad

    // write associativity property
    val associativity = aPossiblyMonad.flatMap(f).flatMap(g) == aPossiblyMonad.flatMap(x => f(x).flatMap(g))

    // what is the idea with these properties used in this context? -> can't grasp it
    // any generalizetion that can be spotted? -> not at this moment. Can't see the implications

    println(leftIdentity)
    println(rightIdentity)
    println(associativity)
    // false because the apply method constructs a new lambda all the time
    // and the runtime has no way of comparing wether 2 functions produce
    // the same values all the time
    println(IO(3) == IO(3))
    // ^^ false negative.

    // real tests: values produced + side effects ordering
    val leftIdentity_v2 = pure(42).flatMap(f).unsafeRun() == f(42).unsafeRun()
    val rightIdentity_v2 = aPossiblyMonad.flatMap(pure).unsafeRun() == aPossiblyMonad.unsafeRun()
    val associativity_v2 = aPossiblyMonad.flatMap(f).flatMap(g).unsafeRun() == aPossiblyMonad.flatMap(x => f(x).flatMap(g)).unsafeRun()

    println(leftIdentity_v2)
    println(rightIdentity_v2)
    println(associativity_v2)

    val fs = (x: Int) => IO {
      println("Incrementing")
      x + 1
    }

    val gs = (x: Int) => IO {
      println("Doubling")
      x * 2
    }

    val associativity_v3 = aPossiblyMonad.flatMap(fs).flatMap(gs).unsafeRun() == aPossiblyMonad.flatMap(x => fs(x).flatMap(gs)).unsafeRun()
  }

  def possiblyMonadExample(): Unit = {
    val aPossiblyMonad = IO {
      println("Printing my first possibly monad")
      // do some computations
      42
    }

    val anotherPM = IO {
      println("my second PM")
      "Scala"
    }

//    val aResult = aPossiblyMonad.unsafeRun()
//    println(aResult)

    val aForComprehension = for { // computations are DESCRIBED, but not EXECUTED
      num <- aPossiblyMonad
      lang <- anotherPM
    } yield s"$num-$lang"
  }

  def main(args: Array[String]): Unit = {
//    PossiblyMonadStory()
//    possiblyMonadExample()
    possiblyMonadStory()
  }
}
