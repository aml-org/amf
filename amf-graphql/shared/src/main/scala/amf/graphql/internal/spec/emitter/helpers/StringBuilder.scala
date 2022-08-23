package amf.graphql.internal.spec.emitter.helpers

object StringBuilder {
  def apply(parts: String*): String                           = buildStringWithSpaceBetween(removeEmptyParts(parts))
  def build(parts: Seq[String]): String                       = buildStringWithSpaceBetween(removeEmptyParts(parts))
  private def removeEmptyParts(parts: Seq[String])            = parts.filter(_.nonEmpty)
  private def buildStringWithSpaceBetween(parts: Seq[String]) = parts.mkString(" ")
}
