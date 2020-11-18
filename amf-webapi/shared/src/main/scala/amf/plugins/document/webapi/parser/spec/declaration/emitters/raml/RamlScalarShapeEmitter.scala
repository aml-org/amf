package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, RawValueEmitter, ValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.webapi.annotations.Inferred
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.NumberTypeToYTypeConverter
import amf.plugins.document.webapi.parser.{
  OasTypeDefMatcher,
  RamlTypeDefMatcher,
  RamlTypeDefStringValueMatcher,
  TypeName
}
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.{ScalarShape, TypeDef}
import amf.plugins.domain.shapes.parser.{TypeDefXsdMapping, TypeDefYTypeMapping}
import org.yaml.model.YType
import amf.core.utils.AmfStrings

import scala.collection.mutable.ListBuffer

case class RamlScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {

  private val rawTypeDef: TypeDef       = TypeDefXsdMapping.typeDef(scalar.dataType.value())
  private val TypeName(typeDef, format) = RamlTypeDefStringValueMatcher.matchType(rawTypeDef, scalar.format.option())

  override protected val valuesTag: YType = TypeDefYTypeMapping(rawTypeDef)

  override def emitters(): Seq[EntryEmitter] = {
    val fs = scalar.fields

    val typeEmitterOption = if (scalar.inherits.isEmpty) {
      fs.entry(ScalarShapeModel.DataType)
        .flatMap(f =>
          if (!f.value.annotations.contains(classOf[Inferred])) {
            scalar.fields
              .removeField(ShapeModel.Inherits) // for scalar doesn't make any sense to write the inherits, because it will always be another scalar with the same t
            Some(MapEntryEmitter("type", typeDef, position = pos(f.value.annotations)))
          } else None) // TODO check this  - annotations of typeDef in parser
    } else {
      None
    }

    // use option for not alter the previous default order. (After resolution not any lexical info annotation remains here)

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*) ++ typeEmitterOption

    emitOASFields(fs, result)

    fs.entry(ScalarShapeModel.Pattern).foreach { f =>
      result += RamlScalarEmitter("pattern", processRamlPattern(f))
    }

    fs.entry(ScalarShapeModel.Minimum)
      .foreach(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .foreach(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    fs.entry(ScalarShapeModel.MultipleOf)
      .foreach(f => result += RamlScalarEmitter("multipleOf", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    result ++= emitFormat(rawTypeDef, fs, format)

    result
  }

  def emitFormat(rawTypeDef: TypeDef, fs: Fields, format: Option[String]): Option[EntryEmitter] = {
    val formatKey =
      if (rawTypeDef.isNumber | rawTypeDef.isDate) "format"
      else "format".asRamlAnnotation

    // this formats are here just because we parsed from OAS, the type in RAML has enough
    // information, we don't need the annotation with this format.
    // They will be re-generated correctly when translating into OAS
    val translationFormats = OasTypeDefMatcher.knownFormats.diff(RamlTypeDefMatcher.knownFormats)

    val explicitFormatOption = fs.entry(ScalarShapeModel.Format) flatMap {
      case entry if entry.element.isInstanceOf[AmfScalar] =>
        val entryFormat = entry.scalar.value.toString
        Some(entryFormat).filter(fmt => !translationFormats(fmt))
      case _ => None
    }

    val finalFormatOption = explicitFormatOption.orElse(format)

    val annotations = fs.entry(ScalarShapeModel.Format) match {
      case Some(entry) if entry.value.value.isInstanceOf[AmfScalar] => entry.value.annotations
      case _                                                        => Annotations()
    }

    val isExplicit = explicitFormatOption.isDefined

    // we always mapping 'number' in RAML to xsd:float, if we are to emit 'float'
    // as the format must be because it has been explicitly set in this way, not because
    // we are adding that through the number -> xsd:float mapping
    finalFormatOption map {
      case "float" if isExplicit =>
        RawValueEmitter(formatKey, ScalarShapeModel.Format, "float", annotations)
      case "int32" if isExplicit =>
        RawValueEmitter(formatKey, ScalarShapeModel.Format, "int32", annotations)
      case otherFormat =>
        RawValueEmitter(formatKey, ScalarShapeModel.Format, otherFormat, annotations)
    }
  }

  override val typeName: Option[String] = None // exceptional case for get the type (scalar) and format
}
