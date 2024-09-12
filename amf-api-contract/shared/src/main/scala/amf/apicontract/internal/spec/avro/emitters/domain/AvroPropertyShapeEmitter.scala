package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.apicontract.internal.spec.avro.parser.domain.AvroFieldOrder
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class AvroPropertyShapeEmitter(
    prop: PropertyShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(prop, ordering) {

  override def emitSpecificFields(b: EntryBuilder): Unit = {
    prop.fields
      .entry(PropertyShapeModel.SerializationOrder)
      .foreach(f => b.entry("order", AvroFieldOrder.fromInt(f.scalar.value.toString.toInt)))

    prop.range match {
      // todo: if you have duplicated common fields, emit a 'type' and then the range
      case nestedShape if spec.isComplex(nestedShape) =>
        val typeEmitter = AvroShapeEmitter(nestedShape, ordering)
        if (areCommonFieldsDefinedTwice()) {
          b.entry(
            "type",
            (pb: PartBuilder) => {
              pb.obj((eb: EntryBuilder) => typeEmitter.entries().foreach(_.emit(eb)))
            }
          )
        } else {
          typeEmitter.entries().foreach(_.emit(b))
        }

      case nestedShape =>
        val avroType = spec.getAvroType(nestedShape).getOrElse("default")
        b.entry("type", avroType)
    }
  }

  override def emitCommonFields(b: EntryBuilder): Unit = {
    // and only emit a common field if it's not present in the prop range, or if the range has it but they're not equal
    def checkDuplicatedField(field: Field, avroField: String): Unit = {
      prop.fields.entry(field).foreach { f =>
        val rangeNameField = prop.range.fields.entry(field)
        val shouldEmit = rangeNameField match {
          case Some(fe: FieldEntry) if f.scalar.toString != fe.value.toString => true
          case None                                                           => true
          case _                                                              => false
        }
        if (shouldEmit) {
          f.value.value match {
            case AmfArray(values, _) => b.entry(avroField, _.list(pb => values.foreach(pb += _.toString)))
            case _                   => b.entry(avroField, f.scalar.toString)
          }
        }
      }
    }

    checkDuplicatedField(AnyShapeModel.Name, "name")
    checkDuplicatedField(AnyShapeModel.Aliases, "aliases")
    checkDuplicatedField(AnyShapeModel.Description, "doc")
  }

  def isFieldDefinedTwice(field: Field): Boolean = {
    val propField  = prop.fields.entry(field)
    val rangeField = prop.range.fields.entry(field)
    propField match {
      case Some(value) =>
        val propValue = value.value.toString
        rangeField match {
          case Some(value) =>
            value.value.toString != propValue
          case None => false
        }
      case None => false
    }
  }

  // checks if common fields are defined differently in the prop and it's range
  def areCommonFieldsDefinedTwice(): Boolean = {
    val commonFields = Seq(AnyShapeModel.Name, AnyShapeModel.Aliases, AnyShapeModel.Description)
    commonFields.map(isFieldDefinedTwice).exists(identity)
  }

  override def position(): Position = pos(prop.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)

}
