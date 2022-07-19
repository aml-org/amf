package amf.shapes.internal.spec.common.parser

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{YMapOps, _}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{MultipleDefinitionKey, UnableToParseShape}
import org.yaml.model.{YMap, YScalar}

object TypeDeclarationParser {

  def parseTypeDeclarations(map: YMap, definitionsKey: String, declarationKeysHolder: Option[DeclarationKeyCollector])(
      implicit ctx: ShapeParserContext
  ): List[AnyShape] = parseTypeDeclarations(map, Seq(definitionsKey), declarationKeysHolder)

  def parseTypeDeclarations(
      map: YMap,
      definitionsKeys: Seq[String],
      declarationKeysHolder: Option[DeclarationKeyCollector]
  )(implicit ctx: ShapeParserContext): List[AnyShape] = {
    validateMultipleDeclarationKeys(map, definitionsKeys)
    definitionsKeys.flatMap(defKey => parseDeclarationMap(map, defKey, declarationKeysHolder)).toList
  }

  private def validateMultipleDeclarationKeys(map: YMap, definitionsKeys: Seq[String])(implicit
      ctx: ShapeParserContext
  ): Unit = {
    val foundDefKeys = definitionsKeys.flatMap(map.key(_))
    if (foundDefKeys.size > 1) { // If there is more than 1 definition key present in the map
      ctx.eh.warning(
        specification = MultipleDefinitionKey,
        node = "",
        message =
          MultipleDefinitionKey.message + s". You should use only one of them: ${definitionsKeys.mkString(", ")}",
        location = map.location
      )
    }
  }

  private def parseDeclarationMap(
      map: YMap,
      definitionsKey: String,
      declarationKeysHolder: Option[DeclarationKeyCollector]
  )(implicit
      ctx: ShapeParserContext
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
