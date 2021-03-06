package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.EncodingModel
import amf.plugins.domain.webapi.metamodel.ResponseModel.Headers
import amf.plugins.domain.webapi.models.{Encoding, Parameter}
import org.yaml.model.{YMap, YMapEntry}

case class OasEncodingParser(map: YMap, producer: String => Encoding)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def newEncoding(entry: YMapEntry): Encoding = {
    val keyNode  = ScalarNode(entry.key)
    val encoding = producer(keyNode.text().toString).add(Annotations(entry))
    encoding.set(EncodingModel.PropertyName, keyNode.string(), Annotations(entry.key))
  }

  def parse(): Seq[Encoding] = {
    map.entries
      .map { entry =>
        val m        = entry.value.as[YMap]
        val encoding = newEncoding(entry)
        m.key("contentType", EncodingModel.ContentType in encoding)

        m.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              OasHeaderParametersParser(entry.value.as[YMap], { header =>
                header.adopted(encoding.id)
                encoding.add(Headers, header)
              }).parse()
            encoding.fields.set(encoding.id,
                                EncodingModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
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
