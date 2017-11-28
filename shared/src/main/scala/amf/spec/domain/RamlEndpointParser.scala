package amf.spec.domain

import amf.common.core.TemplateUri
import amf.domain.Annotation.ParentEndPoint
import amf.domain.{Annotations, EndPoint, Operation, Parameter}
import amf.metadata.domain.EndPointModel
import amf.metadata.domain.EndPointModel.Path
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YScalarYRead}
import amf.spec.ParserContext
import amf.spec.common.{AnnotationParser, ValueNode}
import org.yaml.model.{YMap, YMapEntry, YNode}

import scala.collection.mutable

/**
  *
  */
case class RamlEndpointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              parent: Option[EndPoint],
                              collector: mutable.ListBuffer[EndPoint],
                              parseOptionalOperations: Boolean = false)(implicit ctx: ParserContext) {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.as[String]

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    if (!TemplateUri.isValid(path))
      ctx.violation(endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(e => e.path == path)) ctx.violation(endpoint.id, "Duplicated resource path " + path, entry)
    else parseEndpoint(endpoint)
  }

  private def parseEndpoint(endpoint: EndPoint) =
    entry.value.to[YMap] match {
      case Left(_) => collector += endpoint
      case Right(map) =>
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
          "uriParameters",
          entry => {
            val parameters: Seq[Parameter] =
              RamlParametersParser(entry.value.as[YMap], endpoint.withParameter)
                .parse()
                .map(_.withBinding("path"))
            endpoint.set(EndPointModel.UriParameters,
                         AmfArray(parameters, Annotations(entry.value)),
                         Annotations(entry))
          }
        )

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
              operations += RamlOperationParser(entry, endpoint.withOperation, parseOptionalOperations)
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

        collector += endpoint

        AnnotationParser(() => endpoint, map).parse()

        map.regex(
          "^/.*",
          entries => {
            entries.foreach(RamlEndpointParser(_, producer, Some(endpoint), collector).parse())
          }
        )
    }
}
