package amf.maker

import amf.builder.APIDocumentationBuilder
import amf.compiler.Root
import amf.domain.APIDocumentation
import amf.parser.ASTNode
import amf.spec.Spec

/**
  * API Documentation maker.
  */
class WebApiMaker(root: Root) extends Maker[APIDocumentation] {

  override def make: APIDocumentation = {
    val builder: APIDocumentationBuilder = APIDocumentationBuilder()
    val map                              = root.ast.children.head
    map.children.foreach(matcher(builder, _))
    builder.build
  }

  private def matcher(builder: APIDocumentationBuilder, entry: ASTNode[_]): Unit = {
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
