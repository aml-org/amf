package amf.validations

import java.util.regex.Pattern

import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar, DomainElement}
import amf.core.parser.Annotations
import amf.core.services.RuntimeValidator.CustomShaclFunctions
import amf.core.utils.RegexConverter
import amf.plugins.document.webapi.validation.Oas3ExpressionValidator
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.{FileShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel.security.{
  OAuth2SettingsModel,
  OpenIdConnectSettingsModel,
  SecuritySchemeModel
}
import amf.plugins.domain.webapi.metamodel.{
  CallbackModel,
  IriTemplateMappingModel,
  ParameterModel,
  TemplatedLinkModel,
  WebApiModel
}
import amf.plugins.domain.webapi.models.IriTemplateMapping
import amf.plugins.domain.webapi.models.security.{OAuth2Settings, OpenIdConnectSettings}

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
    "headerParamNameMustBeAscii" -> ((element, violation) => {
      for {
        name    <- element.fields.?[AmfScalar](ParameterModel.Name)
        binding <- element.fields.?[AmfScalar](ParameterModel.Binding)
      } yield {
        val isAscii: (String) => Boolean = (toMatch) => toMatch.matches("^[\\x00-\\x7F]+$")
        val bindingVal                   = binding.value.toString
        val nameVal                      = name.value.toString
        (bindingVal, nameVal) match {
          case ("header", name) if !isAscii(name) => violation(None)
          case _                                  => // ignore
        }
      }

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
            violation(Some(Annotations(), WebApiModel.Schemes))
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
    }),
    "requiredOpenIdConnectUrl" -> ((element, violation) => {
      element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
        case OpenIdConnectSettings(fields, _) =>
          if (!fields.exists(OpenIdConnectSettingsModel.Url)) violation(None)
        case _ =>
      }
    }),
    "requiredFlowsInOAuth2" -> ((element, violation) => {
      element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
        case OAuth2Settings(fields, _) =>
          if (!fields.exists(OAuth2SettingsModel.Flows)) violation(None)
        case _ =>
      }
    }),
    "validCallbackExpression" -> ((callback, violation) => {
      val optExpression = callback.fields.getValueAsOption(CallbackModel.Expression).map(_.value)
      validateExpression(optExpression, () => violation(Some(Annotations(), CallbackModel.Expression)))
    }),
    "validLinkRequestBody" -> ((link, violation) => {
      val optExpression = link.fields.getValueAsOption(TemplatedLinkModel.RequestBody).map(_.value)
      validateExpression(optExpression, () => violation(Some(Annotations(), TemplatedLinkModel.RequestBody)))

    }),
    "validLinkParameterExpressions" -> ((link, violation) => {
      val maybeArray = link.fields.?[AmfArray](TemplatedLinkModel.Mapping)
      maybeArray foreach { array =>
        array.values.foreach {
          case link: IriTemplateMapping =>
            val optExpression: Option[AmfElement] =
              link.fields.getValueAsOption(IriTemplateMappingModel.LinkExpression).map(_.value)
            validateExpression(optExpression, () => violation(Some(Annotations(), TemplatedLinkModel.Mapping)))
          case _ =>
        }
      }
    })
  )

  def validateExpression(exp: Option[AmfElement], throwValidation: () => Unit): Unit = exp foreach {
    case exp: AmfScalar =>
      if (!Oas3ExpressionValidator.validate(exp.toString))
        throwValidation()
    case _ =>
  }

}
