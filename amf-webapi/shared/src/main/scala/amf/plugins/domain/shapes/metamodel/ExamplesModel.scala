package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.{Examples => ExamplesInstance}

/**
  *
  */
object ExamplesModel extends DomainElementModel with LinkableElementModel {

  val Examples = Field(
    Array(ExampleModel),
    Document + "examples",
    ModelDoc(ModelVocabularies.AmlDoc, "examples", "Examples list", Seq((Namespace.Schema + "name").iri()))
  )

  override def fields: List[Field] =
    List(Examples) ++ DomainElementModel.fields ++ LinkableElementModel.fields

  override val `type`: List[ValueType] = Document + "NamedExamples" :: DomainElementModel.`type`

  override def modelInstance: ExamplesInstance = ExamplesInstance()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Examples",
    "Wrapper for a list of examples"
  )
}
