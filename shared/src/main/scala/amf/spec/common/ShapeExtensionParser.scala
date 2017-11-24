package amf.spec.common

import amf.framework.parser._
import amf.plugins.domain.shapes.metamodel.ShapeModel
import amf.plugins.domain.shapes.models.{Shape, ShapeExtension}
import amf.remote.{Oas, Raml}
import amf.spec.ParserContext
import org.yaml.model.YMap

case class ShapeExtensionParser(shape: Shape, map: YMap, ctx: ParserContext) {
  def parse(): Unit = {
    val shapeExtensionDefinitions = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)
    shapeExtensionDefinitions.flatMap(_.values).distinct.foreach { shapeExensionDefinition =>
      val extensionKey = ctx.vendor match {
        case Raml => shapeExensionDefinition.name
        case Oas  => s"x-facet-${shapeExensionDefinition.name}"
        case _    =>
          ctx.violation(shape.id, s"Cannot parse shape extension for vendor ${ctx.vendor}", map)
          shapeExensionDefinition.name
      }
      map.key(
        extensionKey,
        entry => {
          val dataNode  = DataNodeParser(entry.value, parent = Some(shape.id + s"/extension/$extensionKey"))(ctx).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        }
      )
    }
  }
}