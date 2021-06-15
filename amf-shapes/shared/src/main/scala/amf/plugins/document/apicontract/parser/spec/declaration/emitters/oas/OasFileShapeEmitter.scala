package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  OasTypeFacetEmitter
}
import amf.plugins.domain.shapes.metamodel.FileShapeModel
import amf.plugins.domain.shapes.models.{FileShape, TypeDef}

import scala.collection.mutable.ListBuffer

case class OasFileShapeEmitter(scalar: FileShape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               isHeader: Boolean)(override implicit val spec: OasLikeShapeEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = None

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    result += OasTypeFacetEmitter("file", scalar)

    emitCommonFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += spec.arrayEmitter("fileTypes".asOasExtension, f, ordering))

    result
  }
}
