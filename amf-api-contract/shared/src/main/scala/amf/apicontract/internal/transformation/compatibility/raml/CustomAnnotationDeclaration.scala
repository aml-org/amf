package amf.apicontract.internal.transformation.compatibility.raml

import amf.apicontract.internal.spec.common.parser.WellKnownAnnotation
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.transform.stages.TransformationStep

class CustomAnnotationDeclaration() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      val annotationsTypes = WellKnownAnnotation.ramlKnownAnnotations
        .map(name => CustomDomainProperty().withName(s"amf-$name").withSchema(AnyShape()))

      model match {
        case d: DeclaresModel =>
          annotationsTypes.foreach(d.withDeclaredElement)
          model
        case _ =>
          model
      }
    } catch {
      case _: Throwable => model
    }
  }
}
