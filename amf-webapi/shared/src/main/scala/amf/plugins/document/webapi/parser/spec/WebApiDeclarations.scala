package amf.plugins.document.webapi.parser.spec

import amf.core.model.document.Fragment
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.{Annotations, ErrorHandler, Fields, FutureDeclarations}
import amf.core.utils.QName
import amf.plugins.document.webapi.parser.spec.SearchScope.{All, Fragments, Named}
import amf.plugins.domain.shapes.models.{CreativeWork, Example, UnresolvedShape}
import amf.core.annotations.SourceAST
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, DomainElement, Shape}
import amf.core.parser.{Annotations, Declarations, ErrorHandler, Fields, SearchScope}
import amf.plugins.document.webapi.annotations.DeclaredElement
import amf.plugins.domain.shapes.models.{CreativeWork, Example, NodeShape, UnresolvedShape}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YPart


/**
  * Declarations object.
  */
case class WebApiDeclarations(override var libraries: Map[String, WebApiDeclarations] = Map(),
                              override var fragments: Map[String, DomainElement] = Map(),
                              var shapes: Map[String, Shape] = Map(),
                              override var annotations: Map[String, CustomDomainProperty] = Map(),
                              var resourceTypes: Map[String, ResourceType] = Map(),
                              var parameters: Map[String, Parameter] = Map(),
                              var payloads: Map[String, Payload] = Map(),
                              var traits: Map[String, Trait] = Map(),
                              var securitySchemes: Map[String, SecurityScheme] = Map(),
                              errorHandler: Option[ErrorHandler])
  extends Declarations(libraries, fragments, annotations, errorHandler) {

  override def +=(element: DomainElement): WebApiDeclarations = {
    element match {
      case r: ResourceType         => resourceTypes = resourceTypes + (r.name      -> r)
      case t: Trait                => traits = traits + (t.name                    -> t)
      case s: Shape                => shapes = shapes + (s.name                    -> s)
      case p: Parameter            => parameters = parameters + (p.name            -> p)
      case ss: SecurityScheme      => securitySchemes = securitySchemes + (ss.name -> ss)
      case _                       => super.+=(element)
    }
    this
  }

  /** Find domain element with the same name. */
  override def findEquivalent(element: DomainElement): Option[DomainElement] = element match {
    case r: ResourceType         => findResourceType(r.name, SearchScope.All)
    case t: Trait                => findTrait(t.name, SearchScope.All)
    case s: Shape                => findType(s.name, SearchScope.All)
    case p: Parameter            => findParameter(p.name, SearchScope.All)
    case ss: SecurityScheme      => findSecurityScheme(ss.name, SearchScope.All)
    case _                       => super.findEquivalent(element)
  }

  def registerParameter(parameter: Parameter, payload: Payload): Unit = {
    parameters = parameters + (parameter.name -> parameter)
    payloads = payloads + (parameter.name     -> payload)
  }

  def parameterPayload(parameter: Parameter): Payload = payloads(parameter.name)

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): WebApiDeclarations = {
    libraries.get(alias) match {
      case Some(lib) => lib
      case None =>
        val result = WebApiDeclarations(errorHandler = errorHandler)
        libraries = libraries + (alias -> result)
        result
    }
  }

  private def error(message: String, ast: Option[YPart]): Unit = errorHandler match {
    case Some(handler) => handler.violation(message, ast)
    case _             => throw new Exception(message)
  }

  private def error(message: String, ast: YPart): Unit = error(message, Option(ast))

  override def declarables(): Seq[DomainElement] =
    super.declarables() ++ (shapes.values ++  resourceTypes.values ++ traits.values ++ parameters.values ++ securitySchemes.values).toSeq

  def findParameterOrError(ast: YPart)(key: String, scope: SearchScope.Scope): Parameter =
    findParameter(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"Parameter '$key' not found", ast)
        ErrorParameter
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
        ErrorResourceType
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
      ErrorTrait
  }

  private def findTrait(key: String, scope: SearchScope.Scope): Option[Trait] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].traits, scope) collect {
      case t: Trait => t
    }

  def findType(key: String, scope: SearchScope.Scope): Option[Shape] = findForType(key, _.asInstanceOf[WebApiDeclarations].shapes, scope) collect {
    case s: Shape => s
  }

  def findSecuritySchemeOrError(ast: YPart)(key: String, scope: SearchScope.Scope): SecurityScheme =
    findSecurityScheme(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"SecurityScheme '$key' not found", ast)
        ErrorSecurityScheme
    }

  def findSecurityScheme(key: String, scope: SearchScope.Scope): Option[SecurityScheme] =
    findForType(key, _.asInstanceOf[WebApiDeclarations].securitySchemes, scope) collect {
      case ss: SecurityScheme => ss
    }

  def findNamedExampleOrError(ast: YPart)(key: String): Example = findNamedExample(key) match {
    case Some(result) => result
    case _ =>
      error(s"NamedExample '$key' not found", ast)
      ErrorNamedExample
  }

  def findNamedExample(key: String): Option[Example] = fragments.get(key) collect { case e: Example => e }

  /** Resolve all [[UnresolvedShape]] references or fail. */
  def resolve(): Unit = {
    // we fail unresolved references
    promises.values.flatten.filter(!_.resolved).foreach(_.fail())
  }


  trait ErrorDeclaration

  object ErrorTrait                extends Trait(Fields(), Annotations()) with ErrorDeclaration
  object ErrorResourceType         extends ResourceType(Fields(), Annotations()) with ErrorDeclaration
  object ErrorCustomDomainProperty extends CustomDomainProperty(Fields(), Annotations()) with ErrorDeclaration
  object ErrorSecurityScheme       extends SecurityScheme(Fields(), Annotations()) with ErrorDeclaration
  object ErrorNamedExample         extends Example(Fields(), Annotations()) with ErrorDeclaration
  object ErrorCreativeWork         extends CreativeWork(Fields(), Annotations()) with ErrorDeclaration
  object ErrorParameter            extends Parameter(Fields(), Annotations()) with ErrorDeclaration

}

object WebApiDeclarations {

  def apply(declarations: Seq[DomainElement], errorHandler: Option[ErrorHandler]): WebApiDeclarations = {
    val result = WebApiDeclarations(errorHandler = errorHandler)
    declarations.foreach(result += _)
    result
  }
}