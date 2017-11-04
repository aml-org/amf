package amf.spec

import amf.common.core.QName
import amf.document.Fragment.Fragment
import amf.domain._
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.dialects.DomainEntity
import amf.domain.extensions.CustomDomainProperty
import amf.domain.security.SecurityScheme
import amf.model.AmfArray
import amf.shape.{Shape, UnresolvedShape}

/**
  * Declarations object.
  */
case class Declarations(var libraries: Map[String, Declarations] = Map(),
                        var fragments: Map[String, DomainElement] = Map(),
                        var shapes: Map[String, Shape] = Map(),
                        var annotations: Map[String, CustomDomainProperty] = Map(),
                        var resourceTypes: Map[String, ResourceType] = Map(),
                        var documentations: Map[String, CreativeWork] = Map(),
                        var parameters: Map[String, Parameter] = Map(),
                        var payloads: Map[String, Payload] = Map(),
                        var traits: Map[String, Trait] = Map(),
                        var securitySchemes: Map[String, SecurityScheme] = Map()) {

  def +=(fragment: (String, Fragment)): Declarations = {
    fragment match {
      case (url, f) => fragments = fragments + (url -> f.encodes)
    }
    this
  }

  def +=(element: DomainElement): Declarations = {
    element match {
      case r: ResourceType         => resourceTypes = resourceTypes + (r.name      -> r)
      case u: CreativeWork         => documentations = documentations + (u.title   -> u)
      case t: Trait                => traits = traits + (t.name                    -> t)
      case a: CustomDomainProperty => annotations = annotations + (a.name          -> a)
      case s: Shape                => shapes = shapes + (s.name                    -> s)
      case p: Parameter            => parameters = parameters + (p.name            -> p)
      case ss: SecurityScheme      => securitySchemes = securitySchemes + (ss.name -> ss)
    }
    this
  }

  def registerParameter(parameter: Parameter, payload: Payload): Unit = {
    parameters = parameters + (parameter.name -> parameter)
    payloads = payloads + (parameter.name     -> payload)
  }

  def parameterPayload(parameter: Parameter): Payload = payloads(parameter.name)

  /** Get or create specified library. */
  def getOrCreateLibrary(alias: String): Declarations = {
    libraries.get(alias) match {
      case Some(lib) => lib
      case None =>
        val result = Declarations()
        libraries = libraries + (alias -> result)
        result
    }
  }

  def declarables(): Seq[DomainElement] =
    (shapes.values ++ annotations.values ++ resourceTypes.values ++ documentations.values ++ traits.values ++ parameters.values ++ securitySchemes.values).toSeq

  def findParameterOrFail(key: String): Parameter =
    findParameter(key).getOrElse(throw new Exception(s"Parameter '$key' not found"))

  def findParameter(key: String): Option[Parameter] = findForType(key, _.parameters) collect {
    case p: Parameter => p
  }

  def findResourceTypeOrFail(key: String): ResourceType =
    findResourceType(key).getOrElse(throw new Exception(s"ResourceType '$key' not found"))

  def findResourceType(key: String): Option[ResourceType] = findForType(key, _.resourceTypes) collect {
    case r: ResourceType => r
  }

  def findDocumentationOrFail(key: String): CreativeWork =
    findDocumentations(key).getOrElse(throw new Exception(s"Documentation '$key' not found"))

  def findDocumentations(key: String): Option[CreativeWork] = findForType(key, _.documentations) collect {
    case u: CreativeWork => u
  }

  def findTraitOrFail(key: String): Trait = findTrait(key).getOrElse(throw new Exception(s"Trait '$key' not found"))

  def findTrait(key: String): Option[Trait] = findForType(key, _.traits) collect {
    case t: Trait => t
  }

  def findAnnotationOrFail(key: String): CustomDomainProperty =
    findAnnotation(key).getOrElse(throw new Exception(s"Annotation '$key' not found"))

  def findAnnotation(key: String): Option[CustomDomainProperty] = findForType(key, _.annotations) collect {
    case a: CustomDomainProperty => a
  }

  def findTypeOrFail(key: String): Shape = findType(key).getOrElse(throw new Exception(s"Type '$key' not found"))

  def findType(key: String): Option[Shape] = findForType(key, _.shapes) collect {
    case s: Shape => s
  }

  def findSecuritySchemeOrFail(key: String): SecurityScheme =
    findSecurityScheme(key).getOrElse(throw new Exception(s"SecurityScheme '$key' not found"))

  def findSecurityScheme(key: String): Option[SecurityScheme] = findForType(key, _.securitySchemes) collect {
    case ss: SecurityScheme => ss
  }

  def findNamedExampleOrFail(key: String): Example =
    findNamedExample(key).getOrElse(throw new Exception(s"NamedExample '$key' not found"))

  def findNamedExample(key: String): Option[Example] = fragments.get(key) collect { case e: Example => e }

  /** Resolve all [[UnresolvedShape]] references or fail. */
  def resolve(): Unit = shapes.values.foreach(resolveShape)

  private def resolveShape(shape: Shape): Shape = {
    shape.fields.foreach {
      case (field, value) =>
        val resolved = value.value match {
          case u: UnresolvedShape => resolveOrFail(u)
          case s: Shape           => resolveShape(s)
          case a: AmfArray =>
            AmfArray(a.values.map {
              case u: UnresolvedShape => resolveOrFail(u)
              case s: Shape           => resolveShape(s)
              case o                  => o
            }, a.annotations)
          case o => o
        }

        shape.fields.setWithoutId(field, resolved, value.annotations)
    }
    shape
  }

  private def resolveOrFail(unresolved: UnresolvedShape): Shape = {
    shapes.get(unresolved.reference) match {
      case Some(target) => unresolved.resolve(target)
      case None         => throw new Exception(s"Reference '${unresolved.reference}' not found")
    }
  }

  private def findForType(key: String, map: Declarations => Map[String, DomainElement]): Option[DomainElement] = {
    val fqn = QName(key)

    val result = if (fqn.isQualified) {
      libraries.get(fqn.qualification).flatMap(_.findForType(fqn.name, map))
    } else None

    result
      .orElse {
        map(this).get(key)
      }
      .orElse {
        fragments.get(key)
      }
  }
}

object Declarations {

  def apply(declarations: Seq[DomainElement]): Declarations = {
    val result = Declarations()
    declarations.foreach(result += _)
    result
  }
}
