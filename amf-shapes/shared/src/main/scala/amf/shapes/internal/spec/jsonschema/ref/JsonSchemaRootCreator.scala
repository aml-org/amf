package amf.shapes.internal.spec.jsonschema.ref

import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment, RecursiveUnit}
import amf.core.client.scala.parse.document.{ParsedReference, Reference, SchemaReference, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.spec.common.parser
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.{YDocument, YMap, YNode}

object JsonSchemaRootCreator {

  def createRootFrom(inputFragment: BaseUnit, pointer: Option[String], ctx: ShapeParserContext): Root = {
    val encoded: YNode = getYNodeFrom(inputFragment, ctx)
    createRoot(inputFragment, pointer, encoded)
  }

  def getYNodeFrom(inputFragment: BaseUnit, ctx: ShapeParserContext): YNode = {
    inputFragment match {
      case fragment: ExternalFragment => fragment.encodes.parsed.getOrElse(parsedFragment(inputFragment, ctx))
      case fragment: RecursiveUnit if fragment.raw.isDefined => parsedFragment(inputFragment, ctx)
      case _ =>
        ctx.eh.violation(
          UnableToParseJsonSchema,
          inputFragment,
          None,
          "Cannot parse JSON Schema from unit with missing syntax information"
        )
        YNode(YMap(IndexedSeq(), ""))
    }
  }

  private def parsedFragment(inputFragment: BaseUnit, ctx: ShapeParserContext) =
    parser.JsonYamlParser(inputFragment)(ctx).document().node

  private def createRoot(inputFragment: BaseUnit, pointer: Option[String], encoded: YNode): Root = {
    Root(
      SyamlParsedDocument(YDocument(encoded)),
      buildJsonReference(inputFragment, pointer),
      `application/json`,
      toParsedReferences(inputFragment.references),
      SchemaReference,
      inputFragment.raw.getOrElse("")
    )
  }

  private def buildJsonReference(inputFragment: BaseUnit, pointer: Option[String]) = {
    val url = inputFragment.location().getOrElse(inputFragment.id)
    JsonReference(url, pointer).toString
  }

  private def toParsedReferences(references: Seq[BaseUnit]) = {
    references.map(ref => ParsedReference(ref, Reference(ref.location().getOrElse(""), Nil), None))
  }
}
