package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.templates.{ResourceTypeModel, TraitModel}
import amf.apicontract.internal.spec.common.parser.{AbstractDeclarationsParser, WebApiShapeParserContextAdapter, YamlTagValidator}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.OasLinkParser
import amf.apicontract.internal.spec.oas.parser.domain.{Oas30RequestParser, OasHeaderParametersParser, OasLinkParser}
import amf.core.internal.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.internal.parser.Root
import amf.plugins.document.vocabularies.parser.common.DeclarationKey
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class Oas3DocumentParser(root: Root)(implicit override val ctx: OasWebApiContext) extends OasDocumentParser(root) {

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
