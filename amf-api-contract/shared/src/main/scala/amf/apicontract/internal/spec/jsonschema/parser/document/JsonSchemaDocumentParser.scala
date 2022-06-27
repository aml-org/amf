package amf.apicontract.internal.spec.jsonschema.parser.document

import amf.aml.internal.parse.common.DeclarationKeyCollector
import amf.apicontract.client.scala.model.document.{APIContractProcessingData, JsonSchemaDocument}
import amf.apicontract.internal.metamodel.document.JsonSchemaDocumentModel
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.jsonschema.{JsonSchemaEntry, JsonSchemaWebApiContext}
import amf.apicontract.internal.spec.oas.parser.document.OasLikeTypeDeclarationParser.parseTypeDeclarations
import amf.apicontract.internal.spec.oas.parser.document.{OasLikeDeclarationsHelper, OasLikeTypeDeclarationParser}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.MandatorySchema
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class JsonSchemaDocumentParser(root: Root)(implicit val ctx: ShapeParserContext)
    extends DeclarationKeyCollector
    with QuickFieldParserOps {

  val map: YMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

  def parse(): JsonSchemaDocument = {

    val document = JsonSchemaDocument()
    document.withLocation(root.location).withProcessingData(APIContractProcessingData().withSourceSpec(Spec.JSONSCHEMA))

    val (schemaVersion, _) = setSchemaVersion(document)

    val rootSchema = new JsonSchemaParser().parse(root, ctx, ctx.parsingOptions, Some(schemaVersion))
    document.withEncodes(rootSchema)
    // Parsing declaration schemas from "definitions"
    parseTypeDeclarations(map, declarationsKey(schemaVersion), Some(this))

    addDeclarationsToModel(document, ctx.shapes.values.toList)

    document
  }

  private def setSchemaVersion(document: JsonSchemaDocument): (JSONSchemaVersion, JsonSchemaDocument) = {
    val schemaEntry: Option[YMapEntry]   = map.key("$schema")
    val schemaVersion: JSONSchemaVersion = computeSchemaVersion(document, schemaEntry)

    if (schemaEntry.isDefined) {
      schemaEntry.map { entry =>
        document.set(
          JsonSchemaDocumentModel.SchemaVersion,
          AmfScalar(schemaVersion.url, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    }
    (schemaVersion, document)
  }

  private def computeSchemaVersion(document: JsonSchemaDocument, schemaEntry: Option[YMapEntry]) = {
    schemaEntry
      .flatMap(entry => JsonSchemaEntry(entry.value.as[YScalar].text)) match {
      case Some(version) => version
      case None =>
        ctx.eh.violation(MandatorySchema, document, MandatorySchema.message)
        JSONSchemaUnspecifiedVersion
    }
  }

  private def declarationsKey(version: JSONSchemaVersion) = version match {
    case JSONSchemaDraft201909SchemaVersion => "$defs"
    case _                                  => "definitions"
  }
}
