package amf.plugins.document.webapi.parser.spec

import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.{
  Annotations,
  Declarations,
  EmptyFutureDeclarations,
  ErrorHandler,
  Fields,
  FutureDeclarations,
  SearchScope
}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations._
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, Example}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, Payload, Response}
import org.yaml.model.YPart

/**
  * Declarations object.
  */
class WebApiDeclarations(alias: Option[String],
                         var libs: Map[String, WebApiDeclarations] = Map(),
                         var frags: Map[String, DomainElement] = Map(),
                         var shapes: Map[String, Shape] = Map(),
                         var anns: Map[String, CustomDomainProperty] = Map(),
                         var resourceTypes: Map[String, ResourceType] = Map(),
                         var parameters: Map[String, Parameter] = Map(),
                         var payloads: Map[String, Payload] = Map(),
                         var traits: Map[String, Trait] = Map(),
                         var securitySchemes: Map[String, SecurityScheme] = Map(),
                         var responses: Map[String, Response] = Map(),
                         errorHandler: Option[ErrorHandler],
                         futureDeclarations: FutureDeclarations)
    extends Declarations(libs, frags, anns, errorHandler, futureDeclarations = futureDeclarations) {

  def merge(other: WebApiDeclarations): WebApiDeclarations = {
    val merged = new WebApiDeclarations(alias = alias,
                                        errorHandler = errorHandler,
                                        futureDeclarations = EmptyFutureDeclarations())
    libs.foreach { case (k, s)                  => merged.libs += (k            -> s) }
    other.libs.foreach { case (k, s)            => merged.libs += (k            -> s) }
    frags.foreach { case (k, s)                 => merged.frags += (k           -> s) }
    other.frags.foreach { case (k, s)           => merged.frags += (k           -> s) }
    libraries.foreach { case (k, s)             => merged.libraries += (k       -> s) }
    other.libraries.foreach { case (k, s)       => merged.libraries += (k       -> s) }
    fragments.foreach { case (k, s)             => merged.fragments += (k       -> s) }
    other.fragments.foreach { case (k, s)       => merged.fragments += (k       -> s) }
    shapes.foreach { case (k, s)                => merged.shapes += (k          -> s) }
    other.shapes.foreach { case (k, s)          => merged.shapes += (k          -> s) }
    anns.foreach { case (k, s)                  => merged.anns += (k            -> s) }
    other.anns.foreach { case (k, s)            => merged.anns += (k            -> s) }
    resourceTypes.foreach { case (k, s)         => merged.resourceTypes += (k   -> s) }
    other.resourceTypes.foreach { case (k, s)   => merged.resourceTypes += (k   -> s) }
    parameters.foreach { case (k, s)            => merged.parameters += (k      -> s) }
    other.parameters.foreach { case (k, s)      => merged.parameters += (k      -> s) }
    payloads.foreach { case (k, s)              => merged.payloads += (k        -> s) }
    other.payloads.foreach { case (k, s)        => merged.payloads += (k        -> s) }
    traits.foreach { case (k, s)                => merged.traits += (k          -> s) }
    other.traits.foreach { case (k, s)          => merged.traits += (k          -> s) }
    securitySchemes.foreach { case (k, s)       => merged.securitySchemes += (k -> s) }
    other.securitySchemes.foreach { case (k, s) => merged.securitySchemes += (k -> s) }
    responses.foreach { case (k, s)             => merged.responses += (k       -> s) }
    other.responses.foreach { case (k, s)       => merged.responses += (k       -> s) }
    merged
  }

  override def +=(element: DomainElement): WebApiDeclarations = {
    element match {
      case r: ResourceType =>
        futureDeclarations.resolveRef(aliased(r.name.value()), r)
        resourceTypes = resourceTypes + (r.name.value() -> r)
      case t: Trait =>
        futureDeclarations.resolveRef(aliased(t.name.value()), t)
        traits = traits + (t.name.value() -> t)
      case s: Shape =>
        futureDeclarations.resolveRef(aliased(s.name.value()), s)
        shapes = shapes + (s.name.value() -> s)
      case p: Parameter =>
        futureDeclarations.resolveRef(aliased(p.name.value()), p)
        parameters = parameters + (p.name.value() -> p)
      case ss: SecurityScheme =>
        futureDeclarations.resolveRef(aliased(ss.name.value()), ss)
        securitySchemes = securitySchemes + (ss.name.value() -> ss)
      case re: Response =>
        futureDeclarations.resolveRef(aliased(re.name.value()), re)
        responses = responses + (re.name.value() -> re)
      case _ => super.+=(element)
    }
    this
  }

  def aliased(name: String) = alias match {
    case Some(prefix) => s"$prefix.$name"
    case None         => name
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

  def registerParameter(parameter: Parameter, payload: Payload): Unit = {
    parameters = parameters + (parameter.name.value() -> parameter)
    payloads = payloads + (parameter.name.value()     -> payload)
  }

  def parameterPayload(parameter: Parameter): Payload = payloads(parameter.name.value())

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): WebApiDeclarations = {
    libraries.get(alias) match {
      case Some(lib: WebApiDeclarations) => lib
      case _ =>
        val result = new WebApiDeclarations(Some(alias),
                                            errorHandler = errorHandler,
                                            futureDeclarations = EmptyFutureDeclarations())
        libraries = libraries + (alias -> result)
        result
    }
  }

  override def declarables(): Seq[DomainElement] =
    super
      .declarables()
      .toList ++ (shapes.values ++ resourceTypes.values ++ traits.values ++ parameters.values ++ securitySchemes.values ++ responses.values).toList

  def findParameterOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Parameter =
    findParameter(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"Parameter '$key' not found", ast)
        ErrorParameter(key, ast)
    }

  def findParameter(key: String, scope: SearchScope.Scope): Option[Parameter] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].parameters, scope) collect {
      case p: Parameter => p
    }

  def findResourceTypeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): ResourceType =
    findResourceType(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"ResourceType $key not found", ast)
        ErrorResourceType(key, ast)
    }

  def findResourceType(key: String, scope: SearchScope.Scope): Option[ResourceType] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].resourceTypes, scope) collect {
      case r: ResourceType => r
    }

  def findDocumentations(key: String, scope: SearchScope.Scope): Option[CreativeWork] =
    findForType(key, Map(), scope) collect {
      case u: CreativeWork => u
    }

  def findTraitOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Trait = findTrait(key, scope) match {
    case Some(result) => result
    case _ =>
      error(s"Trait $key not found", ast)
      ErrorTrait(key, ast)
  }

  private def findTrait(key: String, scope: SearchScope.Scope): Option[Trait] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].traits, scope) collect {
      case t: Trait => t
    }

  def findType(key: String, scope: SearchScope.Scope): Option[AnyShape] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].shapes, scope) collect {
      case s: AnyShape => s
    }

  def findSecuritySchemeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): SecurityScheme =
    findSecurityScheme(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"SecurityScheme '$key' not found", ast)
        ErrorSecurityScheme(key, ast)
    }

  def findSecurityScheme(key: String, scope: SearchScope.Scope): Option[SecurityScheme] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].securitySchemes, scope) collect {
      case ss: SecurityScheme => ss
    }

  def findResponse(key: String, scope: SearchScope.Scope): Option[Response] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].responses, scope) collect {
      case re: Response => re
    }

  def findResponseOrError(ast: YPart)(key: String, searchScope: SearchScope.Scope): Response =
    findResponse(key, searchScope) match {
      case Some(result) => result
      case _ =>
        error(s"Response '$key' not found", ast)
        ErrorResponse(key, ast)
    }

  def findNamedExampleOrError(ast: YPart)(key: String): Example = findNamedExample(key) match {
    case Some(result) => result
    case _ =>
      error(s"NamedExample '$key' not found", ast)
      ErrorNamedExample(key, ast)
  }

  def findNamedExample(key: String): Option[Example] = fragments.get(key) collect { case e: Example => e }
}

