package amf.apicontract.internal.spec.async.parser.context

package object syntax {

  def add(syntax: Map[String, Set[String]], key: String, values: Set[String]): Map[String, Set[String]] = {
    val nextSet = syntax.getOrElse(key, Set.empty) ++ values
    syntax + (key -> nextSet)
  }
}
