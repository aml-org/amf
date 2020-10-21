package amf.plugins.document.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.document._
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.plugins.document.webapi.model._
import amf.core.vocabulary.Namespace.{ApiContract, Security, Shapes}
import amf.core.vocabulary.ValueType

object FragmentsTypesModels {

  object DocumentationItemFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(ApiContract + "UserDocumentationFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DocumentationItemFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "DocumentationItemFragment",
      "Fragment encoding a RAML documentation item"
    )
  }

  object DataTypeFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Shapes + "DataTypeFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DataTypeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.Shapes,
      "DataTypeFragment",
      "Fragment encoding a RAML data type"
    )
  }

  object NamedExampleFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(ApiContract + "NamedExampleFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = NamedExampleFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "NamedExampleFragment",
      "Fragment encoding a RAML named example"
    )
  }

  object ResourceTypeFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(ApiContract + "ResourceTypeFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = ResourceTypeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "ResourceTypeFragment",
      "Fragment encoding a RAML resource type"
    )
  }

  object TraitFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(ApiContract + "TraitFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = TraitFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "TraitFragment",
      "Fragment encoding a RAML trait"
    )
  }

  object AnnotationTypeDeclarationFragmentModel extends FragmentModel {

    override val fields: List[Field] = FragmentModel.fields

    override val `type`
      : List[ValueType] = List(ApiContract + "AnnotationTypeDeclarationFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = AnnotationTypeDeclarationFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.ApiContract,
      "AnnotationTypeFragment",
      "Fragment encoding a RAML annotation type"
    )
  }

  object SecuritySchemeFragmentModel extends FragmentModel {
    override val fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Security + "SecuritySchemeFragment") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = SecuritySchemeFragment()

    override val doc: ModelDoc = ModelDoc(
      ModelVocabularies.Security,
      "SecuritySchemeFragment",
      "Fragment encoding a RAML security scheme"
    )
  }
}
