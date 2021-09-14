package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.annotations.NullSecurity

class CleanNullSecurity() extends TransformationStep {

  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
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
