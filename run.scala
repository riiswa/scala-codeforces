//> using scala "3"
//> using lib "dev.zio::zio:2.0.5"
//> using lib "org.jsoup:jsoup:1.15.3"
//> using lib "dev.zio::zio-process:0.7.1"

import zio._
import zio.process._
import zio.internal.Blocking
import zio.Duration._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import collection.JavaConverters._
import scala.sys.process._

object App extends ZIOAppDefault:
  def parseArgs(args: Chunk[String]): IO[IllegalArgumentException, String] =
    ZIO
      .fromOption(args.headOption)
      .mapError(_ =>
        new IllegalArgumentException("Please provide a path as an argument")
      )
      .filterOrFail(arg => """\d+\/[A-Z]""".r.matches(arg))(
        new IllegalArgumentException(
          """The provided argument should be a valid path (\d+\/[A-Z])."""
        )
      )

  def connect(path: String): Task[Document] =
    ZIO.attempt(
      Jsoup.connect(s"https://codeforces.com/problemset/problem/${path}").get()
    )

  def getText(doc: Document)(selector: String): Task[String] =
    ZIO.attempt(doc.select(selector).asScala.map(_.text).mkString("\n"))

  def run(path: String, input: String): IO[CommandError, Chunk[String]] =
    (Command("scala-cli", s"$path.scala") << input).lines

  def check(output: Chunk[String], expected: String): UIO[Boolean] =
    ZIO.succeed(output.mkString("\n").endsWith(expected))

  def run = for {
    args <- getArgs
    arg <- parseArgs(args)
    doc <- connect(arg)
    getTextDoc = getText(doc) _
    input <- getTextDoc(".input > pre > div")
    expected <- getTextDoc(".output > pre")
    output <- run(arg, input)
    valid <- check(output, expected)
    _ <- Console.printLine(valid)
  } yield ()
