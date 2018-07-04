package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, DefaultType}
import amf.plugins.domain.webapi.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload, Response}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10ResponseParser(entry: YMapEntry, adopt: Response => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, adopt, parseOptional) {

  override def parseMap(response: Response, map: YMap): Unit = {
    AnnotationParser(response, map).parse()

  }

  override protected val defaultType: DefaultType = AnyDefaultType
}

case class Raml08ResponseParser(entry: YMapEntry, adopt: Response => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, adopt, parseOptional) {
  override protected def parseMap(response: Response, map: YMap): Unit = Unit

  override protected val defaultType: DefaultType = AnyDefaultType
}

abstract class RamlResponseParser(entry: YMapEntry, adopt: Response => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  protected def parseMap(response: Response, map: YMap)

  protected val defaultType: DefaultType

  def parse(): Response = {
    val node: AmfScalar = ScalarNode(entry.key).text()

    val response: Response = entry.value.tagType match {
      case YType.Null =>
        val response = Response(entry).set(ResponseModel.Name, node).set(ResponseModel.StatusCode, node)
        adopt(response)
        response
      case YType.Str | YType.Int =>
        val ref           = entry.value.as[YScalar].text
        val res: Response = ctx.declarations.findResponseOrError(entry.value)(ref, SearchScope.All).link(ref)
        res.set(ResponseModel.Name, node).annotations ++= Annotations(entry)
        res
      case _ =>
        val map = entry.value.as[YMap] // if not scalar, must be the response, if not, violation.
        val res = Response(entry).set(ResponseModel.Name, node)
        adopt(res)
        res.withStatusCode(if (res.name.value() == "default") "200" else res.name.value())

        if (parseOptional && node.toString.endsWith("?")) { // only in raml the method can be optional, check?
          res.set(ResponseModel.Optional, value = true)
          val name = node.toString.stripSuffix("?")
          res.set(ResponseModel.Name, name)
          res.set(ResponseModel.StatusCode, name)
        }

        map.key("description", (ResponseModel.Description in res).allowingAnnotations)

        map.key("headers",
                (ResponseModel.Headers in res using RamlHeaderParser
                  .parse((p: Parameter) => p.adopted(res.id), parseOptional)).treatMapAsArray.optional)

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
                    val entries = m.entries.filter(e => !e.key.as[YScalar].text.matches(".*/.*"))
                    val others  = YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))
                    if (others.entries.nonEmpty) {
                      if (payloads.isEmpty) {
                        if (others.entries.map(_.key.as[YScalar].text) == List("example") && !ctx.globalMediatype) {
                          ctx.violation(ParserSideValidations.ParsingErrorSpecification.id,
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
