package amf.plugins.document.webapi.parser.spec.common

import amf.core.AMFSerializer
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, ShapeRenderOptions, SpecOrdering}
import amf.core.model.document.Document
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position
import amf.core.remote.JsonSchema
import amf.core.services.RuntimeSerializer
import amf.plugins.document.webapi.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import amf.plugins.document.webapi.contexts.emitter.oas.{
  CompactJsonSchemaEmitterContext,
  DefinitionsEmissionHelper,
  JsonSchemaEmitterContext
}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, JSONSchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.OasDeclarationsEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

trait JsonSchemaSerializer {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape): String = {
    element.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedJSONSchema]) match {
          case Some(g) => g.rawText
          case _       => generateJsonSchema(element)
        }
    }
  }

  protected def generateJsonSchema(element: AnyShape, options: ShapeRenderOptions = ShapeRenderOptions()): String = {
    AMFSerializer.init()
    val originalId = element.id
    val document   = Document().withDeclares(Seq(fixNameIfNeeded(element)) ++ element.closureShapes)
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

case class JsonSchemaEmitter(root: Shape,
                             declarations: Seq[DomainElement],
                             ordering: SpecOrdering = SpecOrdering.Lexical,
                             options: ShapeRenderOptions) {

  def emitDocument(): YDocument = {
    val schemaVersion: JSONSchemaVersion = JSONSchemaVersion.fromClientOptions(options.schemaVersion)
    val emitters                         = Seq(JsonSchemaEntry(schemaVersion), jsonSchemaRefEntry) ++ sortedTypeEntries(schemaVersion)
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private val jsonSchemaRefEntry = new EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name =
        if (options.isWithCompactedEmission) DefinitionsEmissionHelper.normalizeName(root.name.option())
        else root.name.value()
      b.entry("$ref", OasDefinitions.appendDefinitionsPrefix(name))
    }

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries(schemaVersion: JSONSchemaVersion) = {
    val context =
      if (options.isWithCompactedEmission)
        CompactJsonSchemaEmitterContext(options.errorHandler, options, schemaVersion = schemaVersion)
      else new JsonSchemaEmitterContext(options.errorHandler, options, schemaVersion = schemaVersion)
    ordering.sorted(OasDeclarationsEmitter(declarations, SpecOrdering.Lexical, Seq())(context).emitters)
  } // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

}
