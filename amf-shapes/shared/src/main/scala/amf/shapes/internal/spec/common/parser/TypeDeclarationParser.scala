package amf.shapes.internal.spec.common.parser

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseShape
import org.yaml.model.{YMap, YScalar}
import amf.core.internal.parser._

object TypeDeclarationParser {

  def parseTypeDeclarations(map: YMap, definitionsKey: String, declarationKeysHolder: Option[DeclarationKeyCollector])(
      implicit ctx: ShapeParserContext
  ): List[AnyShape] = {
    map.key(definitionsKey).toList.flatMap { entry =>
      declarationKeysHolder.foreach(_.addDeclarationKey(DeclarationKey(entry)))
      entry.value
        .as[YMap]
        .entries
        .flatMap(e => {
          val typeName = e.key.as[YScalar].text
          OasTypeParser
            .buildDeclarationParser(
              e,
              shape => {
                shape.setWithoutId(ShapeModel.Name, AmfScalar(typeName, Annotations(e.key.value)), Annotations(e.key))
              }
            )
            .parse() match {
            case Some(shape) =>
              ctx.addDeclaredShape(shape.add(DeclaredElement()))
              Some(shape)
            case None =>
              ctx.eh.violation(UnableToParseShape, NodeShape().id, s"Error parsing shape at $typeName", e.location)
              None
          }
        })
    }
  }
}
