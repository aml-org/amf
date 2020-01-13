package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{SynthesizedField, VirtualObject}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.core.utils.{IdCounter, _}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncOperationBindingsParser
import amf.plugins.document.webapi.parser.spec.oas.{
  Oas20RequestParser,
  Oas30CallbackParser,
  Oas30ParametersParser,
  Oas30RequestParser
}
import amf.plugins.domain.webapi.metamodel.{OperationModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Operation, Request, Response}
import amf.validations.ParserSideValidations.DuplicatedOperationId
import org.yaml.model._

import scala.collection.mutable

abstract class OasLikeOperationParser(entry: YMapEntry, producer: String => Operation)(
    implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  def parse(): Operation = {

    val operation: Operation = producer(ScalarNode(entry.key).string().value.toString).add(Annotations(entry))
    val map                  = entry.value.as[YMap]

    ctx.closedShape(operation.id, map, "operation")

    map.key("operationId").foreach { entry =>
      val operationId = entry.value.toString()
      if (!ctx.registerOperationId(operationId))
        ctx.eh.violation(DuplicatedOperationId, operation.id, s"Duplicated operation id '$operationId'", entry.value)
    }

    map.key("operationId", OperationModel.Name in operation)
    map.key("description", OperationModel.Description in operation)
    map.key("summary", OperationModel.Summary in operation)
    map.key("externalDocs",
            OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id)))

    AnnotationParser(operation, map).parseOrphanNode("responses")
    AnnotationParser(operation, map).parse()

    operation
  }
}

abstract class OasOperationParser(entry: YMapEntry, producer: String => Operation)(
    override implicit val ctx: OasWebApiContext)
    extends OasLikeOperationParser(entry, producer) {
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

    map.key(
      "security",
      entry => {
        val idCounter = new IdCounter()
        // TODO check for empty array for resolution ?
        val securedBy = entry.value
          .as[Seq[YNode]]
          .map(s => OasLikeSecurityRequirementParser(s, operation.withSecurity, idCounter).parse())
          .collect { case Some(s) => s }

        operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "responses",
      entry => {
        val responses = mutable.ListBuffer[Response]()

        entry.value
          .as[YMap]
          .entries
          .filter(y => !isOasAnnotation(y.key.as[YScalar].text))
          .foreach { entry =>
            val node = ScalarNode(entry.key).text()
            responses += OasResponseParser(entry.value.as[YMap], { r =>
              r.set(ResponseModel.Name, node)
                .adopted(operation.id)
                .withStatusCode(r.name.value())
              r.annotations ++= Annotations(entry)
            }).parse()
          }

        operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), Annotations(entry))
      }
    )

    operation
  }
}

case class Oas20OperationParser(entry: YMapEntry, producer: String => Operation)(
    override implicit val ctx: OasWebApiContext)
    extends OasOperationParser(entry, producer) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]
    Oas20RequestParser(map, (r: Request) => r.adopted(operation.id))
      .parse()
      .map(r => operation.set(OperationModel.Request, AmfArray(Seq(r)), Annotations() += SynthesizedField()))

    map.key("schemes", OperationModel.Schemes in operation)
    map.key("consumes", OperationModel.Accepts in operation)
    map.key("produces", OperationModel.ContentType in operation)

    operation
  }
}

case class Oas30OperationParser(entry: YMapEntry, producer: String => Operation)(
    override implicit val ctx: OasWebApiContext)
    extends OasOperationParser(entry, producer) {
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

    if (operation.fields.exists(OperationModel.Request)) operation.request.annotations += VirtualObject()
    ctx.factory.serversParser(map, operation).parse()

    operation
  }

}

case class AsyncOperationParser(entry: YMapEntry, producer: String => Operation)(
    override implicit val ctx: AsyncWebApiContext)
    extends OasLikeOperationParser(entry, producer) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "tags",
      entry => {
        val tags = OasLikeTagsParser(operation.id, entry).parse()
        operation.set(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "message",
      entry =>
        messageType() foreach { msgType =>
          val messages = AsyncMessageParser(operation.id, entry.value.as[YMap], msgType).parse()
          operation.setArray(msgType.field, messages, Annotations(entry.value))
      }
    )

    map.key("bindings").foreach { entry =>
      val bindings = AsyncOperationBindingsParser.parse(entry.value.as[YMap], operation.id)
      operation.setArray(OperationModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(operation, map).parseOrphanNode("bindings")
    }

//    map.key("traits", OperationModel. in operation)

    operation
  }

  private def messageType(): Option[MessageType] =
    entry.key.value match {
      case scalar: YScalar if scalar.text == "publish"   => Some(Publish)
      case scalar: YScalar if scalar.text == "subscribe" => Some(Subscribe)
      // invalid message type is validated with closed shape of pathItem
      case _ => None
    }
}
