package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import org.mulesoft.common.client.lexical.Position

case class GraphQLDescriptionEmitter(
    description: Option[String],
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder,
    pos: Option[Position] = None
) {
  def emit(): Unit = {
    description match {
      case Some(desc) =>
        if (pos.isDefined) {
          b.+=("\"\"\"", pos.get)
        } else {
          b.+=("\"\"\"")
        }
        b.+=(desc)
        b.+=("\"\"\"")
      case _ => // ignore
    }
  }
}
