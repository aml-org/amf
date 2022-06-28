package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.aml.client.scala.model.document.Dialect
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SyamlParsedDocument, UnspecifiedReference}
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.parser.Root
import amf.core.internal.remote.{JsonSchemaDialect, Mimes, Spec}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.JSONSchemaDraft201909SchemaVersion
import amf.shapes.internal.spec.contexts.parser.JsonSchemaContext
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.ExceededMaxCombiningComplexity
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.reference.SemanticContextReferenceHandler
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.{
  DialectWrapper,
  SchemaTransformer,
  SchemaTransformerOptions
}
import org.yaml.model.YMap

object JsonSchemaDialectParsePlugin extends AMFParsePlugin {

  override val id: String = "JSON Schema Dialect"

  override def applies(element: Root): Boolean = element.parsed match {
    // Removed the check of $schema key to support unidentified JSON Schemas
    // Added check of unspecified reference to avoid to parse references of the root JSON Schema with this plugin
    case syamlDoc: SyamlParsedDocument =>
      element.referenceKind == UnspecifiedReference && syamlDoc.document.node.asOption[YMap].isDefined
    case _ => false
  }

  override def priority: PluginPriority = HighPriority

  override def withIdAdoption: Boolean = false

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val options = SchemaTransformerOptions.DEFAULT
    val newCtx  = context(ctx.copyWithSonsReferences().copy(refs = document.references))
    val parsed  = new JsonSchemaParser().parse(document, newCtx, ctx.parsingOptions)
    new IdAdopter(parsed, document.location).adoptFromRelative()
    // Evaluate the combining complexity of the Dialect: given the current behavior of AML, we need to generate all the possible mappings
    // that could be generated in an allOf. The result of this could be huge, so there is a parsing option to limit it.
    if (evaluateCombiningComplexity(parsed, ctx)) transformSchemaToDialect(document, ctx, options, parsed)
    else dummyDialect(document, options)
  }

  private def transformSchemaToDialect(
      document: Root,
      ctx: ParserContext,
      options: SchemaTransformerOptions,
      parsed: AnyShape
  ): Dialect = {
    val transformed = SchemaTransformer(parsed, options)(ctx.eh).transform()
    val dialect     = DialectWrapper(transformed, options, document.location).wrapTransformationResult()
    val vocabulary  = transform.VocabularyGenerator(dialect, transformed.terms, options).generateVocabulary()
    vocabulary.foreach(vocab => dialect.withReferences(Seq(vocab)))
    dialect
  }

  private def dummyDialect(document: Root, options: SchemaTransformerOptions): Dialect = Dialect()
    .withId(document.location)
    .withName(options.dialectName)
    .withVersion(options.dialectVersion)
    .withRoot(true)

  private def evaluateCombiningComplexity(parsed: AnyShape, ctx: ParserContext): Boolean =
    ctx.parsingOptions.maxJSONComplexity match {
      case Some(max) =>
        val shapeComplexity = new CombiningComplexityCalculator().calculateComplexity(parsed)
        if (shapeComplexity > max) {
          ctx.violation(
            ExceededMaxCombiningComplexity,
            parsed,
            s"The JSON Schema is too complex: it has a combining complexity of $shapeComplexity when the maximum is $max"
          )
          false
        } else true
      case None => true
    }

  private def context(wrapped: ParserContext): ShapeParserContext =
    JsonSchemaContext(
      wrapped,
      Some(JSONSchemaDraft201909SchemaVersion)
    ) // If $schema key is absent, default schema version is 2019-09

  /** media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(Mimes.`application/semantics+schema+json`, Mimes.`application/json`)

  /** media types which specifies vendors that may be referenced.
    */
  override def validSpecsToReference: Seq[Spec] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SemanticContextReferenceHandler(eh)

  override def allowRecursiveReferences: Boolean = true

  override def spec: Spec = JsonSchemaDialect
}
