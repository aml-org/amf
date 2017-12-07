package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.plugins.domain.shapes.models.Example
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType

/**
  *
  */
object ExampleModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name        = Field(Str, Schema + "name")
  val DisplayName = Field(Str, Document + "displayName")
  val Description = Field(Str, Schema + "description")
  val Value       = Field(Str, Document + "value")
  val Strict      = Field(Bool, Document + "strict")
  val MediaType   = Field(Str, Http + "mediaType")

  override val key: Field = Name

  override def fields: List[Field] =
    List(Name, DisplayName, Description, Value, Strict, MediaType) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "Example" :: DomainElementModel.`type`

  override def modelInstance = Example()
}
