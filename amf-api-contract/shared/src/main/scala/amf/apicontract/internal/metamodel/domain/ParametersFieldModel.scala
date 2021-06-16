package amf.apicontract.internal.metamodel.domain

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}

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
