package amf.plugins.document.apicontract.parser.spec.common

import amf.client.execution.BaseExecutionEnvironment
import amf.client.remod.AMFGraphConfiguration
import amf.core.AMFSerializer
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.DataNode
import amf.core.remote.Payload
import amf.core.unsafe.PlatformSecrets
import amf.core.utils._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument

import scala.concurrent.ExecutionContext

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
