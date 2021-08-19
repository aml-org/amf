package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnableToParseShape
import amf.core.client.scala.model.domain.{AmfScalar, NamedDomainElement}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YScalarYRead
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.{YMap, YScalar}

trait OasLikeDeclarationsHelper {
  protected val definitionsKey: String

  def parseTypeDeclarations(map: YMap, typesPrefix: String, declarationKeysHolder: Option[DeclarationKeyCollector])(
      implicit ctx: OasLikeWebApiContext): Unit = {
    map.key(
      definitionsKey,
      entry => {
        declarationKeysHolder.foreach(_.addDeclarationKey(DeclarationKey(entry)))
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName = e.key.as[YScalar].text
            OasTypeParser
              .buildDeclarationParser(e, shape => {
                shape.set(ShapeModel.Name, AmfScalar(typeName, Annotations(e.key.value)), Annotations(e.key))
                shape.adopted(typesPrefix)
              })(WebApiShapeParserContextAdapter(ctx))
              .parse() match {
              case Some(shape) =>
                ctx.declarations += shape.add(DeclaredElement())
              case None =>
                ctx.eh.violation(UnableToParseShape,
                                 NodeShape().adopted(typesPrefix).id,
                                 s"Error parsing shape at $typeName",
                                 e.location)
            }
          })
      }
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
                s"Name $name does not match regular expression ${keyRegex.toString()} for component declarations")
          case None =>
            violation(elem, "No name is defined for given component declaration")
        }
      case _ =>
    }
    def violation(elem: NamedDomainElement, msg: String): Unit = {
      ctx.eh.violation(
        ParserSideValidations.InvalidFieldNameInComponents,
        elem.id,
        msg,
        elem.annotations
      )
    }
  }
}
