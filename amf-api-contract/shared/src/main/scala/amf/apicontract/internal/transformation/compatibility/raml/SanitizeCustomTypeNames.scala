package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.transform.stages.TransformationStep

class SanitizeCustomTypeNames() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model match {
      case doc: Document =>
        doc.declares.collect {
          case d: NamedDomainElement =>
            sanitizeName(d.name.value()) match {
              case Some(name) => d.withName(name)
              case None       => // Nothing to do
            }
        }
        doc.iterator().foreach {
          case d: DomainElement =>
            d match {
              case l: Linkable if l.isLink =>
                sanitizeName(l.linkLabel.value()) match {
                  case Some(name) => l.withLinkLabel(name)
                  case None       => // Nothing to do
                }
              case _ => // Nothing
            }
        }
      case _ => // Nothing
    }
    model
  }

  def sanitizeName(name: String): Option[String] = {
    val excludedChars = Set(' ', '[', ']')
    if (excludedChars.exists(name.contains(_))) Some(name.filterNot(excludedChars))
    else None
  }

}
