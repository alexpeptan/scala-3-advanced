package com.rockthejvm.practice

import java.util.Date

// decoupled and extensible type class based JSON Serialization library for a bunch of case classes
object JSONSerialization {
  /*
    Users, posts, feeds
    Serialize to JSON
  */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  /*
    1 - intermediate data: numbers, strings, lists, objects
    2 - type class to convert data to intermediate data
    3 - serialize to JSON
  */

  // part 1 - intermediate data

  sealed trait JSONValue {
    def stringify(): String
  }

  final case class JSONString(value: String) extends JSONValue {
    override def stringify() = "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    override def stringify() = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    override def stringify() = values.map(_.stringify()).mkString("[", ",", "]") // ["string", 3, ... ]
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    override def stringify() = values
      .map {
        case (key, value) => "\"" + key + "\":" + value.stringify()
      }
      .mkString("{", ",", "}")
  }

  /*
    {
      "name": "John",
      "age": 22,
      "friends": [...],
      "latestPost": { ... }
    }
  */

  val data = JSONObject(Map(
    "user" -> JSONString("Alex"),
    "posts" -> JSONArray(List(
      JSONString("Scala is awesome!"),
      JSONNumber(42)
    ))
  ))

  // part 2 - type class pattern
  // 1 - TC definition
  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  // 2 - TC instances for String, Int, Date, User, Post, Feed
  given stringConverter: JSONConverter[String] with
    override def convert(value: String) = new JSONString(value)

  given intConverter: JSONConverter[Int] with
    override def convert(value: Int) = JSONNumber(value)

  given dateConverter: JSONConverter[Date] with
    override def convert(value: Date) = JSONString(value.toString)

  given userConverter: JSONConverter[User] with
    override def convert(user: User) = JSONObject(Map(
//      "name" -> stringConverter.convert(user.name),
      "name" -> JSONConverter[String].convert(user.name), // alternative... to using stringConverter -> allows user to define different converters
//      "age" -> intConverter.convert(user.age),
      "age" -> JSONConverter[Int].convert(user.age),
      "email" -> /*stringConverter*/JSONConverter[String].convert(user.email)
    ))

  given postConverter: JSONConverter[Post] with
    override def convert(post: Post) = JSONObject(Map(
      "content" -> JSONConverter[String].convert(post.content),
      "createdAt" -> JSONConverter[String].convert(post.createdAt.toString)
    ))

  given feedConverter: JSONConverter[Feed] with
    override def convert(feed: Feed) = JSONObject(Map(
//      "user" -> userConverter.convert(feed.user),
      "user" -> JSONConverter[User].convert(feed.user), // decoupled and extensible type class based JSON Serialization library for a bunch of case classes
//      "posts" -> JSONArray(feed.posts.map(postConverter.convert(_)))
      "posts" -> JSONArray(feed.posts.map(JSONConverter[Post].convert(_))) // decoupled and extensible type class based JSON Serialization library for a bunch of case classes
    ))

  // 3 - user-facing API
  object JSONConverter {
    def convert[T](value: T)(using converter: JSONConverter[T]): JSONValue =
      converter.convert(value)

    def apply[T](using instance: JSONConverter[T]): JSONConverter[T] = instance
  }

  // example
  val now = new Date(System.currentTimeMillis())
  val john = User("John", 34, "john@rockthejvm.com")
  val feed = Feed(john, List(
    Post("Hello, I'm learning type classes", now),
    Post("Look at this cute putty", now),
  ))

  // 4 - extension methods
  object JSONSyntax {
    extension [T](value: T)
      def toIntermediate(using converter: JSONConverter[T]): JSONValue =
        converter.convert(value)

      def toJSON(using converter: JSONConverter[T]): String =
        toIntermediate.stringify()
  }

  def main(args: Array[String]): Unit = {
//    println(data.stringify())
    println(JSONConverter.convert(feed).stringify())
    import JSONSyntax.*
    println(feed.toIntermediate.stringify()) // identical
    println(feed.toJSON) // identical - FINAL - ultimate expressiveness!
  }
}
