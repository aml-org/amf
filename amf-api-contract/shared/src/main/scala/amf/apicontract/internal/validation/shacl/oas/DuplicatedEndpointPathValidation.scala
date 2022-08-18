package amf.apicontract.internal.validation.shacl.oas
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.scala.model.domain.AmfObject
import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, ValidationInfo}

trait DuplicatedEndpointPathValidation {
  def apply(): CustomShaclFunction = {
    new CustomShaclFunction {
      override val name: String = validationName
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element match {
          case api: Api =>
            api.endPoints.foreach({ endPoint =>
              val endPointPath = endPoint.path.value()
              if (pathIsDuplicated(api, endPointPath)) {
                validate(
                  Some(
                    ValidationInfo(
                      WebApiModel.EndPoints,
                      Some(s"Duplicated resource path: '$endPointPath'"),
                      Some(endPoint.annotations)
                    )
                  )
                )
              }
            })
        }
      }
    }
  }
  def identicalPaths(first: String, second: String): Boolean
  def pathIsDuplicated(api: Api, endPointPath: String): Boolean = {
    api.endPoints.count(other => identicalPaths(other.path.value(), endPointPath)) > 1
  }
  def validationName: String
}

object DuplicatedOas3EndpointPathValidation extends DuplicatedEndpointPathValidation {
  override def identicalPaths(first: String, second: String): Boolean = normalizePath(first) == normalizePath(second)
  override def validationName: String = "duplicatedOas3EndpointPath"

  private def normalizePath(s: String): String = {
    val trimmed = if (s.endsWith("/")) s.init else s
    trimmed.replaceAll("\\{.*?\\}", "{parameter}")
  }
}

object DuplicatedCommonEndpointPathValidation extends DuplicatedEndpointPathValidation {
  override def identicalPaths(first: String, second: String): Boolean = {
    first  == second
  }
  override def validationName: String = "duplicatedCommonEndpointPath"
}
