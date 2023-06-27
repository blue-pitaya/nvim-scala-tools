package devtools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParsingSpec extends AnyFlatSpec with Matchers {
  "transformCaseClassDefn" should "be ok" in {
    val word = "CaseClass()"
    val fields = List("a", "b")
    val expected = "CaseClass(a = ???, b = ???)"

    CmdService.transformCaseClassDefn(word, fields) shouldEqual expected
  }

  "splitLineWithRange" should "be ok" in {
    import CmdService._
    val line = "  abc  df  e "
    val expected = List(Word("abc", 2, 5), Word("df", 7, 9), Word("e", 11, 12))
    val result = CmdService.splitLineWithRange(line)

    result shouldEqual expected
  }
}
