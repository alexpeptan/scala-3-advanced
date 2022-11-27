package com.rockthejvm.part4context

object Implicits {

  // giving/using clauses - the ability to pass arguments automatically(implicitly) by the compiler
  trait Semigroup[A] {
    def combile(x: A, y: A): A
  }

  def combineAll[A](list: List[A])(/*using*/implicit semigroup: Semigroup[A]): A =
    list.reduce(semigroup.combile)

  /*given*/implicit val intSemigroup: Semigroup[Int] /*with*/= new Semigroup[Int] {
    override def combile(x: Int, y: Int) = x + y
  }

  val sumOf10 = combineAll((1 to 10).toList)

  // implicit agr -> using clause
  // implicit val -> given declaration

  // extension methods = implicit class in Scala 2 (supported in Scala 3)
  // implicit class in Scala 2 -> extension zones(methods) - in Scala 3
  // Scala 3 version:
  extension (number: Int)
    def isEven = number % 2 == 0
  val is23Even =  23.isEven
  // Scala 2 version:
  implicit class MyRichInteger(number: Int) {
    // exrtension methods here
    def isEven = number % 2 == 0
  }

  val questionOfMyLife = 23.isEven // new MyRichInteger(23).isEven

  // implicit conversions
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  implicit def string2Person(x: String): Person = Person(x)
  val danielSaysHi = "Daniel".greet() // string2Person("Daniel").greet()

  // If I want to create an implicit semigroup of all Option types
  // if I have a semigroup of a plain type in scope:

  // implicit def => synthesize NEW implicit values
  implicit def semigroupOfOption[A](implicit semigroup: Semigroup[A]) = new Semigroup[Option[A]] {
    override def combile(x: Option[A], y: Option[A]) = for {
      valueX <- x
      valueY <- y
    } yield semigroup.combine(valueX, valueY)
  }

  // given semigroupOfOption[A](using semigroup: Semigroup[A]): Semigroup[Option[A]] with ...

  /*
    Why implicits will be phased out:
    - the implicit keyword has many different meanings
    - conversions are easy to abuse
    - implicits are very hard to track down while debugging (givens also not trivial, but they are explicitly improved)
  */

  // organizing implicits == organizing contextual abstractions
  // import yourPackage.* // also imports implicits -> probably the only practical difference between given/using clauses and implicits

  /*
    Contextual abstractions:
    - given/using clauses
    - extension methods
    - explicitly declared implicit conversions
  */
  def main(args: Array[String]): Unit = {
    println(sumOf10)
  }
}
