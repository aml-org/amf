package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.annotations.ExplicitField
import amf.core.emitter.BaseEmitters.{MapEntryEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.document.webapi.parser.spec.declaration.RamlTypeEntryEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.common.{Draft4DependenciesEmitter, TypeEmitterFactory}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypeEmitter
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.metamodel.NodeShapeModel.Dependencies
import amf.plugins.domain.shapes.models.NodeShape
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
  implicit spec: ShapeEmitterContext)
  extends RamlAnyShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs = node.fields

    fs.entry(NodeShapeModel.MinProperties).map(f => result += RamlScalarEmitter("minProperties", f))
    fs.entry(NodeShapeModel.MaxProperties).map(f => result += RamlScalarEmitter("maxProperties", f))

    val hasPatternProperties = node.properties.exists(_.patternName.nonEmpty)
    fs.entry(NodeShapeModel.Closed)
      .foreach { f =>
        val closed = node.closed.value()
        if (!hasPatternProperties && (closed || f.value.annotations.contains(classOf[ExplicitField]))) {
          result += MapEntryEmitter("additionalProperties",
            (!closed).toString,
            YType.Bool,
            position = pos(f.value.annotations))
        }
      }

    fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
      .map(
        f => {
          val shape = f.value.value.asInstanceOf[Shape]
          result += RamlTypeEntryEmitter("additionalProperties".asRamlAnnotation, shape, ordering, references)
        }
      )

    fs.entry(NodeShapeModel.Discriminator).map(f => result += RamlScalarEmitter("discriminator", f))
    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += RamlScalarEmitter("discriminatorValue", f))

    fs.entry(NodeShapeModel.Properties).map { f =>
      typeEmitted = true
      result += RamlPropertiesShapeEmitter(f, ordering, references)
    }

    val emitterFactory: TypeEmitterFactory = shape =>
      OasTypeEmitter(shape, ordering, Seq(), references, Seq(), Seq())(spec.toOasNext)
    if (fs.entry(Dependencies).isDefined) {
      result += Draft4DependenciesEmitter(node, ordering, isRamlExtension = true, typeFactory = emitterFactory)
    }
    if (!typeEmitted)
      result += MapEntryEmitter("type", "object")

    result
  }

  override val typeName: Option[String] = node.annotations.find(classOf[ExplicitField]).map(_ => "object")
}
