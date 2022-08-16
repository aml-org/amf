package amf.apicontract.internal.spec.common

import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.client.scala.model.document.DataTypeFragment
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.bindings.{
  ChannelBindings,
  MessageBindings,
  OperationBindings,
  ServerBindings
}
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.bindings.{
  ChannelBindingsModel,
  MessageBindingsModel,
  OperationBindingsModel,
  ServerBindingsModel
}
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.templates.{ResourceTypeModel, TraitModel}
import amf.apicontract.internal.spec.common.WebApiDeclarations._
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{DataNode, DomainElement, ObjectNode, Shape}
import amf.core.client.scala.parse.document.EmptyFutureDeclarations
import amf.core.internal.annotations.{DeclaredElement, DeclaredHeader, ErrorDeclaration}
import amf.core.internal.parser.domain.SearchScope.All
import amf.core.internal.parser.domain._
import amf.core.internal.utils.QName
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.domain.metamodel.CreativeWorkModel
import amf.shapes.internal.spec.common.error.ErrorNamedExample
import amf.shapes.internal.spec.common.parser.ShapeDeclarations
import org.yaml.model.{YNode, YPart}

/** Declarations object.
  */
class WebApiDeclarations(
    alias: Option[String],
    val errorHandler: AMFErrorHandler,
    val futureDeclarations: FutureDeclarations,
    extractor: QualifiedNameExtractor
) extends ShapeDeclarations(alias, errorHandler, futureDeclarations = futureDeclarations, extractor) {

  var resourceTypes: Map[String, ResourceType]          = Map()
  var parameters: Map[String, Parameter]                = Map()
  var payloads: Map[String, Payload]                    = Map()
  var traits: Map[String, Trait]                        = Map()
  var securitySchemes: Map[String, SecurityScheme]      = Map()
  var responses: Map[String, Response]                  = Map()
  var requests: Map[String, Request]                    = Map()
  var headers: Map[String, Parameter]                   = Map()
  var links: Map[String, TemplatedLink]                 = Map()
  var correlationIds: Map[String, CorrelationId]        = Map()
  var callbacks: Map[String, List[Callback]]            = Map()
  var messages: Map[String, Message]                    = Map()
  var messageBindings: Map[String, MessageBindings]     = Map()
  var operationBindings: Map[String, OperationBindings] = Map()
  var channelBindings: Map[String, ChannelBindings]     = Map()
  var serverBindings: Map[String, ServerBindings]       = Map()
  var operationTraits: Map[String, Operation]           = Map()
  var messageTraits: Map[String, Message]               = Map()
  var others: Map[String, BaseUnit]                     = Map()

  override def addLibrary(alias: String, declarations: Declarations): Unit = {
    libraries = libraries + (alias -> declarations)
  }

  def setLibraries(libraries: Map[String, Declarations]): Unit = {
    this.libraries = libraries
  }

  protected def mergeParts(other: WebApiDeclarations, merged: WebApiDeclarations): Unit = {
    libraries.foreach { case (k, s) => merged.addLibrary(k, s) }
    other.libraries.foreach { case (k, s) => merged.addLibrary(k, s) }
    fragments.foreach { case (k, s) => merged.fragments += (k -> s) }
    other.fragments.foreach { case (k, s) => merged.fragments += (k -> s) }
    shapes.foreach { case (k, s) => merged.shapes += (k -> s) }
    other.shapes.foreach { case (k, s) => merged.shapes += (k -> s) }
    annotations.foreach { case (k, s) => merged.annotations += (k -> s) }
    other.annotations.foreach { case (k, s) => merged.annotations += (k -> s) }
    resourceTypes.foreach { case (k, s) => merged.resourceTypes += (k -> s) }
    other.resourceTypes.foreach { case (k, s) => merged.resourceTypes += (k -> s) }
    parameters.foreach { case (k, s) => merged.parameters += (k -> s) }
    other.parameters.foreach { case (k, s) => merged.parameters += (k -> s) }
    payloads.foreach { case (k, s) => merged.payloads += (k -> s) }
    other.payloads.foreach { case (k, s) => merged.payloads += (k -> s) }
    traits.foreach { case (k, s) => merged.traits += (k -> s) }
    other.traits.foreach { case (k, s) => merged.traits += (k -> s) }
    securitySchemes.foreach { case (k, s) => merged.securitySchemes += (k -> s) }
    other.securitySchemes.foreach { case (k, s) => merged.securitySchemes += (k -> s) }
    responses.foreach { case (k, s) => merged.responses += (k -> s) }
    other.responses.foreach { case (k, s) => merged.responses += (k -> s) }
    extensions.foreach { case (k, s) => merged.extensions = merged.extensions + (k -> s) }
  }

  def merge(other: WebApiDeclarations): WebApiDeclarations = {
    val merged =
      new WebApiDeclarations(
        alias = alias,
        errorHandler = errorHandler,
        futureDeclarations = EmptyFutureDeclarations(),
        extractor
      )
    mergeParts(other, merged)
    merged
  }

  override def copy(): WebApiDeclarations = {
    val next =
      super.copy(
        new WebApiDeclarations(alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations, extractor)
      )
    libraries.foreach(entry => next.addLibrary(entry._1, entry._2))
    next.resourceTypes = resourceTypes
    next.parameters = parameters
    next.payloads = payloads
    next.traits = traits
    next.securitySchemes = securitySchemes
    next.responses = responses
    next.requests = requests
    next.headers = headers
    next.links = links
    next.correlationIds = correlationIds
    next.callbacks = callbacks
    next.messages = messages
    next.messageBindings = messageBindings
    next.operationBindings = operationBindings
    next.channelBindings = channelBindings
    next.serverBindings = serverBindings
    next.operationTraits = operationTraits
    next.messageTraits = next.messageTraits
    next.others = others
    next
  }

  override def +=(element: DomainElement): WebApiDeclarations = {
    // future declarations are used for shapes, and therefore only resolved for that case
    element match {
      case r: ResourceType =>
        resourceTypes = resourceTypes + (r.name.value() -> r)
      case t: Trait =>
        traits = traits + (t.name.value() -> t)
      case h: Parameter if h.annotations.contains(classOf[DeclaredHeader]) =>
        headers = headers + (h.name.value() -> h)
      case p: Parameter =>
        parameters = parameters + (p.name.value() -> p)
      case p: Payload =>
        payloads = payloads + (p.name.value() -> p)
      case ss: SecurityScheme =>
        securitySchemes = securitySchemes + (ss.name.value() -> ss)
      case re: Response =>
        responses = responses + (re.name.value() -> re)
      case rq: Request =>
        requests = requests + (rq.name.value() -> rq)
      case l: TemplatedLink =>
        links = links + (l.name.value() -> l)
      case l: CorrelationId =>
        correlationIds = correlationIds + (l.name.value() -> l)
      case m: Message =>
        if (m.isAbstract.value()) messageTraits = messageTraits + (m.name.value() -> m)
        else messages = messages + (m.name.value()                                -> m)
      case o: Operation if o.isAbstract.value() =>
        operationTraits = operationTraits + (o.name.value() -> o)
      case b: MessageBindings =>
        messageBindings = messageBindings + (b.name.value() -> b)
      case b: ServerBindings =>
        serverBindings = serverBindings + (b.name.value() -> b)
      case b: ChannelBindings =>
        channelBindings = channelBindings + (b.name.value() -> b)
      case b: OperationBindings =>
        operationBindings = operationBindings + (b.name.value() -> b)

      case c: Callback =>
        val name = c.name.value()
        callbacks.get(name) match {
          case Some(prev) => callbacks = callbacks + (name -> (c :: prev))
          case None       => callbacks = callbacks + (name -> List(c))
        }
      case _ => super.+=(element)
    }
    this
  }

  /** Find domain element with the same name. */
  override def findEquivalent(element: DomainElement): Option[DomainElement] = element match {
    case r: ResourceType    => findResourceType(r.name.value(), SearchScope.All)
    case t: Trait           => findTrait(t.name.value(), SearchScope.All)
    case s: Shape           => findType(s.name.value(), SearchScope.All)
    case p: Parameter       => findParameter(p.name.value(), SearchScope.All)
    case ss: SecurityScheme => findSecurityScheme(ss.name.value(), SearchScope.All)
    case re: Response       => findResponse(re.name.value(), SearchScope.All)
    case _                  => super.findEquivalent(element)
  }

  def registerOasParameter(oasParameter: OasParameter): Unit = {
    oasParameter.domainElement.add(DeclaredElement())
    this += oasParameter.domainElement
  }

  def registerHeader(header: Parameter): Unit = {
    this.headers = headers + (header.name.value() -> header)
  }

  def parameterPayload(parameter: Parameter): Payload = payloads(parameter.name.value())

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): WebApiDeclarations = {
    libraries.get(alias) match {
      case Some(lib: WebApiDeclarations) => lib
      case _ =>
        val result = new WebApiDeclarations(
          Some(alias),
          errorHandler = errorHandler,
          futureDeclarations = EmptyFutureDeclarations(),
          extractor
        )
        addLibrary(alias, result)
        result
    }
  }

  override def declarables(): Seq[DomainElement] =
    super
      .declarables()
      .toList ++ (shapes.values ++ resourceTypes.values ++ traits.values ++ parameters.values ++ payloads.values ++ securitySchemes.values ++ responses.values ++ examples.values ++ requests.values ++ links.values ++ callbacks.values.flatten ++ headers.values ++ correlationIds.values ++ messageBindings.values ++ operationBindings.values ++ channelBindings.values ++ serverBindings.values ++ messages.values ++ operationTraits.values ++ messageTraits.values).toList

  def findParameterOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Parameter =
    findParameter(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"Parameter '$key' not found", ast.location)
        ErrorParameter(key, ast)
    }

  def findParameter(key: String, scope: SearchScope.Scope): Option[Parameter] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].parameters, scope) collect { case p: Parameter =>
      p
    }

  def findPayload(key: String, scope: SearchScope.Scope): Option[Payload] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].payloads, scope) collect { case p: Payload =>
      p
    }

  def findRequestBody(key: String, scope: SearchScope.Scope): Option[Request] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].requests, scope) collect { case r: Request =>
      r
    }

  def findResponse(key: String, scope: SearchScope.Scope): Option[Response] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].responses, scope) collect { case r: Response =>
      r
    }

  def findTemplatedLink(key: String, scope: SearchScope.Scope): Option[TemplatedLink] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].links, scope) collect { case l: TemplatedLink =>
      l
    }

  def findHeader(key: String, scope: SearchScope.Scope): Option[Parameter] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].headers, scope) collect { case p: Parameter =>
      p
    }

  def findCorrelationId(key: String, scope: SearchScope.Scope): Option[CorrelationId] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].correlationIds, scope) collect { case c: CorrelationId =>
      c
    }

  def findServerBindings(key: String, scope: SearchScope.Scope): Option[ServerBindings] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].serverBindings, scope) collect { case c: ServerBindings =>
      c
    }

  def findOperationBindings(key: String, scope: SearchScope.Scope): Option[OperationBindings] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].operationBindings, scope) collect { case c: OperationBindings =>
      c
    }

  def findChannelBindings(key: String, scope: SearchScope.Scope): Option[ChannelBindings] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].channelBindings, scope) collect { case c: ChannelBindings =>
      c
    }

  def findMessageBindings(key: String, scope: SearchScope.Scope): Option[MessageBindings] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].messageBindings, scope) collect { case c: MessageBindings =>
      c
    }

  def findMessage(key: String, scope: SearchScope.Scope): Option[Message] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].messages, scope) collect { case m: Message =>
      m
    }

  def findOperationTrait(key: String, scope: SearchScope.Scope): Option[Operation] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].operationTraits, scope) collect { case o: Operation =>
      o
    }

  def findDialect(key: String): Option[Dialect] = {
    val fqn = QName(key)
    if (fqn.isQualified) {
      val maybeDeclarations: Option[WebApiDeclarations] = libraries
        .get(fqn.qualification)
        .collect { case decl: WebApiDeclarations => decl }
      maybeDeclarations.flatMap(_.findDialect(fqn.name))
    } else extensions.get(key)
  }

  def findMessageTrait(key: String, scope: SearchScope.Scope): Option[Message] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].messageTraits, scope) collect { case o: Message =>
      o
    }

  def findCallbackInDeclarations(key: String, scope: SearchScope.Scope = All): Option[List[Callback]] =
    findForType(
      key,
      _.asInstanceOf[WebApiDeclarations].callbacks.map(tuple => (tuple._1 -> tuple._2.head)),
      scope
    ) match {
      case Some(c: Callback) => Some(List(c))
      case other             => None
    }

  def findResourceTypeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): ResourceType =
    findResourceType(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"ResourceType $key not found", ast.location)
        ErrorResourceType(key, ast)
    }

  def findResourceType(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit] = None
  ): Option[ResourceType] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].resourceTypes, scope) match {
      case Some(r: ResourceType) => Some(r)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type ResourceType")
        )
        None
      case _ => None
    }

  def findTraitOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Trait = findTrait(key, scope) match {
    case Some(result) => result
    case _ =>
      error(s"Trait $key not found", ast.location)
      ErrorTrait(key, ast)
  }

  private def findTrait(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[Trait] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].traits, scope) match {
      case Some(t: Trait) => Some(t)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type Trait")
        )
        None
      case _ => None
    }

  def findSecuritySchemeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): SecurityScheme =
    findSecurityScheme(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"SecurityScheme '$key' not found", ast.location)
        ErrorSecurityScheme(key, ast)
    }

  def findSecurityScheme(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit] = None
  ): Option[SecurityScheme] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].securitySchemes, scope) match {
      case Some(ss: SecurityScheme) => Some(ss)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type SecurityScheme")
        )
        None
      case _ => None
    }

  def findResponseOrError(ast: YPart)(key: String, searchScope: SearchScope.Scope): Response =
    findResponse(key, searchScope) match {
      case Some(result) => result
      case _ =>
        error(s"Response '$key' not found", ast.location)
        ErrorResponse(key, ast)
    }

  def nonEmpty: Boolean = {
    libraries.nonEmpty || fragments.nonEmpty || shapes.nonEmpty || annotations.nonEmpty || resourceTypes.nonEmpty ||
    parameters.nonEmpty || payloads.nonEmpty || traits.nonEmpty || securitySchemes.nonEmpty || responses.nonEmpty
  }
}

