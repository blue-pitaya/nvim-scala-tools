package devtools

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import fs2._
import fs2.io.file.Files
import fs2.io.file.Flags
import fs2.io.file.Path
import fs2.io.file.PosixPermission
import fs2.io.file.PosixPermissions
import fs2.io.process.ProcessBuilder

import java.io.PrintWriter
import scala.io.Source
import devtools.Cmd.GetActionsForLine
import devtools.Cmd.Test

case class Error(msg: String) extends Throwable {
  override def toString(): String = msg
}

sealed trait Cmd
object Cmd {
  final case class GetActionsForLine(line: String, cursorPos: Int) extends Cmd
  final case class Test(arg: String) extends Cmd

  def parse(value: String): Option[Cmd] = {
    val lines = value.split('\n').toList

    lines match {
      case cmd :: line :: cursorPos :: Nil if cmd == "get_actions_for_line" =>
        cursorPos.toIntOption.map(curPos => GetActionsForLine(line, curPos))
      case cmd :: arg :: Nil if cmd == "test" => Some(Test(arg))
      case _                                  => None
    }
  }
}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for {
    stateRef <- Ref.of[IO, State](State.empty)
    _ <- DefnBuilder.initialBuild(args, stateRef)
    respFn = cmdCallback(stateRef) _
    _ <- ConnectionService.startServer(respFn)
  } yield (ExitCode.Success)

  private def cmdCallback(stateRef: State.Ref)(req: String): IO[String] =
    Cmd.parse(req) match {
      case Some(cmd) => for {
          response <- handleCmd(cmd, stateRef)
          _ <- IO.println("Req success.")
        } yield (response)
      case None => errorRepsonse()
    }

  private def handleCmd(cmd: Cmd, stateRef: State.Ref): IO[String] = cmd match {
    case GetActionsForLine(line, cursorPos) => for {
        _ <- IO.println("Executing cmd: get_actions_for_line")
        state <- stateRef.get
        actions = CmdService.getActionsForLine(line, cursorPos, state)
        actionsResponse = getActionsResponse(actions)
        output = "ok\n" + actionsResponse
        _ <- IO.println("Response: \n" + output)
      } yield (output)
    case Test(arg) => IO.pure(s"Arg for test: $arg")
  }

  private def errorRepsonse(): IO[String] = IO.pure("error")

  private def getActionsResponse(actions: List[Action]): String = actions
    .map(a =>
      a match {
        case InsertCaseClassFields(caseClassName, replacedLine) =>
          caseClassName + "\n" + replacedLine
      }
    )
    .mkString("\n")
}
