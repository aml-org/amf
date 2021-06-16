package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Payload, Request}
import amf.apicontract.internal.metamodel.domain.{RequestModel, ResponseModel}
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorRequest
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations.RequestBodyContentRequired
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.client.scala.annotations.ExternalReferenceUrl
import amf.shapes.internal.spec.common.parser.AnnotationParser
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
