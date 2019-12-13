package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.model.domain.NamedDomainElement
import amf.core.parser._
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.AbstractDeclarationsParser
import amf.plugins.document.webapi.parser.spec.domain.{OasLinkParser, OasHeaderParametersParser, Oas3NamedExamplesParser}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{Parameter, WebApi}
import amf.validations.ParserSideValidations
import org.yaml.model._

case class Oas3DocumentParser(root: Root)(implicit override val ctx: OasWebApiContext)
    extends OasDocumentParser(root) {

  override def parseWebApi(map: YMap): WebApi = {
    val api = super.parseWebApi(map)

    map.key("consumes".asOasExtension, WebApiModel.Accepts in api)
    map.key("produces".asOasExtension, WebApiModel.ContentType in api)
    map.key("schemes".asOasExtension, WebApiModel.Schemes in api)

    api
  }

  override protected val definitionsKey: String = "schemas"
  override protected val securityKey: String    = "securitySchemes"

  override def parseDeclarations(root: Root, map: YMap): Unit =
    map.key("components").foreach { components =>
      val parent = root.location + "#/declarations"
      val map    = components.value.as[YMap]
      parseExamplesDeclaration(map, parent + "/examples")
      parseLinkDeclarations(map, parent + "/links")
      super.parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
      super.parseTypeDeclarations(map, parent + "/types")
      parseHeaderDeclarations(map, parent + "/headers")
      super.parseParameterDeclarations(map, parent + "/parameters")
      super.parseResponsesDeclarations("responses", map, parent + "/responses")
      parseRequestBodyDeclarations(map, parent + "/requestBodies")
      parseCallbackDeclarations(map, parent + "/callbacks")

      super.parseAnnotationTypeDeclarations(map, parent)
      AbstractDeclarationsParser("resourceTypes".asOasExtension,
                                 (entry: YMapEntry) => ResourceType(entry),
                                 map,
                                 parent + "/resourceTypes").parse()
      AbstractDeclarationsParser("traits".asOasExtension, (entry: YMapEntry) => Trait(entry), map, parent + "/traits")
        .parse()
      ctx.closedShape(parent, map, "components")
      validateNames()
    }

  def parseExamplesDeclaration(map: YMap, parent: String): Unit = {
    map.key(
      "examples",
      e => {
        Oas3NamedExamplesParser(e, parent)
          .parse()
          .foreach(ex => ctx.declarations += ex.add(DeclaredElement()))
      }
    )
  }

  def parseRequestBodyDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "requestBodies",
      e => {
        e.value
          .as[YMap]
          .entries
          .foreach(entry => {
            val requestBody =
              Oas3RequestParser(entry.value.as[YMap], parent, entry).parse()
            ctx.declarations += requestBody.add(DeclaredElement())
          })
      }
    )
  }

  def parseHeaderDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "headers",
      entry => {
        val headers: Seq[Parameter] =
          OasHeaderParametersParser(entry.value.as[YMap], _.adopted(parent)).parse()
        headers.foreach(header => {
          header.add(DeclaredElement()).add(DeclaredHeader())
          ctx.declarations.registerHeader(header)
        })
      }
    )
  }

  def parseLinkDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "links",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(entry => ctx.declarations += OasLinkParser(parent, entry).parse().add(DeclaredElement()))
    )
  }

  def parseCallbackDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "callbacks",
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            val callbacks =
              CallbackParser(callbackEntry.value.as[YMap], _.withName(name).adopted(parent), name, callbackEntry)
                .parse()
            callbacks.foreach { callback =>
              callback.add(DeclaredElement())
              ctx.declarations += callback
            }
          }
      }
    )
  }

  def validateNames(): Unit = {
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
      ctx.violation(
        ParserSideValidations.InvalidFieldNameInComponents,
        elem.id,
        msg,
        elem.annotations
      )
    }
  }

}
