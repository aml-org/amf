package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, FieldEntry, Fields, Value}
import amf.core.internal.render.BaseEmitters.{RawValueEmitter, ValueEmitter}
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, TypeDef}
import amf.shapes.internal.spec.common.emitter.{NumberTypeToYTypeConverter, ShapeEmitterContext}
import amf.shapes.internal.spec.raml.emitter.RamlFormatTranslator
import scala.collection.mutable.ListBuffer

// TODO Refactor: This inheritance is very weird
trait OasCommonOASFieldsEmitter extends RamlFormatTranslator {

  def typeDef: Option[TypeDef] = None

  implicit val spec: ShapeEmitterContext

  def emitCommonFields(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    emitFormatRanges(fs, result)

    fs.entry(ScalarShapeModel.MultipleOf)
      .map(f => result += ValueEmitter("multipleOf", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

  }

  def emitFormatRanges(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    if (typeDef.exists(_.isNumber) && spec.isJsonSchema) {
      fs.entry(ScalarShapeModel.Format) match {
        case Some(fe) =>
          val format = fe.value.toString
          val minMax: Option[(Double, Double)] = format match {
            case "int8"  => Some((-128, 127))
            case "int16" => Some((-32768, 32767))
            case "int32" => Some((-2147483648, 2147483647))
            case "int64" =>
              Some((-9223372036854775808.0, 9223372036854775807.0)) // long type // todo fix syaml for long numbers
            case _ => None
          }

          fs.entry(ScalarShapeModel.Minimum).fold(minMax.foreach(m => buildMin(m._1, result)))(f => emitMin(f, result))
          fs.entry(ScalarShapeModel.Maximum).fold(minMax.foreach(m => buildMax(m._2, result)))(f => emitMax(f, result))

        case _ =>
          emitMinAndMax(fs, result)
      }
    } else {
      fs.entry(ScalarShapeModel.Format).map { f =>
        result += RawValueEmitter(
          "format",
          ScalarShapeModel.Format,
          checkRamlFormats(f.scalar.toString),
          f.value.annotations
        )
      }
      emitMinAndMax(fs, result)
    }
  }

  private def emitMinAndMax(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion) {
      // exclusiveMinimum/exclusiveMaximum should be emitted as numbers
      fs.entry(ScalarShapeModel.Minimum)
        .foreach(emitMin(_, result, fs.entry(ScalarShapeModel.ExclusiveMinimum).exists(_.scalar.toBool)))
      fs.entry(ScalarShapeModel.Maximum)
        .foreach(emitMax(_, result, fs.entry(ScalarShapeModel.ExclusiveMaximum).exists(_.scalar.toBool)))
      fs.entry(ScalarShapeModel.ExclusiveMinimumNumeric)
        .map(f => result += ValueEmitter("exclusiveMinimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))
      fs.entry(ScalarShapeModel.ExclusiveMaximumNumeric)
        .map(f => result += ValueEmitter("exclusiveMaximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))
    } else {
      // exclusiveMinimum/exclusiveMaximum should be emitted as Booleans
      fs.entry(ScalarShapeModel.Minimum).foreach(emitMin(_, result))
      fs.entry(ScalarShapeModel.Maximum).foreach(emitMax(_, result))
      fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("exclusiveMinimum", f))
      fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("exclusiveMaximum", f))
      convertNumericExclusiveToBoolean(fs, result)
    }
  }

  private def convertNumericExclusiveToBoolean(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(ScalarShapeModel.ExclusiveMinimumNumeric)
      .map(f => {
        if (fs.entry(ScalarShapeModel.Minimum).isEmpty) {
          convertNumericExclusiveMinToBoolean(result, f)
        }
      })
    fs.entry(ScalarShapeModel.ExclusiveMaximumNumeric)
      .map(f => {
        if (fs.entry(ScalarShapeModel.Maximum).isEmpty) {
          convertNumericExclusiveMaxToBoolean(result, f)
        }
      })
  }

  private def convertNumericExclusiveMaxToBoolean(result: ListBuffer[EntryEmitter], f: FieldEntry) = {
    emitMax(f, result)
    result += RawValueEmitter("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximum, AmfScalar(true))
  }

  private def convertNumericExclusiveMinToBoolean(result: ListBuffer[EntryEmitter], f: FieldEntry) = {
    emitMin(f, result)
    result += RawValueEmitter("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimum, AmfScalar(true))
  }

  private def emitMin(f: FieldEntry, result: ListBuffer[EntryEmitter], isExclusive: Boolean = false) =
    result += ValueEmitter(
      if (isExclusive) "exclusiveMinimum" else "minimum",
      f,
      Some(NumberTypeToYTypeConverter.convert(typeDef))
    )

  private def emitMax(f: FieldEntry, result: ListBuffer[EntryEmitter], isExclusive: Boolean = false) =
    result += ValueEmitter(
      if (isExclusive) "exclusiveMaximum" else "maximum",
      f,
      Some(NumberTypeToYTypeConverter.convert(typeDef))
    )

  private def buildMin(min: Double, result: ListBuffer[EntryEmitter]): Unit =
    build(min, "minimum", ScalarShapeModel.Minimum, result)

  private def buildMax(max: Double, result: ListBuffer[EntryEmitter]): Unit =
    build(max, "maximum", ScalarShapeModel.Maximum, result)

  private def build(value: Double, constraint: String, f: Field, result: ListBuffer[EntryEmitter]): Unit =
    result += ValueEmitter(
      constraint,
      FieldEntry(f, Value(AmfScalar(value), Annotations())),
      Some(NumberTypeToYTypeConverter.convert(typeDef))
    )

}
