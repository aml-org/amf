package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, SearchScope, _}
import amf.plugins.document.apicontract.annotations.ExternalReferenceUrl
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorRequest
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.apicontract.parser.spec.domain.OasContentsParser
import amf.plugins.domain.apicontract.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.apicontract.models.{Payload, Request}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations.RequestBodyContentRequired
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

case class Oas30RequestParser(map: YMap, parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def adopt(request: Request) = {
    request
      .add(Annotations(definitionEntry))
      .set(RequestModel.Name, ScalarNode(definitionEntry.key).string(), Annotations(definitionEntry.key))
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
            ctx.eh.violation(RequestBodyContentRequired,
                             request.id,
                             s"Request body must have a 'content' field defined",
                             map)
        }
        request.set(ResponseModel.Payloads, AmfArray(payloads, Annotations.virtual()), Annotations.inferred())

        AnnotationParser(request, map)(WebApiShapeParserContextAdapter(ctx)).parse()
        ctx.closedShape(request.id, map, "request")
        request
    }
  }

  private def parseRef(fullRef: String): Request = {
    val name = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "requestBodies")
    ctx.declarations
      .findRequestBody(name, SearchScope.Named)
      .map(req => adopt(req.link(AmfScalar(name), Annotations(map), Annotations.synthesized())))
      .getOrElse {
        ctx.navigateToRemoteYNode(fullRef) match {
          case Some(navigation) =>
            Oas30RequestParser(navigation.remoteNode.as[YMap], parentId, definitionEntry)(navigation.context)
              .parse()
              .add(ExternalReferenceUrl(fullRef))
          case None =>
            ctx.eh.violation(CoreValidations.UnresolvedReference,
                             "",
                             s"Cannot find requestBody reference $fullRef",
                             map)
            adopt(ErrorRequest(fullRef, map).link(name))
        }
      }
  }
}
