package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.emitter.BaseEmitters.{pos, raw, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YNode, YScalar, YType}

case class RamlPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val fs = property.fields

    val name: String = fs
      .entry(PropertyShapeModel.MinCount)
      .map(f => {
        if (f.scalar.value.asInstanceOf[Int] == 0 && !f.value.annotations.contains(classOf[ExplicitField]))
          property.name.value() + "?"
        else if (property.patternName.option().isDefined && property.name.value() != "//")
          s"/${property.name.value()}/"
        else
          property.name.value()
      })
      .getOrElse(property.name.value())

    val key = YNode(YScalar(name), YType.Str)

    if (property.range.annotations.contains(classOf[SynthesizedField])) {
      b.entry(
        key,
        raw(_, "", YType.Null)
      )
    } else {

      val additionalEmitters: Seq[EntryEmitter] =
        RamlRequiredShapeEmitter(shape = property.range, property.fields.entry(PropertyShapeModel.MinCount))
          .emitter()
          .toSeq

      property.range match {
        case range: AnyShape =>
          b.entry(
            key,
            pb => {
              Raml10TypePartEmitter(range, ordering, None, references = references).emitter match {
                case Left(p)        => p.emit(pb)
                case Right(entries) => pb.obj(traverse(ordering.sorted(entries ++ additionalEmitters), _))
              }
            }
          )
        case _ => // ignore
          b.entry(key, _.obj(e => traverse(additionalEmitters, e)))
      }
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}
