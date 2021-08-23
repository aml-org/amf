package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.domain.{DocumentMapping, DocumentsModel, External}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchema, JsonSchemaDialect, Mimes, Spec}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.contexts.parser.JsonSchemaContext
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.dialect.{SchemaTransformer, TransformationResult}

object JsonSchemaDialectParsePlugin extends AMFParsePlugin {

  override val id: String = "JSON Schema Dialect"

  override def applies(element: Root): Boolean = true

  override def priority: PluginPriority = NormalPriority

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val newCtx      = context(ctx)
    val parsed      = new JsonSchemaParser().parse(document, newCtx, ctx.parsingOptions)
    val transformed = SchemaTransformer(parsed).transform()
    wrapTransformationResult(document.location, transformed)
  }

  private def wrapTransformationResult(location: String, transformed: TransformationResult): BaseUnit = {
    val documentMapping = transformed.encoded match {
      case Left(nm) =>
        DocumentsModel()
          .withId(location + "/documents")
          .withRoot(DocumentMapping().withId(location + "/docMapping").withEncoded(nm.name.value()))
      case Right(unm) =>
        DocumentsModel()
          .withId(location + "/documents")
          .withRoot(DocumentMapping().withId(location + "/docMapping").withEncoded(unm.name.value()))
    }
    val dialect = Dialect()
      .withId(location)
      .withName("generated_dialect")
      .withVersion("1.0")
      .withDocuments(documentMapping)
      .withDeclares(transformed.declared)
    if (transformed.externals.nonEmpty) {
      val externals = transformed.externals.map {
        case (ns, prefix) =>
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
  override def mediaTypes: Seq[String] = Seq(Mimes.`application/semantics+schema+json`, Mimes.`application/json`)

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validSpecsToReference: Seq[Spec] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = true

  override def spec: Spec = JsonSchemaDialect
}
