package amf.plugins.parser

import amf.client.remod.amfcore.plugins.parse.AMFParsePlugin
import amf.client.remod.amfcore.plugins.{NormalPriority, PluginPriority}
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.remote.{JsonSchema, JsonSchemaDialect, Vendor}
import amf.plugins.document.apicontract.contexts.parser.JsonSchemaContext
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.jsonschema.JsonSchemaParser
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{DocumentMapping, DocumentsModel, External}
import amf.plugins.parser.dialect.{SchemaTransformer, TransformationResult}

import scala.language.postfixOps

object JsonSchemaDialectParsePlugin extends AMFParsePlugin {

  override val id: String = Vendor.JSONSCHEMADIALECT.name

  override def applies(element: Root): Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx = context(ctx)
    val parsed = new JsonSchemaParser().parse(document, newCtx, ctx.parsingOptions)
    val transformed = SchemaTransformer(parsed).transform()
    wrapTransformationResult(document.location, transformed)
  }

  private def wrapTransformationResult(location: String, transformed: TransformationResult): BaseUnit = {
    val documentMapping = transformed.encoded match {
      case Left(nm)   => DocumentsModel().withId(location + "/documents").withRoot(DocumentMapping().withId(location + "/docMapping").withEncoded(nm.name.value()))
      case Right(unm) => DocumentsModel().withId(location + "/documents").withRoot(DocumentMapping().withId(location + "/docMapping").withEncoded(unm.name.value()))
    }
    val dialect = Dialect().withId(location).withName("generated_dialect").withVersion("1.0").withDocuments(documentMapping).withDeclares(transformed.declared)
    if (transformed.externals.nonEmpty) {
      val externals = transformed.externals.map {  case (ns, prefix) =>
        External().withId(location + "/external/" + prefix).withBase(prefix).withAlias(ns)
      }
      dialect.withExternals(externals.toSeq)
    }
    dialect
  }

  private def context(wrapped: ParserContext): ShapeParserContext = JsonSchemaContext(wrapped)

  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(JsonSchemaDialect.mediaType, JsonSchema.mediaType)

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validMediaTypesToReference: Seq[String] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = true





}
