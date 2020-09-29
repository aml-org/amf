package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations.DeclaredElement
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfScalar, NamedDomainElement}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.annotations.DeclarationKey
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.NodeShape
import amf.validations.ParserSideValidations
import amf.validations.ParserSideValidations.UnableToParseShape
import org.yaml.model.{YMap, YScalar}

trait OasLikeDeclarationsHelper {
  protected val definitionsKey: String

  def parseTypeDeclarations(map: YMap, typesPrefix: String)(implicit ctx: OasLikeWebApiContext): Unit = {
    map.key(
      definitionsKey,
      entry => {
        ctx.addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName = e.key.as[YScalar].text
            OasTypeParser
              .buildDeclarationParser(e, shape => {
                shape.set(ShapeModel.Name, AmfScalar(typeName, Annotations(e.key.value)), Annotations(e.key))
                shape.adopted(typesPrefix)
              })(ctx)
              .parse() match {
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
