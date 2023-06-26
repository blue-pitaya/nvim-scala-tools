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

case class Error(msg: String) extends Throwable

case class State(caseClassDefns: List[CaseClassDefn])
object State {
  def empty = State(caseClassDefns = List())
}

object Main extends IOApp {

  private val pipePath = "/tmp/scala-dev-tools-crack-pipe"

  override def run(args: List[String]): IO[ExitCode] = for {
    stateRef <- Ref.of[IO, State](State.empty)
    _ <- DefnBuilder.initialBuild(args, stateRef)
    _ <- createNamedPipe
    _ <- reciveCommands(pipePath, cmd => cmdCallback(cmd, stateRef))
  } yield (ExitCode.Success)

  def cmdCallback(cmd: String, stateRef: Ref[IO, State]): IO[Unit] = for {
    _ <- IO.println(cmd)
  } yield ()

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
