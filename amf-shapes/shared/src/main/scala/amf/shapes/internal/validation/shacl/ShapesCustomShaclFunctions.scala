package amf.shapes.internal.validation.shacl

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar, DomainElement}
import amf.core.internal.utils.RegexConverter
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel._
import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, ValidationInfo}
import org.mulesoft.common.collections._

import java.util.regex.Pattern

object ShapesCustomShaclFunctions extends BaseCustomShaclFunctions {

  override protected[amf] val listOfFunctions: Seq[CustomShaclFunction] = Seq(
    new CustomShaclFunction {
      override val name: String = "minimumMaximumValidation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          minInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.Minimum)
          maxInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.Maximum)
        } yield {
          val minValue = minInclusive.toString.toDouble
          val maxValue = maxInclusive.toString.toDouble
          if (minValue > maxValue) {
            validate(None)
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "minMaxItemsValidation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          minInclusive <- element.fields.?[AmfScalar](ArrayShapeModel.MinItems)
          maxInclusive <- element.fields.?[AmfScalar](ArrayShapeModel.MaxItems)
        } yield {
          val minValue = minInclusive.toString.toDouble
          val maxValue = maxInclusive.toString.toDouble
          if (minValue > maxValue) {
            validate(None)
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "minMaxPropertiesValidation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          minInclusive <- element.fields.?[AmfScalar](NodeShapeModel.MinProperties)
          maxInclusive <- element.fields.?[AmfScalar](NodeShapeModel.MaxProperties)
        } yield {
          val minValue = minInclusive.toString.toDouble
          val maxValue = maxInclusive.toString.toDouble
          if (minValue > maxValue) {
            validate(None)
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "minMaxLengthValidation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          minInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.MinLength)
          maxInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.MaxLength)
        } yield {
          val minValue = minInclusive.value.toString.toDouble
          val maxValue = maxInclusive.value.toString.toDouble
          if (minValue > maxValue) {
            validate(None)
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "patternValidation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element.fields.?[AmfScalar](ScalarShapeModel.Pattern).foreach { pattern =>
          try Pattern.compile(pattern.toString.convertRegex)
          catch {
            case _: Throwable =>
              validate(None)
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "xmlWrappedScalar"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element match {
          case scalar: ScalarShape =>
            scalar.fields.?[DomainElement](AnyShapeModel.XMLSerialization) match {
              case Some(xmlSerialization) =>
                xmlSerialization.fields
                  .fields()
                  .find(f => f.field.value.iri().endsWith("xmlWrapped"))
                  .foreach { isWrappedEntry =>
                    val isWrapped = isWrappedEntry.scalar.toBool
                    if (isWrapped) {
                      validate(None)
                    }
                  }
              case _ =>
            }
          case _ =>
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "xmlNonScalarAttribute"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element.fields.getValueAsOption(AnyShapeModel.XMLSerialization) match {
          case Some(xmlSerialization) =>
            xmlSerialization.value match {
              case xmlElement: DomainElement =>
                val xmlAttribute = xmlElement.fields.?[AmfScalar](XMLSerializerModel.Attribute)
                xmlAttribute
                  .foreach { attributeScalar =>
                    val isAttribute = attributeScalar.toBool
                    val isNonScalar = !element.meta.`type`.exists(_.name == "ScalarShape")
                    if (isAttribute && isNonScalar)
                      validate(None)
                  }
              case _ =>
            }
          case None => // Nothing
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "duplicatePropertyNames"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          propertiesArray <- element.fields.?[AmfArray](NodeShapeModel.Properties)
        } yield {
          val properties = propertiesArray.values
          val duplicatedNames = properties
            .map(_.asInstanceOf[PropertyShape])
            .flatMap(_.name.option())
            .legacyGroupBy(identity)
            .filter(_._2.size > 1)
            .keys
            .toSeq
            .sorted
          if (duplicatedNames.nonEmpty) {
            val message = s"Duplicated property names: ${duplicatedNames.mkString(", ")}"
            val info    = ValidationInfo(NodeShapeModel.Properties, Some(message))
            validate(Some(info))
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "discriminatorInRequiredProperties"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          discriminator <- element.fields.?[AmfScalar](NodeShapeModel.Discriminator)
        } yield {
          element match {
            case shape: NodeShape if !isRequiredPropertyInShape(shape, discriminator.value.toString) =>
              validate(Some(ValidationInfo(NodeShapeModel.Discriminator)))
            case _ => // ignore
          }
        }
      }
    }
  )

  private def isRequiredPropertyInShape(shape: NodeShape, name: String) =
    shape.properties.filter(isRequiredProperty).exists { p =>
      p.name.option() match {
        case Some(propertyName) => propertyName == name
        case _                  => false
      }
    }

  private def isRequiredProperty(shape: PropertyShape) = shape.minCount.option().contains(1)

}
