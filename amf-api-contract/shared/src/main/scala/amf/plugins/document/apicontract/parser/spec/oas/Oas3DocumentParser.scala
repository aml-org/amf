package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.Root
import amf.core.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.model.domain.NamedDomainElement
import amf.core.parser._
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.vocabularies.parser.common.DeclarationKey
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.common.YamlTagValidator
import amf.plugins.document.apicontract.parser.spec.declaration.AbstractDeclarationsParser
import amf.plugins.document.apicontract.parser.spec.domain.{
  Oas3NamedExamplesParser,
  OasHeaderParametersParser,
  OasLinkParser
}
import amf.plugins.domain.apicontract.metamodel._
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.metamodel.templates.{ResourceTypeModel, TraitModel}
import amf.plugins.domain.apicontract.models.api.WebApi
import amf.plugins.domain.apicontract.models.templates.{ResourceType, Trait}
import amf.plugins.domain.apicontract.models.Parameter
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
                                 ResourceTypeModel,
                                 this).parse()
      AbstractDeclarationsParser("traits".asOasExtension,
                                 (entry: YMapEntry) => Trait(entry),
                                 map,
                                 parent + "/traits",
                                 TraitModel,
                                 this)
        .parse()
      ctx.closedShape(parent, map, "components")
      validateNames()
    }

  def parseExamplesDeclaration(map: YMap, parent: String): Unit = {
    map.key(
      "examples",
      e => {
        addDeclarationKey(DeclarationKey(e))
        Oas3NamedExamplesParser(e, parent)(WebApiShapeParserContextAdapter(ctx))
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
