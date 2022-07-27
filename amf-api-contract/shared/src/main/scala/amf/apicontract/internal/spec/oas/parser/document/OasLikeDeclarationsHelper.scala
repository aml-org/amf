package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.common.DeclarationKeyCollector
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.TypeDeclarationParser
import org.yaml.model.YMap

object Draft4JsonSchemaDeclarationsParser extends OasLikeDeclarationsHelper {
  override protected val definitionsKey: String = "definitions"
}

trait OasLikeDeclarationsHelper {
  protected val definitionsKey: String

  def parseTypeDeclarations(map: YMap)(implicit ctx: OasLikeWebApiContext): List[AnyShape] =
    parseTypeDeclarations(map, None)

  def parseTypeDeclarations(map: YMap, declarationKeysHolder: Option[DeclarationKeyCollector])(implicit
      ctx: OasLikeWebApiContext
  ): List[AnyShape] = {
    TypeDeclarationParser.parseTypeDeclarations(map, definitionsKey, declarationKeysHolder)
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
