package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.VirtualElement
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.core.utils.{IdCounter, _}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{Oas3WebApiContext, OasWebApiContext}
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasLikeCreativeWorkParser
import amf.plugins.document.webapi.parser.spec.oas.{
  Oas20RequestParser,
  Oas30CallbackParser,
  Oas30ParametersParser,
  Oas30RequestParser
}
import amf.plugins.domain.webapi.metamodel.OperationModel.Method
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.metamodel.{OperationModel, ResponseModel}
import amf.plugins.domain.webapi.models.security.SecurityRequirement
import amf.plugins.domain.webapi.models.{Operation, Request, Response}
import amf.validations.ParserSideValidations.{DuplicatedOperationId, InvalidStatusCode}
import org.yaml.model._

import scala.collection.mutable

abstract class OasLikeOperationParser(entry: YMapEntry, parentId: String)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  protected def methodNode: AmfScalar = ScalarNode(entry.key).string()

  def parse(): Operation = {
    val operation: Operation = Operation(Annotations(entry))
    operation.set(Method, methodNode, Annotations.inferred())
    operation.adopted(parentId)

    val map = entry.value.as[YMap]

    ctx.closedShape(operation.id, map, "operation")

    map.key("operationId").foreach { entry =>
      val operationId = entry.value.toString()
      if (!ctx.registerOperationId(operationId))
        ctx.eh.violation(DuplicatedOperationId, operation.id, s"Duplicated operation id '$operationId'", entry.value)
    }

    parseOperationId(map, operation)

    map.key("description", OperationModel.Description in operation)
    map.key("summary", OperationModel.Summary in operation)
    map.key("externalDocs",
            OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id)))

    AnnotationParser(operation, map).parseOrphanNode("responses")
    AnnotationParser(operation, map).parse()

    operation
  }

  def parseOperationId(map: YMap, operation: Operation) = {
    map.key("operationId", OperationModel.Name in operation)
    map.key("operationId", OperationModel.OperationId in operation)
  }
}

abstract class OasOperationParser(entry: YMapEntry, parentId: String)(override implicit val ctx: OasWebApiContext)
    extends OasLikeOperationParser(entry, parentId) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key("deprecated", OperationModel.Deprecated in operation)

    map.key(
      "tags",
      entry => {
        val tags = StringTagsParser(entry.value.as[YSequence], operation.id).parse()
        operation.set(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "is".asOasExtension,
      entry => {
        val traits = entry.value
          .as[Seq[YNode]]
          .map(value => {
            ParametrizedDeclarationParser(value, operation.withTrait, ctx.declarations.findTraitOrError(value))
              .parse()
          })
        if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
      }
    )

    map.key("security".asOasExtension, entry => { parseSecurity(operation, entry) })

    map.key("security", entry => { parseSecurity(operation, entry) })

    map.key(
      "responses",
      entry => {
        val responses = mutable.ListBuffer[Response]()

        entry.value
          .as[YMap]
          .entries
          .filter(y => !isOasAnnotation(y.key.as[YScalar].text))
          .foreach {
            entry =>
              val node = ScalarNode(entry.key)
              responses += OasResponseParser(
                entry.value.as[YMap], { r =>
                  r.withName(node)
                    .adopted(operation.id)
                    .set(ResponseModel.StatusCode, node.text(), Annotations.inferred())
                  r.annotations ++= Annotations(entry)
                  // Validation for OAS 3
                  if (ctx.isInstanceOf[Oas3WebApiContext] && entry.key.tagType != YType.Str)
                    ctx.eh.violation(InvalidStatusCode,
                                     r.id,
                                     "Status code for a Response object must be a string",
                                     entry.key)
                }
              ).parse()
          }

        operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), Annotations(entry))
      }
    )

    operation
  }

  private def parseSecurity(operation: Operation, entry: YMapEntry): Unit = {
    val idCounter = new IdCounter()
    // TODO check for empty array for resolution ?
    val requirements = entry.value
      .as[Seq[YNode]]
      .flatMap(s =>
        OasLikeSecurityRequirementParser(s, (s: SecurityRequirement) => s.adopted(operation.id), idCounter).parse())
    val extension = operation.security
    operation.set(WebApiModel.Security,
                  AmfArray(requirements ++ extension, Annotations(entry.value)),
                  Annotations(entry))
  }
}

case class Oas20OperationParser(entry: YMapEntry, parentId: String)(override implicit val ctx: OasWebApiContext)
    extends OasOperationParser(entry, parentId) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]
    Oas20RequestParser(map, (r: Request) => r.adopted(operation.id))
      .parse()
      .map(r => operation.set(OperationModel.Request, AmfArray(Seq(r)), Annotations.synthesized()))

    map.key("schemes", OperationModel.Schemes in operation)
    map.key("consumes", OperationModel.Accepts in operation)
    map.key("produces", OperationModel.ContentType in operation)

    operation
  }
}

case class Oas30OperationParser(entry: YMapEntry, parentId: String)(override implicit val ctx: OasWebApiContext)
    extends OasOperationParser(entry, parentId) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "requestBody",
      entry => {
        operation.withRequest(Oas30RequestParser(entry.value.as[YMap], operation.id, entry).parse())
      }
    )

    // parameters defined in endpoint are stored in the request
    Oas30ParametersParser(map, Option(operation.request).map(() => _).getOrElse(operation.withRequest))
      .parseParameters()

    map.key(
      "callbacks",
      entry => {
        val callbacks = entry.value
          .as[YMap]
          .entries
          .flatMap { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            Oas30CallbackParser(callbackEntry.value.as[YMap],
                                _.withName(name).adopted(operation.id),
                                name,
                                callbackEntry)
              .parse()
          }
        operation.withCallbacks(callbacks)
      }
    )

    if (operation.fields.exists(OperationModel.Request)) operation.request.annotations += VirtualElement()
    ctx.factory.serversParser(map, operation).parse()

    operation
  }

}