object WebApiDeclarations {

  def apply(
      declarations: Seq[DomainElement],
      errorHandler: AMFErrorHandler,
      futureDeclarations: FutureDeclarations,
      extractor: QualifiedNameExtractor
  ): WebApiDeclarations = {
    val result =
      new WebApiDeclarations(None, errorHandler = errorHandler, futureDeclarations = futureDeclarations, extractor)
    declarations.foreach(result += _)
    result
  }

  case class ErrorTrait(idPart: String, ast: YPart)
      extends Trait(Fields(), Annotations(ast))
      with ErrorDeclaration[TraitModel.type] {
    override val namespace: String = "http://amferror.com/#errorTrait/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[TraitModel.type] = ErrorTrait(idPart, ast)
    override val model: TraitModel.type                                        = TraitModel
  }

  case class ErrorResourceType(idPart: String, ast: YPart)
      extends ResourceType(Fields(), Annotations(ast))
      with ErrorDeclaration[ResourceTypeModel.type] {
    override val namespace: String = "http://amferror.com/#errorResourceType/"
    withId(idPart)

    override def dataNode: DataNode = ObjectNode()

    override protected def newErrorInstance: ErrorDeclaration[ResourceTypeModel.type] = ErrorResourceType(idPart, ast)
    override val model: ResourceTypeModel.type                                        = ResourceTypeModel
  }

