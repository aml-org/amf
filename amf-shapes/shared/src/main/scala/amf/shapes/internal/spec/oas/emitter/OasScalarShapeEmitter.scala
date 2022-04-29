package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, RawValueEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.annotations.TypePropertyLexicalInfo
import amf.shapes.internal.spec.common.TypeDef.UndefinedType
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.domain.parser.TypeDefXsdMapping
import amf.shapes.internal.spec.CommonOasTypeDefMatcher.{matchFormat, matchType}
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.shapes.internal.spec.jsonschema.emitter.ContentEmitters
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

case class OasScalarShapeEmitter(
    scalar: ScalarShape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    isHeader: Boolean = false
)(override implicit val spec: OasLikeShapeEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = scalar.dataType.option().map(TypeDefXsdMapping.typeDef)

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = scalar.fields

    fs.entry(ScalarShapeModel.DataType)
      .foreach { f =>
        val typeDefStr = matchType(typeDef.get)
        scalar.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", typeDefStr, YType.Str, lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter(
              "type",
              typeDefStr,
              position = pos(f.value.annotations)
            ) // TODO check this  - annotations of typeDef in parser
        }
      }

    result ++= ContentEmitters.emitters(
      scalar,
      spec.schemaVersion,
      (key, version) => OasEntryShapeEmitter(key, version, ordering, references, Seq(), Seq())
    )

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        matchFormat(typeDef.getOrElse(UndefinedType)) match {
          case Some(format) =>
            result += RawValueEmitter("format", ScalarShapeModel.Format, checkRamlFormats(format))
          case None => // ignore
        }
    }
    emitCommonFields(fs, result)

    result
  }
}
