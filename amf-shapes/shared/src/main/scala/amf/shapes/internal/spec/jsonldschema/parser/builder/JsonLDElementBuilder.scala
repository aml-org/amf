package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.parse.document.ErrorHandlingContext
import amf.shapes.client.scala.model.document.{EntityContextBuilder, JsonLDInstanceDocument}
import amf.shapes.internal.spec.jsonldschema.instance.model.domain.JsonLDElement
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model.IllegalTypeHandler

import scala.collection.mutable.ListBuffer

abstract class JsonLDElementBuilder(val location: SourceLocation) {
  type THIS <: JsonLDElementBuilder
  def build(ctxBuilder: EntityContextBuilder): JsonLDElement

  val classTerms: ListBuffer[String] = ListBuffer()
  def merge(other: THIS)(implicit ctx: JsonLDParserContext): THIS = {
    other.classTerms.foreach { t => if (!classTerms.contains(t)) classTerms.append(t) }
    this.asInstanceOf[THIS]
  }

  def canEquals(other: Any): Boolean
}
