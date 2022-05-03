package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.ParametrizedSecurityScheme
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep

class DeclareUndeclaredSecuritySchemes() extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        DeclareSecuritySchemes(doc)
      case _ =>
    }
    model
  }

  protected def DeclareSecuritySchemes(doc: Document): Unit = {
    val api = doc.encodes.asInstanceOf[Api]

    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        operation.security.foreach(req => {
          req.schemes.foreach(parametrizedScheme => {
            if (!schemaIsDeclared(doc, parametrizedScheme)) {
              val name            = parametrizedScheme.name.value()
              val nameAnnotations = parametrizedScheme.name.annotations()
              val securityScheme  = parametrizedScheme.scheme.withName(name, nameAnnotations)
              doc.withDeclaredElement(securityScheme)
            }
          })
        })
      }
    }
  }

  private def schemaIsDeclared(doc: Document, parametrizedScheme: ParametrizedSecurityScheme) = {
    doc.declares.contains(parametrizedScheme.scheme)
  }
}