  case class ErrorEndPoint(idPart: String, ast: YPart)
      extends EndPoint(Fields(), Annotations(ast))
      with ErrorDeclaration[EndPointModel.type] {
    override val namespace: String = "http://amferror.com/#errorEndPoint/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[EndPointModel.type] = ErrorEndPoint(idPart, ast)
    override val model: EndPointModel.type                                        = EndPointModel
  }

  case class ErrorSecurityScheme(idPart: String, ast: YPart)
      extends SecurityScheme(Fields(), Annotations(ast))
      with ErrorDeclaration[SecuritySchemeModel.type] {
    override val namespace: String = "http://amferror.com/#errorSecurityScheme/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[SecuritySchemeModel.type] =
      ErrorSecurityScheme(idPart, ast)
    override val model: SecuritySchemeModel.type = SecuritySchemeModel
  }
  case class ErrorCreativeWork(idPart: String, ast: YPart)
      extends CreativeWork(Fields(), Annotations(ast))
      with ErrorDeclaration[CreativeWorkModel.type] {
    override val namespace: String = "http://amferror.com/#errorCrativeWork/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[CreativeWorkModel.type] = ErrorCreativeWork(idPart, ast)
    override val model: CreativeWorkModel.type                                        = CreativeWorkModel
  }
  case class ErrorParameter(idPart: String, ast: YPart)
      extends Parameter(Fields(), Annotations(ast))
      with ErrorDeclaration[ParameterModel.type] {
    override val namespace: String = "http://amferror.com/#errorParameter/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[ParameterModel.type] = ErrorParameter(idPart, ast)
    override val model: ParameterModel.type                                        = ParameterModel
  }

