package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.RecursiveShape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape}
import org.yaml.model.YDocument.EntryBuilder

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    array.items match {
      case webapiArrayItem: AnyShape =>
        if (webapiArrayItem.isLink) {
          b.entry("items", Raml10TypePartEmitter(webapiArrayItem, ordering, None, Nil, references).emit(_))
        } else {
          // todo garrote review ordering
          b.entry(
            "items",
            _.obj { b =>
              Raml10TypeEmitter(webapiArrayItem, ordering, references = references)
                .entries()
                .foreach(_.emit(b))
            }
          )
        }
      case r: RecursiveShape =>
        b.entry(
          "items",
          _.obj { b =>
            Raml10TypeEmitter(r, ordering, references = references).entries().foreach(_.emit(b))
          }
        )
      case _ => // ignore
    }
  }

  override def position(): Position = {
    pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
  }
}
