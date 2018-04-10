package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, DefaultType}
import amf.plugins.domain.webapi.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Payload, Response}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10ResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, producer, parseOptional) {

  override def parseMap(response: Response, map: YMap): Unit = {
    AnnotationParser(response, map).parse()

  }

  override protected val defaultType: DefaultType = AnyDefaultType
}

case class Raml08ResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, producer, parseOptional) {
  override protected def parseMap(response: Response, map: YMap): Unit = Unit

  override protected val defaultType: DefaultType = AnyDefaultType
}

abstract class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  protected def parseMap(response: Response, map: YMap)

  protected val defaultType: DefaultType

  def parse(): Response = {
    val node = ScalarNode(entry.key).text()

    val response: Response = entry.value.tagType match {
      case YType.Null =>
        producer(node.toString).add(Annotations(entry)).set(ResponseModel.StatusCode, node)
      case YType.Str | YType.Int =>
        val ref           = entry.value.as[YScalar].text
        val res: Response = ctx.declarations.findResponseOrError(entry.value)(ref, SearchScope.All).link(ref)
        res.withName(node.toString)
      case _ =>
        val map = entry.value.as[YMap] // if not scalar, must be the response, if not, violation.

        val res = producer(node.toString).add(Annotations(entry)).set(ResponseModel.StatusCode, node)

        if (parseOptional && node.toString.endsWith("?")) { // only in raml the method can be optional, check?
          res.set(ResponseModel.Optional, value = true)
          val name = node.toString.stripSuffix("?")
          res.set(ResponseModel.Name, name)
          res.set(ResponseModel.StatusCode, name)
        }

        map.key("description", (ResponseModel.Description in res).allowingAnnotations)

        map.key("headers",
                (ResponseModel.Headers in res using RamlHeaderParser
                  .parse(res.withHeader, parseOptional)).treatMapAsArray.optional)

        map.key(
          "body",
          entry => {
            val payloads = mutable.ListBuffer[Payload]()

            val payload = Payload()
            payload.adopted(res.id)

            entry.value.tagType match {
              case YType.Null =>
                ctx.factory
                  .typeParser(entry, shape => shape.withName("default").adopted(payload.id), false, defaultType)
                  .parse()
                  .foreach { schema =>
                    schema.annotations += SynthesizedField()
                    payloads += payload.withSchema(schema)
                  }
                res.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

              case YType.Str =>
                ctx.factory
                  .typeParser(entry, shape => shape.withName("default").adopted(payload.id), false, defaultType)
                  .parse()
                  .foreach(payloads += payload.withSchema(_))
                res.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

              case _ =>
                // Now we parsed potentially nested shapes for different data types
                entry.value.to[YMap] match {
                  case Right(m) =>
                    m.regex(
                      ".*/.*",
                      entries => {
                        entries.foreach(entry => {
                          payloads += ctx.factory.payloadParser(entry, res.withPayload, false).parse()
                        })
                      }
                    )
                    val others = YMap(m.entries.filter(e => !e.key.as[YScalar].text.matches(".*/.*")))
                    if (others.entries.nonEmpty) {
                      if (payloads.isEmpty) {
                        if (others.entries.map(_.key.as[YScalar].text) == List("example") && !ctx.globalMediatype) {
                          ctx.violation(ParserSideValidations.ParsingErrorSpecification.id(),
                                        res.id,
                                        "Invalid media type",
                                        m)
                        }
                        ctx.factory
                          .typeParser(entry, shape => shape.withName("default").adopted(res.id), false, defaultType)
                          .parse()
                          .foreach(payloads += res.withPayload(None).withSchema(_)) // todo
                      } else {
                        others.entries.foreach(
                          e =>
                            ctx.violation(s"Unexpected key '${e.key.as[YScalar].text}'. Expecting valid media types.",
                                          e))
                      }
                    }
                  case _ =>
                }
                if (payloads.nonEmpty)
                  res.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
            }
          }
        )

        val examples = OasResponseExamplesParser("examples".asRamlAnnotation, map).parse()
        if (examples.nonEmpty) res.set(ResponseModel.Examples, AmfArray(examples))

        ctx.closedShape(res.id, map, "response")

        parseMap(res, map)
        res
    }

    response
  }
}
