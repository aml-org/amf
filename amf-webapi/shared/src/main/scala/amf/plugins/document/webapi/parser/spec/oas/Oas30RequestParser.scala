package amf.plugins.document.webapi.parser.spec.oas

import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, ScalarNode, SearchScope, _}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorRequest
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.domain.OasContentsParser
import amf.plugins.domain.webapi.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Payload, Request}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations.RequestBodyContentRequired
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

case class Oas30RequestParser(map: YMap, parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def adopt(request: Request) = {
    request
      .add(Annotations(definitionEntry))
      .set(RequestModel.Name, ScalarNode(definitionEntry.key).string())
      .adopted(parentId)
  }

  def parse(): Request = {
    ctx.link(map) match {
      case Left(fullRef) =>
        parseRef(fullRef)
      case Right(_) =>
        val request = adopt(Request())

        map.key("description", RequestModel.Description in request)
        map.key("required", RequestModel.Required in request)

        val payloads = mutable.ListBuffer[Payload]()

        map.key("content") match {
          case Some(entry) =>
            payloads ++= OasContentsParser(entry, request.withPayload).parse()
          case None =>
            ctx.violation(RequestBodyContentRequired,
                          request.id,
                          s"Request body must have a 'content' field defined",
                          map)
        }
        request.set(ResponseModel.Payloads, AmfArray(payloads))

        AnnotationParser(request, map).parse()
        ctx.closedShape(request.id, map, "request")
        request
    }
  }

  private def parseRef(fullRef: String): Request = {
    val name = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "requestBodies")
    ctx.declarations
      .findRequestBody(name, SearchScope.Named)
      .map(req => adopt(req.link(name, Annotations(map))))
      .getOrElse {
        ctx.obtainRemoteYNode(fullRef) match {
          case Some(requestNode) =>
            Oas30RequestParser(requestNode.as[YMap], parentId, definitionEntry).parse()
          case None =>
            ctx.violation(CoreValidations.UnresolvedReference, "", s"Cannot find requestBody reference $fullRef", map)
            adopt(ErrorRequest(fullRef, map).link(name))
        }
      }
  }
}
