package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation
import amf.plugins.domain.shapes.models.AnyShape

class CustomAnnotationDeclaration() extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
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
