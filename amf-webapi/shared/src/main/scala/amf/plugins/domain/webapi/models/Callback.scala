package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.metamodel.CallbackModel
import amf.plugins.domain.webapi.metamodel.CallbackModel._
import org.yaml.model.YMap

/**
  * Callback internal model
  */
case class Callback(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def expression: StrField = fields.field(Expression)
  def endpoint: EndPoint   = fields.field(Endpoint)

  def withExpression(expression: String): this.type = set(Expression, expression)
  def withEndpoint(endpoint: EndPoint): this.type   = set(Endpoint, endpoint)

  def withEndpoint(path: String): EndPoint = {
    val result = EndPoint().withPath(path)
    set(Endpoint, result)
    result
  }

  override def meta: Obj = CallbackModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + name.option().getOrElse("default-callback").urlComponentEncoded +
      s"/${expression.option().getOrElse("default-expression").urlComponentEncoded}"
  override protected def nameField: Field = Name

  override def linkCopy(): Linkable = {
    val callback = Callback().withId(id)
    name.option().foreach(callback.withName(_))
    expression.option().foreach(callback.withExpression)
    callback
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new Callback(fields, annot)
}

object Callback {

  def apply(): Callback = apply(Annotations())

  def apply(ast: YMap): Callback = apply(Annotations(ast))

  def apply(annotations: Annotations): Callback = new Callback(Fields(), annotations)
}
