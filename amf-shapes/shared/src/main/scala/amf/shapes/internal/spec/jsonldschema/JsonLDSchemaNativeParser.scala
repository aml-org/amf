package amf.shapes.internal.spec.jsonldschema

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonLDSchemaNodeParser}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleDomainElement
import org.yaml.model.YNode

class JsonLDSchemaNativeParser(eh: AMFErrorHandler) {

  def parse(root: Root, jsonSchema: Document): JsonLDInstanceDocument = {
    val shape: Shape = jsonSchema.encodes match {
      case s: Shape => s
      case other =>
        eh.violation(IncompatibleDomainElement, other.id, IncompatibleDomainElement.message)
        AnyShape()
    }
    val node = root.asInstanceOf[SyamlParsedDocument].document.node

    val builder = JsonLDSchemaNodeParser(shape, node)(new JsonLDParserContext(eh)).parse()
    val element = builder.build()
    JsonLDInstanceDocument().withEncodes(element)
  }

  def parseNode(shape: Shape, node: YNode) = {
    shape match {
      case nodeShape: NodeShape =>
    }

  }

}
