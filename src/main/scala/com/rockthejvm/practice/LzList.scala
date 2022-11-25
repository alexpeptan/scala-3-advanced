package com.rockthejvm.practice

import scala.annotation.tailrec

// Write a lazily evaluated, potentially INFINITE linked list
abstract class LzList[A] {
  def isEmpty: Boolean
  def head: A
  def tail: LzList[A]

  // utilities
  def #::(element: A): LzList[A] // prepending
  infix def ++(another: => LzList[A]): LzList[A]

  // classigs
  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): LzList[B]
  def flatMap[B](f: A => LzList[B]): LzList[B]
  def filter(predicate: A => Boolean): LzList[A]
  def withFilter(predicate: A => Boolean): LzList[A] = filter(predicate)

  def take(n: Int): LzList[A] // takes the first n elements from this lazy list
  def takeAsList(n: Int): List[A] =
    take(n).toList
  def toList: List[A] = { // use this carefully - risky (on infinite collections)
    @tailrec
    def toListAux(remaining: LzList[A], acc: List[A]): List[A] =
      if (remaining.isEmpty) acc.reverse
      else toListAux(remaining.tail, remaining.head :: acc)

    toListAux(this, List())
  }
}

case class LzEmpty[A]() extends LzList[A] {
  def isEmpty: Boolean = true
  lazy val head: A = throw new NoSuchElementException
  lazy val tail: LzList[A] = throw new NoSuchElementException

  // utilities
  def #::(element: A): LzList[A] = LzCons(element, this) // prepending
  infix def ++(another: => LzList[A]): LzList[A] = another // don't get what the problem would be // TODO warning

  // classigs
  def foreach(f: A => Unit): Unit = ()
  def map[B](f: A => B): LzList[B] = LzEmpty()
  def flatMap[B](f: A => LzList[B]): LzList[B] = LzEmpty()
  def filter(predicate: A => Boolean): LzList[A] = this

  def take(n: Int): LzList[A] =
    if (n==0) this // takes the first n elements from this lazy list
    else throw new RuntimeException(s"Cannot take $n elements from an empty lazy list")
}

// both head and tail are lazily evaluated and are passed by name
// case classes cannot have arguments passed by name because they are defined as fields
// the compiler turns them into fields and they have to be eagerly evaluated
class LzCons[A](hd: => A, tl: => LzList[A]) extends LzList[A] {
  // hint: use call by need
  def isEmpty: Boolean = false
  override lazy val head: A = hd
  override lazy val tail: LzList[A] = tl

  // utilities
  def #::(element: A): LzList[A] = LzCons(element, this) // prepending
  infix def ++(another: => LzList[A]): LzList[A] = {
    // see how I use that pattern "x as need" or smth
    new LzCons(head, tail ++ another)
  }

  // classigs
  def foreach(f: A => Unit): Unit = {
    @tailrec
    def foreachTailRec(lzList: LzList[A]): Unit =
      if (lzList.isEmpty) ()
      else {
        f(lzList.head)
        foreachTailRec(lzList.tail)
      }

    foreachTailRec(this)
  }

  def map[B](f: A => B): LzList[B] = {
    LzCons(f(head), tail.map(f) /*map(tail)*/)
  }

  def flatMap[B](f: A => LzList[B]): LzList[B] = {
//    LzCons(f(head), tail.flatMap(f) /*flatMap(tail)*/)
    // OR
    f(head) ++ tail.flatMap(f) // preserves lazy eval

    // EARLIER - breaks lazy evaluation
    // ++ operator is at fault here because it also needs to preserve the lazy eval
    // and currently it forces the evaluation of tail.flatMap(f) for the whole stream
    // ++ has to use argument BY NAME, bot BY VALUE! to ensure this lazy functionality
  }

  def filter(predicate: A => Boolean): LzList[A] = {
    if (predicate(head)) LzCons(head, tail.filter(predicate) /*filter(tail)*/) // preserves lazy eval
    else tail.filter(predicate)
  }

  def take(n: Int): LzList[A] = { // takes the first n elements from this lazy list
    if (n <= 0) LzEmpty()
    else if (n == 1) new LzCons(head, LzEmpty())
    else LzCons(head, tail.take(n-1)) // preserves lazy eval
  }
}

object LzList {
  def empty[A]: LzList[A] = LzEmpty()

