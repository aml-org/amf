package amf.apicontract.internal.spec.raml.parser.context

import amf.shapes.internal.spec.common.parser.IgnoreCriteria

trait RamlIgnoreCriteria extends IgnoreCriteria {

  protected val supportsAnnotations: Boolean

  override def shouldIgnore(shape: String, property: String): Boolean = {
    def isAnnotation = supportsAnnotations && property.startsWith("(") && property.endsWith(")")

    def isAllowedNestedEndpoint = {
      val shapesIgnoringNestedEndpoints = "webApi" :: "endPoint" :: Nil
      property.startsWith("/") && shapesIgnoringNestedEndpoints.contains(shape)
    }

    def reportedByOtherConstraint = {
      val nestedEndpointsConstraintShapes = "resourceType" :: Nil
      property.startsWith("/") && nestedEndpointsConstraintShapes.contains(shape)
    }

    def isAllowedParameter = {
      val shapesWithParameters = "resourceType" :: "trait" :: Nil
      property.matches("<<.+>>") && shapesWithParameters.contains(shape)
    }

    isAnnotation || isAllowedNestedEndpoint || isAllowedParameter || reportedByOtherConstraint
  }
}

object Raml10IgnoreCriteria extends RamlIgnoreCriteria {
  override protected val supportsAnnotations: Boolean = true
}

object Raml08IgnoreCriteria extends RamlIgnoreCriteria {
  override protected val supportsAnnotations: Boolean = false
}
