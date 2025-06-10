package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.spec.common.parser.{WebApiLikeReferencesParser, YamlTagValidator}
import amf.apicontract.internal.spec.oas.parser.Oas3ReferencesParser
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.domain.{
  Oas30CallbackParser,
  Oas30RequestParser,
  OasHeaderParametersParser,
  OasLinkParser
}
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{AnnotationParser, Oas3NamedExamplesParser}
import org.yaml.model.{YMap, YScalar}

object Oas3DocumentParser {
  def apply(root: Root, spec: Spec = Spec.OAS30)(implicit ctx: OasWebApiContext): Oas3DocumentParser =
    new Oas3DocumentParser(root, spec)
}

class Oas3DocumentParser(root: Root, spec: Spec = Spec.OAS30)(implicit override val ctx: OasWebApiContext)
    extends Oas2DocumentParser(root, spec) {

  override protected def buildReferencesParser(document: Document, map: YMap): WebApiLikeReferencesParser =
    Oas3ReferencesParser(document, root)

  override def parseWebApi(map: YMap): WebApi = {
    YamlTagValidator.validate(root)
    val api = super.parseWebApi(map)
    AnnotationParser(api, map).parseOrphanNode("components")
    api
  }

  override protected val definitionsKey: String = "schemas"
  override protected val securityKey: String    = "securitySchemes"

  override def parseDeclarations(root: Root, map: YMap, parentObj: AmfObject): Unit = {
    map.key("components").foreach { components =>
      val parent = root.location + "#/declarations"
      val map    = components.value.as[YMap]

      parseExamplesDeclaration(map, parent + "/examples")
      parseLinkDeclarations(map, parent + "/links")
      super.parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
      super.parseTypeDeclarations(map, Some(this))
      parseHeaderDeclarations(map, parent + "/headers")
      super.parseParameterDeclarations(map, parent + "/parameters")
      super.parseResponsesDeclarations("responses", map)
      parseRequestBodyDeclarations(map, parent + "/requestBodies")
      parseCallbackDeclarations(map, parent + "/callbacks")
      super.parseAnnotationTypeDeclarations(map, parent)
      super.parseAbstractDeclarations(parent, map)

      ctx.closedShape(parentObj, map, "components")
      validateNames()
    }
    AnnotationParser(parentObj, map).parseOrphanNode("components")
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
          OasHeaderParametersParser(entry.value.as[YMap], _ => Unit).parse()
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
              Oas30CallbackParser(callbackEntry.value.as[YMap], _.withName(name), name, callbackEntry)
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
