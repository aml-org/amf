package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.annotations.ExternalReferenceUrl
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.common.RamlExternalReferenceUrlEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{ExamplesEmitter, RamlShapeEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.PartBuilder

import scala.collection.mutable.ListBuffer

case class RamlExternalSchemaWrapperEmitter(shape: AnyShape,
                                            ordering: SpecOrdering,
                                            ignored: Seq[Field] = Nil,
                                            references: Seq[BaseUnit],
                                            forceEntry: Boolean = false)(implicit spec: RamlShapeEmitterContext)
    extends PartEmitter
    with ExamplesEmitter {

  override def emit(b: PartBuilder): Unit = {
    val fs = shape.fields
    if (shape.inherits.nonEmpty) {
      val result = ListBuffer[EntryEmitter]()
      fs.entry(ShapeModel.DisplayName).map(f => result += RamlScalarEmitter("displayName", f))
      fs.entry(ShapeModel.Description).map(f => result += RamlScalarEmitter("description", f))
      fs.entry(ShapeModel.Default) match {
        case Some(f) =>
          result += EntryPartEmitter("default",
                                     DataNodeEmitter(shape.default, ordering)(spec.eh),
                                     position = pos(f.value.annotations))
        case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
      }
      emitExamples(shape, result, ordering, references)
      result ++= shape.inherits.headOption.toSeq.flatMap(s =>
        Raml10TypeEmitter(s, ordering, ignored, references, forceEntry).entries())
      b.obj(traverse(ordering.sorted(result), _))
    } else {
      shape.inherits.headOption.foreach(s => emitReference(s, b))
    }
  }

  private def emitReference(shape: Shape, b: PartBuilder): Unit = shape match {
    case shape: AnyShape if shapeWasParsedFromAnExternalFragment(shape) =>
      RamlExternalSourceEmitter(shape, references).emit(b)
    case shape: Shape if hasExternalReferenceUrl(shape) =>
      RamlExternalReferenceUrlEmitter(shape)().emit(b)

  }

  private def hasExternalReferenceUrl(shape: Shape) = shape.annotations.contains(classOf[ExternalReferenceUrl])

  private def shapeWasParsedFromAnExternalFragment(shape: AnyShape) = {
    shape.fromExternalSource && references.exists {
      case e: ExternalFragment => e.encodes.id.equals(shape.externalSourceID.getOrElse(""))
      case _                   => false
    }
  }

  override def position(): Position = pos(shape.annotations)
}
