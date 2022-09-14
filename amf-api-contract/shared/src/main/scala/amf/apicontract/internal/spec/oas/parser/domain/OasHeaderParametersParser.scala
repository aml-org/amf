package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Payload}
import amf.apicontract.internal.metamodel.domain.{ParameterModel, PayloadModel, ResponseModel}
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorParameter
import amf.apicontract.internal.spec.common.parser.{Oas3ParameterParser, SpecParserOps}
import amf.apicontract.internal.spec.oas.parser.context.{Oas3Syntax, OasWebApiContext}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, Shape}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition}
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasExamplesParser, YMapEntryLike}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class OasHeaderParametersParser(map: YMap, adopt: Parameter => Unit)(implicit ctx: OasWebApiContext) {
  def parse(): Seq[Parameter] = {
    map.entries
      .map(entry =>
        OasHeaderParameterParser(
          entry.value.as[YMap],
          { header =>
            header.add(Annotations(entry))
            header.setWithoutId(ParameterModel.Name, ScalarNode(entry.key).string(), Annotations.inferred())
            adopt(header)
          }
        ).parse()
      )
  }
}

case class OasHeaderParameterParser(map: YMap, adopt: Parameter => Unit)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Parameter = {

    def commonHeader: Parameter = {
      val parameter = Parameter()
      adopt(parameter)
      map.key("description", ParameterModel.Description in parameter)
      AnnotationParser(parameter, map).parse()
      parameter
    }

    val header: Parameter = if (ctx.spec == Spec.OAS30) {
      ctx.link(map) match {
        case Left(fullRef) =>
          val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "headers")
          ctx.declarations
            .findHeader(label, SearchScope.Named)
            .map(header => {
              val ref: Option[YScalar]  = map.key("$ref").flatMap(v => v.value.asOption[YScalar])
              val annotations           = ref.map(Annotations(_)).getOrElse(Annotations.synthesized())
              val linkHeader: Parameter = header.link(AmfScalar(label), annotations, Annotations.synthesized())
              adopt(linkHeader)
              linkHeader
            })
            .getOrElse {
              ctx.navigateToRemoteYNode(fullRef) match {
                case Some(result) =>
                  val requestNode = result.remoteNode
                  OasHeaderParameterParser(requestNode.as[YMap], adopt)(result.context)
                    .parse()
                    .add(ExternalReferenceUrl(fullRef))
                case None =>
                  ctx.eh.violation(
                    CoreValidations.UnresolvedReference,
                    "",
                    s"Cannot find header reference $fullRef",
                    map.location
                  )
                  val error = ErrorParameter(label, map)
                  adopt(error)
                  error
              }
            }
        case Right(_) =>
          val header = commonHeader
          parseOas3Header(header, map)
          header
      }
    } else {
      val header = commonHeader
      parseOas2Header(header, map)
      header
    }
    header.setWithoutId(
      ParameterModel.Binding,
      AmfScalar("header"),
      Annotations.synthesized()
    ) // we need to add the binding in order to conform all parameters validations
    header
  }

  protected def parseOas2Header(parameter: Parameter, map: YMap): Unit = {
    val name = Option(parameter.name).map(_.value())
    parameter.setWithoutId(ParameterModel.Required, AmfScalar(!name.exists(_.endsWith("?"))), Annotations.synthesized())

    map.key("x-amf-required", (ParameterModel.Required in parameter).explicit)

    val adoption: Shape => Unit = shape => Unit

    map.key(
      "type",
      _ => {
        OasTypeParser(YMapEntryLike(map), "schema", adoption, OAS20SchemaVersion(SchemaPosition.Schema))
          .parse()
          .map(s => parameter.setWithoutId(ParameterModel.Schema, tracking(s, parameter), Annotations(map)))
      }
    )
  }

  protected def parseOas3Header(parameter: Parameter, map: YMap): Unit = {
    map.key("required", (ParameterModel.Required in parameter).explicit)
    map.key("deprecated", (ParameterModel.Deprecated in parameter).explicit)
    map.key("allowEmptyValue", (ParameterModel.AllowEmptyValue in parameter).explicit)
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, (shape) => shape.withName("schema"))
          .parse()
          .map(s => parameter.setWithoutId(ParameterModel.Schema, tracking(s, parameter), Annotations(entry)))
      }
    )
    map.key(
      "content",
      entry => {
        val payloadProducer: Option[String] => Payload = mediaType => {
          val res = Payload()
          mediaType.map(res.withMediaType)
          res
        }
        val payloads = OasContentsParser(entry, payloadProducer).parse()
        if (payloads.nonEmpty) parameter.setWithoutId(ResponseModel.Payloads, AmfArray(payloads), Annotations(entry))
      }
    )
    Oas3ParameterParser.validateSchemaOrContent(map, parameter)

    def setShape(examples: Seq[Example], maybeEntry: Option[YMapEntry]): Unit =
      if (examples.nonEmpty)
        maybeEntry
          .map(entry => parameter.setWithoutId(PayloadModel.Examples, AmfArray(examples), Annotations(entry)))
          .getOrElse(parameter.setWithoutId(PayloadModel.Examples, AmfArray(examples)))

    OasExamplesParser(map, parameter).parse()

    parameter.syntheticBinding("header")
    Oas3ParameterParser.parseStyleField(map, parameter)
    Oas3ParameterParser.parseExplodeField(map, parameter)

    ctx.closedShape(parameter, map, "header")
  }
}
