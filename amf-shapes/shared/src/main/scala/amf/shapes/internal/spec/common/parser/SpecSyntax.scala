package amf.shapes.internal.spec.common.parser

/** Created by pedro.colunga on 11/9/17.
  */
trait SpecSyntax {
  val nodes: Map[String, Set[String]]
}

object SpecSyntax {
  val empty: SpecSyntax = new SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map.empty
  }
}
