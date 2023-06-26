package devtools

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel
import cats.effect.kernel.Resource
import cats.syntax.all._

import java.io.BufferedReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.meta._

//TODO: rewrite to fs2 file
object DefnBuilder {
  def initialBuild(
      args: List[String],
      stateRef: kernel.Ref[IO, State]
  ): IO[Unit] = for {
    caseClassDefns <- buildCaseClassDefns(args)
    _ <- stateRef.update(_.copy(caseClassDefns = caseClassDefns))
  } yield ()

  private def buildCaseClassDefns(args: List[String]): IO[List[CaseClassDefn]] =
    for {
      path <- getPath(args)
      scalaFiles <- findScalaFiles(path)
      caseClassDefns = scalaFiles
        .flatMap(path => Parser.parseFile(path.toString()))
        .toList
    } yield (caseClassDefns)

  private def getPath(args: List[String]): IO[Path] = args match {
    case head :: tail => IO.pure(head).map(Paths.get(_).toAbsolutePath())
    case Nil          => IO.raiseError(Error("Invalid arguments."))
  }

  private def findFiles(path: Path): IO[Iterator[Path]] =
    IO(Files.walk(path).iterator().asScala)

  private def isScalaFile(path: Path): Boolean = Files.isRegularFile(path) &&
    path.toString().endsWith(".scala")

  private def findScalaFiles(path: Path): IO[Iterator[Path]] = for {
    files <- findFiles(path)
    scalaFiles = files.filter(isScalaFile)
  } yield (scalaFiles)

  private def printlnScalaFiles(files: Iterator[Path]): IO[Unit] = files
    .toList
    .map(p => IO.println(p.toString()))
    .sequence_
}
