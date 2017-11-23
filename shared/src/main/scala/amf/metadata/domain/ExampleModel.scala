package amf.metadata.domain

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Bool, Str}
import amf.vocabulary.Namespace._
import amf.vocabulary.ValueType

/**
  *
  */
object ExampleModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name        = Field(Str, Document + "name")
  val DisplayName = Field(Str, Document + "displayName")
  val Description = Field(Str, Document + "description")
  val Value       = Field(Str, Document + "value")
  val Strict      = Field(Bool, Document + "strict")
  val MediaType   = Field(Str, Http + "mediaType")

  override val key: Field = Name

  override def fields: List[Field] =
    List(Name, DisplayName, Description, Value, Strict, MediaType) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "Example" :: DomainElementModel.`type`
}
