package amf.plugins.document.webapi.resolution.pipelines.compatibility.common

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{AmfArray, DomainElement, Linkable}
import amf.core.resolution.stages.ResolutionStage
import amf.core.resolution.stages.elements.resolution.ReferenceResolution
import amf.core.resolution.stages.elements.resolution.ReferenceResolution.ASSERT_DIFFERENT

abstract class AmfElementLinkResolutionStage()(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
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
