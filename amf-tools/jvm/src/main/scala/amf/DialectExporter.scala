package amf

import amf.core.{AMF, AMFSerializer}
import amf.core.emitter.RenderOptions
import amf.core.services.RuntimeSerializer
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.dialects.OAS20Dialect

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DialectExporter {

  def main(args: Array[String]): Unit = {
    AMF.registerPlugin(AMLPlugin)
    val f = AMF.init() map { _ =>
      val dialectText = RuntimeSerializer(
        OAS20Dialect(),
        "application/yaml",
        "AML 1.0",
        RenderOptions()
      )
      println(dialectText)
    }

    Await.result(f, Duration.Inf)
  }
}
