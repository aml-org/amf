package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorParameter
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry}

case class OasHeaderParametersParser(map: YMap, adopt: Parameter => Unit)(implicit ctx: OasWebApiContext) {
  def parse(): Seq[Parameter] = {
    map.entries
      .map(entry =>
        OasHeaderParameterParser(entry.value.as[YMap], { header =>
          header.add(Annotations(entry))
          header.set(ParameterModel.Name, ScalarNode(entry.key).string())
          adopt(header)
        }).parse())
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

    val header: Parameter = if (ctx.syntax == Oas3Syntax) {
      ctx.link(map) match {
        case Left(fullRef) =>
          val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "headers")
          ctx.declarations
            .findHeader(label, SearchScope.Named)
            .map(header => {
              val linkHeader: Parameter = header.link(label)
              adopt(linkHeader)
              linkHeader
            })
            .getOrElse {
              ctx.obtainRemoteYNode(fullRef) match {
                case Some(requestNode) =>
                  OasHeaderParameterParser(requestNode.as[YMap], adopt).parse().add(ExternalReferenceUrl(fullRef))
                case None =>
                  ctx.eh.violation(CoreValidations.UnresolvedReference,
                                   "",
                                   s"Cannot find header reference $fullRef",
                                   map)
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
    header.set(ParameterModel.Binding, AmfScalar("header"), Annotations() += SynthesizedField()) // we need to add the binding in order to conform all parameters validations
    header
  }

  protected def parseOas2Header(parameter: Parameter, map: YMap): Unit = {
    val name = Option(parameter.name).map(_.value())
    parameter.set(ParameterModel.Required, !name.exists(_.endsWith("?")))

    map.key("x-amf-required", (ParameterModel.Required in parameter).explicit)

    map.key(
      "type",
      _ => {
        OasTypeParser(map, name.getOrElse("default"), (shape) => shape.withName("schema").adopted(parameter.id))
          .parse()
          .map(s => parameter.set(ParameterModel.Schema, tracking(s, parameter.id), Annotations(map)))
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
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id))
          .parse()
          .map(s => parameter.set(ParameterModel.Schema, tracking(s, parameter.id), Annotations(entry)))
      }
    )
    map.key(
      "content",
      entry => {
        val payloadProducer: Option[String] => Payload = mediaType => {
          val res = Payload()
          mediaType.map(res.withMediaType)
          res.adopted(parameter.id)
        }
        val payloads = OasContentsParser(entry, payloadProducer).parse()
        if (payloads.nonEmpty) parameter.set(ResponseModel.Payloads, AmfArray(payloads), Annotations(entry))
      }
    )
    Oas3ParameterParser.validateSchemaOrContent(map, parameter)

    def setShape(examples: Seq[Example], maybeEntry: Option[YMapEntry]): Unit =
      if (examples.nonEmpty)
        maybeEntry
          .map(entry => parameter.set(PayloadModel.Examples, AmfArray(examples), Annotations(entry)))
          .getOrElse(parameter.set(PayloadModel.Examples, AmfArray(examples)))

    OasExamplesParser(map, parameter).parse()

    parameter.withBinding("header")
    Oas3ParameterParser.parseStyleField(map, parameter)
    Oas3ParameterParser.parseExplodeField(map, parameter)

    ctx.closedShape(parameter.id, map, "header")
  }
}
