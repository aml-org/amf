package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElement
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable.ListBuffer

abstract class JsonLDElementBuilder(val location: SourceLocation) {
  type THIS <: JsonLDElementBuilder
  def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type)

  val classTerms: ListBuffer[String] = ListBuffer()
  def merge(other: THIS)(implicit ctx: JsonLDParserContext): THIS = {
    other.classTerms.foreach { t => if (!classTerms.contains(t)) classTerms.prepend(t) }
    this.asInstanceOf[THIS]
  }

  def canEquals(other: Any): Boolean
}