  class ErrorLink(idPart: String, ast: YPart)
      extends TemplatedLink(Fields(), Annotations(ast))
      with ErrorDeclaration[TemplatedLinkModel.type] {
    override val namespace: String = "http://amferror.com/#errorTemplateLink/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[TemplatedLinkModel.type] = new ErrorLink(idPart, ast)
    override val model: TemplatedLinkModel.type                                        = TemplatedLinkModel
  }

  class ErrorCorrelationId(idPart: String, ast: YPart)
      extends CorrelationId(Fields(), Annotations(ast))
      with ErrorDeclaration[CorrelationIdModel.type] {
    override val namespace: String = "http://amferror.com/#errorCorrelationId/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[CorrelationIdModel.type] =
      new ErrorCorrelationId(idPart, ast)
    override val model: CorrelationIdModel.type = CorrelationIdModel
  }

  class ErrorMessage(idPart: String, ast: YPart, isAbstract: Boolean = false)
      extends Message(Fields(), Annotations(ast))
      with ErrorDeclaration[MessageModel.type] {
    override val namespace: String = "http://amferror.com/#errorMessage/"
    withId(idPart)
    isAbstract(isAbstract)

    override def linkCopy(): Message = new ErrorMessage(idPart, ast, isAbstract)

    override protected def newErrorInstance: ErrorDeclaration[MessageModel.type] =
      new ErrorMessage(idPart, ast, isAbstract)
    override val model: MessageModel.type = MessageModel
  }