  def generate[A](startValue: A)(generator: A => A): LzList[A] = {
    new LzCons(startValue, generate(generator(startValue))(generator))
  }
  def from[A](list: List[A]): LzList[A] =
    list.reverse.foldLeft(LzList.empty) {
      (currentLzList, newElement) =>
//        currentList :: newElement // OR:
        new LzCons(newElement, currentLzList)
    }

  def apply[A](values: A*) = LzList.from(values.toList)

  def generateFibonacci(a: Int, b: Int): LzList[Int] = {
    new LzCons(a, generateFibonacci(b, a + b))
  }

//  var sieve: LzList[Int] = LzList.generate(2)(n => n + 1) // TODO Get rid of var
//  val nextSievedPrimeElem: LzList[Int] = sieve

  def generatePrimesViaSieve(upTo: Int) = {
    @tailrec
    def generatePrimesTailrec(nextPrimeToProcess: LzList[Int], currentSieve: LzList[Int]): LzList[Int] = {
      if (nextPrimeToProcess.head * nextPrimeToProcess.head > upTo)
        currentSieve
      else {
        // actually apply sieve for next element
        val primeGoingThroughSieve: Int = nextPrimeToProcess.head
        val filteredSieve = currentSieve.filter(x => x % primeGoingThroughSieve != 0 || x == primeGoingThroughSieve)
        generatePrimesTailrec(nextPrimeToProcess.tail, filteredSieve) // NOT USING LzCons constructor I am doomed -> not using the lazy eval mechanism
      }
    }

    val naturalsFrom2 = LzList.generate(2)(n => n + 1)
    generatePrimesTailrec(nextPrimeToProcess = naturalsFrom2, currentSieve = naturalsFrom2)
  }

  def generatePrimesViaSieve2(upTo: Int) = {
    def generatePrimesRecursively(nextPrimeToProcess: LzList[Int], currentSieve: LzList[Int]): LzList[Int] = {
      if (nextPrimeToProcess.head * nextPrimeToProcess.head > upTo)
        currentSieve
      else {
        // actually apply sieve for next element
        val primeGoingThroughSieve: Int = currentSieve.head //nextPrimeToProcess.head
        val filteredSieve = currentSieve.filter(x => x % primeGoingThroughSieve != 0)
        println(s"prime: $primeGoingThroughSieve. Fltered sieve: ${filteredSieve.takeAsList(100)}")
        LzCons(primeGoingThroughSieve, generatePrimesRecursively(nextPrimeToProcess.tail, filteredSieve))
        // NOT USING LzCons constructor I am doomed -> not using the lazy eval mechanism
        // recursive call is called on second argument - on tail for LzCons and would be lazy-evaluated
        // -> logic needs to be corrected somehow & not dependent on isPrine method
        // tranzitionare usor de la solutia mea - care nu merge pe infinite collections - la ceva ce poate merge pe infinite collections
      }
    }

    val naturalsFrom2 = LzList.generate(2)(n => n + 1)
    generatePrimesRecursively(nextPrimeToProcess = naturalsFrom2, currentSieve = naturalsFrom2)
  }

  // taking advantage of infinite collections - not using upper limit anymore
  def generatePrimesViaSieve3() = {
    def generatePrimesInfiniteCollection(nextPrimeToProcess: LzList[Int], currentSieve: LzList[Int]): LzList[Int] = {
      // actually apply sieve for next element
      val primeGoingThroughSieve: Int = currentSieve.head
      val filteredSieve = currentSieve.filter(x => x % primeGoingThroughSieve != 0)
      LzCons(primeGoingThroughSieve, generatePrimesInfiniteCollection(nextPrimeToProcess.tail, filteredSieve))
    }

    val naturalsFrom2 = LzList.generate(2)(n => n + 1)
    generatePrimesInfiniteCollection(nextPrimeToProcess = naturalsFrom2, currentSieve = naturalsFrom2)
  }

  // get rid of next prime param
  def generatePrimesViaSieve4() = {
    def generatePrimesInfiniteCollection(currentSieve: LzList[Int]): LzList[Int] = {
      // actually apply sieve for next element
      val primeGoingThroughSieve: Int = currentSieve.head
      val filteredSieve = currentSieve.filter(_ % primeGoingThroughSieve != 0)
      LzCons(primeGoingThroughSieve, generatePrimesInfiniteCollection(filteredSieve))
    }

    val naturalsFrom2 = LzList.generate(2)(n => n + 1)
    generatePrimesInfiniteCollection(currentSieve = naturalsFrom2)
  }

