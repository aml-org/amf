package amf.validations

import java.util.regex.Pattern

import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.Annotations
import amf.core.services.RuntimeValidator.CustomShaclFunctions
import amf.core.utils.RegexConverter
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.{FileShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel.{ParameterModel, WebApiModel}

object CustomShaclFunctions {

  val functions: CustomShaclFunctions = Map(
    "pathParameterRequiredProperty" -> ((element, violation) => {
      val optBindingValue  = element.fields.?[AmfScalar](ParameterModel.Binding).map(_.value)
      val optRequiredValue = element.fields.?[AmfScalar](ParameterModel.Required).map(_.value)
      (optBindingValue, optRequiredValue) match {
        case (Some("path"), Some(false)) | (Some("path"), None) =>
          violation(None)
        case _ =>
      }
    }),
    "fileParameterMustBeInFormData" -> ((element, violation) => {
      element.fields
        .getValueAsOption(ParameterModel.Schema)
        .foreach(field =>
          field.value match {
            case _: FileShape =>
              val optBindingValue = element.fields.?[AmfScalar](ParameterModel.Binding).map(_.value)
              if (optBindingValue.isEmpty || optBindingValue.get != "formData")
                violation(None)
            case _ => None
        })
    }),
    "minimumMaximumValidation" ->
      ((element, violation) => {
        for {
          minInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.Minimum)
          maxInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.Maximum)
        } yield {
          val minValue = minInclusive.toString.toDouble
          val maxValue = maxInclusive.toString.toDouble
          if (minValue > maxValue) {
            violation(None)
          }
        }
      }),
    "minMaxItemsValidation" -> ((element, violation) => {
      for {
        minInclusive <- element.fields.?[AmfScalar](ArrayShapeModel.MinItems)
        maxInclusive <- element.fields.?[AmfScalar](ArrayShapeModel.MaxItems)
      } yield {
        val minValue = minInclusive.toString.toDouble
        val maxValue = maxInclusive.toString.toDouble
        if (minValue > maxValue) {
          violation(None)
        }
      }
    }),
    "minMaxPropertiesValidation" -> ((element, violation) => {
      for {
        minInclusive <- element.fields.?[AmfScalar](NodeShapeModel.MinProperties)
        maxInclusive <- element.fields.?[AmfScalar](NodeShapeModel.MaxProperties)
      } yield {
        val minValue = minInclusive.toString.toDouble
        val maxValue = maxInclusive.toString.toDouble
        if (minValue > maxValue) {
          violation(None)
        }
      }
    }),
    "minMaxLengthValidation" -> ((element, violation) => {
      for {
        minInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.MinLength)
        maxInclusive <- element.fields.?[AmfScalar](ScalarShapeModel.MaxLength)
      } yield {
        val minValue = minInclusive.value.toString.toDouble
        val maxValue = maxInclusive.value.toString.toDouble
        if (minValue > maxValue) {
          violation(None)
        }
      }
    }),
    "xmlWrappedScalar" -> ((element, violation) => {
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
                    violation(None)
                  }
                }
            case _ =>
          }
        case _ =>
      }
    }),
    "xmlNonScalarAttribute" -> ((element, violation) => {
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
                    violation(None)
                }
            case _ =>
          }
        case None => // Nothing
      }
    }),
    "patternValidation" -> ((element, violation) => {
      element.fields.?[AmfScalar](ScalarShapeModel.Pattern).foreach { pattern =>
        try Pattern.compile(pattern.toString.convertRegex)
        catch {
          case _: Throwable =>
            violation(None)
        }
      }
    }),
    "nonEmptyListOfProtocols" -> ((element, violation) => {
      val maybeValue = element.fields.getValueAsOption(WebApiModel.Schemes)
      maybeValue
        .map(_.value)
        .foreach {
          case AmfArray(elements, _) if elements.isEmpty =>
            violation(Some(maybeValue.map(_.annotations).getOrElse(Annotations()), WebApiModel.Schemes))
          case _ =>
        }
    }),
    "exampleMutuallyExclusiveFields" -> ((element, violation) => {
      for {
        _ <- element.fields.getValueAsOption(ExampleModel.StructuredValue)
        _ <- element.fields.getValueAsOption(ExampleModel.ExternalValue)
      } yield {
        violation(None)
      }
    })
  )

}
