package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.domain._
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ExamplesModel
import org.yaml.model.YPart

/**
  * Examples meta model. Contains a list of examples.
  */
class Examples(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with Linkable
    with ExternalSourceElement {

  def examples: Seq[Example] = fields.field(ExamplesModel.Examples)

  def withExamples(examples: Seq[Example]): this.type = setArray(ExamplesModel.Examples, examples)

  def withExample(name: Option[String]): Example = {
    val example = Example()
    name.foreach(example.withName(_))
    this ++ Seq(example)
    example
  }

  def withExampleWithMediaType(mediaType: String): Example = {
    val example = Example()
    add(ExamplesModel.Examples, example)
    example.withMediaType(mediaType)
    example
  }

  def ++(e: Seq[Example]): this.type = examples match {
    case null | Nil => withExamples(e)
    case _          => withExamples(examples ++ e)
  }

  def linkNorEmpty: Boolean = examples.nonEmpty || isLink

  override def linkCopy(): Examples = Examples().withId(id)

  override def meta: Obj = ExamplesModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/examples/"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Examples.apply
}

object Examples {

  def apply(): Examples = apply(Annotations())

  def apply(ast: YPart): Examples = apply(Annotations(ast))

  def apply(annotations: Annotations): Examples = Examples(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Examples = new Examples(fields, annotations)
}