  def eratosthenesDanielOfficial() = {
    def isPrime(n: Int): Boolean = {
      @tailrec
      def isPrimeUntil(t: Int): Boolean =
        if (t <= 1) true
        else n % t != 0 && isPrimeUntil(t - 1)

      isPrimeUntil(n / 2)
    }

    def sieveDanielofficial(numbers: LzList[Int]): LzList[Int] = {
      if (numbers.isEmpty) numbers
      else if (!isPrime(numbers.head)) sieveDanielofficial(numbers.tail)
      else new LzCons[Int](numbers.head, sieveDanielofficial(numbers.tail.filter(_ % numbers.head != 0)))
    }

    val naturalsFrom2 = LzList.generate(2)(_ + 1)
    sieveDanielofficial(naturalsFrom2)
  }
}

object LzListPlayground {
  def isPrime(n: Int): Boolean = {
    @tailrec
    def isPrimeUntil(t: Int): Boolean =
      if (t <= 1) true
      else n % t != 0 && isPrimeUntil(t - 1)

    isPrimeUntil(n / 2)
  }

  def main(args: Array[String]): Unit = {
    val naturals = LzList.generate(1)(n => n + 1) // INFINITE list of natural numbers

    println(naturals.head)
    println(naturals.tail.head)
    println(naturals.tail.tail.head)

    val first50k = naturals.take(50000)
    first50k.foreach(println)
    val first50kList = first50k.toList

    // classics
    println(naturals.map(_ * 2).takeAsList(100))
//    println(naturals.flatMap(x => new LzCons(x, new LzCons(x + 1, LzEmpty()))).takeAsList(10))
    println(naturals.flatMap(x => LzList(x, x + 1)).takeAsList(100))
    println(naturals.filter(_ < 10).takeAsList(9))
//    println(naturals.filter(_ < 10).takeAsList(10)) // crash with SO or infinite recursion

    val combinationsLazy: LzList[String] = for {
      number <- LzList(1,2,3)
      string <- LzList("black", "white")
    } yield s"$number-$string"
    println(combinationsLazy.toList)

    /**
       Exercises:
       1. Lazy list of Fibonacci numbers
        1,2,3,5,8,13,21,34 ...
       2. Infinite list of prime numbers
        - filter with isPrime
        - Eratosthenes' sieve
        [2,3,4,5,6,7,8,9,10,11,12,13,14,15,...]
        [2,3,5,7,9,11,13,15,...]
        [2,3,5,7,11,13,17,19,23,25,29...]
        [2,3,5,7,11,13,17,19,23,29...]

     */

    val fibbonacci = LzList.generateFibonacci(1, 2)
    println(fibbonacci.head)
    println(fibbonacci.tail.head)
    println(fibbonacci.tail.tail.head)
    println(fibbonacci.tail.tail.tail.head)



    val primes = naturals.filter(isPrime).takeAsList(10)
    println(primes)

    // infinite collection primes -> 1,2,3,4,5,6,7,8
    // primes.to(k) = naturals.filter(_ % i != 0), i from 2 to sqrt(k)
    // primes(x) = naturals.filter()

//    println(LzList.sieve)
//    println(LzList.lastSievedPrime)

    val primesUpTo = 100
//    println(LzList.generatePrimesViaSieve(primesUpTo).takeAsList(primesUpTo))
    println(s"Sieve1 method: ${LzList.generatePrimesViaSieve(primesUpTo).takeAsList(primesUpTo).filter(_ <= primesUpTo)}")
    println(s"Sieve2 method: ${LzList.generatePrimesViaSieve2(primesUpTo).takeAsList(primesUpTo).filter(_ <= primesUpTo)}")
    println(s"Sieve3 method: ${LzList.generatePrimesViaSieve3().takeAsList(primesUpTo).filter(_ <= primesUpTo)}")
    println(s"Sieve4 method: ${LzList.generatePrimesViaSieve4().takeAsList(primesUpTo).filter(_ <= primesUpTo)}")
    println(s"EratosthenesDanielOfficial method: ${LzList.eratosthenesDanielOfficial().takeAsList(primesUpTo).filter(_ <= primesUpTo)}")
  }
}
