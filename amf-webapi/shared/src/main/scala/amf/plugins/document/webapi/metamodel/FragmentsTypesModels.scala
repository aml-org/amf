package amf.plugins.document.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.document._
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.plugins.document.webapi.model._
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object FragmentsTypesModels {

  object DocumentationItemFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "UserDocumentation") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DocumentationItemFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Documentation Item Fragment",
      "Fragment encoding a RAML documentation item"
    )
  }

  object DataTypeFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DataType") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DataTypeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Data Type Fragment",
      "Fragment encoding a RAML data type"
    )
  }

  object NamedExampleFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "NamedExample") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = NamedExampleFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Named Example Fragment",
      "Fragment encoding a RAML named example"
    )
  }

  object ResourceTypeFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ResourceTypeFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = ResourceTypeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Resource Type Fragment",
      "Fragment encoding a RAML resource type"
    )
  }

  object TraitFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "TraitFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = TraitFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Trait Fragment",
      "Fragment encoding a RAML trait"
    )
  }

  object AnnotationTypeDeclarationFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "AnnotationTypeDeclarationFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = AnnotationTypeDeclarationFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Annotation Type Fragment",
      "Fragment encoding a RAML annotation type"
    )
  }

  object SecuritySchemeFragmentModel extends FragmentModel {
    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "SecuritySchemeFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = SecuritySchemeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "Security Scheme Fragment",
      "Fragment encoding a RAML security scheme"
    )
  }
}
