package amf.plugins.domain.apicontract.resolution.stages.common

import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.annotations.TrackedElement
import amf.core.internal.metamodel.Field
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.shapes.metamodel.common.ExamplesField.Examples
import amf.plugins.domain.shapes.models.{AnyShape, Example, ExemplifiedDomainElement}

trait ExamplePropagationHelper {

  def trackExamplesOf(exemplified: DomainElement, shape: Shape, examplesField: Field = ExamplesField.Examples): Unit =
    shape match {
      case anyShape: AnyShape =>
        val examples: Seq[Example] = exemplified.fields.field(examplesField)
        examples.foreach { example =>
          if (!anyShape.examples.exists(_.id == example.id)) {
            example.add(TrackedElement(exemplified.id))
            anyShape.setArrayWithoutId(ExamplesField.Examples, anyShape.examples ++ Seq(example))
          }
          exemplified.fields.removeField(examplesField)
        }
      case _ => // ignore
    }

}
