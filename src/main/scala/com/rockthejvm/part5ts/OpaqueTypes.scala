package com.rockthejvm.part5ts

object OpaqueTypes {

  object SocialNetwork {
    // some data structures = "domain"
    opaque type Name = String // only applied to type definitions / aliases

    object Name {
      def apply(str: String): Name = str
    }

    extension (name: Name)
      def length: Int = name.length // use the String API

    // inside, Name <-> String
    def addFriend(person1: Name, person2: Name): Boolean =
      person1.length == person2.length // use the entire String API
  }

  // outside SocialNetwork, Name and String are NOT related
  import SocialNetwork.*
//  val name: Name = "Alex" // will not compile

  // why: you don't need/want to have access to the entire String API for the Name type

  object Graphics {
    opaque type Color = Int // in hex
    opaque type ColorFilter <: Color = Int

    val Red: Color = 0xFF000000
    val Green: Color = 0x00FF0000
    val Blue: Color = 0x0000FF00
    val halfTransparency: ColorFilter = 0x88 // 50%
  }

  import Graphics.*
  case class OverlayFilter(c: Color)
  val fadeLayer = OverlayFilter(halfTransparency) // ColorFilter <: Color -> so compiler sees this as being ok
  // - as long as the type relationships are satisfied - and they are

  // how can we create instances of opaque types + how to access their APIs?
  // 1 - companion objects
  val aName = Name("Daniel")
  // 2 - extension methods
  val nameLength = aName.length // ok, because of the extension method

  def main(args: Array[String]): Unit = {

  }
}
