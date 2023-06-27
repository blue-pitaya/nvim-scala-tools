package devtools

import cats.effect._
import fs2._
import fs2.io.net.Socket
import fs2.io.net.unixsocket.UnixSocketAddress
import fs2.io.net.unixsocket.UnixSockets

object ConnectionService extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- createServer()
  } yield (ExitCode.Success)

  def createServer(): IO[Unit] = {
    val addr = UnixSocketAddress("/tmp/nvim-scala-tools.sock")
    val server = UnixSockets[IO].server(addr)
    server
      .foreach { socket =>
        IO.println("Socket opened!") >> respond(socket)
      }
      .compile
      .drain
  }

  def respond(s: Socket[IO]): IO[Unit] = for {
    _ <- s
      .reads
      .through(text.utf8.decode)
      .flatMap { str =>
        write(s, str)
      }
      .compile
      .drain
  } yield ()

  def write(s: Socket[IO], t: String) = {
    Stream(s"Hello man! $t").through(text.utf8.encode).through(s.writes)
  }
}
