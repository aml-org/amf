package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ArrayEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.domain.shapes.metamodel.FileShapeModel
import amf.plugins.domain.shapes.models.{FileShape, TypeDef}
import amf.core.utils.AmfStrings

import scala.collection.mutable.ListBuffer

case class OasFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit], isHeader: Boolean)(
    override implicit val spec: OasLikeSpecEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = None

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    result += spec.oasTypePropertyEmitter("file", scalar)

    emitCommonFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes".asOasExtension, f, ordering))

    result
  }
}
