package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.model.domain.NamedDomainElement
import amf.core.parser._
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.AbstractDeclarationsParser
import amf.plugins.document.webapi.parser.spec.domain.{
  Oas3ResponseExamplesParser,
  OasHeaderParametersParser,
  OasLinkParser
}
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
      validateNames()
    }

  def parseExamplesDeclaration(map: YMap, parent: String): Unit = {
    map.key(
      "examples",
      e => {
        val examples = Oas3ResponseExamplesParser(e).parse()
        examples.foreach(ex => {
          ex.adopted(parent)
          ctx.declarations += ex.add(DeclaredElement())
        })
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
          .map(entry => {
            val typeName = entry.key.as[YScalar].text
            val requestBody =
              Oas3RequestParser(entry.value.as[YMap], (req) => req.withName(typeName).adopted(parent)).parse()
            requestBody.foreach(ctx.declarations += _.add(DeclaredElement()))
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
          .foreach { entry =>
            val linkName = ScalarNode(entry.key).text().value.toString
            OasLinkParser(entry.value, linkName, (link) => link.adopted(parent))
              .parse()
              .foreach { link =>
                link.add(DeclaredElement())
                ctx.declarations += link
              }
        }
    )
  }

  def parseCallbackDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "callbacks",
      entry => {
        entry.value
          .as[YMap]
          .entries
          .map { callbackEntry =>
            val name     = callbackEntry.key.as[YScalar].text
            val callback = CallbackParser(callbackEntry.value.as[YMap], _.withName(name).adopted(parent)).parse()
            callback.add(DeclaredElement())
            ctx.declarations += callback
          }
      }
    )
  }

  def validateNames(): Unit = {
    val declarations = ctx.declarations.declarables()
    val keyRegex     = """^[a-zA-Z0-9\.\-_]+$""".r
    declarations.foreach {
      case elem: NamedDomainElement =>
        val name = elem.name.value()
        if (!keyRegex.pattern.matcher(name).matches())
          ctx.violation(
            ParserSideValidations.InvalidFieldNameInComponents,
            elem.id,
            s"Name $name does not match regular expression ${keyRegex.toString()} for component declarations",
            elem.annotations
          )
      case _ =>
    }
  }

}
