package amf.plugins.domain.apicontract.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.{ApiContract, Core, Security}

trait ParametersFieldModel {

  val Headers = Field(
    Array(ParameterModel),
    ApiContract + "header",
    ModelDoc(ModelVocabularies.ApiContract,
             "header",
             "Parameter passed as a header to an operation for communication models")
  )

  val QueryParameters = Field(
    Array(ParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.ApiContract, "parameter", "Parameters associated to the communication model"))

  val QueryString = Field(
    ShapeModel,
    ApiContract + "queryString",
    ModelDoc(ModelVocabularies.ApiContract, "queryString", "Query string for the communication model"))

  val UriParameters =
    Field(Array(ParameterModel),
          ApiContract + "uriParameter",
          ModelDoc(ModelVocabularies.ApiContract, "uri parameter", ""))

}

object ParametersFieldModel extends ParametersFieldModel