  class ErrorServerBindings(idPart: String, ast: YPart)
      extends ServerBindings(Fields(), Annotations(ast))
      with ErrorDeclaration[ServerBindingsModel.type] {
    override val namespace: String = "http://amferror.com/#serverBindings/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[ServerBindingsModel.type] =
      new ErrorServerBindings(idPart, ast)
    override val model: ServerBindingsModel.type = ServerBindingsModel
  }

  class ErrorOperationBindings(idPart: String, ast: YPart)
      extends OperationBindings(Fields(), Annotations(ast))
      with ErrorDeclaration[OperationBindingsModel.type] {
    override val namespace: String = "http://amferror.com/#operationBindings/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[OperationBindingsModel.type] =
      new ErrorOperationBindings(idPart, ast)
    override val model: OperationBindingsModel.type = OperationBindingsModel
  }

  class ErrorChannelBindings(idPart: String, ast: YPart)
      extends ChannelBindings(Fields(), Annotations(ast))
      with ErrorDeclaration[ChannelBindingsModel.type] {
    override val namespace: String = "http://amferror.com/#channelBindings/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[ChannelBindingsModel.type] =
      new ErrorChannelBindings(idPart, ast)
    override val model: ChannelBindingsModel.type = ChannelBindingsModel
  }

  class ErrorMessageBindings(idPart: String, ast: YPart)
      extends MessageBindings(Fields(), Annotations(ast))
      with ErrorDeclaration[MessageBindingsModel.type] {
    override val namespace: String = "http://amferror.com/#messageBindings/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[MessageBindingsModel.type] =
      new ErrorMessageBindings(idPart, ast)
    override val model: MessageBindingsModel.type = MessageBindingsModel
  }

  class ErrorOperationTrait(idPart: String, ast: YPart)
      extends Operation(Fields(), Annotations(ast))
      with ErrorDeclaration[OperationModel.type] {
    override val namespace: String = "http://amferror.com/#operationTraits/"
    withId(idPart)
    withAbstract(true)

    override def linkCopy(): ErrorOperationTrait = new ErrorOperationTrait(idPart, ast).withAbstract(true)
    override protected def newErrorInstance: ErrorDeclaration[OperationModel.type] =
      new ErrorOperationTrait(idPart, ast)
    override val model: OperationModel.type = OperationModel
  }

