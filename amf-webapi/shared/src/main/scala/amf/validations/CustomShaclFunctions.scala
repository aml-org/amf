package amf.validations

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.services.RuntimeValidator.{CustomShaclFunction, CustomShaclFunctions, ValidationInfo}
import amf.core.utils.RegexConverter
import amf.plugins.document.webapi.validation.runtimeexpression.{AsyncExpressionValidator, Oas3ExpressionValidator}
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.{FileShape, NodeShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.api.BaseApiModel
import amf.plugins.domain.webapi.metamodel.bindings.{BindingHeaders, BindingQuery, HttpMessageBindingModel}
import amf.plugins.domain.webapi.metamodel.security.{
  OAuth2SettingsModel,
  OpenIdConnectSettingsModel,
  SecuritySchemeModel
}
import amf.plugins.domain.webapi.models.IriTemplateMapping
import amf.plugins.domain.webapi.models.security.{OAuth2Settings, OpenIdConnectSettings}

import java.util.regex.Pattern

object CustomShaclFunctions {

  val listOfFunctions: List[CustomShaclFunction] = List(
    new CustomShaclFunction {
      override val name: String = "pathParameterRequiredProperty"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val optBindingValue  = element.fields.?[AmfScalar](ParameterModel.Binding).map(_.value)
        val optRequiredValue = element.fields.?[AmfScalar](ParameterModel.Required).map(_.value)
        (optBindingValue, optRequiredValue) match {
          case (Some("path"), Some(false)) | (Some("path"), None) =>
            validate(None)
          case _ =>
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "fileParameterMustBeInFormData"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element.fields
          .getValueAsOption(ParameterModel.Schema)
          .foreach(field =>
            field.value match {
              case _: FileShape =>
                val optBindingValue = element.fields.?[AmfScalar](ParameterModel.Binding).map(_.value)
                if (optBindingValue.isEmpty || optBindingValue.get != "formData")
                  validate(None)
              case _ => None
          })
      }
    },
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
      override val name: String = "nonEmptyListOfProtocols"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val maybeValue = element.fields.getValueAsOption(BaseApiModel.Schemes)
        maybeValue
          .map(_.value)
          .foreach {
            case AmfArray(elements, _) if elements.isEmpty =>
              validate(Some(ValidationInfo(BaseApiModel.Schemes)))
            case _ =>
          }
      }
    },
    new CustomShaclFunction {
      override val name: String = "exampleMutuallyExclusiveFields"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          _ <- element.fields.getValueAsOption(ExampleModel.StructuredValue)
          _ <- element.fields.getValueAsOption(ExampleModel.ExternalValue)
        } yield {
          validate(None)
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "requiredOpenIdConnectUrl"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
          case OpenIdConnectSettings(fields, _) =>
            if (!fields.exists(OpenIdConnectSettingsModel.Url)) validate(None)
          case _ =>
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "requiredFlowsInOAuth2"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
          case OAuth2Settings(fields, _) =>
            if (!fields.exists(OAuth2SettingsModel.Flows)) validate(None)
          case _ =>
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "validCallbackExpression"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val optExpression = element.fields.getValueAsOption(CallbackModel.Expression).map(_.value)
        validateOas3Expression(optExpression, () => validate(Some(ValidationInfo(CallbackModel.Expression))))
      }
    },
    new CustomShaclFunction {
      override val name: String = "validLinkRequestBody"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val optExpression = element.fields.getValueAsOption(TemplatedLinkModel.RequestBody).map(_.value)
        validateOas3Expression(optExpression, () => validate(Some(ValidationInfo(TemplatedLinkModel.RequestBody))))
      }
    },
    new CustomShaclFunction {
      override val name: String = "validLinkParameterExpressions"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val maybeArray = element.fields.?[AmfArray](TemplatedLinkModel.Mapping)
        maybeArray foreach { array =>
          array.values.foreach {
            case link: IriTemplateMapping =>
              val optExpression: Option[AmfElement] =
                link.fields.getValueAsOption(IriTemplateMappingModel.LinkExpression).map(_.value)
              validateOas3Expression(optExpression, () => validate(Some(ValidationInfo(TemplatedLinkModel.Mapping))))
            case _ =>
          }
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "validCorrelationIdLocation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val optExpression = element.fields.getValueAsOption(CorrelationIdModel.Location).map(_.value)
        validateAsyncExpression(optExpression, () => validate(Some(ValidationInfo(CorrelationIdModel.Location))))
      }
    },
    new CustomShaclFunction {
      override val name: String = "validParameterLocation"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        val location = element.fields.getValueAsOption(ParameterModel.Binding)
        if (location.exists(!_.annotations.contains(classOf[SynthesizedField]))) {
          validateAsyncExpression(location.map(_.value), () => validate(Some(ValidationInfo(ParameterModel.Binding))))
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "mandatoryHeadersObjectNodeWithPropertiesFacet"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          headerElement <- element.fields.?[AmfElement](BindingHeaders.Headers)
        } yield {
          val isObjectAndHasProperties = validateObjectAndHasProperties(headerElement)
          if (!isObjectAndHasProperties) validate(None)
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "mandatoryQueryObjectNodeWithPropertiesFacet"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          queryElement <- element.fields.?[AmfElement](BindingQuery.Query)
        } yield {
          val isObjectAndHasProperties = validateObjectAndHasProperties(queryElement)
          if (!isObjectAndHasProperties) validate(None)
        }
      }
    },
    new CustomShaclFunction {
      override val name: String = "mandatoryHeaderNamePattern"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          headerName <- element.fields ? [AmfScalar] ParameterModel.ParameterName
          binding    <- element.fields ? [AmfScalar] ParameterModel.Binding
        } yield {
          binding.toString match {
            case "header" if isInvalidHttpHeaderName(headerName.toString) => validate(None)
            case _                                                        => // ignore
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
    },
    new CustomShaclFunction {
      override val name: String = "mandatoryHeaderBindingNamePattern"
      override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
        for {
          header <- element.fields.?[Shape](HttpMessageBindingModel.Headers)
        } yield {
          header match {
            case n: NodeShape =>
              n.properties.foreach { p =>
                p.name.option() match {
                  case Some(name) if isInvalidHttpHeaderName(name) =>
                    validate(Some(ValidationInfo(PropertyShapeModel.Name)))
                  case _ => // ignore
                }
              }
            case _ => // ignore
          }
        }
      }
    },
  )

  val functions: CustomShaclFunctions = listOfFunctions.map(f => f.name -> f).toMap

  private def isRequiredPropertyInShape(shape: NodeShape, name: String) =
    shape.properties.filter(isRequiredProperty).exists { p =>
      p.name.option() match {
        case Some(propertyName) => propertyName == name
        case _                  => false
      }
    }

  private def validateObjectAndHasProperties(element: AmfElement) = {
    element match {
      case element: NodeShape =>
        element.properties.exists(p => p.patternName.option().isEmpty) || element.fields.exists(
          NodeShapeModel.Properties)
      case _ => false
    }
  }

  def validateOas3Expression(exp: Option[AmfElement], throwValidation: () => Unit): Unit = exp foreach {
    case exp: AmfScalar =>
      if (!Oas3ExpressionValidator.validate(exp.toString))
        throwValidation()
    case _ =>
  }

  def validateAsyncExpression(exp: Option[AmfElement], throwValidation: () => Unit): Unit = exp foreach {
    case exp: AmfScalar =>
      if (!AsyncExpressionValidator.expression(exp.toString))
        throwValidation()
    case _ =>
  }

  // Obtained from the BNF in: https://tools.ietf.org/html/rfc7230#section-3.2
  private def isInvalidHttpHeaderName(name: String): Boolean =
    !name.matches("^[!#$%&'*\\+\\-\\.^\\_\\`\\|\\~0-9a-zA-Z]+$")

  private def isRequiredProperty(shape: PropertyShape) = shape.minCount.option().contains(1)
}
