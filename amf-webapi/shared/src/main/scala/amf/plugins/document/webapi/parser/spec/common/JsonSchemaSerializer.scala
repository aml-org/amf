package amf.plugins.document.webapi.parser.spec.common

import amf.client.execution.BaseExecutionEnvironment
import amf.core.AMFSerializer
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, ShapeRenderOptions, SpecOrdering}
import amf.client.remod.amfcore.config.{ShapeRenderOptions => ImmutableShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.Document
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position
import amf.core.remote.JsonSchema
import amf.core.services.RuntimeSerializer
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import amf.plugins.document.webapi.contexts.emitter.jsonschema.{
  InlinedJsonSchemaEmitterContext,
  JsonSchemaEmitterContext
}
import amf.plugins.document.webapi.contexts.emitter.oas.AliasDefinitions
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
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

    // TODO: WE SHOULDN'T HAVE TO CREATE A DOCUMENT TO EMIT A SCHEMA!
    AMFSerializer.init()
    val originalId = element.id
    val document   = Document().withDeclares(Seq(fixNameIfNeeded(element)))
    val jsonSchema = RuntimeSerializer(document, "application/schema+json", JsonSchema.name, options)
    // TODO: why are we stripping annotations??
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
    val schemaUri = version match {
      case JSONSchemaUnspecifiedVersion => JSONSchemaDraft4SchemaVersion.url
      case _                            => version.url
    }
    b.entry("$schema", schemaUri)
  }

  override def position(): Position = Position.ZERO
}

// TODO improve JsonSchemaEmitter interface
case class JsonSchemaEmitter(root: Shape,
                             declarations: Seq[DomainElement],
                             ordering: SpecOrdering = SpecOrdering.Lexical,
                             options: ImmutableShapeRenderOptions,
                             errorHandler: ErrorHandler) {

  def emitDocument(): YDocument = {
    val schemaVersion = SchemaVersion.fromClientOptions(options.schemaVersion)
    val context       = createContextWith(schemaVersion)
    val emitters      = Seq(JsonSchemaEntry(schemaVersion), jsonSchemaRefEntry(context)) ++ sortedTypeEntries(context)
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private def createContextWith(schemaVersion: JSONSchemaVersion) = {
    if (options.isWithCompactedEmission)
      new JsonSchemaEmitterContext(errorHandler, options, schemaVersion = schemaVersion)
    else InlinedJsonSchemaEmitterContext(errorHandler, options, schemaVersion = schemaVersion)
  }

  private def jsonSchemaRefEntry(ctx: JsonSchemaEmitterContext) = new EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name =
        if (options.isWithCompactedEmission) ctx.definitionsQueue.normalizeName(root.name.option())
        else root.name.value()
      val prefix = s"#${ctx.schemasDeclarationsPath}"
      b.entry("$ref", s"$prefix$name")
    }

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries(ctx: JsonSchemaEmitterContext) = {
    ordering.sorted(OasDeclarationsEmitter(declarations, SpecOrdering.Lexical, Seq())(ctx).emitters)
  } // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

}
