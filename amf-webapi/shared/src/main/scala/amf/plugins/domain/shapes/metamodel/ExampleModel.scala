package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ExternalSourceElementModel, LinkableElementModel}
import amf.plugins.domain.shapes.models.Example
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType

/**
  *
  */
object ExampleModel
    extends DomainElementModel
    with LinkableElementModel
    with KeyField
    with ExternalSourceElementModel {

  val Name            = Field(Str, Schema + "name")
  val DisplayName     = Field(Str, Document + "displayName")
  val Summary         = Field(Str, Http + "guiSummary")
  val Description     = Field(Str, Schema + "description")
  val Value           = Field(Str, Document + "value")
  val ExternalValue   = Field(Str, Document + "externalValue")
  val StructuredValue = Field(DataNodeModel, Document + "structuredValue")
  val Strict          = Field(Bool, Document + "strict")
  val MediaType       = Field(Str, Http + "mediaType")

  override val key: Field = Name

  override def fields: List[Field] =
    List(Name, DisplayName, Summary, Description, Value, ExternalValue, Strict, MediaType, StructuredValue) ++ DomainElementModel.fields ++ LinkableElementModel.fields ++ ExternalSourceElementModel.fields

  override val `type`: List[ValueType] = Document + "Example" :: DomainElementModel.`type`

  override def modelInstance = Example()
}
