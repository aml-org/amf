package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ValueEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.CreativeWork
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.emitter
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationsEmitter, FacetsEmitter}
import amf.shapes.internal.spec.common.emitter.{
  EnumValuesEmitter,
  OasEntryCreativeWorkEmitter,
  RamlShapeEmitterContext,
  XMLSerializerEmitter
}
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

abstract class RamlShapeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext) {

  val typeName: Option[String]
  var typeEmitted                = false
  protected val valuesTag: YType = YType.Str

  def emitters(): Seq[EntryEmitter] = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    fs.entry(ShapeModel.DisplayName).map(f => result += RamlScalarEmitter("displayName", f))
    fs.entry(ShapeModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering)(spec.eh),
                                   position = pos(f.value.annotations))
      case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
    }

    fs.entry(ShapeModel.Values).map(f => result += emitter.EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(AnyShapeModel.Documentation)
      .map(
        f =>
          result += OasEntryCreativeWorkEmitter("externalDocs".asRamlAnnotation,
                                                f.value.value.asInstanceOf[CreativeWork],
                                                ordering))

    fs.entry(PropertyShapeModel.ReadOnly).map(fe => result += ValueEmitter("readOnly".asRamlAnnotation, fe))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.customFacetsEmitter(f, ordering, references)
      })

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.Inherits)
      .fold(
        typeName.foreach { value =>
          RamlTypeFacetEmitter(value, shape) match {
            case Some(emitter) =>
              typeEmitted = true
              result += emitter
            case None =>
              typeEmitted = false
          }
        }
      )(f => {
        f.array.values.map(_.asInstanceOf[Shape]).collectFirst({ case r: RecursiveShape => r }) match {
          case Some(r: RecursiveShape) =>
            typeEmitted = true
            result ++= RamlRecursiveShapeEmitter(r, ordering, references).emitters()
          case _ =>
            typeEmitted = true
            result += RamlShapeInheritsEmitter(f, ordering, references = references)
        }
      })

    if (Option(shape.and).isDefined && shape.and.nonEmpty)
      result += RamlAndConstraintEmitter(shape, ordering, references)
    if (Option(shape.or).isDefined && shape.or.nonEmpty) result += RamlOrConstraintEmitter(shape, ordering, references)
    if (Option(shape.xone).isDefined && shape.xone.nonEmpty)
      result += RamlXoneConstraintEmitter(shape, ordering, references)
    if (Option(shape.not).isDefined) result += RamlNotConstraintEmitter(shape, ordering, references)

    result
  }
}
