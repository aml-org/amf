package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.TemplateUri
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import org.yaml.model.{YMap, YMapEntry, YNode, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters"
}

case class Raml08EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters|baseUriParameters"
}

abstract class RamlEndpointParser(entry: YMapEntry,
                                  producer: String => EndPoint,
                                  parent: Option[EndPoint],
                                  collector: mutable.ListBuffer[EndPoint],
                                  parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext) {

  def parse(): Unit = {

    val path = parsePath()

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    if (!TemplateUri.isValid(path))
      ctx.violation(endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(e => e.path == path)) ctx.violation(endpoint.id, "Duplicated resource path " + path, entry)
    else {
      entry.value.tagType match {
        case YType.Null => collector += endpoint
        case _ =>
          val map = entry.value.as[YMap]
          parseEndpoint(endpoint, map)
      }
    }
  }

  protected def parseEndpoint(endpoint: EndPoint, map: YMap): Unit = {
    ctx.closedShape(endpoint.id, map, "endPoint")

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "type",
      entry =>
        ParametrizedDeclarationParser(entry.value,
                                      endpoint.withResourceType,
                                      ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
    )

    map.key(
      "is",
      entry => {
        entry.value
          .as[Seq[YNode]]
          .map(value =>
            ParametrizedDeclarationParser(value, endpoint.withTrait, ctx.declarations.findTraitOrError(value))
              .parse())
      }
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {

          operations += ctx.factory
            .operationParser(entry, endpoint.withOperation, parseOptionalOperations)
            .parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    map.key(
      "securedBy",
      entry => {
        // TODO check for empty array for resolution ?
        val securedBy = entry.value
          .as[Seq[YNode]]
          .map(s => RamlParametrizedSecuritySchemeParser(s, endpoint.withSecurity).parse())

        endpoint.set(EndPointModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    // TODO refactor this changes for baseUriParameters/UriParameters
    val entries = map.regex(uriParametersKey)
    entries match {
      case Nil =>
        val implicitParameters = TemplateUri
          .variables(parsePath())
          .map { variable =>
            val implicitParam = endpoint.withParameter(variable).withBinding("path").withRequired(true)
            implicitParam.withScalarSchema(variable).withDataType((Namespace.Xsd + "string").iri())
            implicitParam.annotations += SynthesizedField()
            implicitParam
          }
        if (implicitParameters.nonEmpty)
          endpoint.set(EndPointModel.UriParameters,
                       AmfArray(implicitParameters, Annotations(entry.value)),
                       Annotations(entry))
      case _ =>
        val parameters = entries.flatMap { entry =>
          val explicitParameters: Seq[Parameter] =
            RamlParametersParser(entry.value.as[YMap], endpoint.withParameter)
              .parse()
              .map(_.withBinding("path"))
          val implicitParameters = TemplateUri
            .variables(parsePath())
            .filter(variable => !explicitParameters.exists(_.name == variable))
            .map { variable =>
              val implicitParam = endpoint.withParameter(variable).withBinding("path").withRequired(true)
              implicitParam.withScalarSchema(variable).withDataType((Namespace.Xsd + "string").iri())
              implicitParam.annotations += SynthesizedField()
              implicitParam
            }
          explicitParameters ++ implicitParameters
        }.toSeq

        endpoint.set(EndPointModel.UriParameters,
                     AmfArray(parameters, Annotations(entries.head.value)),
                     Annotations(entries.head))
    }

    collector += endpoint

    AnnotationParser(() => endpoint, map).parse()

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(ctx.factory.endPointParser(_, producer, Some(endpoint), collector, false).parse())
      }
    )
  }

  protected def parsePath(): String = parent.map(_.path).getOrElse("") + entry.key.as[String]

  protected def uriParametersKey: String
}
