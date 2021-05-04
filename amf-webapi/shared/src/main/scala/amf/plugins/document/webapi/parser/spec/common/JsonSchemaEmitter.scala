package amf.plugins.document.webapi.parser.spec.common

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.jsonschema.{
  InlinedJsonSchemaEmitterContext,
  JsonSchemaEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.plugins.document.webapi.parser.spec.oas.OasDeclarationsEmitter
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

// TODO improve JsonSchemaEmitter interface
case class JsonSchemaEmitter(root: Shape,
                             declarations: Seq[DomainElement],
                             ordering: SpecOrdering = SpecOrdering.Lexical,
                             options: ShapeRenderOptions,
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
