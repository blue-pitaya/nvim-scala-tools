package devtools

import scala.meta._

//nvim lines start at 1, scalameta lines start at 0

case class CursorPos(line: Int, col: Int)

object Parser {
  def getCaseClassFields(path: String, cursorPos: CursorPos): List[String] = ???

  def symbolAt(fileContent: String, cursorPos: CursorPos) = {
    val tree = fileContent.parse[Source].get
    println(tree.structure)
    tree.traverse { case t: Term.Name =>
      val startLine = t.pos.startLine
      val endLine = t.pos.endLine
      val startPos = t.pos.startColumn
      val endPost = t.pos.endColumn

      if (
        startLine == endLine && startLine == cursorPos.line &&
        startPos <= cursorPos.col && endPost > cursorPos.col
      ) println("!!!")

      val x = s"$startLine $endLine $startPos $endPost ::: $t"
      println(x)
    }
  }
}
