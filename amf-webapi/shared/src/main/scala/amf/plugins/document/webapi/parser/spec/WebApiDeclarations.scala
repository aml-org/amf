package amf.plugins.document.webapi.parser.spec

import amf.core.annotations.{DeclaredElement, DeclaredHeader}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DataNode, DomainElement, ObjectNode, Shape}
import amf.core.parser.{
  Annotations,
  Declarations,
  EmptyFutureDeclarations,
  ErrorHandler,
  Fields,
  FragmentRef,
  FutureDeclarations,
  SearchScope
}
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations._
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, Example}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{Callback, EndPoint, Parameter, Payload, Request, Response, TemplatedLink}
import org.yaml.model.{YNode, YPart}

/**
  * Declarations object.
  */
class WebApiDeclarations(val alias: Option[String],
                         var libs: Map[String, WebApiDeclarations] = Map(),
                         var frags: Map[String, FragmentRef] = Map(),
                         var shapes: Map[String, Shape] = Map(),
                         var anns: Map[String, CustomDomainProperty] = Map(),
                         var resourceTypes: Map[String, ResourceType] = Map(),
                         var parameters: Map[String, Parameter] = Map(),
                         var payloads: Map[String, Payload] = Map(),
                         var traits: Map[String, Trait] = Map(),
                         var securitySchemes: Map[String, SecurityScheme] = Map(),
                         var responses: Map[String, Response] = Map(),
                         var examples: Map[String, Example] = Map(),
                         var requests: Map[String, Request] = Map(),
                         var headers: Map[String, Parameter] = Map(),
                         var links: Map[String, TemplatedLink] = Map(),
                         var callbacks: Map[String, Callback] = Map(),
                         val errorHandler: Option[ErrorHandler],
                         val futureDeclarations: FutureDeclarations,
                         var others: Map[String, BaseUnit] = Map())
    extends Declarations(libs, frags, anns, errorHandler, futureDeclarations = futureDeclarations) {

  def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape = {
    fragments.get(text) match {
      case Some(fragmentRef) =>
        promotedFragments :+= DataTypeFragment()
          .withId(fragmentRef.location.getOrElse(fullRef))
          .withLocation(fragmentRef.location.getOrElse(fullRef))
          .withEncodes(shape)
        fragments += (text -> FragmentRef(shape, fragmentRef.location))
      case _ =>
        promotedFragments :+= DataTypeFragment().withId(fullRef).withLocation(fullRef).withEncodes(shape)
        fragments += (text -> FragmentRef(shape, None))
    }
    shape
  }

  protected def mergeParts(other: WebApiDeclarations, merged: WebApiDeclarations): Unit = {
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
    annotations.foreach { case (k, s)           => merged.annotations += (k     -> s) }
    other.annotations.foreach { case (k, s)     => merged.annotations += (k     -> s) }
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
  }

  def merge(other: WebApiDeclarations): WebApiDeclarations = {
    val merged =
      new WebApiDeclarations(alias = alias,
                             errorHandler = errorHandler,
                             futureDeclarations = EmptyFutureDeclarations())
    mergeParts(other, merged)
    merged
  }

  protected def addSchema(s: Shape) = {
    futureDeclarations.resolveRef(aliased(s.name.value()), s)
    shapes = shapes + (s.name.value() -> s)
  }

  override def +=(element: DomainElement): WebApiDeclarations = {
    //future declarations are used for shapes, and therefore only resolved for that case
    element match {
      case r: ResourceType =>
        resourceTypes = resourceTypes + (r.name.value() -> r)
      case t: Trait =>
        traits = traits + (t.name.value() -> t)
      case s: Shape =>
        addSchema(s)
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
      case ex: Example =>
        examples = examples + (ex.name.value() -> ex)
      case rq: Request =>
        requests = requests + (rq.name.value() -> rq)
      case l: TemplatedLink =>
        links = links + (l.name.value() -> l)
      case c: Callback =>
        callbacks = callbacks + (c.name.value() -> c)
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
      .toList ++ (shapes.values ++ resourceTypes.values ++ traits.values ++ parameters.values ++ payloads.values ++ securitySchemes.values ++ responses.values ++ examples.values ++ requests.values ++ links.values ++ callbacks.values ++ headers.values).toList

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

  def findPayload(key: String, scope: SearchScope.Scope): Option[Payload] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].payloads, scope) collect {
      case p: Payload => p
    }

  def findRequestBody(key: String, scope: SearchScope.Scope): Option[Request] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].requests, scope) collect {
      case r: Request => r
    }

  def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].examples, scope) collect {
      case e: Example => e
    }

  def findResponse(key: String, scope: SearchScope.Scope): Option[Response] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].responses, scope) collect {
      case r: Response => r
    }

  def findTemplatedLink(key: String, scope: SearchScope.Scope): Option[TemplatedLink] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].links, scope) collect {
      case l: TemplatedLink => l
    }

  def findHeader(key: String, scope: SearchScope.Scope): Option[Parameter] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].headers, scope) collect {
      case p: Parameter => p
    }

  def findCallback(key: String, scope: SearchScope.Scope): Option[Callback] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].callbacks, scope) collect {
      case c: Callback => c
    }

  def findResourceTypeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): ResourceType =
    findResourceType(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"ResourceType $key not found", ast)
        ErrorResourceType(key, ast)
    }

  def findResourceType(key: String,
                       scope: SearchScope.Scope,
                       error: Option[String => Unit] = None): Option[ResourceType] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].resourceTypes, scope) match {
      case Some(r: ResourceType) => Some(r)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type ResourceType"))
        None
      case _ => None
    }

  def findDocumentations(key: String,
                         scope: SearchScope.Scope,
                         error: Option[String => Unit] = None): Option[CreativeWork] =
    findForType(key, Map(), scope) match {
      case Some(u: CreativeWork) => Some(u)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(_(
          s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type DocumentationItem"))
        None
      case _ => None
    }

  def findTraitOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Trait = findTrait(key, scope) match {
    case Some(result) => result
    case _ =>
      error(s"Trait $key not found", ast)
      ErrorTrait(key, ast)
  }

  private def findTrait(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[Trait] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].traits, scope) match {
      case Some(t: Trait) => Some(t)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type Trait"))
        None
      case _ => None
    }

  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].shapes, scope) match {
      case Some(anyShape: AnyShape) => Some(anyShape)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type DataType"))
        None
      case _ => None
    }

  def findSecuritySchemeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): SecurityScheme =
    findSecurityScheme(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"SecurityScheme '$key' not found", ast)
        ErrorSecurityScheme(key, ast)
    }

  def findSecurityScheme(key: String,
                         scope: SearchScope.Scope,
                         error: Option[String => Unit] = None): Option[SecurityScheme] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].securitySchemes, scope) match {
      case Some(ss: SecurityScheme) => Some(ss)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type SecurityScheme"))
        None
      case _ => None
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

  def findNamedExample(key: String, error: Option[String => Unit] = None): Option[Example] =
    fragments.get(key).map(_.encoded) match {
      case Some(e: Example) => Some(e)
      case Some(_) =>
        error.foreach(_(s"Fragment defined in $key does not conform to the expected type NamedExample"))
        None
      case _ => None
    }

  def nonEmpty: Boolean = {
    libs.nonEmpty || frags.nonEmpty || shapes.nonEmpty || anns.nonEmpty || resourceTypes.nonEmpty ||
    parameters.nonEmpty || payloads.nonEmpty || traits.nonEmpty || securitySchemes.nonEmpty || responses.nonEmpty
  }
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

    override def dataNode: DataNode = ObjectNode()
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
  class ErrorLink(idPart: String, ast: YPart) extends TemplatedLink(Fields(), Annotations(ast)) with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorTemplateLink/"
    withId(idPart)
  }
  class ErrorCallback(idPart: String, ast: YPart) extends Callback(Fields(), Annotations(ast)) with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorCallback/"
    withId(idPart)
  }
  case class ErrorResponse(idPart: String, ast: YPart)
      extends Response(Fields(), Annotations(ast))
      with ErrorDeclaration {
    override val namespace: String = "http://amferror.com/#errorResponse/"
    withId(idPart).withStatusCode("200")
  }
}

