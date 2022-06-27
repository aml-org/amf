package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnableToParseShape
import amf.core.client.scala.model.domain.{AmfScalar, NamedDomainElement, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YScalarYRead
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.{YMap, YScalar}

object Draft4JsonSchemaDeclarationsParser extends OasLikeDeclarationsHelper {
  override protected val definitionsKey: String = "definitions"
}

trait OasLikeDeclarationsHelper {
  protected val definitionsKey: String

  def parseTypeDeclarations(map: YMap)(implicit ctx: OasLikeWebApiContext): List[AnyShape] =
    OasLikeTypeDeclarationParser.parseTypeDeclarations(map, definitionsKey)

  def parseTypeDeclarations(map: YMap, declarationKeysHolder: Option[DeclarationKeyCollector])(implicit
      ctx: OasLikeWebApiContext
  ): List[AnyShape] = {
    OasLikeTypeDeclarationParser.parseTypeDeclarations(map, definitionsKey, declarationKeysHolder)(
      WebApiShapeParserContextAdapter(ctx)
    )
  }

  def validateNames()(implicit ctx: OasLikeWebApiContext): Unit = {
    val declarations = ctx.declarations.declarables()
    val keyRegex     = """^[a-zA-Z0-9\.\-_]+$""".r
    declarations.foreach {
      case elem: NamedDomainElement =>
        elem.name.option() match {
          case Some(name) =>
            if (!keyRegex.pattern.matcher(name).matches())
              violation(
                elem,
                s"Name $name does not match regular expression ${keyRegex.toString()} for component declarations"
              )
          case None =>
            violation(elem, "No name is defined for given component declaration")
        }
      case _ =>
    }
    def violation(elem: NamedDomainElement, msg: String): Unit = {
      ctx.eh.violation(
        ParserSideValidations.InvalidFieldNameInComponents,
        elem,
        msg,
        elem.annotations
      )
    }
  }
}

object OasLikeTypeDeclarationParser {
  def parseTypeDeclarations(map: YMap, definitionsKey: String)(implicit ctx: OasLikeWebApiContext): List[AnyShape] =
    parseTypeDeclarations(map, definitionsKey, None)(WebApiShapeParserContextAdapter(ctx))

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
