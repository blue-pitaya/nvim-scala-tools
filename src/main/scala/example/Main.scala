package example

import scala.meta._

object Main extends App {
  val program = """object Main extends App { print("Hello!") }"""
  val tree = program.parse[Source].get.structure

  println(tree)
}
