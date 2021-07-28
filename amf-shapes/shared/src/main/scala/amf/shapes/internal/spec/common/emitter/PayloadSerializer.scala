package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.remote.{Mimes, SpecId}
import amf.core.internal.render.{AMFSerializer, SpecOrdering}
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.MediaTypeMatcher
import amf.shapes.client.scala.model.domain.Example
import amf.core.internal.remote.Mimes._
import org.yaml.model.YDocument
trait PayloadSerializer extends PlatformSecrets {

  protected def toJson(example: Example, config: AMFGraphConfiguration): String = {

    example.raw.option().map(_.guessMediaType(false)) match {
      case Some(`application/json`) => example.raw.value()
      case Some(`application/xml`)  => ""
      case _                        => dump(example.structuredValue, config)
    }
  }

  protected def toYaml(example: Example, config: AMFGraphConfiguration): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some(`application/json`) => dump(example.structuredValue, config)
      case Some(`application/xml`)  => ""
      case Some(_)                  => example.raw.value()
      case _                        => dump(example.structuredValue, config)
    }
  }

  protected def toXml(example: Example): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some(`application/xml`) => example.raw.value()
      case _                       => ""
    }
  }

  private def dump(dataNode: DataNode, config: AMFGraphConfiguration): String = {
    new AMFSerializer(PayloadFragment(dataNode, `application/json`), `application/json`, config.renderConfiguration)
      .render()
  }
}

case class PayloadEmitter(dataNode: DataNode, ordering: SpecOrdering = SpecOrdering.Lexical)(
    implicit eh: AMFErrorHandler) {
  def emitDocument(): YDocument = {
    val f: YDocument.PartBuilder => Unit = DataNodeEmitter(dataNode, ordering)(eh).emit
    YDocument(f)
  }
}
