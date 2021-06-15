package amf.plugins.domain.shapes.models

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.domain.{
  DataNode,
  DomainElement,
  ExternalSourceElement,
  Linkable,
  NamedDomainElement
}
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ExternalSourceElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.common.PayloadSerializer
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.ExampleModel._
import org.yaml.model.YPart

/**
  *
  */
class Example(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable
    with ExternalSourceElement
    with PayloadSerializer
    with Key {

  def displayName: StrField     = fields.field(DisplayName)
  def description: StrField     = fields.field(Description)
  def structuredValue: DataNode = fields.field(StructuredValue)
  def strict: BoolField         = fields.field(Strict)
  def mediaType: StrField       = fields.field(MediaType)

  def withDisplayName(displayName: String): this.type = set(DisplayName, displayName)
  def withDescription(description: String): this.type = set(Description, description)
  def withValue(value: String): this.type             = set(ExternalSourceElementModel.Raw, value)
  def withStructuredValue(value: DataNode): this.type = set(StructuredValue, value)
  def withStrict(strict: Boolean): this.type =
    set(Strict, strict)
  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)

  override def linkCopy(): Example = Example().withId(id)

  override def meta: ExampleModel.type = ExampleModel

  override def key: StrField = fields.field(ExampleModel.key)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/example/" + name.option().getOrElse("default-example").urlComponentEncoded

  def toJson: String = toJson(this, AMFGraphConfiguration.predefined())

  def toYaml: String = toYaml(this, AMFGraphConfiguration.predefined())

  def toJson(config: AMFGraphConfiguration): String = toJson(this, config)

  def toYaml(config: AMFGraphConfiguration): String = toYaml(this, config)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Example.apply
  override def nameField: Field                                                                 = Name
}

object Example {

  def apply(): Example = apply(Annotations())

  def apply(ast: YPart): Example = apply(Annotations(ast))

  def apply(annotations: Annotations): Example = Example(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Example = new Example(fields, annotations)
}
