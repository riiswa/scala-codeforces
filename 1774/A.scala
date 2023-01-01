//> using scala "2.12"

import scala.io.StdIn.{readInt, readLine}

object Solution extends App {
  (1 to readInt).foreach { _ =>
    val n = readInt
    val result = readLine.map(_ - '0').toList match {
      case head :: next =>
        next.foldLeft((head, "")) {
          case ((1, output), 1) => (0, output + "-")
          case ((v, output), x) =>
            (v + x, output + "+")
        }
      case Nil => ('0', "")
    }
    println(result._2)
  }
}
