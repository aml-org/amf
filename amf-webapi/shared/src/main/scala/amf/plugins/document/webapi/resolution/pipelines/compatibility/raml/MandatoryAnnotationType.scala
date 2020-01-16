package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.AnyShape

class MandatoryAnnotationType()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  var annotationCounter = 0

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model match {
        case doc: Document =>
          model.iterator().foreach {
            case domainExtension: DomainExtension =>
              val customDomainPropertyWithSchema = Option(domainExtension.definedBy) match {
                case Some(customDomainProperty) =>
                  ensureType(customDomainProperty)
                case None =>
                  annotationCounter += 1
                  val customDomainProperty = CustomDomainProperty()
                    .withId(model.location() + s"#genAnnotation$annotationCounter")
                    .withName(domainExtension.name.value())
                    .withSchema(AnyShape())
                  domainExtension.withDefinedBy(customDomainProperty)
                  customDomainProperty
              }

              if (!doc.declares.exists(_.id == customDomainPropertyWithSchema.id)) {
                doc.withDeclaredElement(customDomainPropertyWithSchema)
              }

            case _ =>
            // ignore
          }

        case _ => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want to break anything
    }

    model
  }

  protected def ensureType(customDomainProperty: CustomDomainProperty): CustomDomainProperty = {
    if (Option(customDomainProperty.schema).isEmpty) {
      customDomainProperty.withSchema(AnyShape())
    }
    customDomainProperty
  }

}
