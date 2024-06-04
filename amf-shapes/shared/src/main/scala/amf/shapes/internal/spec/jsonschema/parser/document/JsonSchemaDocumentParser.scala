package amf.shapes.internal.spec.jsonschema.parser.document

import amf.aml.internal.parse.common.DeclarationKeyCollector
import amf.core.client.scala.model.document.BaseUnitProcessingData
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.core.internal.remote.Spec
import amf.core.internal.utils.{AmfStrings, UriUtils}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, UnresolvedShape}
import amf.shapes.internal.document.metamodel.JsonSchemaDocumentModel
import amf.shapes.internal.spec.common.parser.TypeDeclarationParser.parseTypeDeclarations
import amf.shapes.internal.spec.common.parser.{
  BaseReferencesParser,
  QuickFieldParserOps,
  ShapeParserContext,
  YMapEntryLike
}
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion
}
import amf.shapes.internal.spec.jsonschema.JsonSchemaEntry
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaParsingHelper
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{MandatorySchema, UnknownSchemaDraft}
import org.yaml.model.{YDocument, YMap, YMapEntry, YScalar}

case class JsonSchemaDocumentParser(root: Root)(implicit val ctx: ShapeParserContext)
    extends DeclarationKeyCollector
    with QuickFieldParserOps {

  def parse(): JsonSchemaDocument = {
    val yDocument: YDocument = root.parsed.asInstanceOf[SyamlParsedDocument].document

    val doc = JsonSchemaDocument(Annotations(yDocument))
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(Spec.JSONSCHEMA))

    yDocument.toOption[YMap].foreach { rootMap =>
      ctx.setJsonSchemaAST(rootMap)

      val fullRef = normalizeRef()

      val tempShape = temporalShape(rootMap, fullRef)

      val (schemaVersion, _) = setSchemaVersion(rootMap, doc)

      val references =
        BaseReferencesParser(doc, root.location, "uses".asOasExtension, rootMap, root.references).parse()

      if (references.nonEmpty) doc.withReferences(references.baseUnitReferences())

      // Parsing declaration schemas from "definitions" or "$defs"
      parseTypeDeclarations(
        rootMap,
        declarationsKey(schemaVersion),
        Some(this),
        Some(doc),
        Option(schemaVersion)
      )
      addDeclarationsToModel(doc, ctx.shapes.values.toList)

      val rootSchema = parseRootSchema(rootMap, schemaVersion)
      doc.set(DocumentModel.Encodes, rootSchema, Annotations.inferred())

      resolveFutureDeclarations(fullRef, tempShape, rootSchema)
    }

    doc
  }

  private def normalizeRef(): String = {
    UriUtils.normalizePath(ctx.rootContextDocument) + "#"
  }

  private def resolveFutureDeclarations(fullRef: String, tempShape: UnresolvedShape, rootSchema: AnyShape): Unit = {
    ctx.futureDeclarations.resolveRef(fullRef, rootSchema)
    ctx.registerJsonSchema(fullRef, rootSchema)
    tempShape.resolve(rootSchema)
    ctx.futureDeclarations.resolve()
  }

  private def temporalShape(rootMap: YMap, fullRef: String): UnresolvedShape = {
    JsonSchemaParsingHelper.createTemporaryShape(
      _ => {},
      rootMap,
      ctx,
      fullRef
    )
  }

  private def parseRootSchema(map: YMap, schemaVersion: JSONSchemaVersion, name: String = "schema"): AnyShape = {
    OasTypeParser(YMapEntryLike(map), name, _ => {}, schemaVersion)
      .parse()
      .getOrElse(AnyShape().withName(name))
  }

  private def setSchemaVersion(map: YMap, document: JsonSchemaDocument): (JSONSchemaVersion, JsonSchemaDocument) = {
    val schemaEntry: Option[YMapEntry]   = map.key("$schema")
    val schemaVersion: JSONSchemaVersion = processSchemaEntry(document, schemaEntry)

    schemaEntry.map { entry =>
      document.set(
        JsonSchemaDocumentModel.SchemaVersion,
        AmfScalar(schemaVersion.url, Annotations(entry.value)),
        Annotations(entry)
      )
    }

    (schemaVersion, document)
  }

  private def processSchemaEntry(document: JsonSchemaDocument, schemaEntry: Option[YMapEntry]): JSONSchemaVersion = {
    schemaEntry match {
      case Some(entry) => computeSchemaVersion(document, entry)
      case None =>
        ctx.eh.violation(MandatorySchema, document, MandatorySchema.message)
        JSONSchemaUnspecifiedVersion
    }
  }

  private def computeSchemaVersion(document: JsonSchemaDocument, schemaEntry: YMapEntry) = {
    JsonSchemaEntry(schemaEntry.value.as[YScalar].text) match {
      case Some(version) => version
      case None =>
        ctx.eh.violation(UnknownSchemaDraft, document, UnknownSchemaDraft.message)
        JSONSchemaUnspecifiedVersion
    }
  }

  private def declarationsKey(version: JSONSchemaVersion): Seq[String] = version match {
    case JSONSchemaDraft201909SchemaVersion => Seq($defKey, definitionsKey)
    case _                                  => Seq(definitionsKey)
  }

  final val definitionsKey  = "definitions"
  final val $defKey: String = "$defs"
}
