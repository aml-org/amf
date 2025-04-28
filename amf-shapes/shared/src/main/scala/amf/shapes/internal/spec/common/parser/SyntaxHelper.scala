package amf.shapes.internal.spec.common.parser

object SyntaxHelper {

  def add(syntax: Map[String, Set[String]], key: String, values: Set[String]): Map[String, Set[String]] = {
    val nextSet = syntax.getOrElse(key, Set.empty) ++ values
    syntax + (key -> nextSet)
  }

  def add(syntax: Map[String, Set[String]], pairs: (String, Set[String])*): Map[String, Set[String]] = {
    pairs.foldLeft(syntax) { case (acc, (key, values)) =>
      val nextSet = acc.getOrElse(key, Set.empty) ++ values
      acc + (key -> nextSet)
    }
  }
}
