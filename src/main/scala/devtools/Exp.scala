package devtools

import java.nio.file.Files
import java.nio.file.Paths
import scala.meta._
import scala.meta.internal.semanticdb.SymbolInformation.Kind.PARAMETER
import scala.meta.internal.semanticdb.TextDocument
import scala.meta.internal.semanticdb.TextDocuments

object Exp extends App {
  val program = """object Main extends App { print("Hello!") }"""
  val tree = program.parse[Source].get.structure

  val filePath =
    "/home/kodus/projects/scala-dev-tools/target/scala-2.13/meta/META-INF/semanticdb/src/main/scala/devtools/examples/CaseClasses.scala.semanticdb"
  val fileBytes = Files.readAllBytes(Paths.get(filePath))

  val x = TextDocuments.parseFrom(fileBytes)
  x.documents
    .foreach { (doc: TextDocument) =>
      println(doc.uri)
      doc
        .symbols
        .foreach { symbolInfo =>
          if (symbolInfo.isCase && symbolInfo.isClass) {
            // selects only case classes :)
            println(s"case class: ${symbolInfo.symbol}")
          }

          if (symbolInfo.symbol.contains("<init>")) {
            println(symbolInfo.symbol)
          }

        // symbolInfo.kind match {
        //  case PARAMETER =>
        //    println("Parameter:")
        //    println(symbolInfo.symbol)
        //  case _ => ()
        // }
        }
    }
}
