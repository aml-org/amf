package amf.maker

import amf.builder.WebApiBuilder
import amf.common.AMFToken.MapToken
import amf.compiler.Root
import amf.domain.WebApi
import amf.parser.ASTNode
import amf.spec.{ParserContext, Spec}

/**
  * API Documentation maker.
  */
class WebApiMaker(root: Root) extends Maker[WebApi] {

  override def make: WebApi = {
    val builder: WebApiBuilder = WebApiBuilder(root.annotations()).resolveId(root.location)
    val map                    = root.ast > MapToken
    val context                = ParserContext()
    map.children.foreach(matcher(builder, _, context))
    builder.build
  }

  private def matcher(container: WebApiBuilder, entry: ASTNode[_], context: ParserContext): Unit = {
    if (entry.children.nonEmpty) {
      Spec(root.vendor).fields.find(_.matcher.matches(entry)) match {
        case Some(field) => field.parser.parse(field, entry, container, context)
        case _           => // Unknown node...
      }
    }
  }
}

object WebApiMaker {
  def apply(root: Root): WebApiMaker = new WebApiMaker(root)
}
