package amf.plugins.domain.shapes.models

import amf.core.model.domain.DomainElement
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.metamodel.common.ExamplesField.Examples

trait ExemplifiedDomainElement extends DomainElement {

  def examples: Seq[Example] = fields.field(Examples)

  def withExamples(examples: Seq[Example], annotations: Annotations = Annotations()): this.type =
    setArray(Examples, examples, annotations)

  def withExample(name: Option[String]): Example = {
    val example = Example()
    name.foreach { example.withName(_) }
    add(Examples, example)
    example
  }

  def removeExamples(): Unit = fields.removeField(Examples)
}
