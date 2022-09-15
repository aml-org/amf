package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElement
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable.ListBuffer

abstract class JsonLDElementBuilder(val location: SourceLocation) {
  private var overridedTerm: Option[String] = None
  type THIS <: JsonLDElementBuilder
  def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type)

  def merge(other: THIS)(implicit ctx: JsonLDParserContext): THIS = {
    other.getOverridedTerm.foreach(this.withOverridedTerm)
    this.asInstanceOf[THIS]
  }

  def canEquals(other: Any): Boolean

  def withOverridedTerm(term: String): THIS = {
    overridedTerm = Some(term)
    this.asInstanceOf[THIS]
  }

  def getOverridedTerm: Option[String] = overridedTerm
}

trait JsonLDErrorBuilder
