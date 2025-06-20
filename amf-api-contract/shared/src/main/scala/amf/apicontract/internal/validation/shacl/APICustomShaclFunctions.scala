package amf.apicontract.internal.validation.shacl

import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{IBMMQChannelBinding, IBMMQMessageBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{HasTopicConfiguration, KafkaChannelBinding040}
import amf.apicontract.client.scala.model.domain.security.{
  ApiKeySettings,
  HttpSettings,
  OAuth2Settings,
  OpenIdConnectSettings,
  SecurityScheme
}
import amf.apicontract.client.scala.model.domain.{EndPoint, Request}
import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.metamodel.domain.security.{
  ApiKeySettingsModel,
  HttpSettingsModel,
  OAuth2SettingsModel,
  OpenIdConnectSettingsModel,
  ParametrizedSecuritySchemeModel,
  SecuritySchemeModel
}
import amf.apicontract.internal.validation.runtimeexpression.{AsyncExpressionValidator, Oas3ExpressionValidator}
import amf.apicontract.internal.validation.shacl.graphql._
import amf.apicontract.internal.validation.shacl.graphql.values.ValueValidator
import amf.apicontract.internal.validation.shacl.oas.{
  DuplicatedCommonEndpointPathValidation,
  DuplicatedOas3EndpointPathValidation
}
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.annotations.{LexicalInformation, SynthesizedField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.internal.annotations.{DirectiveArguments, ParsedJSONSchema}
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.metamodel.operations.{AbstractParameterModel, ShapeRequestModel}
import amf.shapes.internal.validation.shacl.{BaseCustomShaclFunctions, ShapesCustomShaclFunctions}
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo.validationInfo
import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, ValidationInfo}

object APICustomShaclFunctions extends BaseCustomShaclFunctions {

  override protected[amf] val listOfFunctions: Seq[CustomShaclFunction] =
    ShapesCustomShaclFunctions.listOfFunctions ++ Seq(
      new CustomShaclFunction {
        override val name: String = "requiredFields"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj = GraphQLObject(element.asInstanceOf[NodeShape])
          GraphQLValidator.validateRequiredFields(obj).foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "validGraphQLInheritance"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val node = element.asInstanceOf[NodeShape]
          node.inherits.foreach {
            case n: NodeShape if n.isAbstract.value() => // ok
            case _: UnresolvedShape                   => // skip, already validated when validating references
            case _: RecursiveShape                    => // skip, already validated when validating cyclic inheritance
            case p =>
              val message = {
                val childName  = node.name.value()
                val parentName = p.name.value()
                s"Type '$childName' cannot implement non-interface type '$parentName'"
              }
              val result = ValidationInfo(NodeShapeModel.Inherits, Some(message), Some(node.annotations))
              validate(Some(result))
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "mandatoryGraphqlNonEmptyEndpoints"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val webapi            = element.asInstanceOf[Api]
          val hasEndpointsField = webapi.fields.exists(BaseApiModel.EndPoints)
          val hasEndpoints      = webapi.endPoints.nonEmpty
          if (!hasEndpoints && !hasEndpointsField) {
            validate(
              Some(
                ValidationInfo(
                  BaseApiModel.EndPoints,
                  Some("Must have 'schema' node or 'Query', 'Mutation' or 'Subscription' types"),
                  Some(element.annotations)
                )
              )
            )
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "reservedEndpoints"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val reserved  = Set("_service", "_entities")
          val api       = element.asInstanceOf[WebApi]
          val endpoints = api.endPoints
          endpoints.foreach { endpoint =>
            endpoint.path
              .option()
              .map(_.stripPrefix("/query/").stripPrefix("/mutation/").stripPrefix("/subscription/"))
              .flatMap {
                case path if reserved.contains(path) =>
                  val rootKind = {
                    val name = endpoint.name.value()
                    val end  = name.indexOf(".")
                    name.substring(0, end)
                  }
                  Some(
                    ValidationInfo(
                      EndPointModel.Path,
                      Some(s"Cannot declare field '$path' in type $rootKind since it is reserved by Federation"),
                      Some(element.annotations)
                    )
                  )
                case _ => None
              }
              .foreach(res => validate(Some(res)))
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "reservedTypeNames"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val reserved = Set("_Any", "FieldSet", "link__Import", "link__Purpose", "_Entity", "_Service")
          val shape    = element.asInstanceOf[AnyShape]
          shape.name
            .option()
            .flatMap {
              case name if reserved.contains(name) =>
                val kind = shape match {
                  case s: ScalarShape if s.values.nonEmpty   => "enum"
                  case _: ScalarShape                        => "scalar"
                  case n: NodeShape if n.isAbstract.value()  => "interface"
                  case n: NodeShape if n.isInputOnly.value() => "input object"
                  case _: NodeShape                          => "object"
                  case _: UnionShape                         => "union"
                  case _                                     => "type" // should be unreachable
                }
                Some(
                  ValidationInfo(
                    ScalarShapeModel.Name,
                    Some(s"Cannot declare $kind with name '$name' since it is reserved by Federation"),
                    Some(element.annotations)
                  )
                )
              case _ => None
            }
            .foreach(res => validate(Some(res)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "requiresExternal"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val prop = element.asInstanceOf[PropertyShape]
          prop.requires
            .map(_.path.last) // only the last should be @external
            .filter(!_.isStub.value())
            .map(prop =>
              ValidationInfo(
                PropertyShapeModel.Requires,
                Some(s"'${prop.name}' should be declared as @external to be used in @requires"),
                Some(element.annotations)
              )
            )
            .foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "keyDirectiveValidations"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val results = GraphQLValidator.validateKeyDirective(element.asInstanceOf[NodeShape])
          results.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "providesExternal"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val prop = element.asInstanceOf[PropertyShape]
          prop.provides
            .map(_.path.last) // only the last should be @external
            .filter(!_.isStub.value())
            .map(prop =>
              ValidationInfo(
                PropertyShapeModel.Provides,
                Some(s"'${prop.name}' should be declared as @external to be used in @provides"),
                Some(element.annotations)
              )
            )
            .foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "invalidOutputTypeInEndpoint"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val endpoint = GraphQLEndpoint(element.asInstanceOf[EndPoint])
          val results  = GraphQLValidator.validateOutputTypes(endpoint)
          results.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "invalidOutputType"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj     = GraphQLObject(element.asInstanceOf[NodeShape])
          val results = GraphQLValidator.validateOutputTypes(obj)
          results.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "invalidInputTypeInEndpoint"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val endpoint = GraphQLEndpoint(element.asInstanceOf[EndPoint])
          val results  = GraphQLValidator.validateInputTypes(endpoint)
          results.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "invalidInputType"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val obj     = GraphQLObject(element.asInstanceOf[NodeShape])
          val results = GraphQLValidator.validateInputTypes(obj)
          results.foreach(info => validate(Some(info)))
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
                validationInfo(
                  NodeShapeModel.Properties,
                  s"$nodeType definitions must have at least one field",
                  element.annotations
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
              case n: NodeShape if n.isAbstract.value()  => true // interfaces
              case n: NodeShape if n.isInputOnly.value() => true // input objects
              case r: RecursiveShape => r.fixpointTarget.exists(_.isInstanceOf[NodeShape]) // input objects
              case _: UnresolvedShape => false // unresolved shapes are already validated when resolving references
              case any if !any.isInstanceOf[NodeShape] => true  // not an Object
              case _                                   => false // an Object
            }
            val validationResults = invalidMembers.map { elem =>
              ValidationInfo(
                UnionShapeModel.AnyOf,
                Some(s"'${elem.name.value()}' is not a valid union member (only Object types)"),
                Some(union.annotations)
              )
            }
            validationResults.foreach(info => validate(Some(info)))
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLDirectiveApplicationTypeValidation"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val directive         = GraphQLAppliedDirective(element.asInstanceOf[DomainExtension])
          val validationResults = GraphQLValidator.validateDirectiveApplication(directive)
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
        override val name: String = "pathParameterDefinitionCheck"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case webApi: WebApi =>
              webApi.endPoints.foreach { endpoint =>
                val path    = endpoint.path.value()
                val pattern = """\{([^}]+)\}""".r
                val pathParams = pattern.findAllMatchIn(path).map(_.group(1)).toList
                pathParams.foreach { pathParam =>
                  val operationsParams = endpoint.operations.flatMap(_.requests).flatMap(_.uriParameters)
                  val allPathParams = (endpoint.parameters ++ operationsParams)
                    .filter(_.binding.value().equals("path"))
                    .map(_.name.value())
                  if (!allPathParams.contains(pathParam))
                    validate(
                      validationInfo(
                        RequestModel.UriParameters,
                        s"Path parameter $pathParam must be defined in the parameters object",
                        endpoint.annotations
                      )
                    )
                }
              }
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
        override val name: String = "requiredHttpScheme"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
            case HttpSettings(fields, _) =>
              if (!fields.exists(HttpSettingsModel.Scheme)) validate(None)
            case _ =>
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "requiredApiKeyName"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
            case ApiKeySettings(fields, _) =>
              if (!fields.exists(ApiKeySettingsModel.Name)) validate(None)
            case _ =>
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "requiredApiKeyIn"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element.fields.getValueAsOption(SecuritySchemeModel.Settings).map(_.value) foreach {
            case ApiKeySettings(fields, _) =>
              if (!fields.exists(ApiKeySettingsModel.In)) validate(None)
            case _ =>
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
            case t: Shape =>
              if (hasIntrospectionName(t)) validate(Some(ValidationInfo(AnyShapeModel.Name)))
            case n: NamedDomainElement =>
              if (hasIntrospectionName(n)) {
                validate(Some(ValidationInfo(NameFieldSchema.Name)))
              }
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
                { name: String => s"Union must have at most one member with name '$name'" },
                Some(union.annotations)
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
                { name: String => s"$typeName cannot implement interface '$name' more than once" },
                Some(shape.annotations)
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
                { name: String => s"Each enum value must be unique, '$name' it's not" },
                Some(s.annotations)
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueTypeValidationDirective"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val node = element.asInstanceOf[NodeShape]
          val results = node.properties.flatMap { property =>
            val expected = property.range
            val actual   = property.default
            ValueValidator.validate(expected, actual)(ShapeModel.Default)
          }
          results.foreach(result => validate(Some(result)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "GraphQLArgumentDefaultValueTypeValidationParameter"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val param = element.asInstanceOf[AbstractParameter]
          val validationResults =
            ValueValidator.validate(param.schema, param.defaultValue)(AbstractParameterModel.Default)
          validationResults.foreach(info => validate(Some(info)))
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedDirectiveApplication"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case s: DomainElement =>
              val directiveApplications                                             = s.customDomainProperties
              def directiveHasLocationsToBeAppliedOn(ext: DomainExtension): Boolean = Option(ext.definedBy).isDefined
              def shouldSkip(element: NamedDomainElement): Boolean = {
                element match {
                  case ext: DomainExtension =>
                    directiveHasLocationsToBeAppliedOn(ext) && ext.definedBy.repeatable.value()
                  case _ => false
                }
              }
              checkDuplicates(
                directiveApplications,
                validate,
                ShapeModel.CustomDomainProperties,
                { directiveName: String => s"Directive '$directiveName' can only be applied once per location" },
                Some(s.annotations),
                shouldSkip
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedField"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val isDirectiveArgumentsShape = element.annotations.contains(classOf[DirectiveArguments])
          element match {
            case obj: NodeShape if !isDirectiveArgumentsShape =>
              val fields = obj.properties ++ obj.operations
              checkDuplicates(
                fields,
                validate,
                NodeShapeModel.Properties,
                { fieldName: String => s"Cannot exist two or more fields with name '$fieldName'" },
                Some(obj.annotations)
              )

            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedArgumentField"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case obj: NodeShape =>
              obj.operations.foreach({ op =>
                val arguments = op.request.queryParameters
                checkDuplicates(
                  arguments,
                  validate,
                  ShapeRequestModel.QueryParameters,
                  { argumentName: String => s"Cannot exist two or more arguments with name '$argumentName'" },
                  Some(op.annotations)
                )
              })
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "duplicatedArgumentDirective"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case directive: CustomDomainProperty =>
              val arguments = directive.schema.asInstanceOf[NodeShape].properties
              checkDuplicates(
                arguments,
                validate,
                NodeShapeModel.Properties,
                { argumentName: String => s"Cannot exist two or more arguments with name '$argumentName'" },
                Some(directive.annotations)
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "invalidDirectiveApplication"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val isDirectiveArgument = element.annotations.contains(classOf[DirectiveArguments])
          element match {
            case directive: CustomDomainProperty =>
              val arguments = directive.schema.asInstanceOf[NodeShape].properties
              val results = arguments.flatMap({ arg =>
                val directiveApplications = arg.customDomainProperties
                GraphQLDirectiveLocationValidator(directiveApplications, arg, appliedToDirectiveArgument = true)
              })
              results.foreach(validate(_))
            case elem: DomainElement if !isDirectiveArgument =>
              val directiveApplications = elem.customDomainProperties
              val results               = GraphQLDirectiveLocationValidator(directiveApplications, elem)
              results.foreach(validate(_))
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "exclusiveQueryStringAndQueryParametersProperties"

        private def reportIfExclusiveFieldsDefined(
            elem: DomainElement,
            field: Field,
            validate: Option[ValidationInfo] => Unit
        ): Unit = {
          val queryString    = elem.fields.getValueAsOption(ParametersFieldModel.QueryString)
          val queryParameter = elem.fields.getValueAsOption(ParametersFieldModel.QueryParameters)
          if (queryString.isDefined && queryParameter.isDefined) {
            val hasLexical  = elem.annotations.contains(classOf[LexicalInformation])
            val annotations = if (hasLexical) elem.annotations else queryString.get.annotations
            validate(exclusiveFieldsError(field, annotations))
          }
        }

        private def exclusiveFieldsError(field: Field, annotations: Annotations): Option[ValidationInfo] = {
          Option(
            ValidationInfo(
              field,
              Option(
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be specified, explicitly or implicitly, on the same method of the same resource."
              ),
              Option(annotations)
            )
          )
        }

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case request: Request => reportIfExclusiveFieldsDefined(request, OperationModel.Request, validate)
            case scheme: SecurityScheme =>
              reportIfExclusiveFieldsDefined(scheme, ParametrizedSecuritySchemeModel.Scheme, validate)
            case _ => // ignore
          }
        }
      },
      DuplicatedOas3EndpointPathValidation(),
      DuplicatedCommonEndpointPathValidation(),
      new CustomShaclFunction {
        override val name: String = "inheritanceFromJsonSchema"

        def isJsonSchema(s: Shape): Boolean = s.annotations.contains(classOf[ParsedJSONSchema])

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case s: ScalarShape if s.inherits.nonEmpty =>
              s.inherits
                .filter(isJsonSchema)
                .foreach(s =>
                  validate(
                    validationInfo(
                      ShapeModel.Inherits,
                      s"JSON Schemas can't be used in type expressions or type inheritance",
                      s.annotations
                    )
                  )
                )

            case u: UnionShape if u.anyOf.nonEmpty =>
              u.anyOf
                .filter(isJsonSchema)
                .foreach(s =>
                  validate(
                    validationInfo(
                      UnionShapeModel.AnyOf,
                      s"${s.name} is a JSON Schema and can't be used in type expressions or type inheritance",
                      u.annotations
                    )
                  )
                )

            case arr: ArrayShape if Option(arr.items).isDefined =>
              if (isJsonSchema(arr.items))
                validate(
                  validationInfo(
                    ArrayShapeModel.Items,
                    s"JSON Schemas can't be used in type expressions or type inheritance",
                    arr.annotations
                  )
                )

            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "anypointMQHeadersValidation"

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {

          element.asInstanceOf[AnypointMQMessageBinding].headers match {
            case node: NodeShape =>
              node.fields.?[AmfArray](NodeShapeModel.Properties) match {
                case Some(_) => // ignore
                case None =>
                  validate(
                    validationInfo(
                      AnypointMQMessageBindingModel.Headers,
                      "AnypointMQ Message Binding 'headers' field must have a 'properties' field",
                      element.annotations
                    )
                  )
              }

            case elem if elem != null =>
              validate(
                validationInfo(
                  AnypointMQMessageBindingModel.Headers,
                  "AnypointMQ Message Binding 'headers' field must be an object",
                  elem.annotations
                )
              )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "IBMMQDestinationValidation"

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val binding = element.asInstanceOf[IBMMQChannelBinding]
          if (binding.topic != null && binding.queue != null) {
            validate(
              validationInfo(
                IBMMQChannelBindingModel.Queue,
                "'queue' and 'topic' fields MUST NOT coexist within an IBMMQ channel binding",
                element.annotations
              )
            )
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "IBMMQHeadersValidation"

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val binding = element.asInstanceOf[IBMMQMessageBinding]
          if (Seq("string", "jms").contains(binding.messageType.value()) && binding.headers.nonEmpty) {
            validate(
              validationInfo(
                IBMMQMessageBindingModel.Headers,
                "IBMMQ message Binding 'headers' MUST NOT be specified if 'type' field is 'string' or 'jms'",
                element.annotations
              )
            )
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "IBMMQMaxMsgLengthValidation"

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          val binding             = element.asInstanceOf[IBMMQChannelBinding]
          val length              = binding.maxMsgLength.value()
          val isMaxMsgLengthValid = 0 <= length && length <= 104857600
          if (!isMaxMsgLengthValid) {
            validate(
              validationInfo(
                IBMMQChannelBindingModel.MaxMsgLength,
                "IBMMQ channel Binding 'maxMsgLength' field must be a number between 0-104857600 (100MB)",
                element.annotations
              )
            )
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "KafkaTopicConfigurationValidations"

        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element match {
            case hasTopic: HasTopicConfiguration =>
              val topicConfiguration = hasTopic.topicConfiguration
              val cleanupPolicy      = topicConfiguration.cleanupPolicy.map(_.value())
              if (cleanupPolicy.nonEmpty && cleanupPolicy.intersect(Seq("delete", "compact")).isEmpty)
                validate(
                  validationInfo(
                    KafkaTopicConfigurationModel.CleanupPolicy,
                    "Kafka Topic Configuration 'cleanup.policy' field can only contain 'delete' and/or 'compact'.",
                    topicConfiguration.annotations
                  )
                )
              if (topicConfiguration.retentionMs.value() < -1)
                validate(
                  validationInfo(
                    KafkaTopicConfigurationModel.RetentionMs,
                    "Kafka Topic Configuration 'retention.ms' field valid values are [-1,...]",
                    topicConfiguration.annotations
                  )
                )
              if (topicConfiguration.deleteRetentionMs.value() < 0)
                validate(
                  validationInfo(
                    KafkaTopicConfigurationModel.DeleteRetentionMs,
                    "Kafka topic configuration 'delete.retention.ms' field must be a positive number",
                    topicConfiguration.annotations
                  )
                )
              if (topicConfiguration.maxMessageBytes.value() < 0)
                validate(
                  validationInfo(
                    KafkaTopicConfigurationModel.MaxMessageBytes,
                    "Kafka topic configuration 'max.message.bytes' field must be a positive number",
                    topicConfiguration.annotations
                  )
                )
            case _ => // ignore
          }
        }
      },
      new CustomShaclFunction {
        override val name: String = "schemaVersionNotImplemented"
        override def run(element: AmfObject, validate: Option[ValidationInfo] => Unit): Unit = {
          element.fields.getValueAsOption(AnyShapeModel.SchemaVersion) foreach { value =>
            if (!value.isInferred)
              validate(
                validationInfo(
                  AnyShapeModel.SchemaVersion,
                  "$schema support in OAS 3.1 is still not implemented",
                  element.annotations
                )
              )
          }
        }
      }
    )

  private def validateObjectAndHasProperties(element: AmfElement): Boolean = {
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
    !name.matches("^[!#$%&'*+\\-.^_`|~0-9a-zA-Z]+$")

  private def hasIntrospectionName(element: NamedDomainElement): Boolean =
    element.name.nonNull && element.name.value().startsWith("__")

  def checkDuplicates(
      s: Seq[NamedDomainElement],
      validate: Option[ValidationInfo] => Unit,
      field: Field,
      message: String => String,
      annotations: Some[Annotations],
      shouldSkip: NamedDomainElement => Boolean = _ => false
  ): Unit = {
    s.foreach({ elem =>
      val elemName = elem.name.value()
      if (elemName != null && !shouldSkip(elem) && isDuplicated(elemName, s))
        validate(Some(ValidationInfo(field, Some(message(elemName)), annotations)))
    })
  }
  private def isDuplicated(elemName: String, s: Seq[NamedDomainElement]) = s.count(_.name.value() == elemName) > 1
}
