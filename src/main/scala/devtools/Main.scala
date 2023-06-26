package devtools

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import fs2._
import fs2.io.file.Files
import fs2.io.file.Flags
import fs2.io.file.Path
import fs2.io.file.PosixPermission
import fs2.io.file.PosixPermissions
import fs2.io.process.ProcessBuilder

import scala.io.Source
import cats.effect.kernel.Ref
import devtools.Cmd.GetCaseClassFields

case class Error(msg: String) extends Throwable {
  override def toString(): String = msg
}

case class State(caseClassDefns: List[CaseClassDefn])
object State {
  def empty = State(caseClassDefns = List())
}

sealed trait Cmd
object Cmd {
  final case class GetCaseClassFields(name: String, filePath: String)
      extends Cmd

  def parse(value: String): Option[Cmd] = {
    val words = value.split(" ").toList

    words match {
      case cmd :: param :: path :: Nil if cmd == "get_case_class_fields" =>
        Some(GetCaseClassFields(param, path))
      case _ => None
    }
  }
}

object Main extends IOApp {

  private val pipePath = "/tmp/scala-dev-tools-crack-pipe"

  override def run(args: List[String]): IO[ExitCode] = for {
    stateRef <- Ref.of[IO, State](State.empty)
    _ <- DefnBuilder.initialBuild(args, stateRef)
    _ <- createNamedPipe
    _ <- reciveCommands(pipePath, cmd => cmdCallback(cmd, stateRef))
  } yield (ExitCode.Success)

  def cmdCallback(cmdStr: String, stateRef: Ref[IO, State]): IO[Unit] = {
    val cmdOpt = Cmd.parse(cmdStr)

    cmdOpt match {
      case Some(cmd) => executeCmd(cmd, stateRef)
      // dont raise error, it's really not important
      case None => IO.println("Error: unknown command.")
    }
  }

  def executeCmd(cmd: Cmd, stateRef: Ref[IO, State]): IO[Unit] = cmd match {
    case GetCaseClassFields(name, filePath) => for {
        caseClassDefnOpt <- getCaseClassDefn(name, filePath, stateRef)
        _ <- caseClassDefnOpt match {
          case None => IO.println(
              s"Error: case class definition for ${name} not found in path: ${filePath}"
            )
          case Some(defn) =>
            val fieldNames = defn.fieldNames.mkString(",")
            IO.println(s"ok(${fieldNames})")
        }
      } yield ()
  }

  def getCaseClassDefn(
      name: String,
      filePath: String,
      stateRef: Ref[IO, State]
  ): IO[Option[CaseClassDefn]] = for {
    state <- stateRef.get
    defnOpt =
      state.caseClassDefns.find(v => v.name == name && v.sourcePath == filePath)
  } yield (defnOpt)

  def createNamedPipe: IO[Unit] = for {
    fileExists <- Files[IO].exists(Path(pipePath))
    _ <- IO.whenA(!fileExists)(mkfifo)
  } yield ()

  def mkfifo: IO[Unit] = for {
    exitCode <- ProcessBuilder("mkfifo", pipePath).spawn[IO].use(_.exitValue)
    _ <- IO.raiseWhen(exitCode != 0)(
      Error(s"Faile making named pipe in path ${pipePath}")
    )
  } yield ()

  def reciveCommands(pipePath: String, cb: String => IO[Unit]): IO[Unit] = {
    Stream
      .repeatEval(IO(Source.fromFile(pipePath).iterator))
      .map(iter => iter.mkString)
      .through(text.lines)
      .foreach(cb)
      .compile
      .drain
  }
}
