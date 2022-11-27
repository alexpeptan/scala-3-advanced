package com.rockthejvm.practice

import java.util.Date

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
  // 2 - TC instances for User, Post, Feed
  // 3 - user-facing API
  // 4 - extension methods

  def main(args: Array[String]): Unit = {
    println(data.stringify())
  }
}
