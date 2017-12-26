package amf.plugins.document.webapi.parser.spec.common

import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.parser._
import amf.core.remote.{Oas, Raml}
import amf.plugins.document.webapi.contexts.WebApiContext
import org.yaml.model.YMap

case class ShapeExtensionParser(shape: Shape, map: YMap, ctx: WebApiContext) {
  def parse(): Unit = {
    val shapeExtensionDefinitions = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)
    shapeExtensionDefinitions.flatMap(_.values).distinct.foreach { shapeExensionDefinition =>
      val extensionKey = ctx.vendor match {
        case r: Raml => shapeExensionDefinition.name
        case Oas     => s"x-facet-${shapeExensionDefinition.name}"
        case _ =>
          ctx.violation(shape.id, s"Cannot parse shape extension for vendor ${ctx.vendor}", map)
          shapeExensionDefinition.name
      }
      map.key(
        extensionKey,
        entry => {
          val dataNode =
            DataNodeParser(entry.value, parent = Some(shape.id + s"/extension/$extensionKey"))(ctx).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        }
      )
    }
  }
}
