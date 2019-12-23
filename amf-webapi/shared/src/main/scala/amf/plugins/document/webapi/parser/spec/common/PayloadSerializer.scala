package amf.plugins.document.webapi.parser.spec.common

import amf.core.AMFSerializer
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.DataNode
import amf.core.remote.Payload
import amf.core.services.RuntimeSerializer
import amf.core.utils._
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument

trait PayloadSerializer {

  protected def toJson(example: Example): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/json") => example.raw.value()
      case Some("application/xml")  => ""
      case _                        => dump(example.structuredValue)
    }
  }

  protected def toYaml(example: Example): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/json") => dump(example.structuredValue)
      case Some("application/xml")  => ""
      case Some(_)                  => example.raw.value()
      case _                        => dump(example.structuredValue)
    }
  }

  protected def toXml(example: Example): String = {
    example.raw.option().map(_.guessMediaType(false)) match {
      case Some("application/xml") => example.raw.value()
      case _                       => ""
    }
  }

  private def dump(dataNode: DataNode): String = {
    AMFSerializer.init()
    RuntimeSerializer(PayloadFragment(dataNode, "application/json"), "application/payload+json", Payload.name)
  }
}

case class PayloadEmitter(dataNode: DataNode, ordering: SpecOrdering = SpecOrdering.Lexical)(implicit eh: ErrorHandler) {
  def emitDocument(): YDocument = {
    val f: YDocument.PartBuilder => Unit = DataNodeEmitter(dataNode, ordering)(eh).emit
    YDocument(f)
  }
}
