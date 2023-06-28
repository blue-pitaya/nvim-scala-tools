package devtools

import cats.effect._
import fs2._
import fs2.io.net.Socket
import fs2.io.net.unixsocket.UnixSocketAddress
import fs2.io.net.unixsocket.UnixSockets

object ConnectionService {
  private val socketAddr = "/tmp/nvim-scala-tools.sock"

  def startServer(respondFn: String => IO[String]): IO[Unit] = socketStream()
    .foreach(s => respond(s, respondFn))
    .compile
    .drain

  private def socketStream() = UnixSockets[IO]
    .server(UnixSocketAddress(socketAddr))

  private def respond(s: Socket[IO], fn: String => IO[String]): IO[Unit] = s
    .reads
    .through(text.utf8.decode)
    .evalMap(fn)
    .flatMap(str => Stream(str).through(text.utf8.encode).through(s.writes))
    .compile
    .drain
}
