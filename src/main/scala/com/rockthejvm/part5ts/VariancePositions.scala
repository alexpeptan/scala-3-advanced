package com.rockthejvm.part5ts

object VariancePositions {

  class Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  /**
   * 1 - type bounds
   */

  class Cage[A <: Animal] // A must be a subtype of Animal.    As said earlier in video:   A <: Animal ---> type A "must conform to" the Animal type
  val aRealCage = new Cage[Dog] // ok, because Dog <: Animal ---> "because Dog is a subtype of Animal"
//  val aCage = new Cage[String] // not ok, because String is not a subtype of animal

  class WeirdContainer[A >: Animal] // A must be a supertype of Animal

  /**
   * 2 - variance positions
   */

  // type of val fields are in COVARIANT positions
  //  class Vet[-T](val favoriteAnimal: T)

  /*
    val garfield = new Cat
    val theVet: Vet[Animal] = Vet[Animal](garfield)
    val aDogVet: Vet[Dog] = theVet // possible, theVet is Vet[Animal]
    val aDog: Dog = aDogVet.favoriteAnimal // must be a Dog - type conflict!
  */

  // types of var fields are in COVARIANT positions
  // (same reason)

  // types of var fields are in CONTRAVARIANT position
//  class MutableOption[+T](var contents: T)


  /*
    val maybeAnimal: MutableOption[Animal] = new MutableOption[Dog](new Dog)
    maybeAnimal.contents = new Cat // type conflict
  */

  // types of method arguments are in CONTRAVARIANT POSITION
//  class MyList[+T] {
//    def add(element: T): MyList[T] = ???
//  }

  /*
    val animals: MyList[Animal] = new MyList[Cat]
    val biggerListOfAnimals = animals.add(new Dog) // type conflict!
  */

  // method return types are in COVARIANT positions
//  abstract class Vet2[-T] {
//    def rescueAnimal(): T
//  }

  /*
    val vet: Vet2[Animal] = new Vet2[Animal] {
      override def rescueAnimal(): Animal = new Cat
    }
    val lassiesVet: Vet2[Dog] = vet // Vet2[Animal]
    val rescueDog: Dog = lassiesVet.rescueAnimal() // must return a Dog, returns a CAt - type conflict!
  */

  class Vet[-T] {
    def heal(animal: T): Boolean = true
  }

  /**
   * 3 - solving variance positions problems
   */
  abstract class LList[+A] {
    def head: A
    def tail: LList[A]
    def add[B >: A](element: B): LList[B] // widen the type
  }

  // val animals = list of cats
  // val newAnimals: List[Animals] = animals.add(new Dog)

  class Vehicle
  class Car extends Vehicle
  class Supercar extends Car
  class RepairShop[-A <: Vehicle] {
    def repair[B](vehicle: B): B = vehicle // narrowing the type
  }

  val myRepairShop: RepairShop[Car] = new RepairShop[Vehicle]
  val myBeatupVW = new Car
  val freshCar = myRepairShop.repair(myBeatupVW) // works, and returns a car
  val damagedFerrari = new Supercar
  val freshFerrari = myRepairShop.repair(damagedFerrari) // works, returns a Supercar

  def main(args: Array[String]): Unit = {

  }
}
