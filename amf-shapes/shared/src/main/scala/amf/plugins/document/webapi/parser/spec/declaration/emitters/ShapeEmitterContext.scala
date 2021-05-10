package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.CommonOasTypeDefMatcher
import amf.plugins.document.webapi.parser.spec.declaration.common.ExternalLinkQuery.queryResidenceUnitOfLinked
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.models.{Example, TypeDef, UnionShape}
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import org.yaml.model.{YDocument, YNode, YType}

trait SpecAwareEmitterContext {
  def factoryIsOas3: Boolean
  def isOasLike: Boolean
  def isRaml: Boolean
  def isJsonSchema: Boolean
  def factoryIsAsync: Boolean
}

trait ShapeEmitterContext extends SpecAwareEmitterContext {

  def externalLink(link: Linkable, refs: Seq[BaseUnit]): Option[BaseUnit] = queryResidenceUnitOfLinked(link, refs)

  def toOasNext: ShapeEmitterContext

  def ramlTypePropertyEmitter(typeName: String, shape: Shape): Option[MapEntryEmitter] = {
    shape.fields.?(NodeShapeModel.Inherits) match {
      case None =>
        // If the type is union and anyOf is empty it isn't resolved and type will be emitted in the UnionEmitter
        if (typeName == "union" && shape.asInstanceOf[UnionShape].anyOf.nonEmpty) None
        else {
          shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
            case Some(lexicalInfo) =>
              Some(MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start))
            case _ => None
          }
        }
      case _ => None
    }
  }

  def localReference(shape: Shape): PartEmitter

  def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter

  def recursiveShapeEmitter(recursive: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): Emitter

  def oasMatchType(get: TypeDef): String = CommonOasTypeDefMatcher.matchType(get)

  def oasMatchFormat(typeDef: TypeDef): Option[String] = CommonOasTypeDefMatcher.matchFormat(typeDef)

  def schemasDeclarationsPath: String

  def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter

  def oasTypePropertyEmitter(typeName: String, shape: Shape): EntryEmitter = {
    shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
      case Some(lexicalInfo) =>
        MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start)
      case None =>
        MapEntryEmitter("type", typeName)
    }
  }

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