class OasWebApiDeclarations(val asts: Map[String, YNode],
                            override val alias: Option[String],
                            override val errorHandler: Option[ErrorHandler],
                            override val futureDeclarations: FutureDeclarations)
    extends WebApiDeclarations(alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations) {}

object OasWebApiDeclarations {
  def apply(d: WebApiDeclarations): OasWebApiDeclarations = {
    val declarations = new OasWebApiDeclarations(Map(),
                                                 d.alias,
                                                 errorHandler = d.errorHandler,
                                                 futureDeclarations = d.futureDeclarations)
    declarations.libs = d.libs
    declarations.frags = d.frags
    declarations.shapes = d.shapes
    declarations.anns = d.anns
    declarations.resourceTypes = d.resourceTypes
    declarations.parameters = d.parameters
    declarations.payloads = d.payloads
    declarations.traits = d.traits
    declarations.securitySchemes = d.securitySchemes
    declarations.responses = d.responses
    declarations // add withs methods?
  }
}

class RamlWebApiDeclarations(var externalShapes: Map[String, AnyShape] = Map(),
                             var externalLibs: Map[String, Map[String, AnyShape]] = Map(),
                             override val alias: Option[String],
                             override val errorHandler: Option[ErrorHandler],
                             override val futureDeclarations: FutureDeclarations)
    extends WebApiDeclarations(alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations) {

  def registerExternalRef(external: (String, AnyShape)): WebApiDeclarations = { // particular case for jsonschema # fragment
    externalShapes = externalShapes + (external._1 -> external._2)
    this
  }

  def registerExternalLib(url: String, content: Map[String, AnyShape]): WebApiDeclarations = { // particular case for jsonschema # fragment
    externalLibs = externalLibs + (url -> content)
    this
  }

  def findInExternals(url: String): Option[AnyShape] = externalShapes.get(url)

  def findInExternalsLibs(lib: String, name: String): Option[AnyShape] = externalLibs.get(lib).flatMap(_.get(name))

  def existsExternalAlias(lib: String): Boolean = externalLibs.contains(lib)

  def merge(other: RamlWebApiDeclarations): RamlWebApiDeclarations = {
    val merged =
      new RamlWebApiDeclarations(alias = alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations)
    super.mergeParts(other, merged)
    externalShapes.foreach { case (k, s)       => merged.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => merged.externalShapes += (k -> s) }
    merged
  }

  def absorb(other: RamlWebApiDeclarations): Unit = {
    super.mergeParts(other, this)
    externalShapes.foreach { case (k, s)       => this.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => this.externalShapes += (k -> s) }
  }
}

