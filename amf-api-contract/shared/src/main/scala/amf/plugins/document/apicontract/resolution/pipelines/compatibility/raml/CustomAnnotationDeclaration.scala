package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.resolution.stages.TransformationStep
import amf.plugins.document.apicontract.parser.spec.common.WellKnownAnnotation
import amf.plugins.domain.shapes.models.AnyShape

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
