package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.EncodingModel
import amf.plugins.domain.webapi.metamodel.ResponseModel.Headers
import amf.plugins.domain.webapi.models.{Encoding, Parameter}
import org.yaml.model.{YMap, YScalar}

case class OasEncodingParser(map: YMap, producer: String => Encoding)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  def parse(): Seq[Encoding] = {
    map.entries
      .map { entry =>
        val key: String = entry.key.as[YScalar].text
        val m           = entry.value.as[YMap]
        val encoding    = producer(key)
        m.key("contentType", EncodingModel.ContentType in encoding)

        m.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              OasHeaderParametersParser(entry.value.as[YMap], { header =>
                header.adopted(encoding.id)
                encoding.add(Headers, header)
              }).parse()
            encoding.setArray(EncodingModel.Headers, parameters, Annotations(entry))
          }
        )

        m.key("style", EncodingModel.Style in encoding)
        m.key("explode", EncodingModel.Explode in encoding)
        m.key("allowReserved", EncodingModel.AllowReserved in encoding)

        AnnotationParser(encoding, m).parse()

        ctx.closedShape(encoding.id, m, "encoding")

        encoding
      }
  }

}
