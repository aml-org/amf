package amf.shapes.internal.spec.jsonschema.ref

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment, Fragment, RecursiveUnit}
import amf.core.client.scala.parse.document.{ParsedReference, Reference, SchemaReference, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Mimes
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.spec.common
import amf.shapes.internal.spec.common.parser
import amf.shapes.internal.spec.common.parser.JsonYamlParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.{YDocument, YMap, YNode}

object JsonSchemaRootCreator {

  def createRootFrom(inputFragment: Fragment, pointer: Option[String], errorHandler: AMFErrorHandler): Root = {
    val encoded: YNode = getYNodeFrom(inputFragment, errorHandler)
    createRoot(inputFragment, pointer, encoded)
  }

  def getYNodeFrom(inputFragment: Fragment, errorHandler: AMFErrorHandler): YNode = {
    inputFragment match {
      case fragment: ExternalFragment => fragment.encodes.parsed.getOrElse(parsedFragment(inputFragment, errorHandler))
      case fragment: RecursiveUnit if fragment.raw.isDefined => parsedFragment(inputFragment, errorHandler)
      case _ =>
        errorHandler.violation(
          UnableToParseJsonSchema,
          inputFragment,
          None,
          "Cannot parse JSON Schema from unit with missing syntax information"
        )
        YNode(YMap(IndexedSeq(), ""))
    }
  }

  private def parsedFragment(inputFragment: Fragment, eh: AMFErrorHandler) =
    parser.JsonYamlParser(inputFragment)(eh).document().node

  private def createRoot(inputFragment: Fragment, pointer: Option[String], encoded: YNode): Root = {
    Root(
      SyamlParsedDocument(YDocument(encoded)),
      buildJsonReference(inputFragment, pointer),
      `application/json`,
      toParsedReferences(inputFragment.references),
      SchemaReference,
      inputFragment.raw.getOrElse("")
    )
  }

  private def buildJsonReference(inputFragment: Fragment, pointer: Option[String]) = {
    val url = inputFragment.location().getOrElse(inputFragment.id)
    JsonReference(url, pointer).toString
  }

  private def toParsedReferences(references: Seq[BaseUnit]) = {
    references.map(ref => ParsedReference(ref, Reference(ref.location().getOrElse(""), Nil), None))
  }
}
