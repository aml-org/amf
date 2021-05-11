package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.AsyncSpecEmitterFactory
import amf.plugins.document.webapi.contexts.emitter.jsonschema.JsonSchemaEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.Oas3SpecEmitterFactory
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.{YDocument, YNode}

case class ApiShapeEmitterContextAdapter(spec: SpecEmitterContext) extends ShapeEmitterContext {

  override def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter =
    spec.factory.tagToReferenceEmitter(l, refs)

  override def recursiveShapeEmitter(recursive: RecursiveShape,
                                     ordering: SpecOrdering,
                                     schemaPath: Seq[(String, String)]): Emitter = spec match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.factory.recursiveShapeEmitter(recursive, ordering, schemaPath)
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

  override def schemasDeclarationsPath: String = spec match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.schemasDeclarationsPath
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

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

  override def eh: ErrorHandler = spec.eh

  override def vendor: Vendor = spec.vendor

  override def ref(b: YDocument.PartBuilder, url: String): Unit = spec.ref(b, url)

  override def schemaVersion: SchemaVersion = spec match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.schemaVersion
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

  override def filterLocal(examples: Seq[Example]): Seq[Example] = spec.filterLocal(examples)

  override def options: ShapeRenderOptions = spec.options

  override def anyOfKey: YNode = spec match {
    case oasCtx: OasLikeSpecEmitterContext => oasCtx.anyOfKey
    case _                                 => throw new Exception("Render - can only be called from OAS")
  }

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] = spec match {
    case oasCtx: OasLikeSpecEmitterContext =>
      oasCtx.factory.typeEmitters(shape, ordering, ignored, references, pointer, schemaPath)
    case _ => throw new Exception("Render - can only be called from OAS")
  }

  override def isOas3: Boolean = spec.factory.isInstanceOf[Oas3SpecEmitterFactory]

  override def isOasLike: Boolean = spec.isInstanceOf[OasLikeSpecEmitterContext]

  override def isRaml: Boolean = spec.isInstanceOf[RamlSpecEmitterContext]

  override def isJsonSchema: Boolean = spec.isInstanceOf[JsonSchemaEmitterContext]

  override def isAsync: Boolean = spec.factory.isInstanceOf[AsyncSpecEmitterFactory]

  override def toOasNext: ShapeEmitterContext = copy(toOas(spec))

  override def localReference(shape: Shape): PartEmitter = spec match {
    case ramlSpec: RamlSpecEmitterContext => ramlSpec.localReference(shape)
    case _                                => throw new Exception("Render - can only be called from RAML")
  }
}
