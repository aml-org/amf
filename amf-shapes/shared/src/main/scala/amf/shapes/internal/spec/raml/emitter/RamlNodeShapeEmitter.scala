package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Dependencies
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import amf.shapes.internal.spec.jsonschema.emitter.{Draft4DependenciesEmitter, TypeEmitterFactory}
import amf.shapes.internal.spec.oas.emitter.OasTypeEmitter
import amf.shapes.internal.spec.raml.parser
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.NodeShape

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends RamlAnyShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = node.fields

    fs.entry(NodeShapeModel.MinProperties).map(f => result += RamlScalarEmitter("minProperties", f))
    fs.entry(NodeShapeModel.MaxProperties).map(f => result += RamlScalarEmitter("maxProperties", f))

    val hasPatternProperties = node.properties.exists(_.patternName.nonEmpty)
    fs.entry(NodeShapeModel.Closed)
      .foreach { f =>
        val closed = node.closed.value()
        if (!hasPatternProperties && (closed || f.value.annotations.contains(classOf[ExplicitField]))) {
          result += MapEntryEmitter(
            "additionalProperties",
            (!closed).toString,
            YType.Bool,
            position = pos(f.value.annotations)
          )
        }
      }

    fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
      .map(f => {
        val shape = f.value.value.asInstanceOf[Shape]
        result += parser.RamlTypeEntryEmitter("additionalProperties".asRamlAnnotation, shape, ordering, references)
      })

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
