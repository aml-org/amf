package amf.plugins.document.apicontract.parser.spec.common

import amf.core.client.platform.model.document.PayloadFragment
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.render.{AMFSerializer, SpecOrdering}
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.MediaTypeMatcher
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument

trait PayloadSerializer extends PlatformSecrets {

  protected def toJson(example: Example, config: AMFGraphConfiguration): String = {

    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/json") => example.raw.value()
      case Some("application/xml")  => ""
      case _                        => dump(example.structuredValue, config)
    }
  }

  protected def toYaml(example: Example, config: AMFGraphConfiguration): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/json") => dump(example.structuredValue, config)
      case Some("application/xml")  => ""
      case Some(_)                  => example.raw.value()
      case _                        => dump(example.structuredValue, config)
    }
  }

  protected def toXml(example: Example): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/xml") => example.raw.value()
      case _                       => ""
    }
  }

  private def dump(dataNode: DataNode, config: AMFGraphConfiguration): String = {
    new AMFSerializer(PayloadFragment(dataNode, "application/json"),
                      "application/payload+json",
                      config.renderConfiguration).render()
  }
}

case class PayloadEmitter(dataNode: DataNode, ordering: SpecOrdering = SpecOrdering.Lexical)(
    implicit eh: AMFErrorHandler) {
  def emitDocument(): YDocument = {
    val f: YDocument.PartBuilder => Unit = DataNodeEmitter(dataNode, ordering)(eh).emit
    YDocument(f)
  }
}
