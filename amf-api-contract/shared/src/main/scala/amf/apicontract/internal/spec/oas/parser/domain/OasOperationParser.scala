package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.{Operation, Response}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.{OperationModel, ResponseModel}
import amf.apicontract.internal.spec.common.parser.OasLikeSecurityRequirementParser
import amf.apicontract.internal.spec.oas.parser.context.{Oas31WebApiContext, Oas3WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.raml.parser.domain.ParametrizedDeclarationParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidStatusCode
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.annotations.SourceAST
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, IdCounter}
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation.isOasAnnotation
import org.yaml.model._

import scala.collection.mutable

abstract class OasOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasLikeOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    ctx.closedShape(operation, map, "operation")

    map.key("deprecated", OperationModel.Deprecated in operation)

    map.key(
      "tags",
      entry => {
        val tags = StringTagsParser(entry.value.as[YSequence], operation).parse()
        operation.setWithoutId(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
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

    map.key("security".asOasExtension, entry => parseSecurity(operation, entry))

    map.key("security", entry => parseSecurity(operation, entry))

    map.key(
      "responses",
      entry => {
        val responses = mutable.ListBuffer[Response]()

        entry.value
          .as[YMap]
          .entries
          .filter(y => !isOasAnnotation(y.key.as[YScalar].text))
          .foreach { entry =>
            val node = ScalarNode(entry.key)
            responses += OasResponseParser(
              entry.value.as[YMap],
              { r =>
                r.setWithoutId(ResponseModel.Name, node.text(), Annotations(entry.key))
                  .setWithoutId(ResponseModel.StatusCode, node.text(), Annotations.inferred())
                if (!r.annotations.contains(classOf[SourceAST]))
                  r.annotations ++= Annotations(entry)
                // Validation for OAS 3.0 & 3.1
                if (
                  (ctx.isInstanceOf[Oas3WebApiContext] || ctx
                    .isInstanceOf[Oas31WebApiContext]) && entry.key.tagType != YType.Str
                )
                  ctx.eh.violation(
                    InvalidStatusCode,
                    r,
                    "Status code for a Response object must be a string",
                    entry.key.location
                  )
              }
            ).parse()
          }

        operation.setWithoutId(
          OperationModel.Responses,
          AmfArray(responses, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )

    operation
  }

  private def parseSecurity(operation: Operation, entry: YMapEntry): Unit = {
    val idCounter = new IdCounter()
    // TODO check for empty array for resolution ?
    val requirements = entry.value
      .as[Seq[YNode]]
      .flatMap(s => OasLikeSecurityRequirementParser(s, (s: SecurityRequirement) => Unit, idCounter).parse())
    val extension = operation.security
    operation.setWithoutId(
      WebApiModel.Security,
      AmfArray(requirements ++ extension, Annotations(entry.value)),
      Annotations(entry)
    )
  }
}
