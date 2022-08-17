package amf.apicontract.internal.validation.shacl.oas
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.scala.model.domain.AmfObject
import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, ValidationInfo}

object DuplicatedEndpointPathValidation {
  def apply(): CustomShaclFunction = {
    new CustomShaclFunction {
      override val name: String = "duplicatedEndpointPath"
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
  private def normalizePath(s: String): String = {
    val trimmed = if (s.endsWith("/")) s.init else s
    trimmed.replaceAll("\\{.*?\\}", "{parameter}")
  }

  private def identicalPaths(first: String, second: String): Boolean = normalizePath(first) == normalizePath(second)

  private def pathIsDuplicated(api: Api, endPointPath: String) = {
    api.endPoints.count(other => identicalPaths(other.path.value(), endPointPath)) > 1
  }
}
