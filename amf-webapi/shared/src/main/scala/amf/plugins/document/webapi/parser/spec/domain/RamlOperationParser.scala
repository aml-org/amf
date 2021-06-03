package amf.plugins.document.webapi.parser.spec.domain

import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, IdCounter}
import amf.plugins.document.webapi.annotations.OperationTraitEntry
import amf.plugins.document.webapi.contexts.parser.raml.{RamlWebApiContext, RamlWebApiContextType}
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasLikeCreativeWorkParser
import amf.plugins.document.webapi.parser.spec.oas.Oas30CallbackParser
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.metamodel.OperationModel.Method
import amf.plugins.domain.webapi.models.{Operation, Response}
import amf.validations.ParserSideValidations._
import org.yaml.model._

import scala.collection.mutable

case class RamlOperationParser(entry: YMapEntry, parentId: String, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  private def build(): Operation = {
    val method: String = entry.key.as[YScalar].text
    val methodNode     = ScalarNode(entry.key).string()
    val operation      = Operation(Annotations(entry))

    if (parseOptional && method.endsWith("?")) {
      operation.set(OperationModel.Optional, value = true)
      operation.set(OperationModel.Method,
                    AmfScalar(method.stripSuffix("?"), methodNode.annotations),
                    Annotations.inferred())
    } else {
      operation.set(Method, methodNode, Annotations.inferred())
    }

    operation.adopted(parentId)
  }

  def parse(): Operation = {
    val operation = build()
    entry.value.tagType match {
      // Regular operation
      case YType.Map => parseMap(entry.value.as[YMap], operation)
      // Empty operation
      case _ if entry.value.toOption[YScalar].map(_.text).exists(s => s == "" || s == "null") => operation
      case _ =>
        ctx.eh.violation(InvalidOperationType,
                         operation.id,
                         s"Invalid node ${entry.value} for method ${operation.method.value()}",
                         entry.value)
        operation
    }
  }

  protected def parseMap(map: YMap, operation: Operation): Operation = {
    val map     = entry.value.as[YMap]
    val isTrait = ctx.contextType == RamlWebApiContextType.TRAIT

    ctx.closedShape(operation.id, map, if (isTrait) "trait" else "operation")

    map.key("displayName", OperationModel.Name in operation)
    map.key("oasDeprecated".asRamlAnnotation, OperationModel.Deprecated in operation)
    map.key("summary".asRamlAnnotation, OperationModel.Summary in operation)
    map.key("externalDocs".asRamlAnnotation,
            OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id)))
    map.key("protocols", (OperationModel.Schemes in operation).allowingSingleValue)
    map.key("consumes".asRamlAnnotation, OperationModel.Accepts in operation)
    map.key("produces".asRamlAnnotation, OperationModel.ContentType in operation)
    map.key(
      "tags".asRamlAnnotation,
      entry => {
        val tags = StringTagsParser(entry.value.as[YSequence], operation.id).parse()
        operation.withTags(tags)
      }
    )
    val DeclarationParser = ParametrizedDeclarationParser.parse(operation.withTrait) _
    map.key(
      "is",
      (e: YMapEntry) => {
        operation.annotations += OperationTraitEntry(Range(e.range))
        ((DomainElementModel.Extends in operation using DeclarationParser).allowingSingleValue.optional)(e)
      }
    )

    ctx.factory
      .requestParser(map, () => operation.withInferredRequest(), parseOptional)
      .parse()
      .foreach(req =>
        operation.set(OperationModel.Request, AmfArray(List(req), Annotations.virtual()), Annotations.inferred()))

    map.key(
      "defaultResponse".asRamlAnnotation,
      entry => {
        if (entry.value.tagType == YType.Map) {
          val responses = mutable.ListBuffer[Response]()
          entry.value.as[YMap].entries.foreach { entry =>
            responses += ctx.factory
              .responseParser(entry, (r: Response) => r.adopted(operation.id), parseOptional)
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
                .responseParser(entry, _.adopted(operation.id), parseOptional)
                .parse()
            }
        }

        val defaultResponses = operation.responses
        operation.set(OperationModel.Responses,
                      AmfArray(responses ++ defaultResponses, Annotations(entry.value)),
                      Annotations(entry))
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
            Oas30CallbackParser(callbackEntry.value.as[YMap],
                                _.withName(name).adopted(operation.id),
                                name,
                                callbackEntry)(toOas(ctx))
              .parse()
          }
        operation.withCallbacks(callbacks)
      }
    )

    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(operation.id, idCounter) _
    map.key("securedBy", (OperationModel.Security in operation using RequirementParser).allowingSingleValue)

    map.key("description", (OperationModel.Description in operation).allowingAnnotations)

    AnnotationParser(operation,
                     map,
                     if (isTrait) List(VocabularyMappings.`trait`)
                     else List(VocabularyMappings.operation)).parse()

    operation
  }
}
