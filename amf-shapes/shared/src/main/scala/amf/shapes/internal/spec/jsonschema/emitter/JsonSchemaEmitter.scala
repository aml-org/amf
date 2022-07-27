package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.plugins.render.{EmptyRenderConfiguration, RenderConfiguration}
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.SpecOrdering.Lexical
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.{
  InlineJsonSchemaShapeEmitterContext,
  JsonSchemaShapeEmitterContext,
  OasLikeShapeEmitterContext
}
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.shapes.internal.spec.oas.emitter
import amf.shapes.internal.spec.oas.emitter.OasTypeEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

object JsonSchemaEmitter {
  def apply(renderConfig: RenderConfiguration) =
    new JsonSchemaEmitter(Lexical, renderConfig)(renderConfig.errorHandler)

  def apply(options: RenderOptions, errorHandler: AMFErrorHandler) = {
    val renderConfig = EmptyRenderConfiguration(errorHandler, options)
    new JsonSchemaEmitter(Lexical, renderConfig)(errorHandler)
  }
}

case class JsonSchemaEmitter(ordering: SpecOrdering, renderConfig: RenderConfiguration)(implicit
    private val eh: AMFErrorHandler
) {

  private val options: RenderOptions = renderConfig.renderOptions

  def emit(root: Shape, declarations: Seq[DomainElement]): YDocument = {
    val schemaVersion = SchemaVersion.fromClientOptions(options.schemaVersion)
    val context       = createContextWith(schemaVersion)
    val emitters = Seq(JsonSchemaEntryEmitter(schemaVersion), jsonSchemaRefEntry(root, context)) ++ sortedTypeEntries(
      declarations,
      context
    )

    generateYDocument(emitters)
  }

  def docLikeEmitter(
      root: Shape,
      declarations: Seq[DomainElement],
      schemaVersion: JSONSchemaVersion
  ): YDocument = {
    val context            = createContextWith(schemaVersion)
    val draftEntryEmitter  = JsonSchemaEntryEmitter(schemaVersion)
    val rootEmitter        = OasTypeEmitter(root, ordering, references = Nil)(context).entries()
    val declarationEmitter = sortedTypeEntries(declarations, context)

    val emitters = Seq(draftEntryEmitter) ++ rootEmitter ++ declarationEmitter

    generateYDocument(emitters)
  }

  def emit(root: Shape): YDocument = emit(root, Seq(root))

  def shapeEmitters(
      root: Shape,
      declarations: Seq[DomainElement],
      schemaVersion: JSONSchemaVersion
  ): Seq[EntryEmitter] = {
    val context = createContextWith(schemaVersion)
    jsonSchemaRefEntry(root, context) :: sortedTypeEntries(declarations, context)
  }

  private def generateYDocument(emitters: Seq[EntryEmitter]): YDocument = {
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private def createContextWith(schemaVersion: JSONSchemaVersion) = {
    if (options.isWithCompactedEmission)
      JsonSchemaShapeEmitterContext(eh, schemaVersion, renderConfig)
    else new InlineJsonSchemaShapeEmitterContext(eh, schemaVersion, renderConfig)
  }

  private def jsonSchemaRefEntry(root: Shape, ctx: OasLikeShapeEmitterContext) = new EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name =
        if (options.isWithCompactedEmission) ctx.definitionsQueue.normalizeName(root.name.option())
        else root.name.value()
      val prefix = s"#${ctx.schemasDeclarationsPath}"
      b.entry("$ref", s"$prefix$name")
    }

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries(
      declarations: Seq[DomainElement],
      ctx: OasLikeShapeEmitterContext
  ): List[EntryEmitter] = {
    val shapes = declarations.collect { case shape: Shape =>
      shape
    }
    ordering.sorted(emitter.OasDeclaredShapesEmitter(shapes, SpecOrdering.Lexical)(ctx).toSeq).toList
  }

}

case class JsonSchemaEntryEmitter(version: JSONSchemaVersion) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemaUri = version match {
      case JSONSchemaUnspecifiedVersion => JSONSchemaDraft4SchemaVersion.url
      case _                            => version.url
    }
    b.entry("$schema", schemaUri)
  }

  override def position(): Position = Position.ZERO
}
