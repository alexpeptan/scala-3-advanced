package com.rockthejvm.part5ts

import reflect.Selectable.reflectiveSelectable

object StructuralTypes {

  type SoundMaker = { // structural type(ad-hoc type) - defined by it's content, not by its name
    def makeSound(): Unit
  }

  class Dog {
    def makeSound(): Unit = println("bark!")
  }

  class Car {
    def makeSound(): Unit = println("vroom!")
  }

  val dog: SoundMaker = new Dog // ok - since the structure matches de SoundMaker definition
  val car: SoundMaker = new Car
  // compile-time duck typing - if it quacks like a duck, looks like a duck and walks like a duck it's probably a duck :P
  // usually present in dynamic languages - Python / Javascript
  // here the compiler can determine that

  // type refinements
  abstract class Animal {
    def eat(): String
  }

  type WalkingAnimal = Animal { // refined type
    def walk(): Int
  }
  // A proper substitute for WalkingAnimal must be a trait/class that extends Animal and has walk() in its body

  // why: creating type-safe APIs for existing types following the same structure, but no connection to each other
  type JavaCloseable = java.io.Closeable
  class CustomCloseable {
    def close(): Unit = println("ok ok I'm closing")
    def closeSilently(): Unit = println("not making a sound, I promise")
  }

  // We want an API that can handle both a java.Closeable and a CustomCloseable
  // because java.Closeable may be used in a part of the app and CustomCloseable
  // in another. And I want to handle both in the same way

//  def closeResource(closeable: JavaCloseable | CustomCloseable): Unit =
//    closeable.close() // not ok because compile cannot figure out the similarity of structure between the two
  /**
   * value close is not a member of com.rockthejvm.part5ts.StructuralTypes.JavaCloseable |
   *  com.rockthejvm.part5ts.StructuralTypes.CustomCloseable - did you mean closeable.clone?
   */

  // solution: structural type
  type UnifiedCloseable = {
    def close(): Unit
  }

  def closeResource(closeable: UnifiedCloseable): Unit = closeable.close()
  val jCloseable = new JavaCloseable {
    override def close(): Unit = println("closing Java resource")
  }
  val cCloseable = new CustomCloseable

  def closeResource_v2(closeable: { def close(): Unit}): Unit = closeable.close()

  def main(args: Array[String]): Unit = {
    dog.makeSound() // through reflection - costly(slow) -> needs to detect the presence of makeSound() method at runtime, before being invoked.
    car.makeSound()

    closeResource(jCloseable)
    closeResource(cCloseable)
  }
}
