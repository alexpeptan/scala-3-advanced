package com.rockthejvm.part4context

object OrganizingCAs {

  val aList = List(2,3,1,4)
  val anOrderedList = aList.sorted

  // compiler fetches givens/EMs
  // 1 - local scope
  given reverseOrdering: Ordering[Int] with
    override def compare(x: Int, y: Int) = y - x

  // 2 - imported scope
  case class Person(name: String, age: Int)
  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 17)
  )

  object PersonGivens {
    given ageOrdering: Ordering[Person] with
      override def compare(x: Person, y: Person) = y.age - x.age

    extension (p: Person)
      def greet(): String = s"Heya, I'm ${p.name}. I'm so glad to meet you!"
  }

  // a - import explicitely
//  import PersonGivens.ageOrdering

  // b - import a given for a particular type
  import PersonGivens.{given Ordering[Person]}

  // c - import all givens
//  import PersonGivens.given
  // waring: import PersonGivens.* does NOT also import given instances!
//  import PersonGivens.*

  // 3 - companion of all types involved in method signature
  /*
    - Ordering
    - List
    - Person
   */
  // def sorted[B >: A](implicit ord: Ordering[B]): List[B]
  object Person {
    given byNameOrdering: Ordering[Person] with
      override def compare(x: Person, y: Person) = x.name.compareTo(y.name)

    extension (p: Person)
      def greet(): String = s"Hello, I'm ${p.name}"
  }

  val sortedPersons = persons.sorted

  /*
    Good practice tips:
    1) When you have a "default" given (only ONE that makes sense) add it in the companion object of the type.
    2) When you have MANY possible givens, but ONE that is dominant (most used), add it in the companion object and the rest in another object.
    3) When you have MANY possible givens, but NO ONE is dominant, add them in separate objects and import them explicitly.
  */

  // Same principles apply to the extension methods as well

  /**
   *  Exercises. Create given instances for Ordering[Purchase]
   *  - ordering by total price = 50% of our codebase
   *  - ordering by unit count, descending = 25% of the code base
   *  - ordering by unit price, ascending = 25% of the code base
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
//    given totalPriceOrdering: Ordering[Purchase] with
////      override def compare(x: Purchase, y: Purchase) = y.nUnits * y.unitPrice - x.nUnits * x.unitPrice
//
//      override def compare(x: Purchase, y: Purchase) = {
//        val xTotalPrice = x.nUnits * x.unitPrice
//        val yTotalPrice = y.nUnits * y.unitPrice
//
//        if (xTotalPrice == yTotalPrice) 0
//        else if (xTotalPrice < yTotalPrice) -1
//        else 1
//    }
  }

  object UnitCountOrdering {
    given unitCountDescendingOrdering: Ordering[Purchase] =
      Ordering.fromLessThan((x, y) => y.nUnits > x.nUnits)

    // need some casting from Double to Int
    // However, we'd rather have one object for each non-dominant ordering method
//    given unitPriceAscendingOrdering: Ordering[Purchase] with
//      override def compare(x: Purchase, y: Purchase) = x.unitPrice - y.unitPrice
  }

  object UnitPriceOrdering {
    given unitPricePrdering: Ordering[Purchase] = Ordering.fromLessThan((x, y) => y.unitPrice < x.unitPrice)
  }

  val purchases: List[Purchase] = List(Purchase(10, 2.99), Purchase(1, 1000000), Purchase(3, 80))

  def main(args: Array[String]): Unit = {
//    println(anOrderedList)
//    println(sortedPersons)
//    import PersonGivens.* // includes extension methods
//    println(Person("Alex", 99).greet())
//    import UnitCountOrdering.unitCountDescendingOrdering
    import UnitPriceOrdering.given
    println(purchases.sorted)
  }
}
