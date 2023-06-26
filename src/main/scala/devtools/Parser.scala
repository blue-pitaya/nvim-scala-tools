package devtools

import scala.meta._
import java.nio.file.Path
import java.io.File
import java.io.FileInputStream

//nvim lines start at 1, scalameta lines start at 0

case class CursorPos(line: Int, col: Int)

case class CaseClassDefn(name: String, fieldNames: List[String])

object Parser {

  def parseFile(path: String): List[CaseClassDefn] = {
    val source = scala.io.Source.fromFile(path).mkString
    parse(source)
  }

  def parse(file: String): List[CaseClassDefn] = {
    val tree = file.parse[Source].get

    val caseClassDefns = tree
      .collect { case c: Defn.Class =>
        c
      }
      .map { cDefn =>
        cDefn.collect { case c: Mod.Case =>
          cDefn
        }
      }
      .flatten

    caseClassDefns.map { cDefn =>
      val name = cDefn.name.syntax
      val ctor = cDefn.ctor
      val params = ctor.paramClauses.map(_.values).head
      val ps = params.map(p => p.name.value)

      CaseClassDefn(name = name, fieldNames = ps)
    }
  }

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
