package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation

class CustomAnnotationDeclaration() extends TransformationStep {
  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
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
