package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.internal.spec.async.emitters.context.AsyncSpecEmitterFactory
import amf.apicontract.internal.spec.jsonschema.JsonSchemaEmitterContext
import amf.apicontract.internal.spec.oas.emitter.context.{Oas3SpecEmitterFactory, OasLikeSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.client.scala.model.domain.{CustomizableElement, DomainElement, Linkable}
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.emitter.annotations.FacetsInstanceEmitter
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, ShapeEmitterContext}
import org.yaml.model.YDocument

object AgnosticShapeEmitterContextAdapter {
  def apply(specCtx: SpecEmitterContext) = new AgnosticShapeEmitterContextAdapter(specCtx)
}

class AgnosticShapeEmitterContextAdapter(private val specCtx: SpecEmitterContext) extends ShapeEmitterContext {

  override def config: RenderConfiguration = specCtx.renderConfig

  override def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter =
    specCtx.factory.tagToReferenceEmitter(l, refs)

  override def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter =
    specCtx.arrayEmitter(asOasExtension, f, ordering)

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    specCtx.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    specCtx.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(parent: CustomizableElement,
                                 e: DomainExtension,
                                 default: SpecOrdering): EntryEmitter =
    specCtx.factory.annotationEmitter(parent, e, default)

  override def eh: AMFErrorHandler = specCtx.eh

  override def spec: Spec = specCtx.spec

  override def ref(b: YDocument.PartBuilder, url: String): Unit = specCtx.ref(b, url)

  override def schemaVersion: SchemaVersion = specCtx match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.schemaVersion
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

  override def options: RenderOptions = specCtx.options

  override def isOas3: Boolean = specCtx.factory.isInstanceOf[Oas3SpecEmitterFactory]

  override def isOasLike: Boolean = specCtx.isInstanceOf[OasLikeSpecEmitterContext]

  override def isRaml: Boolean = specCtx.isInstanceOf[RamlSpecEmitterContext]

  override def isJsonSchema: Boolean = specCtx.isInstanceOf[JsonSchemaEmitterContext]

  override def isAsync: Boolean = specCtx.factory.isInstanceOf[AsyncSpecEmitterFactory]
}
