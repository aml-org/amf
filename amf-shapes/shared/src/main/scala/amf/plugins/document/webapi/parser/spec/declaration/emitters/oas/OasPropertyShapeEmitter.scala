package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.RecursiveShape
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{OasLikeShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YNode, YScalar, YType}

case class OasPropertyShapeEmitter(property: PropertyShape,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit],
                                   propertiesKey: String = "properties",
                                   pointer: Seq[String] = Nil,
                                   schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
    extends OasTypePartCollector(property.range, ordering, Nil, references)
    with EntryEmitter {

  val propertyName: String = property.patternName.option().getOrElse(property.name.value())
  val propertyKey: YNode   = YNode(YScalar(propertyName), YType.Str)

  val computedEmitters: Either[PartEmitter, Seq[EntryEmitter]] =
    emitter(pointer ++ Seq(propertiesKey, propertyName), schemaPath)

  override def emit(b: EntryBuilder): Unit = {
    property.range match {
      case _: AnyShape | _: RecursiveShape =>
        b.entry(
          propertyKey,
          pb => {
            computedEmitters match {
              case Left(p)        => p.emit(pb)
              case Right(entries) => pb.obj(traverse(ordering.sorted(entries), _))
            }
          }
        )
      case _ => // ignore
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}
