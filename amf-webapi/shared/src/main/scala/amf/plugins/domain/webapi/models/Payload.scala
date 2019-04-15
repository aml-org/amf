package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings
import amf.plugins.domain.shapes.models.{Examples, _}
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.metamodel.PayloadModel.{Encoding => EncodingModel, _}
import org.yaml.model.YPart

/**
  * Payload internal model.
  */
case class Payload(fields: Fields, annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def mediaType: StrField     = fields.field(MediaType)
  def schema: Shape           = fields.field(Schema)
  def examples: Examples      = fields.field(PayloadModel.Examples)
  def encoding: Seq[Encoding] = fields.field(EncodingModel)

  def withMediaType(mediaType: String): this.type      = set(MediaType, mediaType)
  def withSchema(schema: Shape): this.type             = set(Schema, schema)
  def withEncoding(encoding: Seq[Encoding]): this.type = setArray(EncodingModel, encoding)
  def withExamples(examples: Examples): this.type      = set(PayloadModel.Examples, examples)
  def withExamples(examples: Seq[Example]): this.type = {
    val ex = Examples()
    set(PayloadModel.Examples, ex)
    ex.withExamples(examples)
    this
  }

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(PayloadModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(PayloadModel.Schema, scalar)
    scalar
  }

  def withArraySchema(name: String): ArrayShape = {
    val array = ArrayShape().withName(name)
    set(PayloadModel.Schema, array)
    array
  }

  def withExample(name: Option[String]): Example = {
    val newExample = Example()
    name.foreach(newExample.withName(_))
    examples match {
      case e: Examples => e ++ Seq(newExample)
      case _ =>
        val newExamples = Examples()
        withExamples(newExamples)
        newExamples.withExamples(Seq(newExample))
    }
    newExample
  }

  def exampleValues: Seq[Example] = examples match {
    case e: Examples => e.examples
    case _           => Nil
  }

  def withEncoding(name: String): Encoding = {
    val result = Encoding().withPropertyName(name)
    add(EncodingModel, result)
    result
  }

  override def linkCopy(): Payload = Payload().withId(id)

  def clonePayload(parent: String): Payload = {
    val cloned = Payload(annotations).withMediaType(mediaType.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape(None)
          case o        => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: Obj = PayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + mediaType
      .option()
      .getOrElse(name.option().getOrElse("default"))
      .urlComponentEncoded // todo: / char of media type should be encoded?
  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Payload.apply
  override protected def nameField: Field                                                       = Name
}

object Payload {
  def apply(): Payload = apply(Annotations())

  def apply(ast: YPart): Payload = apply(Annotations(ast))

  def apply(annotations: Annotations): Payload = new Payload(Fields(), annotations)
}
