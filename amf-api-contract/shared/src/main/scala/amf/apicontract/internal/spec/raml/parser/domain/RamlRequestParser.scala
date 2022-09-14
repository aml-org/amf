package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Payload, Request}
import amf.apicontract.internal.annotations.EmptyPayload
import amf.apicontract.internal.metamodel.domain.{PayloadModel, RequestModel}
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, DomainElement}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.{AmfStrings, Lazy}
import amf.core.internal.validation.CoreParserValidations.UnsupportedExampleMediaTypeErrorSpecification
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.raml.parser.{AnyDefaultType, DefaultType, Raml10TypeParser}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.ExclusivePropertiesSpecification
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

import scala.collection.mutable

/** */
case class Raml10RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(implicit
    ctx: RamlWebApiContext
) extends RamlRequestParser(map, producer, parseOptional) {

  override def parse(request: Lazy[Request], target: Target): Unit = {
    map.key(
      "queryString",
      queryEntry => {
        Raml10TypeParser(queryEntry, shape => Unit)
          .parse()
          .map(q => {
            val finalRequest = request.getOrCreate
            if (map.key("queryParameters").isDefined) {
              ctx.eh.violation(
                ExclusivePropertiesSpecification,
                finalRequest,
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together",
                map.location
              )
            }
            finalRequest.setWithoutId(RequestModel.QueryString, tracking(q, finalRequest), Annotations(queryEntry))
          })
      }
    )
  }

  override protected def parseParameter(
      entry: YMapEntry,
      adopt: Parameter => Unit,
      parseOptional: Boolean,
      binding: String
  ): Parameter =
    Raml10ParameterParser(entry, (p: Parameter) => Unit, parseOptional, binding)
      .parse()

  override protected val baseUriParametersKey: String = "baseUriParameters".asRamlAnnotation

  override protected val defaultType: DefaultType = AnyDefaultType
}

case class Raml08RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(implicit
    ctx: RamlWebApiContext
) extends RamlRequestParser(map, producer, parseOptional) {

  override protected val baseUriParametersKey: String = "baseUriParameters"

  override def parse(request: Lazy[Request], target: Target): Unit = Unit

  override protected def parseParameter(
      entry: YMapEntry,
      adopt: Parameter => Unit,
      parseOptional: Boolean,
      binding: String
  ): Parameter =
    Raml08ParameterParser(entry, (p: Parameter) => Unit, parseOptional, binding)
      .parse()

  override protected val defaultType: DefaultType = AnyDefaultType
}

abstract class RamlRequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(implicit
    ctx: RamlWebApiContext
) extends SpecParserOps {
  protected val request = new Lazy[Request](producer)

  protected val baseUriParametersKey: String

  def parse(request: Lazy[Request], target: Target): Unit
  protected val defaultType: DefaultType

  protected def parseParameter(
      entry: YMapEntry,
      adopt: Parameter => Unit,
      parseOptional: Boolean,
      binding: String
  ): Parameter

  def parse(): Option[Request] = {

    val target = new Target {
      override def foreach(fn: AmfObject => Unit): Unit = fn(request.getOrCreate)
    }

    map.key(
      "queryParameters",
      (RequestModel.QueryParameters in target using RamlQueryParameterParser
        .parse((p: Parameter) => Unit, parseOptional)).treatMapAsArray.optional
    )
    map.key(
      "headers",
      (RequestModel.Headers in target using RamlHeaderParser
        .parse((p: Parameter) => Unit, parseOptional)).treatMapAsArray.optional
    )

    // baseUriParameters from raml08. Only complex parameters will be written here, simple ones will be in the parameters with binding path.
    map.key(
      baseUriParametersKey,
      entry => {
        val parameters = entry.value.as[YMap].entries.map { paramEntry =>
          parseParameter(paramEntry, (p: Parameter) => Unit, parseOptional, "path")
        }

        request.getOrCreate
          .setWithoutId(RequestModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))

      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        entry.value.tagType match {
          case YType.Null =>
            ctx.factory
              .typeParser(entry, shape => shape.withName("default"), false, defaultType)
              .parse()
              .foreach { schema =>
                val payload = request.getOrCreate.withPayload(None)
                payload.annotations += EmptyPayload()
                ctx.autoGeneratedAnnotation(schema)
                payloads += payload
                  .add(Annotations(entry))
                  .setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations(entry.value))
              }

          case YType.Str =>
            ctx.factory
              .typeParser(entry, shape => shape.withName("default"), false, defaultType)
              .parse()
              .foreach { schema =>
                val payload = request.getOrCreate.withPayload(None)
                ctx.autoGeneratedAnnotation(schema)
                payloads += payload
                  .add(Annotations(entry))
                  .setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations(entry.value))
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
                      payloads += ctx.factory.payloadParser(entry, request.getOrCreate.id, false).parse()
                    })
                  }
                )
                val entries = m.entries.filter(e => !e.key.as[YScalar].text.matches(mediaTypeRegexPattern))
                val others  = YMap(entries, m.sourceName)
                if (others.entries.nonEmpty) {
                  if (payloads.isEmpty) {
                    ctx.factory
                      .typeParser(entry, shape => shape.withName("default"), false, defaultType)
                      .parse()
                      .foreach { schema =>
                        val payload = request.getOrCreate.withPayload(None)
                        ctx.autoGeneratedAnnotation(schema)
                        payloads += payload
                          .add(Annotations(entry))
                          .setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations(entry.value))
                      }
                  } else {
                    others.entries.foreach(e =>
                      ctx.eh.violation(
                        UnsupportedExampleMediaTypeErrorSpecification,
                        request.getOrCreate,
                        s"Unexpected key '${e.key.as[YScalar].text}'. Expecting valid media types.",
                        e.location
                      )
                    )
                  }
                }
              case _ =>
            }
        }

        if (payloads.nonEmpty)
          request.getOrCreate
            .setWithoutId(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )
    parse(request, target)

    request.option.foreach(_.annotations ++= Annotations.virtual())

    request.option
  }
}
