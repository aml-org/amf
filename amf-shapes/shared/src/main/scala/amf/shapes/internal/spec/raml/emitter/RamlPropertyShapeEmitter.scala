package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.annotations.{ExplicitField, SynthesizedField}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, raw, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YNode, YScalar, YType}

case class RamlPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val name: String = getPropertyName

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

  private def getPropertyName: String = {
    val isPatternProperty = property.patternName.option().isDefined

    if (isPatternProperty) {
      // emit pattern
      val isEmptyPattern = property.name.value() == "//"
      if (isEmptyPattern) "//"
      else s"/${property.patternName.value()}/"
    } else {
      // emit
      property.fields.entry(PropertyShapeModel.MinCount) match {
        case Some(e) if isNotRequired(e) && isNotExplicit(e) => property.name.value() + "?"
        case _                                               => property.name.value()
      }
    }
  }

  private def isNotExplicit(entry: FieldEntry) = !entry.value.annotations.contains(classOf[ExplicitField])

  private def isNotRequired(minCountEntry: FieldEntry) = minCountEntry.scalar.value == 0

  override def position(): Position = pos(property.annotations) // TODO check this
}
