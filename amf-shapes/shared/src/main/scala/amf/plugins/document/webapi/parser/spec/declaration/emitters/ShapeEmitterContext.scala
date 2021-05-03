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
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.{Example, TypeDef}
import org.yaml.model.{YDocument, YNode}

trait SpecAwareEmitterContext {
  def factoryIsOas3: Boolean
  def isOasLike: Boolean
  def isRaml: Boolean
  def isJsonSchema: Boolean
  def factoryIsAsync: Boolean
}

trait ShapeEmitterContext extends SpecAwareEmitterContext {
  def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter

  def recursiveShapeEmitter(recursive: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): Emitter

  def oasMatchType(get: TypeDef): String

  def oasMatchFormat(typeDef: TypeDef): Option[String]

  def schemasDeclarationsPath: String

  def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter

  def oasTypePropertyEmitter(str: String, shape: Shape): EntryEmitter

  def customFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): CustomFacetsEmitter

  def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter

  def eh: ErrorHandler

  def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter

  def vendor: Vendor

  def ref(b: YDocument.PartBuilder, url: String): Unit

  def exampleEmitter(isHeader: Boolean,
                     main: Option[Example],
                     ordering: SpecOrdering,
                     extensions: Seq[Example],
                     references: Seq[BaseUnit]): OasLikeExampleEmitters

  def schemaVersion: SchemaVersion

  def filterLocal(examples: Seq[Example]): Seq[Example]

  def options: ShapeRenderOptions
  def anyOfKey: YNode
  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter]

}
