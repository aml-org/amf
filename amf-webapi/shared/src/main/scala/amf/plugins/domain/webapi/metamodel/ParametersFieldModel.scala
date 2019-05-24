package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.Http

trait ParametersFieldModel {

  val Headers = Field(
    Array(ParameterModel),
    Http + "header",
    ModelDoc(ModelVocabularies.Http, "header", "Parameter passed as a header to an operation for communication models")
  )

  val QueryParameters = Field(
    Array(ParameterModel),
    Http + "parameter",
    ModelDoc(ModelVocabularies.Http, "parameter", "Parameters associated to the communication model"))

  val QueryString = Field(ShapeModel,
                          Http + "queryString",
                          ModelDoc(ModelVocabularies.Http, "query string", "Query string for the communication model"))

  val UriParameters =
    Field(Array(ParameterModel), Http + "uriParameter", ModelDoc(ModelVocabularies.Http, "uri parameter", ""))

}

object ParametersFieldModel extends ParametersFieldModel
