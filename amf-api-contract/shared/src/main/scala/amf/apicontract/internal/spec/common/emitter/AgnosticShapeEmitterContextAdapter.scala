package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.internal.spec.async.emitters.context.AsyncSpecEmitterFactory
import amf.apicontract.internal.spec.jsonschema.JsonSchemaEmitterContext
import amf.apicontract.internal.spec.oas.emitter.context.{Oas3SpecEmitterFactory, OasLikeSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.Vendor
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, ShapeEmitterContext}
import amf.shapes.internal.spec.common.emitter.annotations.FacetsInstanceEmitter
import org.yaml.model.YDocument

object AgnosticShapeEmitterContextAdapter {
  def apply(spec: SpecEmitterContext) = new AgnosticShapeEmitterContextAdapter(spec)
}

class AgnosticShapeEmitterContextAdapter(spec: SpecEmitterContext) extends ShapeEmitterContext {

  override def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter =
    spec.factory.tagToReferenceEmitter(l, refs)

  override def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter =
    spec.arrayEmitter(asOasExtension, f, ordering)

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    spec.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    spec.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter =
    spec.factory.annotationEmitter(e, default)

  override def eh: AMFErrorHandler = spec.eh

  override def vendor: Vendor = spec.vendor

  override def ref(b: YDocument.PartBuilder, url: String): Unit = spec.ref(b, url)

  override def schemaVersion: SchemaVersion = spec match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.schemaVersion
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

  override def options: RenderOptions = spec.options

  override def isOas3: Boolean = spec.factory.isInstanceOf[Oas3SpecEmitterFactory]

  override def isOasLike: Boolean = spec.isInstanceOf[OasLikeSpecEmitterContext]

  override def isRaml: Boolean = spec.isInstanceOf[RamlSpecEmitterContext]

  override def isJsonSchema: Boolean = spec.isInstanceOf[JsonSchemaEmitterContext]

  override def isAsync: Boolean = spec.factory.isInstanceOf[AsyncSpecEmitterFactory]
}
