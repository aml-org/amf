package amf.spec.domain

import amf.domain.Annotation.ParentEndPoint
import amf.domain.{Annotations, EndPoint, Operation, Parameter}
import amf.metadata.domain.EndPointModel
import amf.metadata.domain.EndPointModel.Path
import amf.model.{AmfArray, AmfScalar}
import amf.spec.Declarations
import amf.spec.common.{AnnotationParser, ValueNode}
import org.yaml.model.{YMapEntry, YNode}
import amf.parser.{YMapOps, YValueOps}
import amf.spec.raml.RamlSyntax
import amf.validation.Validation

import scala.collection.mutable

/**
  *
  */
case class RamlEndpointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              parent: Option[EndPoint],
                              collector: mutable.ListBuffer[EndPoint],
                              declarations: Declarations,
                              currentValidation: Validation,
                              parseOptionalOperations: Boolean = false)
    extends RamlSyntax {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.value.toScalar.text

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    val map = entry.value.value.toMap

    validateClosedShape(currentValidation, endpoint.id, map, "endPoint")

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "uriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.value.toMap, endpoint.withParameter, declarations, currentValidation)
            .parse()
            .map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "type",
      entry =>
        ParametrizedDeclarationParser(entry.value.value, endpoint.withResourceType, declarations.resourceTypes)
          .parse()
    )

    map.key(
      "is",
      entry => {
        entry.value.value.toSequence.values.map(value =>
          ParametrizedDeclarationParser(value, endpoint.withTrait, declarations.traits).parse())
      }
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {
          operations += RamlOperationParser(entry,
                                            endpoint.withOperation,
                                            declarations,
                                            currentValidation,
                                            parseOptionalOperations).parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    map.key(
      "securedBy",
      entry => {
        // TODO check for empty array for resolution ?
        val securedBy = entry.value.value.toSequence.nodes
          .collect({ case n: YNode => n })
          .map(s => RamlParametrizedSecuritySchemeParser(s, endpoint.withSecurity, declarations).parse())

        endpoint.set(EndPointModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    collector += endpoint

    AnnotationParser(() => endpoint, map).parse()

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(
          RamlEndpointParser(_, producer, Some(endpoint), collector, declarations, currentValidation).parse())
      }
    )
  }
}
