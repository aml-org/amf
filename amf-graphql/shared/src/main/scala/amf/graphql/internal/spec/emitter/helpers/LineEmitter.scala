package amf.graphql.internal.spec.emitter.helpers
import amf.core.internal.plugins.syntax.StringDocBuilder
import org.mulesoft.common.client.lexical.Position

case class LineEmitter(builder: StringDocBuilder, parts: String*) {
  def emit(): Unit = {
    val line = buildStringWithSpaceBetween(removeEmptyParts())
    writeLine(line)
  }
  private def removeEmptyParts()                              = parts.filter(_.nonEmpty)
  private def buildStringWithSpaceBetween(parts: Seq[String]) = parts.mkString(" ")
  private def writeLine(line: String): Unit                   = builder += line
}