object WebApiDeclarations {

  def apply(declarations: Seq[DomainElement],
            errorHandler: Option[ErrorHandler],
            futureDeclarations: FutureDeclarations): WebApiDeclarations = {
    val result = new WebApiDeclarations(None, errorHandler = errorHandler, futureDeclarations = futureDeclarations)
    declarations.foreach(result += _)
    result
  }

  trait ErrorDeclaration extends DomainElement {
    val namespace: String

    override def withId(value: String): ErrorDeclaration.this.type = super.withId(namespace + value)
  }

  case class ErrorTrait(idPart: String, ast: YPart) extends Trait(Fields(), Annotations(ast)) with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorTrait/"
    withId(idPart)
  }

  case class ErrorResourceType(idPart: String, ast: YPart)
      extends ResourceType(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorResourceType/"
    withId(idPart)
  }

  case class ErrorEndPoint(idPart: String, ast: YPart)
      extends EndPoint(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorEndPoint/"
    withId(idPart)
  }

  case class ErrorSecurityScheme(idPart: String, ast: YPart)
      extends SecurityScheme(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorSecurityScheme/"
    withId(idPart)
  }
  case class ErrorNamedExample(idPart: String, ast: YPart)
      extends Example(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorNamedExample/"
    withId(idPart)
  }
  case class ErrorCreativeWork(idPart: String, ast: YPart)
      extends CreativeWork(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorCrativeWork/"
    withId(idPart)
  }
  case class ErrorParameter(idPart: String, ast: YPart)
      extends Parameter(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorParameter/"
    withId(idPart)
  }
  case class ErrorResponse(idPart: String, ast: YPart)
      extends Response(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorResponse/"
    withId(idPart).withStatusCode("200")
  }
}
