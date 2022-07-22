package amf.shapes.internal.spec.common.parser

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.annotations.DeclarationsKey
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{MultipleDefinitionKey, UnableToParseShape}
import org.yaml.model.{YMap, YMapEntry, YScalar}

object TypeDeclarationParser {

  def parseTypeDeclarations(
      map: YMap,
      definitionsKey: String,
      declarationKeysHolder: Option[DeclarationKeyCollector]
  )(implicit ctx: ShapeParserContext): List[AnyShape] =
    parseTypeDeclarations(map, Seq(definitionsKey), declarationKeysHolder, None)

  def parseTypeDeclarations(
      map: YMap,
      definitionsKeys: Seq[String],
      declarationKeysHolder: Option[DeclarationKeyCollector],
      document: Option[Document] = None
  )(implicit ctx: ShapeParserContext): List[AnyShape] = {
    val definitionEntries = definitionsKeys.flatMap(dk => map.key(dk))
    validateMultipleDeclarationKeys(definitionEntries.size, map, definitionsKeys)
    definitionEntries.headOption // I will keep only the declarations of the first key (it should be in priority order)
      .map { de =>
        // I will annotate the declaration key in the document if present
        document.foreach(_.annotations += DeclarationsKey(de.key.asScalar.get.text))
        parseDeclarationMap(de, declarationKeysHolder)
      }
      .getOrElse(Nil)
  }

  private def validateMultipleDeclarationKeys(foundDefKeys: Int, map: YMap, definitionsKeys: Seq[String])(implicit
      ctx: ShapeParserContext
  ): Unit = {
    if (foundDefKeys > 1) { // If there is more than 1 definition key present in the map
      ctx.eh.violation(
        specification = MultipleDefinitionKey,
        node = "",
        message =
          MultipleDefinitionKey.message + s". You should use only one of them: ${definitionsKeys.mkString(", ")}",
        map.location
      )
    }
  }

  private def parseDeclarationMap(
      declarationEntry: YMapEntry,
      declarationKeysHolder: Option[DeclarationKeyCollector]
  )(implicit
      ctx: ShapeParserContext
  ): List[AnyShape] = {
    declarationKeysHolder.foreach(_.addDeclarationKey(DeclarationKey(declarationEntry)))
    declarationEntry.value
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
      .toList
  }
}
