package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.FileShapeModel
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.FileShape
import amf.shapes.internal.spec.common.TypeDef

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
