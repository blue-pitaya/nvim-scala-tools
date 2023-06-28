package devtools

import cats.effect.kernel
import cats.effect.IO

case class State(caseClassDefns: List[CaseClassDefn])
object State {
  def empty = State(caseClassDefns = List())

  type Ref = kernel.Ref[IO, State]
}
