package amf.spec.common

import amf.domain.extensions.ShapeExtension
import amf.metadata.shape.ShapeModel
import amf.shape.Shape
import org.yaml.model.YMap
import amf.parser._
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.ParserContext

case class ShapeExtensionParser(shape: Shape, map: YMap, ctx: ParserContext) {
  def parse(): Unit = {
    val shapeExtensionDefinitions = shape.collectCustomShapePropertyDefinitions()
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
          val dataNode  = DataNodeParser(entry.value, parent = Some(shape.id + s"/extension/$extensionKey")).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        }
      )
    }
  }
}