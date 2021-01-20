package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.model.domain.NamedDomainElement
import amf.core.parser._
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.annotations.DeclarationKey
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.YamlTagValidator
import amf.plugins.document.webapi.parser.spec.declaration.AbstractDeclarationsParser
import amf.plugins.document.webapi.parser.spec.domain.{
  Oas3NamedExamplesParser,
  OasHeaderParametersParser,
  OasLinkParser
}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.metamodel.templates.{ResourceTypeModel, TraitModel}
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.Parameter
import amf.validations.ParserSideValidations
import org.yaml.model._

case class Oas3DocumentParser(root: Root)(implicit override val ctx: OasWebApiContext)
    extends OasDocumentParser(root) {

  override def parseWebApi(map: YMap): WebApi = {
    YamlTagValidator.validate(root)
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
      super.parseTypeDeclarations(map, parent + "/types", Some(this))
      parseHeaderDeclarations(map, parent + "/headers")
      super.parseParameterDeclarations(map, parent + "/parameters")
      super.parseResponsesDeclarations("responses", map, parent + "/responses")
      parseRequestBodyDeclarations(map, parent + "/requestBodies")
      parseCallbackDeclarations(map, parent + "/callbacks")

      super.parseAnnotationTypeDeclarations(map, parent)
      AbstractDeclarationsParser("resourceTypes".asOasExtension,
                                 (entry: YMapEntry) => ResourceType(entry),
                                 map,
                                 parent + "/resourceTypes",
                                 ResourceTypeModel).parse()
      AbstractDeclarationsParser("traits".asOasExtension,
                                 (entry: YMapEntry) => Trait(entry),
                                 map,
                                 parent + "/traits",
                                 TraitModel)
        .parse()
      ctx.closedShape(parent, map, "components")
      validateNames()
    }

  def parseExamplesDeclaration(map: YMap, parent: String): Unit = {
    map.key(
      "examples",
      e => {
        addDeclarationKey(DeclarationKey(e))
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
        addDeclarationKey(DeclarationKey(e, isAbstract = true))
        e.value
          .as[YMap]
          .entries
          .foreach(entry => {
            val requestBody =
              Oas30RequestParser(entry.value.as[YMap], parent, entry).parse()
            ctx.declarations += requestBody.add(DeclaredElement())
          })
      }
    )
  }

  def parseHeaderDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "headers",
      entry => {
        addDeclarationKey(DeclarationKey(entry, isAbstract = true))
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
      entry => {
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach(entry => ctx.declarations += OasLinkParser(parent, entry).parse().add(DeclaredElement()))
      }
    )
  }

  def parseCallbackDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "callbacks",
      entry => {
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            val callbacks =
              Oas30CallbackParser(callbackEntry.value.as[YMap], _.withName(name).adopted(parent), name, callbackEntry)
                .parse()
            callbacks.foreach { callback =>
              callback.add(DeclaredElement())
              ctx.declarations += callback
            }
          }
      }
    )
  }

}
