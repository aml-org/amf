package amf.graphql.internal.spec.emitter.helpers
import amf.core.internal.plugins.syntax.StringDocBuilder

case class LineEmitter(builder: StringDocBuilder, parts: String*) {
  def emit(): Unit = {
    val line = StringBuilder.build(parts)
    writeLine(line)
  }
  private def writeLine(line: String): Unit                   = builder += line
}
