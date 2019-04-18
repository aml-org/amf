package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain._
import amf.plugins.domain.shapes.models.Example
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.{Namespace, ValueType}

/**
  *
  */
trait ExampleModel
    extends DomainElementModel
    with LinkableElementModel
    with KeyField
    with ExternalSourceElementModel
    with NameFieldSchema {

  val DisplayName = Field(
    Str,
    Core + "displayName",
    ModelDoc(ModelVocabularies.Core,
             "display name",
             "Human readable name for the example",
             Seq((Namespace.Core + "name").iri()))
  )
  val Summary = Field(
    Str,
    ApiContract + "guiSummary",
    ModelDoc(ModelVocabularies.ApiContract,
             "gui summary",
             "Human readable description of the example",
             Seq((Namespace.ApiContract + "description").iri()))
  )
  val Description =
    Field(Str, Core + "description", ModelDoc(ModelVocabularies.Core, "description", ""))
  val ExternalValue = Field(
    Str,
    Document + "externalValue",
    ModelDoc(ModelVocabularies.AmlDoc, "external value", "Raw text containing an unparsable example"))
  val StructuredValue = Field(
    DataNodeModel,
    Document + "structuredValue",
    ModelDoc(ModelVocabularies.AmlDoc, "structured value", "Data structure containing the value of the example"))
  val Strict = Field(Bool,
                     Document + "strict",
                     ModelDoc(ModelVocabularies.AmlDoc,
                              "strict",
                              "Indicates if this example should be validated against an associated schema"))
  val MediaType = Field(Str,
                        Core + "mediaType",
                        ModelDoc(ModelVocabularies.Core, "media type", "Media type associated to the example"))

  override val key: Field = Name

  override val fields: List[Field] =
    List(Name, DisplayName, Summary, Description, ExternalValue, Strict, MediaType, StructuredValue) ++ DomainElementModel.fields ++ LinkableElementModel.fields ++ ExternalSourceElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "Example" :: DomainElementModel.`type`

  override def modelInstance = Example()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Example",
    "Example value for a schema inside an API"
  )
}

object ExampleModel extends ExampleModel
