package amf.spec

import amf.common.core.QName
import amf.document.Fragment.Fragment
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.domain.security.SecurityScheme
import amf.domain.{DomainElement, UserDocumentation}
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
                        var documentations: Map[String, UserDocumentation] = Map(),
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
      case u: UserDocumentation    => documentations = documentations + (u.title   -> u)
      case t: Trait                => traits = traits + (t.name                    -> t)
      case a: CustomDomainProperty => annotations = annotations + (a.name          -> a)
      case s: Shape                => shapes = shapes + (s.name                    -> s)
      case ss: SecurityScheme      => securitySchemes = securitySchemes + (ss.name -> ss)
    }
    this
  }

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
    (shapes.values ++ annotations.values ++ resourceTypes.values ++ documentations.values ++ traits.values).toSeq

  def findResourceType(key: String): Option[ResourceType] = findForType(key, _.resourceTypes) collect {
    case r: ResourceType => r
  }

  def findDocumentations(key: String): Option[UserDocumentation] = findForType(key, _.documentations) collect {
    case u: UserDocumentation => u
  }

  def findTrait(key: String): Option[Trait] = findForType(key, _.traits) collect {
    case t: Trait => t
  }

  def findAnnotation(key: String): Option[CustomDomainProperty] = findForType(key, _.annotations) collect {
    case a: CustomDomainProperty => a
  }

  def findType(key: String): Option[Shape] = findForType(key, _.shapes) collect {
    case s: Shape => s
  }

  def findSecurityScheme(key: String): Option[SecurityScheme] = findForType(key, _.securitySchemes) collect {
    case ss: SecurityScheme => ss
  }

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
