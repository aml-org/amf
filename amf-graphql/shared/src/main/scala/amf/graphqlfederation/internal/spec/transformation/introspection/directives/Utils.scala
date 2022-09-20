package amf.graphqlfederation.internal.spec.transformation.introspection.directives

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.ScalarNode

trait Utils {

  protected def asScalarNode(value: String): ScalarNode =
    ScalarNode()
      .withValue(value)
      .withDataType(DataTypes.String)

  protected def asScalarNode(value: Boolean): ScalarNode =
    ScalarNode()
      .withValue(value.toString)
      .withDataType(DataTypes.Boolean)

}
