package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, RawValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.emitter.ContentEmitters
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
                                 isHeader: Boolean = false)(override implicit val spec: ShapeEmitterContext)
  extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = scalar.dataType.option().map(TypeDefXsdMapping.typeDef)

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs = scalar.fields

    fs.entry(ScalarShapeModel.DataType)
      .foreach { f =>
        val typeDefStr = spec.oasMatchType(typeDef.get)
        scalar.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", typeDefStr, YType.Str, lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter("type", typeDefStr, position = pos(f.value.annotations)) // TODO check this  - annotations of typeDef in parser
        }
      }

    result ++= ContentEmitters.emitters(scalar, spec.schemaVersion, (key, version) => OasEntryShapeEmitter(key, version, ordering, references, Seq(), Seq()))

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        spec.oasMatchFormat(typeDef.getOrElse(UndefinedType)) match {
          case Some(format) =>
            result += RawValueEmitter("format", ScalarShapeModel.Format, checkRamlFormats(format))
          case None => // ignore
        }
    }
    emitCommonFields(fs, result)

    result
  }
}
