package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.metamodel.domain.{CallbackModel, CorrelationIdModel, ParameterModel, TemplatedLinkModel}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain._
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.apicontract.internal.validation.runtimeexpression.{AsyncExpressionValidator, Oas3ExpressionValidator}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.bindings.{BindingHeaders, BindingQuery, HttpMessageBindingModel}
import amf.apicontract.internal.metamodel.domain.security.{OAuth2SettingsModel, OpenIdConnectSettingsModel, SecuritySchemeModel}
import amf.plugins.domain.apicontract.metamodel.TemplatedLinkModel
import amf.apicontract.client.scala.model.domain.security.{OAuth2Settings, OpenIdConnectSettings}
import amf.plugins.features.validation.shacl.custom.CustomShaclValidator.CustomShaclFunctions
import amf.shapes.client.scala.domain.models.{FileShape, IriTemplateMapping, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel._

import java.util.regex.Pattern

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
      val maybeValue = element.fields.getValueAsOption(BaseApiModel.Schemes)
      maybeValue
        .map(_.value)
        .foreach {
          case AmfArray(elements, _) if elements.isEmpty =>
            violation(Some(Annotations(), BaseApiModel.Schemes))
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
      validateOas3Expression(optExpression, () => violation(Some(Annotations(), CallbackModel.Expression)))
    }),
    "validLinkRequestBody" -> ((link, violation) => {
      val optExpression = link.fields.getValueAsOption(TemplatedLinkModel.RequestBody).map(_.value)
      validateOas3Expression(optExpression, () => violation(Some(Annotations(), TemplatedLinkModel.RequestBody)))
    }),
    "validLinkParameterExpressions" -> ((link, violation) => {
      val maybeArray = link.fields.?[AmfArray](TemplatedLinkModel.Mapping)
      maybeArray foreach { array =>
        array.values.foreach {
          case link: IriTemplateMapping =>
            val optExpression: Option[AmfElement] =
              link.fields.getValueAsOption(IriTemplateMappingModel.LinkExpression).map(_.value)
            validateOas3Expression(optExpression, () => violation(Some(Annotations(), TemplatedLinkModel.Mapping)))
          case _ =>
        }
      }
    }),
    "validCorrelationIdLocation" -> ((link, violation) => {
      val optExpression = link.fields.getValueAsOption(CorrelationIdModel.Location).map(_.value)
      validateAsyncExpression(optExpression, () => violation(Some(Annotations(), CorrelationIdModel.Location)))
    }),
    "validParameterLocation" -> ((link, violation) => {
      val location = link.fields.getValueAsOption(ParameterModel.Binding)
      if (location.exists(!_.annotations.contains(classOf[SynthesizedField]))) {
        validateAsyncExpression(location.map(_.value), () => violation(Some(Annotations(), ParameterModel.Binding)))
      }
    }),
    "mandatoryHeadersObjectNodeWithPropertiesFacet" -> ((element, violation) => {
      for {
        headerElement <- element.fields.?[AmfElement](BindingHeaders.Headers)
      } yield {
        val isObjectAndHasProperties = validateObjectAndHasProperties(headerElement)
        if (!isObjectAndHasProperties) violation(None)
      }
    }),
    "mandatoryQueryObjectNodeWithPropertiesFacet" -> ((element, violation) => {
      for {
        queryElement <- element.fields.?[AmfElement](BindingQuery.Query)
      } yield {
        val isObjectAndHasProperties = validateObjectAndHasProperties(queryElement)
        if (!isObjectAndHasProperties) violation(None)
      }
    }),
    "mandatoryHeaderNamePattern" -> ((element, violation) => {
      for {
        headerName <- element.fields ? [AmfScalar] ParameterModel.ParameterName
        binding    <- element.fields ? [AmfScalar] ParameterModel.Binding
      } yield {
        binding.toString match {
          case "header" if isInvalidHttpHeaderName(headerName.toString) => violation(None)
          case _                                                        => // ignore
        }
      }
    }),
    "discriminatorInRequiredProperties" -> ((element, violation) => {
      for {
        discriminator <- element.fields.?[AmfScalar](NodeShapeModel.Discriminator)
      } yield {
        element match {
          case shape: NodeShape if !isRequiredPropertyInShape(shape, discriminator.value.toString) =>
            violation(Some(shape.discriminator.annotations(), NodeShapeModel.Discriminator))
          case _ => // ignore
        }
      }
    }),
    "mandatoryHeaderBindingNamePattern" -> ((element, violation) => {
      for {
        header <- element.fields.?[Shape](HttpMessageBindingModel.Headers)
      } yield {
        header match {
          case n: NodeShape =>
            n.properties.foreach { p =>
              p.name.option() match {
                case Some(name) if isInvalidHttpHeaderName(name) =>
                  violation(Some(p.name.annotations(), PropertyShapeModel.Name))
                case _ => // ignore
              }
            }
          case _ => // ignore
        }
      }
    }),
  )

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
