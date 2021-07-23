package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.Module
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.{Mimes, Raml10}
import amf.core.internal.render.AMFSerializer
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.annotations.{GeneratedRamlDatatype, ParsedRamlDatatype}
import amf.shapes.client.scala.model.domain.AnyShape

import scala.concurrent.ExecutionContext

/** Serializes AnyShape to RAML Data Type. */
trait RamlDatatypeSerializer extends PlatformSecrets {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  protected def toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String = {
    element.annotations.find(classOf[ParsedRamlDatatype]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedRamlDatatype]) match {
          case Some(g) => g.rawText
          case _       => generateRamlDatatype(element, config)
        }
    }
  }

  /** Generates a new RAML Data Type included in RAML 1.0 Library. */
  protected def generateRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String = {
    implicit val executionContext: ExecutionContext = config.getExecutionContext

    val ramlDatatype =
      new AMFSerializer(Module().withDeclaredElement(fixNameIfNeeded(element)),
                        `application/yaml`,
                        config.renderConfiguration).render()
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
