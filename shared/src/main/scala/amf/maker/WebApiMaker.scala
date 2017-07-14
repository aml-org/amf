package amf.maker

import amf.builder.APIDocumentationBuilder
import amf.domain.APIDocumentation
import amf.parser.{AMFUnit, ASTNode}
import amf.spec.Spec

/**
  * API Documentation maker.
  */
class WebApiMaker(unit: AMFUnit) extends Maker[APIDocumentation] {

  def matcher(builder: APIDocumentationBuilder, entry: ASTNode[_]): Unit = {
    Spec(unit.vendor).fields.find(_.matcher.matches(entry)) match {
      case Some(field) => field.parse(field, entry, builder)
      case _           => // Unknown node...
    }
  }

  override def make: APIDocumentation = {
    val builder: APIDocumentationBuilder = APIDocumentationBuilder()
    val root                             = unit.root.children.head

    root.children.foreach(matcher(builder, _))
    builder.build
  }
}

object WebApiMaker {
  def apply(unit: AMFUnit): WebApiMaker = new WebApiMaker(unit)
}
