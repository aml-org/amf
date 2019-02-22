package amf.plugins.document.vocabularies.metamodel.domain
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace

trait NodeWithDiscriminatorModel {
  val TypeDiscriminator = Field(
    Str,
    Namespace.Meta + "typeDiscriminatorMap",
    ModelDoc(ModelVocabularies.Meta,
      "type discriminator map",
      "Information about the discriminator values in the source AST for the property mapping")
  )
  val TypeDiscriminatorName = Field(
    Str,
    Namespace.Meta + "typeDiscriminatorName",
    ModelDoc(ModelVocabularies.Meta,
      "type discriminator name",
      "Information about the field in the source AST to be used as discrimintaro in the property mapping")
  )
}
