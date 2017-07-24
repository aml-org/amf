package amf.maker

import amf.builder.WebApiBuilder
import amf.common.AMFToken.MapToken
import amf.compiler.Root
import amf.domain.WebApi
import amf.parser.ASTNode
import amf.spec.Spec

/**
  * API Documentation maker.
  */
class WebApiMaker(root: Root) extends Maker[WebApi] {

  override def make: WebApi = {
    val builder: WebApiBuilder = WebApiBuilder()
    val map                    = root.ast > MapToken
    map.children.foreach(matcher(builder, _))
    builder.build
  }

  private def matcher(builder: WebApiBuilder, entry: ASTNode[_]): Unit = {
    if (entry.children.nonEmpty) {
      Spec(root.vendor).fields.find(_.matcher.matches(entry)) match {
        case Some(field) => field.parser(field, entry, builder)
        case _           => // Unknown node...
      }
    }
  }
}

object WebApiMaker {
  def apply(root: Root): WebApiMaker = new WebApiMaker(root)
}
