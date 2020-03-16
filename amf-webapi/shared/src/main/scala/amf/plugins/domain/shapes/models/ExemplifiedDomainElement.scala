package amf.plugins.domain.shapes.models

import amf.core.model.domain.DomainElement
import amf.plugins.domain.shapes.metamodel.common.ExamplesField.Examples

trait ExemplifiedDomainElement extends DomainElement {

  def examples: Seq[Example] = fields.field(Examples)

  def withExamples(examples: Seq[Example]): this.type = setArray(Examples, examples)

  def withExample(name: Option[String]): Example = {
    val example = Example()
    name.foreach { example.withName(_) }
    add(Examples, example)
    example
  }

  def removeExamples(): this.type = remove(Examples)
}
