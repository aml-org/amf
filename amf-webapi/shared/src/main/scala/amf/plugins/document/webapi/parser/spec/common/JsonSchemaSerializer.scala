package amf.plugins.document.webapi.parser.spec.common

import amf.client.execution.BaseExecutionEnvironment
import amf.core.AMFSerializer
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, ShapeRenderOptions, SpecOrdering}
import amf.core.model.document.Document
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position
import amf.core.remote.JsonSchema
import amf.core.services.RuntimeSerializer
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import amf.plugins.document.webapi.contexts.emitter.oas.{
  InlinedJsonSchemaEmitterContext,
  AliasDefinitions,
  JsonSchemaEmitterContext
}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, JSONSchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.OasDeclarationsEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.concurrent.ExecutionContext

trait JsonSchemaSerializer extends PlatformSecrets {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape, exec: BaseExecutionEnvironment): String = {
    element.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedJSONSchema]) match {
          case Some(g) => g.rawText
          case _       => generateJsonSchema(element, exec = exec)
        }
    }
  }

  protected def generateJsonSchema(element: AnyShape,
                                   options: ShapeRenderOptions = ShapeRenderOptions(),
                                   exec: BaseExecutionEnvironment): String = {
    implicit val executionContext: ExecutionContext = exec.executionContext

    AMFSerializer.init()
    val originalId = element.id
    val document   = Document().withDeclares(Seq(fixNameIfNeeded(element)))
    val jsonSchema = RuntimeSerializer(document, "application/schema+json", JsonSchema.name, shapeOptions = options)
    element.withId(originalId)
    element.annotations.reject(a =>
      a.isInstanceOf[ParsedJSONSchema] || a.isInstanceOf[GeneratedJSONSchema] || a.isInstanceOf[JSONSchemaRoot])
    element.annotations += GeneratedJSONSchema(jsonSchema)
    jsonSchema
  }

  private def fixNameIfNeeded(element: AnyShape): AnyShape = {
    // Adding an annotation to identify the root shape of the JSON Schema
    element.annotations += JSONSchemaRoot()
    if (element.name.option().isEmpty)
      element.copyShape().withName("root")
    else {
      if (element.name.value().matches(".*/.*")) element.copyShape().withName("root")
      else element
    }
  }
}

case class JsonSchemaEntry(version: JSONSchemaVersion) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val draftVersionNumber = if (version == JSONSchemaDraft7SchemaVersion) 7 else 4
    b.entry("$schema", s"http://json-schema.org/draft-0${draftVersionNumber}/schema#")
  }

  override def position(): Position = Position.ZERO
}

// TODO improve JsonSchemaEmitter interface
case class JsonSchemaEmitter(root: Shape,
                             declarations: Seq[DomainElement],
                             ordering: SpecOrdering = SpecOrdering.Lexical,
                             options: ShapeRenderOptions) {

  def emitDocument(): YDocument = {
    val schemaVersion: JSONSchemaVersion = JSONSchemaVersion.fromClientOptions(options.schemaVersion)
    val context =
      if (options.isWithCompactedEmission)
        new JsonSchemaEmitterContext(options.errorHandler, options, schemaVersion = schemaVersion)
      else InlinedJsonSchemaEmitterContext(options.errorHandler, options, schemaVersion = schemaVersion)
    val emitters = Seq(JsonSchemaEntry(schemaVersion), jsonSchemaRefEntry(context)) ++ sortedTypeEntries(context)
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private def jsonSchemaRefEntry(ctx: JsonSchemaEmitterContext) = new EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name =
        if (options.isWithCompactedEmission) ctx.definitionsQueue.normalizeName(root.name.option())
        else root.name.value()
      b.entry("$ref", OasDefinitions.appendSchemasPrefix(name))
    }

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries(ctx: JsonSchemaEmitterContext) = {
    ordering.sorted(OasDeclarationsEmitter(declarations, SpecOrdering.Lexical, Seq())(ctx).emitters)
  } // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

}
