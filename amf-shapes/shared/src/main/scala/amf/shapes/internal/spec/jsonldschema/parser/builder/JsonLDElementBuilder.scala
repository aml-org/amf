package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElement
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonPath}
import org.mulesoft.common.client.lexical.SourceLocation

abstract class JsonLDElementBuilder(val location: SourceLocation, val path: JsonPath) {

  private var overriddenTerm: Option[String] = None
  private var overriddenType: Option[Type]   = None

  type THIS <: JsonLDElementBuilder

  def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type)

  def merge(other: THIS)(implicit ctx: JsonLDParserContext): THIS = {
    other.getOverriddenTerm.foreach(this.withOverriddenTerm)
    other.getOverriddenType.foreach(this.withOverriddenType)
    this.asInstanceOf[THIS]
  }

  def canEquals(other: Any): Boolean

  def withOverriddenTerm(term: String): THIS = {
    overriddenTerm = Some(term)
    this.asInstanceOf[THIS]
  }
  def withOverriddenType(`type`: Type): THIS = {
    overriddenType = Some(`type`)
    this.asInstanceOf[THIS]
  }

  def getOverriddenTerm: Option[String] = overriddenTerm
  def getOverriddenType: Option[Type]   = overriddenType
}
