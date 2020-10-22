package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParserContext, SyamlParsedDocument}
import amf.core.remote.Platform
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.SchemaShape
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.YMapEntry

class JsonSchemaParser {
  def parse(document: Root, parentContext: ParserContext, options: ParsingOptions): Option[BaseUnit] = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String      = if (document.location.contains("#")) document.location else document.location + "#/"
        val parts: Array[String] = document.location.split("#")
        val url: String          = parts.head
        val hashFragment: Option[String] =
          parts.tail.headOption.map(t => if (t.startsWith("/definitions")) t.stripPrefix("/") else t)

        val jsonSchemaContext = new AstFinder().getJsonSchemaContext(document, parentContext, url, options)
        val rootAst = new AstFinder().getRootAst(parsedDoc, shapeId, hashFragment, url, jsonSchemaContext) match {
          case Right(value) => value
          case Left(value)  => YMapEntry("schema", value)
        }
        val parsed =
          OasTypeParser(rootAst,
            shape => shape.withId(shapeId),
            version = jsonSchemaContext.computeJsonSchemaVersion(rootAst.value))(jsonSchemaContext)
            .parse() match {
            case Some(shape) =>
              shape
            case None =>
              jsonSchemaContext.eh.violation(UnableToParseJsonSchema,
                shapeId,
                s"Cannot parse JSON Schema at ${document.location}",
                rootAst.value)
              SchemaShape().withId(shapeId).withMediaType("application/json").withRaw(document.raw)
          }
        jsonSchemaContext.localJSONSchemaContext = None

        val unit: DataTypeFragment =
          DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
        unit.withRaw(document.raw)
        Some(unit)

      case _ => None
    }
  }
}
