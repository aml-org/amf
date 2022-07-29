package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.parse.document.ErrorHandlingContext
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.internal.spec.jsonldschema.instance.model.domain.JsonLDElement
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model.IllegalTypeHandler

import scala.collection.mutable.ListBuffer

abstract class JsonLDElementBuilder(val location: SourceLocation) {
  def build(): JsonLDElement

  val classTerms: ListBuffer[String] = ListBuffer()
  def merge(other: this.type)(implicit ctx: JsonLDParserContext): this.type = {
    other.classTerms.foreach { t => if (!classTerms.contains(t)) classTerms.append(t) }
    this
  }

  def canEquals(other: Any): Boolean
}
