package amf.plugins.domain.webapi.resolution.stages.common

import amf.core.annotations.TrackedElement
import amf.core.model.domain.Shape
import amf.plugins.domain.shapes.models.{AnyShape, ExemplifiedDomainElement}

trait ExamplePropagationHelper {

  def trackExamplesOf(exemplified: ExemplifiedDomainElement, shape: Shape): Unit = shape match {
    case anyShape: AnyShape =>
      exemplified.examples.foreach { example =>
        if (!anyShape.examples.exists(_.id == example.id)) {
          example.add(TrackedElement(exemplified.id))
          anyShape.withExamples(anyShape.examples ++ Seq(example))
          exemplified.removeExamples()
        }
      }
    case _ => // ignore
  }

}
