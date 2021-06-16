package amf.shapes.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.XMLSerializerModel
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

case class XMLSerializerEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value,
      b.entry(
        key,
        b => {
          val fs     = f.obj.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(XMLSerializerModel.Attribute)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("attribute", f))

          fs.entry(XMLSerializerModel.Wrapped)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("wrapped", f))

          fs.entry(XMLSerializerModel.Name)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("name", f))

          fs.entry(XMLSerializerModel.Namespace).map(f => result += ValueEmitter("namespace", f))

          fs.entry(XMLSerializerModel.Prefix).map(f => result += ValueEmitter("prefix", f))

          result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

          b.obj(traverse(ordering.sorted(result), _))
        }
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
