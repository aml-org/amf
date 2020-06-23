package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, DomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, Lazy}
import amf.plugins.document.webapi.annotations.EmptyPayload
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, DefaultType, Raml10TypeParser}
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.RequestModel
import amf.plugins.domain.webapi.models.{Parameter, Payload, Request}
import amf.validations.ParserSideValidations.{
  ExclusivePropertiesSpecification,
  UnsupportedExampleMediaTypeErrorSpecification
}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {

  override def parse(request: Lazy[Request], target: Target): Unit = {
    map.key(
      "queryString",
      queryEntry => {
        Raml10TypeParser(queryEntry, shape => shape.adopted(request.getOrCreate.id))
          .parse()
          .map(q => {
            val finalRequest = request.getOrCreate
            if (map.key("queryParameters").isDefined) {
              ctx.eh.violation(
                ExclusivePropertiesSpecification,
                finalRequest.id,
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together",
                map
              )
            }
            finalRequest.withQueryString(tracking(q, finalRequest.id))
          })
      }
    )
  }

  override protected def parseParameter(entry: YMapEntry,
                                        adopt: Parameter => Unit,
                                        parseOptional: Boolean): Parameter =
    Raml10ParameterParser(entry, (p: Parameter) => p.adopted(request.getOrCreate.id), parseOptional)
      .parse()

  override protected val baseUriParametersKey: String = "baseUriParameters".asRamlAnnotation

  override protected val defaultType: DefaultType = AnyDefaultType
}

case class Raml08RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {

  override protected val baseUriParametersKey: String = "baseUriParameters"

  override def parse(request: Lazy[Request], target: Target): Unit = Unit

  override protected def parseParameter(entry: YMapEntry,
                                        adopt: Parameter => Unit,
                                        parseOptional: Boolean): Parameter =
    Raml08ParameterParser(entry, (p: Parameter) => p.adopted(request.getOrCreate.id), parseOptional)
      .parse()

  override protected val defaultType: DefaultType = AnyDefaultType
}

abstract class RamlRequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {
  protected val request = new Lazy[Request](producer)

  protected val baseUriParametersKey: String

  def parse(request: Lazy[Request], target: Target): Unit
  protected val defaultType: DefaultType

  protected def parseParameter(entry: YMapEntry, adopt: Parameter => Unit, parseOptional: Boolean): Parameter

  def parse(): Option[Request] = {

    val target = new Target {
      override def foreach(fn: DomainElement => Unit): Unit = fn(request.getOrCreate)
    }

    map.key(
      "queryParameters",
      (RequestModel.QueryParameters in target using RamlQueryParameterParser
        .parse((p: Parameter) => p.adopted(request.getOrCreate.id), parseOptional)).treatMapAsArray.optional
    )
    map.key(
      "headers",
      (RequestModel.Headers in target using RamlHeaderParser
        .parse((p: Parameter) => p.adopted(request.getOrCreate.id), parseOptional)).treatMapAsArray.optional
    )

    // baseUriParameters from raml08. Only complex parameters will be written here, simple ones will be in the parameters with binding path.
    map.key(
      baseUriParametersKey,
      entry => {
        val parameters = entry.value.as[YMap].entries.map { paramEntry =>
          parseParameter(paramEntry, (p: Parameter) => p.adopted(request.getOrCreate.id), parseOptional)
            .withBinding("path")
        }

        request.getOrCreate.set(RequestModel.UriParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))

      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        entry.value.tagType match {
          case YType.Null =>
            ctx.factory
              .typeParser(entry,
                          shape => shape.withName("default").adopted(request.getOrCreate.id),
                          false,
                          defaultType)
              .parse()
              .foreach { schema =>
                val payload = request.getOrCreate.withPayload(None)
                payload.annotations += EmptyPayload()
                ctx.autoGeneratedAnnotation(schema)
                payloads += payload
                  .add(Annotations(entry))
                  .withSchema(tracking(schema, payload.id))
              }

          case YType.Str =>
            ctx.factory
              .typeParser(entry,
                          shape => shape.withName("default").adopted(request.getOrCreate.id),
                          false,
                          defaultType)
              .parse()
              .foreach { schema =>
                val payload = request.getOrCreate.withPayload(None)
                ctx.autoGeneratedAnnotation(schema)
                payloads += payload
                  .add(Annotations(entry))
                  .withSchema(tracking(schema, payload.id))
              }

          case _ =>
            // Now we parsed potentially nested shapes for different data types
            entry.value.to[YMap] match {
              case Right(m) =>
                val mediaTypeRegexPattern = ".*/.*"
                m.regex(
                  mediaTypeRegexPattern,
                  entries => {
                    entries.foreach(entry => {
                      payloads += ctx.factory.payloadParser(entry, request.getOrCreate.withPayload, false).parse()
                    })
                  }
                )
                val entries = m.entries.filter(e => !e.key.as[YScalar].text.matches(mediaTypeRegexPattern))
                val others  = YMap(entries, m.sourceName)
                if (others.entries.nonEmpty) {
                  if (payloads.isEmpty) {
                    ctx.factory
                      .typeParser(entry,
                                  shape => shape.withName("default").adopted(request.getOrCreate.id),
                                  false,
                                  defaultType)
                      .parse()
                      .foreach { schema =>
                        val payload = request.getOrCreate.withPayload(None)
                        ctx.autoGeneratedAnnotation(schema)
                        payloads += payload
                          .add(Annotations(entry))
                          .withSchema(tracking(schema, payload.id))
                      }
                  } else {
                    others.entries.foreach(
                      e =>
                        ctx.eh.violation(UnsupportedExampleMediaTypeErrorSpecification,
                                         request.getOrCreate.id,
                                         s"Unexpected key '${e.key.as[YScalar].text}'. Expecting valid media types.",
                                         e))
                  }
                }
              case _ =>
            }
        }

        if (payloads.nonEmpty)
          request.getOrCreate.set(RequestModel.Payloads,
                                  AmfArray(payloads, Annotations(entry.value)),
                                  Annotations(entry))
      }
    )
    parse(request, target)

    request.option.foreach(_.annotations ++= Annotations(YNode(map)))

    request.option
  }
}