class ExtensionWebApiDeclarations(externalShapes: Map[String, AnyShape] = Map(),
                                  externalLibs: Map[String, Map[String, AnyShape]] = Map(),
                                  parentDeclarations: RamlWebApiDeclarations,
                                  override val alias: Option[String],
                                  override val errorHandler: Option[ErrorHandler],
                                  override val futureDeclarations: FutureDeclarations)
    extends RamlWebApiDeclarations(externalShapes, externalLibs, alias, errorHandler, futureDeclarations) {

  override def findForType(key: String,
                           map: Declarations => Map[String, DomainElement],
                           scope: SearchScope.Scope): Option[DomainElement] = {
    super.findForType(key, map, scope) match {
      case Some(x) => Some(x)
      case None    => parentDeclarations.findForType(key, map, scope)
    }
  }
}

object RamlWebApiDeclarations {
  def apply(d: WebApiDeclarations): RamlWebApiDeclarations = {
    val declarations = new RamlWebApiDeclarations(Map(),
                                                  Map(),
                                                  d.alias,
                                                  errorHandler = d.errorHandler,
                                                  futureDeclarations = d.futureDeclarations)
    declarations.libs = d.libs
    declarations.frags = d.frags
    declarations.shapes = d.shapes
    declarations.anns = d.anns
    declarations.resourceTypes = d.resourceTypes
    declarations.parameters = d.parameters
    declarations.payloads = d.payloads
    declarations.traits = d.traits
    declarations.securitySchemes = d.securitySchemes
    declarations.responses = d.responses
    declarations // add withs methods?
  }
}
