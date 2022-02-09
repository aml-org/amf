package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.domain.{DocumentMapping, DocumentsModel, External}
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SyamlParsedDocument}
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchemaDialect, Mimes, Spec}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.contexts.parser.JsonSchemaContext
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.reference.SemanticContextReferenceHandler
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.{
  SchemaTransformer,
  SchemaTransformerOptions,
  TransformationResult
}
import org.yaml.model.YMap

import scala.collection.mutable

object JsonSchemaDialectParsePlugin extends AMFParsePlugin {

  private val SEMANTIC_JSON_DIALECT_NAME    = "amf-json-schema-generated-dialect"
  private val SEMANTIC_JSON_DIALECT_VERSION = "1.0"

  override val id: String = "JSON Schema Dialect"

  override def applies(element: Root): Boolean = element.parsed match {
    case syamlDoc: SyamlParsedDocument =>
      syamlDoc.document.node.asOption[YMap].exists(_.map.contains("$schema"))
    case _ => false
  }

  override def priority: PluginPriority = HighPriority

  override def withIdAdoption: Boolean = false

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx = context(ctx.copyWithSonsReferences().copy(refs = document.references))
    val parsed = new JsonSchemaParser().parse(document, newCtx, ctx.parsingOptions)
    new IdAdopter(parsed, document.location).adoptFromRelative()
    val transformed = SchemaTransformer(parsed, SchemaTransformerOptions.DEFAULT)(ctx.eh).transform()
    wrapTransformationResult(document.location, transformed)
  }

  private def wrapTransformationResult(location: String, transformed: TransformationResult): BaseUnit = {
    val documentMapping: DocumentsModel = createDocumentMapping(location, transformed)
    createDialectWith(location, transformed, documentMapping)
      .withReferences(transformed.vocab.toList)
  }

  private def createDialectWith(location: String, transformed: TransformationResult, documentMapping: DocumentsModel) = {
    val dialect = Dialect()
      .withId(location)
      .withName(SEMANTIC_JSON_DIALECT_NAME)
      .withVersion(SEMANTIC_JSON_DIALECT_VERSION)
      .withDocuments(documentMapping)
      .withDeclares(transformed.declared)
      .withRoot(true)

    if (transformed.externals.nonEmpty) {
      val externals = extractExternals(transformed, location)
      dialect.withExternals(externals)
    }
    dialect
  }

  private def extractExternals(transformed: TransformationResult, location: String) = {
    transformed.externals.map {
      case (ns, prefix) =>
        External().withId(location + "/external/" + prefix).withBase(prefix).withAlias(ns)
    }.toList
  }

  private def createDocumentMapping(location: String, transformed: TransformationResult) = {
    DocumentsModel()
      .withId(location + "/documents")
      .withRoot(DocumentMapping().withId("#/documents/root").withEncoded(transformed.encoded.id))
  }

  private def context(wrapped: ParserContext): ShapeParserContext = JsonSchemaContext(wrapped)

  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(Mimes.`application/semantics+schema+json`, Mimes.`application/json`)

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validSpecsToReference: Seq[Spec] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SemanticContextReferenceHandler(eh)

  override def allowRecursiveReferences: Boolean = true

  override def spec: Spec = JsonSchemaDialect
}
