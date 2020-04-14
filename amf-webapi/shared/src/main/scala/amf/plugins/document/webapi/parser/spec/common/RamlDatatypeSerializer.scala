package amf.plugins.document.webapi.parser.spec.common

import amf.client.execution.BaseExecutionEnvironment
import amf.core.AMFSerializer
import amf.core.model.document.Module
import amf.core.remote.Raml10
import amf.core.services.RuntimeSerializer
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.{GeneratedRamlDatatype, ParsedRamlDatatype}
import amf.plugins.domain.shapes.models.AnyShape

import scala.concurrent.ExecutionContext

/** Serializes AnyShape to RAML Data Type. */
trait RamlDatatypeSerializer extends PlatformSecrets {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  protected def toRamlDatatype(element: AnyShape, exec: BaseExecutionEnvironment): String = {
    element.annotations.find(classOf[ParsedRamlDatatype]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedRamlDatatype]) match {
          case Some(g) => g.rawText
          case _       => generateRamlDatatype(element, exec)
        }
    }
  }

  /** Generates a new RAML Data Type included in RAML 1.0 Library. */
  protected def generateRamlDatatype(element: AnyShape, exec: BaseExecutionEnvironment): String = {
    implicit val executionContext: ExecutionContext = exec.executionContext

    AMFSerializer.init()
    val ramlDatatype =
      RuntimeSerializer(Module().withDeclaredElement(fixNameIfNeeded(element)), "application/raml", Raml10.name)
    element.annotations.reject(_.isInstanceOf[ParsedRamlDatatype])
    element.annotations.reject(_.isInstanceOf[GeneratedRamlDatatype])
    element.annotations += GeneratedRamlDatatype(ramlDatatype)
    ramlDatatype
  }

  /** Sets element name to "Root" if the name doesn't exist or equals to "type". */
  private def fixNameIfNeeded(element: AnyShape): AnyShape = {
    if (element.name.option().isEmpty) {
      element.copyShape().withName("Root")
    } else {
      if (element.name.value().matches("type")) element.copyShape().withName("Root")
      else element
    }
  }
}
