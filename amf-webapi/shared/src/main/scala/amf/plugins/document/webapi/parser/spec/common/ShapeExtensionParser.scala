package amf.plugins.document.webapi.parser.spec.common

import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.parser._
import amf.core.remote.{Oas, Raml}
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import org.yaml.model.YMap

case class ShapeExtensionParser(shape: Shape,
                                map: YMap,
                                ctx: RamlWebApiContext,
                                isAnnotation: Boolean = false,
                                overrideSyntax: Option[String] = None) {
  def parse(): Unit = {
    val shapeExtensionDefinitions = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)
    val properties                = shapeExtensionDefinitions.flatMap(_.values).distinct
    properties.foreach { shapeExtensionDefinition =>
      val extensionKey = ctx.vendor match {
        case _: Raml => shapeExtensionDefinition.name.value() // TODO check this.
        case _: Oas  => s"facet-${shapeExtensionDefinition.name.value()}".asOasExtension
        case _ =>
          ctx.violation(shape.id, s"Cannot parse shape extension for vendor ${ctx.vendor}", map)
          shapeExtensionDefinition.name.value()
      }
      map.key(
        extensionKey,
        entry => {
          val dataNode =
            DataNodeParser(entry.value, parent = Some(shape.id + s"/extension/$extensionKey"))(ctx).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExtensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        }
      )
    }
    if (!shape.inherits.exists(s => s.isUnresolved)) { // only validate shapes when the father its resolved, to avoid close shape over custom annotations
      val syntax = overrideSyntax match {
        case Some("anyShape") | Some("shape") => shape.ramlSyntaxKey
        case Some(other)                      => other
        case None                             => shape.ramlSyntaxKey
      }

      val extensionsNames = properties.flatMap(_.name.option())
      val m               = YMap(map.entries.filter(e => !extensionsNames.contains(e.key.value.toString)), "")
      ctx.closedRamlTypeShape(shape, m, syntax, isAnnotation)
    }

    // todo: filter map.entries by extension key and call close shape by instance of for the rest?
  }
}
