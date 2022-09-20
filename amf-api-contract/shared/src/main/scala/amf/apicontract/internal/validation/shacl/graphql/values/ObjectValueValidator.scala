package amf.apicontract.internal.validation.shacl.graphql.values

import amf.apicontract.internal.validation.shacl.graphql.GraphQLProperty
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.NodeShape
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object ObjectValueValidator extends ValueValidator[NodeShape] {
  override def validate(shape: NodeShape, value: DataNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    value match {
      case s: ScalarNode if isNull(s) => Seq(typeError("object", "null", s.annotations))
      case s: ScalarNode              => Seq(typeError("object", "scalar", s.annotations))
      case a: ArrayNode               => Seq(typeError("object", "list", a.annotations))
      case o: ObjectNode              => validateProperties(shape, o)
    }
  }

  sealed case class ReportingInfo(shapeName: String, annotations: Annotations)

  private def validateProperties(shape: NodeShape, value: ObjectNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    val actual: Map[String, DataNode] = value.allPropertiesWithName()
    val expected                      = shape.properties
    implicit val info: ReportingInfo  = ReportingInfo(shape.name.value(), value.annotations)

    validateExpectedProperties(expected, actual) ++ validateUnexpectedProperties(expected, actual)
  }

  private def validateExpectedProperties(expected: Seq[PropertyShape], actual: Map[String, DataNode])(implicit
      info: ReportingInfo, targetField: Field
  ): Seq[ValidationInfo] = {
    expected.flatMap { expectedProperty => validateExpectedProperty(expectedProperty, actual) }
  }

  private def validateUnexpectedProperties(
      expected: Seq[PropertyShape],
      actual: Map[String, DataNode]
  )(implicit info: ReportingInfo, targetField: Field): Seq[ValidationInfo] = {
    val expectedPropertyNames = expected.map(_.name.value()).toSet // for performance

    actual.keys
      .filter { actual => !expectedPropertyNames.contains(actual) }
      .map { unexpectedProperty =>
        val message = s"Unexpected property '$unexpectedProperty' is not defined in object '${info.shapeName}'"
        ValidationInfo(targetField, Some(message), Some(info.annotations))
      }
      .toSeq
  }

  private def validateExpectedProperty(
      expected: PropertyShape,
      actual: Map[String, DataNode]
  )(implicit info: ReportingInfo, targetField: Field): Seq[ValidationInfo] = {
    val expectedName = expected.name.value()

    actual.get(expectedName) match {
      case Some(propertyValue) =>
        val expectedRange = expected.range
        ValueValidator.validate(expectedRange, propertyValue)
      case None if GraphQLProperty(expected).isNullable => Nil
      case None =>
        val message = s"Provided value for object '${info.shapeName}' is missing property '$expectedName'"
        Seq(ValidationInfo(targetField, Some(message), Some(info.annotations)))
    }
  }

}
