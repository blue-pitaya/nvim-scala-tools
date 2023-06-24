package devtools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFlatSpec with Matchers {
  private val exampleContent = """
  |package devtools.examples
  |
  |object ExampleObject {
  |
  |  def hello(): String = {
  |    val x = SomeTool()
  |
  |    "ok!"
  |  }
  |}
  """.stripMargin.trim()

  "test" should "be good" in {
    val result = Parser.symbolAt(exampleContent, CursorPos(line = 5, col = 15))

    println(result)
  }
}
