package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.{Operation, Response}
import amf.apicontract.internal.annotations.OperationTraitEntry
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.metamodel.domain.OperationModel.Method
import amf.apicontract.internal.spec.common.parser.{
  RamlSecurityRequirementParser,
  SpecParserOps,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.oas.parser.domain.{Oas30CallbackParser, StringTagsParser}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.spec.toOas
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidOperationType
import amf.core.client.common.position.Range
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.utils.{AmfStrings, IdCounter}
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation.isRamlAnnotation
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser}
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable

case class RamlOperationParser(entry: YMapEntry, parentId: String, parseOptional: Boolean = false)(implicit
    ctx: RamlWebApiContext
) extends SpecParserOps {

  private def build(): Operation = {
    val method: String = entry.key.as[YScalar].text
    val methodNode     = ScalarNode(entry.key).string()
    val operation      = Operation(Annotations(entry))

    if (parseOptional && method.endsWith("?")) {
      operation.set(OperationModel.Optional, value = true)
      operation.set(
        OperationModel.Method,
        AmfScalar(method.stripSuffix("?"), methodNode.annotations),
        Annotations.inferred()
      )
    } else {
      operation.setWithoutId(Method, methodNode, Annotations.inferred())
    }

    operation
  }

  def parse(): Operation = {
    val operation = build()
    entry.value.tagType match {
      // Regular operation
      case YType.Map => parseMap(entry.value.as[YMap], operation)
      // Empty operation
      case _ if entry.value.toOption[YScalar].map(_.text).exists(s => s == "" || s == "null") => operation
      case _ =>
        ctx.eh.violation(
          InvalidOperationType,
          operation,
          s"Invalid node ${entry.value} for method ${operation.method.value()}",
          entry.value.location
        )
        operation
    }
  }

  protected def parseMap(map: YMap, operation: Operation): Operation = {
    val map     = entry.value.as[YMap]
    val isTrait = ctx.contextType == RamlWebApiContextType.TRAIT

    ctx.closedShape(operation, map, if (isTrait) "trait" else "operation")

    map.key("displayName", OperationModel.Name in operation)
    map.key("oasDeprecated".asRamlAnnotation, OperationModel.Deprecated in operation)
    map.key("summary".asRamlAnnotation, OperationModel.Summary in operation)
    map.key(
      "externalDocs".asRamlAnnotation,
      OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id)(
        WebApiShapeParserContextAdapter(ctx)
      ))
    )
    map.key("protocols", (OperationModel.Schemes in operation).allowingSingleValue)
    map.key("consumes".asRamlAnnotation, OperationModel.Accepts in operation)
    map.key("produces".asRamlAnnotation, OperationModel.ContentType in operation)
    map.key(
      "tags".asRamlAnnotation,
      entry => {
        val tags = StringTagsParser(entry.value.as[YSequence], operation).parse()
        operation.withTags(tags)
      }
    )
    val DeclarationParser = ParametrizedDeclarationParser.parse(operation.withTrait) _
    map.key(
      "is",
      (e: YMapEntry) => {
        operation.annotations += OperationTraitEntry(Range(e.range))
        (DomainElementModel.Extends in operation using DeclarationParser).allowingSingleValue.optional(e)
      }
    )

    ctx.factory
      .requestParser(map, () => operation.withInferredRequest(), parseOptional)
      .parse()
      .foreach(req =>
        operation
          .setWithoutId(OperationModel.Request, AmfArray(List(req), Annotations.virtual()), Annotations.inferred())
      )

    map.key(
      "defaultResponse".asRamlAnnotation,
      entry => {
        if (entry.value.tagType == YType.Map) {
          val responses = mutable.ListBuffer[Response]()
          entry.value.as[YMap].entries.foreach { entry =>
            responses += ctx.factory
              .responseParser(entry, (r: Response) => Unit, parseOptional)
              .parse()
          }
          operation.withResponses(responses)
        }
      }
    )
    map.key(
      "responses",
      entry => {
        val responses = mutable.ListBuffer[Response]()
        entry.value.tagType match {
          case YType.Null => // ignore
          case _ =>
            val entries = entry.value
              .as[YMap]
              .entries
              .filter(y => !isRamlAnnotation(y.key.as[YScalar].text))

            entries.foreach { entry =>
              responses += ctx.factory
                .responseParser(entry, _ => Unit, parseOptional)
                .parse()
            }
        }

        val defaultResponses = operation.responses
        operation.setWithoutId(
          OperationModel.Responses,
          AmfArray(responses ++ defaultResponses, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )

    map.key(
      "callbacks".asRamlAnnotation,
      entry => {
        val callbacks = entry.value
          .as[YMap]
          .entries
          .flatMap { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            Oas30CallbackParser(callbackEntry.value.as[YMap], _.withName(name), name, callbackEntry)(toOas(ctx))
              .parse()
          }
        operation.withCallbacks(callbacks)
      }
    )

    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(operation.id, idCounter) _
    map.key("securedBy", (OperationModel.Security in operation using RequirementParser).allowingSingleValue)

    map.key("description", (OperationModel.Description in operation).allowingAnnotations)

    AnnotationParser(
      operation,
      map,
      if (isTrait) List(VocabularyMappings.`trait`)
      else List(VocabularyMappings.operation)
    )(WebApiShapeParserContextAdapter(ctx)).parse()

    operation
  }
}
