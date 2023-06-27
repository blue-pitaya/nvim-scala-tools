package devtools

import cats.effect.kernel.Ref
import cats.effect.IO
import scala.meta._

sealed trait Action
case class InsertCaseClassFields(caseClassName: String, replacedLine: String)
    extends Action

object CmdService {
  // inclusive start, exclusive end
  case class Word(v: String, start: Int, end: Int)

  def getActionsForLine(
      line: String,
      cursorPos: Int,
      state: State
  ): List[Action] = {
    val actionsOpt = for {
      wordUnderCursor <- getWordUnderCursor(line, cursorPos)
      caseClassName = wordToCaseClassName(wordUnderCursor.v)
      caseClassDefns = state.caseClassDefns.filter(v => v.name == caseClassName)
      insertCaseClassFieldsActions = caseClassDefns.map(v =>
        InsertCaseClassFields(
          caseClassName = caseClassName,
          replacedLine = getReplacedLine(line, v, wordUnderCursor)
        )
      )
    } yield (insertCaseClassFieldsActions)

    actionsOpt.getOrElse(List())
  }

  def getReplacedLine(
      originalLine: String,
      caseClassDefn: CaseClassDefn,
      word: Word
  ): String = {
    val newCaseClassDefn =
      transformCaseClassDefn(word.v, caseClassDefn.fieldNames)
    replaceOnRange(originalLine, word.start, word.end, newCaseClassDefn)
  }

  def replaceOnRange(
      s: String,
      start: Int,
      end: Int,
      replacement: String
  ): String = {
    val firstPart = s.take(start)
    val secondPart = s.drop(end + 1)
    firstPart ++ replacement ++ secondPart
  }

  def transformCaseClassDefn(text: String, fields: List[String]): String = {
    val stat = text.parse[Stat].get
    stat
      .transform { case t: Term.ArgClause =>
        val termAssigns = fields
          .map(v => Term.Assign(Term.Name(v), Term.Name("???")))
        Term.ArgClause(termAssigns, None)
      }
      .syntax
  }

  private def wordToCaseClassName(word: String): String = {
    if (word.endsWith("()")) word.slice(0, word.size - 2)
    else word
  }

  private def getWordUnderCursor(line: String, pos: Int): Option[Word] = {
    val words = splitLineWithRange(line)
    words.find(w => pos >= w.start && pos < w.end)
  }

  def splitLineWithRange(line: String): List[Word] = {
    def takeWord(
        line: List[Char],
        pos: Int,
        curr: List[Char] = List()
    ): (List[Char], Int, List[Char]) = line match {
      case head :: tail if head != ' ' =>
        takeWord(tail, pos + 1, curr.appended(head))
      case head :: tail => (tail, pos, curr)
      case _            => (Nil, pos, curr)
    }

    def takeWords(
        line: List[Char],
        start: Int = 0,
        words: List[Word] = List()
    ): List[Word] = line match {
      case head :: _ =>
        val (rest, end, word) = takeWord(line, start)
        val w = Word(word.mkString, start, end)
        takeWords(rest, end + 1, words.appended(w))
      case Nil => words
    }

    takeWords(line.toCharArray().toList).filter(w => !w.v.isEmpty())
  }
}