  class ErrorCallback(idPart: String, ast: YPart)
      extends Callback(Fields(), Annotations(ast))
      with ErrorDeclaration[CallbackModel.type] {
    override val namespace: String = "http://amferror.com/#errorCallback/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[CallbackModel.type] = new ErrorCallback(idPart, ast)
    override val model: CallbackModel.type                                        = CallbackModel
  }
  case class ErrorResponse(idPart: String, ast: YPart)
      extends Response(Fields(), Annotations(ast))
      with ErrorDeclaration[ResponseModel.type] {
    override val namespace: String = "http://amferror.com/#errorResponse/"
    withId(idPart).withStatusCode("200")

    override protected def newErrorInstance: ErrorDeclaration[ResponseModel.type] = ErrorResponse(idPart, ast)
    override val model: ResponseModel.type                                        = ResponseModel
  }

  case class ErrorRequest(idPart: String, ast: YPart)
      extends Request(Fields(), Annotations(ast))
      with ErrorDeclaration[RequestModel.type] {
    override val namespace: String = "http://amferror.com/#errorRequest/"
    withId(idPart)

    override protected def newErrorInstance: ErrorDeclaration[RequestModel.type] = ErrorRequest(idPart, ast)
    override val model: RequestModel.type                                        = RequestModel
  }
}

class OasLikeWebApiDeclarations(
    val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends WebApiDeclarations(
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations,
      OasComponentQualifiedNameExtractor
    ) {}

class OasWebApiDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasLikeWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {}

object OasWebApiDeclarations {
  def apply(d: WebApiDeclarations): OasWebApiDeclarations = {
    val declarations = new OasWebApiDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )
    declarations.setLibraries(d.libraries)
    declarations.fragments = d.fragments
    declarations.shapes = d.shapes
    declarations.annotations = d.annotations
    declarations.resourceTypes = d.resourceTypes
    declarations.parameters = d.parameters
    declarations.payloads = d.payloads
    declarations.traits = d.traits
    declarations.securitySchemes = d.securitySchemes
    declarations.responses = d.responses
    declarations.annotations = d.annotations // FOR OAS -> RAML CONVERSION
    declarations                             // add withs methods?
  }
}

class AsyncWebApiDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasLikeWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {}

object AsyncWebApiDeclarations {
  def apply(d: WebApiDeclarations): AsyncWebApiDeclarations = {
    val declarations = new AsyncWebApiDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )

    // TODO ASYNC complete this
    declarations.securitySchemes = d.securitySchemes
    declarations
  }
}

class JsonSchemaDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {}

object JsonSchemaDeclarations {
  def apply(d: WebApiDeclarations): JsonSchemaDeclarations = {
    val declarations = new JsonSchemaDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )

    declarations.shapes = d.shapes // Currently we will only support schema declarations
    declarations
  }
}

class RamlWebApiDeclarations(
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends WebApiDeclarations(
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations,
      DotQualifiedNameExtractor
    ) {

  def existsExternalAlias(lib: String): Boolean = externalLibs.contains(lib)

  def merge(other: RamlWebApiDeclarations): RamlWebApiDeclarations = {
    val merged =
      new RamlWebApiDeclarations(alias = alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations)
    super.mergeParts(other, merged)
    externalShapes.foreach { case (k, s) => merged.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => merged.externalShapes += (k -> s) }
    merged
  }

  def absorb(other: RamlWebApiDeclarations): Unit = {
    super.mergeParts(other, this)
    externalShapes.foreach { case (k, s) => this.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => this.externalShapes += (k -> s) }
  }
}

class ExtensionWebApiDeclarations(
    parentDeclarations: RamlWebApiDeclarations,
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends RamlWebApiDeclarations(alias, errorHandler, futureDeclarations) {

  override def findForType(
      key: String,
      map: Declarations => Map[String, DomainElement],
      scope: SearchScope.Scope
  ): Option[DomainElement] = {
    super.findForType(key, map, scope) match {
      case Some(x) => Some(x)
      case None    => parentDeclarations.findForType(key, map, scope)
    }
  }
}

object RamlWebApiDeclarations {
  def apply(d: WebApiDeclarations): RamlWebApiDeclarations = {
    val declarations = new RamlWebApiDeclarations(
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )
    declarations.setLibraries(d.libraries)
    declarations.fragments = d.fragments
    declarations.shapes = d.shapes
    declarations.annotations = d.annotations
    declarations.resourceTypes = d.resourceTypes
    declarations.parameters = d.parameters
    declarations.payloads = d.payloads
    declarations.traits = d.traits
    declarations.securitySchemes = d.securitySchemes
    declarations.responses = d.responses
    declarations // add withs methods?
  }
}
