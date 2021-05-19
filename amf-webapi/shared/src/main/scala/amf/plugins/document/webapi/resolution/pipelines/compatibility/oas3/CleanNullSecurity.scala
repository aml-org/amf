package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.annotations.NullSecurity
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.DataType
import amf.core.model.document.BaseUnit
import amf.core.model.domain.ScalarNode
import amf.core.model.domain.extensions.DomainExtension
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.Operation

class CleanNullSecurity() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case op: Operation =>
          // filter null security schemas not supported in OAS
          var isNull = false
          op.security.foreach { requirement =>
            val schemes = requirement.schemes.filter { securityScheme =>
              if (securityScheme.annotations.contains(classOf[NullSecurity])) {
                isNull = true
                false
              } else true
            }
            // Update and mark with an annotation if security is optional
            requirement.withSchemes(schemes)
          }
          if (isNull) {
            op.withCustomDomainProperty(
              DomainExtension()
                .withName("optionalSecurity")
                .withExtension(ScalarNode("true", Some(DataType.Boolean))))
          }

        case _ => // ignore
      }
      model
    } catch {
      case _: Throwable => model // ignore: we don't want this to break anything
    }
  }
}
