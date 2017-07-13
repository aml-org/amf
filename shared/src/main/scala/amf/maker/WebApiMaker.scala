package amf.maker

import amf.builder.BaseWebApiBuilder
import amf.model.BaseWebApi
import amf.parser.{AMFUnit, ASTNode}
import amf.spec.Spec

/**
  * Domain model WebApi Maker.
  */
class WebApiMaker(unit: AMFUnit) extends Maker[BaseWebApi] {

  def matcher(builder: BaseWebApiBuilder, entry: ASTNode[_]): Unit = {
    Spec(unit.vendor).fields.find(_.matcher.matches(entry)) match {
      case Some(field) => field.parse(field, entry, builder)
      case _           => // Unknown node...
    }
  }

  override def make: BaseWebApi = {
    val builder: BaseWebApiBuilder = builders.webApi
    val root                       = unit.root.children.head

    root.children.foreach(matcher(builder, _))
    builder.build
  }
}

object WebApiMaker {
  def apply(unit: AMFUnit): WebApiMaker = new WebApiMaker(unit)
}
