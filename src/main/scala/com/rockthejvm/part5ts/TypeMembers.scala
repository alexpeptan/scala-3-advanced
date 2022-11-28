package com.rockthejvm.part5ts

// extremely useful as part of ZIO and Cats frameworks

object TypeMembers {
  class Animal
  class Dog extends Animal
  class Cat extends Animal

  class AnimalCollection {
    // val, var, def, class, trait, object
    type AnimalType // abstract type member
    type BoundedAnimal <: Animal // abstract type member with a type bound
    type SuperBoundedAnimal >: Dog <: Animal
    type AnimalAlias = Cat // type alias
    type NestedOption = List[Option[Option[Int]]] // often used to alias complex/nested types
  }

  // using type members
  // needing an instance of the class where they are defined
  // each type is one concrete type
  val ac = new AnimalCollection
  val anAnimal: ac.AnimalType = ???

//  val cat: ac.BoundedAnimal = new Cat // BoundedAnimal might be dog - and compiler cannot guarantee type checking for this abstract type
  val aDog: ac.SuperBoundedAnimal = new Dog // ok, because Dog <: SuperBoundedAnimal (and, I assume, Dog <: Dog - not mentioned in training - <: and >: admit equality)
  val aCat: ac.AnimalAlias = new Cat // ok, Cat == AnimalAlias

  // - establish relationships between types
  // - be able to reuse same piece of code on multiple unrelated types -> same goal as generics
  // - abstract type members can be thought of as an
  // alternative to generics

  class LList[T] {
    def add(element: T): LList[T] = ???
  }

  class MyList {
    type T
    def add(element: T): MyList = ???
  }

  // .type
  type CatType = aCat.type
  val newCat: CatType = aCat

//  class MoreConcreteAnimalCollection extends AnimalCollection {
//    override type AnimalType = Dog
//  }

  def main(args: Array[String]): Unit = {

  }
}
