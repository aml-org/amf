package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.remote.Mimes.{`application/xml`, `application/yaml`}
import amf.core.internal.utils.MediaTypeMatcher
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.spec.payload.PayloadRenderPlugin

object ExampleValueRenderer {

  def renderExample(example: Example, mediaType: String): String = {
    val maybeRawValue = example.raw.option()
    maybeRawValue
      .flatMap { raw =>
        val guessedMediaType = raw.guessMediaType(false)
        guessedMediaType match {
          case _ if mediaType == guessedMediaType => Some(raw)
          case _                                  => None
        }
      }
      .getOrElse(defaultDump(example, mediaType))
  }

  private def defaultDump(example: Example, mediaType: String): String = {
    val config = AMFGraphConfiguration.predefined().withPlugin(PayloadRenderPlugin)
    mediaType match {
      case `application/xml` =>
        "" // for backwards compatibility, we could have an xml payload renderer but we don't yet
      case _ => dump(example.structuredValue, mediaType, config)
    }
  }

  private def dump(dataNode: DataNode, mediaType: String, config: AMFGraphConfiguration): String = {
    config.baseUnitClient().render(PayloadFragment(dataNode, mediaType), mediaType)
  }
}
