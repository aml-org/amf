package amf.apicontract.internal.validation.shacl

import amf.apicontract.client.scala.model.domain.security.{OAuth2Settings, OpenIdConnectSettings}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.bindings.{BindingHeaders, BindingQuery, HttpMessageBindingModel}
import amf.apicontract.internal.metamodel.domain.security.{
  OAuth2SettingsModel,
  OpenIdConnectSettingsModel,
  SecuritySchemeModel
}
import amf.apicontract.internal.metamodel.domain.{CallbackModel, CorrelationIdModel, ParameterModel, TemplatedLinkModel}
import amf.apicontract.internal.validation.runtimeexpression.{AsyncExpressionValidator, Oas3ExpressionValidator}
import amf.apicontract.internal.validation.shacl.graphql.{
  GraphQLAppliedDirective,
  GraphQLArgumentValidator,
  GraphQLObject
}
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.metamodel.operations.AbstractParameterModel
import amf.shapes.internal.validation.shacl.{BaseCustomShaclFunctions, ShapesCustomShaclFunctions}
import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, ValidationInfo}

object APICustomShaclFunctions extends BaseCustomShaclFunctions {

  override protected[amf] val listOfFunctions: Seq[CustomShaclFunction] =
    (ShapesCustomShaclFunctions.listOfFunctions ++ Seq(
      new CustomShaclFunction {
        override val name: String = "requiredFields"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj                         = GraphQLObject(element.asInstanceOf[NodeShape])
          val fields                      = obj.fields().names
          val requiredFields: Seq[String] = obj.inherits.flatMap(_.fields().names)
          requiredFields.foreach { requiredField =>
            if (!fields.contains(requiredField))
              validate(
                Some(
                  ValidationInfo(
                    NodeShapeModel.Properties,
                    Some(s"field $requiredField is required in ${obj.name}"),
                    Some(obj.annotations)
                  )
                )
              )
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "emptyDefinition"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val node               = element.asInstanceOf[NodeShape]
          val isInterface        = node.isAbstract.value()
          val isInput            = node.isInputOnly.value()
          val isExtensionWrapper = node.and.nonEmpty
          val isSchema           = node.name.isNullOrEmpty
          if (node.properties.isEmpty && node.operations.isEmpty) {
            if (!isExtensionWrapper && !isSchema) {
              val nodeType =
                if (isInterface) "Interface"
                else if (isInput) "Input Type"
                else "Type"
              validate(
                Some(
                  ValidationInfo(
                    NodeShapeModel.Properties,
                    Some(s"$nodeType definitions must have at least one field"),
                    Some(element.annotations)
                  )
                )
              )
            }
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "emptyUnion"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val union     = element.asInstanceOf[UnionShape]
          val isWrapper = union.name.isNullOrEmpty || union.or.nonEmpty
          if (!isWrapper && union.anyOf.isEmpty) validate(None)
        }
      },
      new CustomShaclFunction {
        override val name: String = "emptyEnum"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val enum = element.asInstanceOf[ScalarShape]
          if (enum.fields.exists(ShapeModel.Values) && enum.values.isEmpty) validate(None)
        }
      },
      new CustomShaclFunction {
        override val name: String = "unionInvalidMembers"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val union     = element.asInstanceOf[UnionShape]
          val isWrapper = union.name.isNullOrEmpty || union.or.nonEmpty
          if (!isWrapper) {
            val members = union.anyOf
            val invalidMembers = members.filter {
              case n: NodeShape if n.isAbstract.value() => true  // interfaces
              case any if !any.isInstanceOf[NodeShape]  => true  // not an Object
              case _                                    => false // an Object
            }
            val validationResults = invalidMembers.map { elem =>
              ValidationInfo(
                UnionShapeModel.AnyOf,
                Some(s"All union members must be Object type, ${elem.name.value()} it's not"),
                Some(elem.annotations)
              )
            }
            validationResults.foreach(info => validate(Some(info)))
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueTypeValidation"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj               = GraphQLObject(element.asInstanceOf[NodeShape])
          val validationResults = GraphQLArgumentValidator.validateDefaultValues(obj)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLDirectiveApplicationTypeValidation"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val directive         = GraphQLAppliedDirective(element.asInstanceOf[DomainExtension])
          val validationResults = GraphQLArgumentValidator.validateDirectiveApplicationTypes(directive)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
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
              }
            )
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
      new CustomShaclFunction {
        override val name: String = "invalidIntrospectionName"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case d: CustomDomainProperty =>
              if (hasIntrospectionName(d)) validate(Some(ValidationInfo(CustomDomainPropertyModel.Name)))
            case t: Shape => if (hasIntrospectionName(t)) validate(Some(ValidationInfo(AnyShapeModel.Name)))
            case n: NamedDomainElement =>
              if (hasIntrospectionName(n)) validate(Some(ValidationInfo(NameFieldSchema.Name)))
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedUnionMembers"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case union: UnionShape =>
              val members = union.anyOf
              checkDuplicates(
                members,
                validate,
                UnionShapeModel.AnyOf,
                { name: String => s"Union must have at most one member with name '$name'" }
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedInterfaceImplementations"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case shape: NodeShape =>
              val typeName   = shape.name.option().getOrElse("unnamedType")
              val interfaces = shape.inherits
              checkDuplicates(
                interfaces,
                validate,
                NodeShapeModel.Inherits,
                { name: String => s"$typeName cannot implement interface '$name' more than once" }
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedEnumValues"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case s: ScalarShape =>
              val enumValues = s.values
              checkDuplicates(
                enumValues,
                validate,
                ScalarShapeModel.Values,
                { name: String => s"Each enum value must be unique, '$name' it's not" }
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueTypeValidationDirective"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj               = GraphQLObject(element.asInstanceOf[NodeShape])
          val validationResults = GraphQLArgumentValidator.validateDefaultValues(obj)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueTypeValidationParameter"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val param             = element.asInstanceOf[AbstractParameter]
          val validationResults = GraphQLArgumentValidator.validateDefaultValues(param)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLDirectiveApplicationTypeValidation"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val directive         = GraphQLAppliedDirective(element.asInstanceOf[DomainExtension])
          val validationResults = GraphQLArgumentValidator.validateDirectiveApplicationTypes(directive)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueInValidationDirective"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj = GraphQLObject(element.asInstanceOf[NodeShape])
          val validationResults = obj.properties.flatMap { prop =>
            prop.property.range match {
              case u: UnionShape if u.anyOf.headOption.exists(_.isInstanceOf[NilShape]) =>
                val realRange = u.anyOf.last
                GraphQLArgumentValidator.validateIn(prop.default, realRange.values, ShapeModel.Default)
              case other =>
                GraphQLArgumentValidator.validateIn(prop.default, other.values, ShapeModel.Default)
            }

          }
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueInValidationParameter"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val param = element.asInstanceOf[AbstractParameter]
          val validationResults =
            GraphQLArgumentValidator.validateIn(param.defaultValue, param.schema.values, AbstractParameterModel.Default)
          validationResults.foreach(info => validate(Some(info)))
        }
      }
    ))

  private def validateObjectAndHasProperties(element: AmfElement) = {
    element match {
      case element: NodeShape =>
        element.properties.exists(p => p.patternName.option().isEmpty) || element.fields.exists(
          NodeShapeModel.Properties
        )
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

  private def hasIntrospectionName(element: NamedDomainElement): Boolean =
    element.name.nonNull && element.name.value().startsWith("__")

  def checkDuplicates(
      s: Seq[NamedDomainElement],
      validate: Option[ValidationInfo] => Unit,
      field: Field,
      message: String => String
  ): Unit = {
    s.foreach({ elem =>
      val elemName = elem.name.value()
      if (elemName != null && isDuplicated(elemName, s))
        validate(Some(ValidationInfo(field, Some(message(elemName)), Some(elem.annotations))))
    })
  }
  private def isDuplicated(elemName: String, s: Seq[NamedDomainElement]) = s.count(_.name.value() == elemName) > 1
}
