package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.RecursiveShape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape}
import org.yaml.model.YDocument.EntryBuilder

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    array.items match {
      case webapiArrayItem: AnyShape =>
        if (webapiArrayItem.isLink) {
          b.entry("items", emitters.Raml10TypePartEmitter(webapiArrayItem, ordering, None, Nil, references).emit(_))
        } else {
          // todo garrote review ordering
          b.entry(
            "items",
            _.obj { b =>
              emitters
                .Raml10TypeEmitter(webapiArrayItem, ordering, references = references)
                .entries()
                .foreach(_.emit(b))
            }
          )
        }
      case r: RecursiveShape =>
        b.entry(
          "items",
          _.obj { b =>
            emitters.Raml10TypeEmitter(r, ordering, references = references).entries().foreach(_.emit(b))
          }
        )
      case _ => // ignore
    }
  }

  override def position(): Position = {
    pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
  }
}
