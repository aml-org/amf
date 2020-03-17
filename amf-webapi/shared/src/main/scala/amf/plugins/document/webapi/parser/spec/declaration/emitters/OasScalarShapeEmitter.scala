package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, RawValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.TypeDef.UndefinedType
import amf.plugins.domain.shapes.models.{ScalarShape, TypeDef}
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

case class OasScalarShapeEmitter(scalar: ScalarShape,
                                 ordering: SpecOrdering,
                                 references: Seq[BaseUnit],
                                 isHeader: Boolean = false)(override implicit val spec: OasLikeSpecEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = scalar.dataType.option().map(TypeDefXsdMapping.typeDef)

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = scalar.fields

    fs.entry(ScalarShapeModel.DataType)
      .foreach { f =>
        val typeDefStr = spec.typeDefMatcher.matchType(typeDef.get)
        scalar.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", typeDefStr, YType.Str, lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter("type", typeDefStr, position = pos(f.value.annotations)) // TODO check this  - annotations of typeDef in parser
        }
      }

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        spec.typeDefMatcher.matchFormat(typeDef.getOrElse(UndefinedType)) match {
          case Some(format) =>
            result += RawValueEmitter("format", ScalarShapeModel.Format, checkRamlFormats(format))
          case None => // ignore
        }
    }
    emitCommonFields(fs, result)

    result
  }
}
