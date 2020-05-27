package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations.DeclaredElement
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, OasTypeParser}
import amf.validations.ParserSideValidations.UnableToParseShape
import org.yaml.model.{YMap, YScalar}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.domain.shapes.annotations.TypeAlias
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}

trait OasLikeDeclarationsHelper {
  protected val definitionsKey: String

  def parseTypeDeclarations(map: YMap, typesPrefix: String)(implicit ctx: OasLikeWebApiContext): Unit = {
    map.key(
      definitionsKey,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName = e.key.as[YScalar].text
            OasTypeParser(e, shape => {
              shape.set(ShapeModel.Name, AmfScalar(typeName, Annotations(e.key.value)), Annotations(e.key))
              shape.adopted(typesPrefix)
            })(ctx).parse() match {
              case Some(shape) =>
                ctx.declarations += shape.add(DeclaredElement())
              case None =>
                ctx.eh.violation(UnableToParseShape,
                                 NodeShape().adopted(typesPrefix).id,
                                 s"Error parsing shape at $typeName",
                                 e)
            }
          })
      }
    )
  }

  private def isDirectTypeAlias(shape: Shape) = shape.isInstanceOf[AnyShape] && shape.isLink
}
