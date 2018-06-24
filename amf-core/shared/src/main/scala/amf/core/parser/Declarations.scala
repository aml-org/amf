package amf.core.parser

import amf.core.model.document.Fragment
import amf.core.model.domain.DomainElement
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.SearchScope.{All, Fragments, Named}
import amf.core.utils.QName
import org.yaml.model.YPart

class Declarations(var libraries: Map[String, Declarations] = Map(),
                   var fragments: Map[String, FragmentRef] = Map(),
                   var annotations: Map[String, CustomDomainProperty] = Map(),
                   errorHandler: Option[ErrorHandler],
                   futureDeclarations: FutureDeclarations) {

  def +=(fragment: (String, Fragment)): Declarations = {
    fragment match {
      case (url, f) => fragments = fragments + (url -> FragmentRef(f.encodes, Some(f.location)))
    }
    this
  }

  def +=(element: DomainElement): Declarations = {
    element match {
      case a: CustomDomainProperty =>
        futureDeclarations.resolveRef(a.name.value(), a)
        annotations = annotations + (a.name.value() -> a)
    }
    this
  }

  /** Find domain element with the same name. */
  def findEquivalent(element: DomainElement): Option[DomainElement] = element match {
    case a: CustomDomainProperty => findAnnotation(a.name.value(), SearchScope.All)
    case _                       => None
  }

  /** Get or create specified library. */
  def getOrCreateLibrary(alias: String): Declarations = {
    libraries.get(alias) match {
      case Some(lib) => lib
      case None =>
        val result = new Declarations(errorHandler = errorHandler, futureDeclarations = futureDeclarations)
        libraries = libraries + (alias -> result)
        result
    }
  }

  protected def error(message: String, ast: YPart): Unit = errorHandler match {
    case Some(handler) => handler.violation(message, ast)
    case _             => throw new Exception(message)
  }

  def declarables(): Seq[DomainElement] =
    annotations.values.toSeq

  def findAnnotationOrError(ast: YPart)(key: String, scope: SearchScope.Scope): CustomDomainProperty =
    findAnnotation(key, scope) match {
      case Some(result) => result
      case _ =>
        error(s"Annotation '$key' not found", ast)
        ErrorCustomDomainProperty
    }

  def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    findForType(key, _.annotations, scope) collect {
      case a: CustomDomainProperty => a
    }

  protected def findForType(key: String,
                            map: Declarations => Map[String, DomainElement],
                            scope: SearchScope.Scope): Option[DomainElement] = {
    def inRef(): Option[DomainElement] = {
      val fqn = QName(key)

      val result = if (fqn.isQualified) {
        libraries.get(fqn.qualification).flatMap(_.findForType(fqn.name, map, scope))
      } else None

      result
        .orElse {
          map(this).get(key)
        }
    }

    scope match {
      case All       => inRef().orElse(fragments.get(key).map(_.encoded))
      case Fragments => fragments.get(key).map(_.encoded)
      case Named     => inRef()
    }
  }

  trait ErrorDeclaration

  object ErrorCustomDomainProperty extends CustomDomainProperty(Fields(), Annotations()) with ErrorDeclaration

}

case class FragmentRef(encoded: DomainElement, location: Option[String])

object FragmentRef {
  def apply(f: Fragment): FragmentRef = new FragmentRef(f.encodes, Option(f.location))
}

object Declarations {

  def apply(declarations: Seq[DomainElement],
            errorHandler: Option[ErrorHandler],
            futureDeclarations: FutureDeclarations): Declarations = {
    val result = new Declarations(errorHandler = errorHandler, futureDeclarations = futureDeclarations)
    declarations.foreach(result += _)
    result
  }
}

object SearchScope {
  trait Scope

  object All       extends Scope
  object Fragments extends Scope
  object Named     extends Scope
}
