package amf.apicontract.internal.transformation.compatibility.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfArray, DomainElement, Linkable}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.elements.resolution.ReferenceResolution
import amf.core.internal.transform.stages.elements.resolution.ReferenceResolution.ASSERT_DIFFERENT

abstract class AmfElementLinkResolutionStage() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model match {
      case doc: Document =>
        val resolver = new ReferenceResolution(errorHandler)
        doc.iterator().foreach {
          case d: DomainElement =>
            d.fields
              .fields()
              .foreach(f =>
                f.element match {
                  case l: Linkable if l.isLink && selector(l, doc) =>
                    resolver.transform(l.asInstanceOf[DomainElement], Seq(ASSERT_DIFFERENT)) match {
                      case Some(resolved) => d.fields.setWithoutId(f.field, resolved)
                      case None           => // Nothing
                    }
                  case a: AmfArray =>
                    val newItems = a.values.map {
                      case l: Linkable if l.isLink && selector(l, doc) =>
                        resolver.transform(l.asInstanceOf[DomainElement], Seq(ASSERT_DIFFERENT)).getOrElse(l)
                      case i => i
                    }
                    d.fields.setWithoutId(f.field, a.copy(newItems))
                  case _ => // Nothing
              })
        }
      case _ => // Nothing
    }
    model
  }

  def selector[T <: BaseUnit](l: Linkable, model: T): Boolean

}
