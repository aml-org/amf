package amf.shapes.internal.spec.jsonldschema

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDObject}
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDElementBuilder
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonLDSchemaNodeParser, JsonPath}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleDomainElement
import org.yaml.model.YNode

class JsonLDSchemaNativeParser(eh: AMFErrorHandler) {

  def parse(root: Root, jsonSchema: Document): JsonLDInstanceDocument = {
    val node    = root.parsed.asInstanceOf[SyamlParsedDocument].document.node
    val builder = getRootBuilder(node, jsonSchema)

    val ctxBuilder = new EntityContextBuilder()
    val element    = builder.build(ctxBuilder)._1
    val instance   = JsonLDInstanceDocument(ctxBuilder.build())
    element match {
      case obj: JsonLDObject  => instance.withEncodes(Seq(obj))
      case array: JsonLDArray => instance.withEncodes(array.jsonLDElements)
      case _                  => // ignore
    }
    instance
  }

  def getRootBuilder(node: YNode, jsonSchema: Document): JsonLDElementBuilder = {
    val shape: Shape = jsonSchema.encodes match {
      case s: Shape => s
      case other =>
        eh.violation(IncompatibleDomainElement, other.id, IncompatibleDomainElement.message)
        AnyShape()
    }

    JsonLDSchemaNodeParser(shape, node, "encodes", JsonPath.empty, isRoot = true)(
      new JsonLDParserContext(eh)
    ).parse()
  }
}
