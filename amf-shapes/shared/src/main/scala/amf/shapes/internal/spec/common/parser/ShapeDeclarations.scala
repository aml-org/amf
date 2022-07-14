package amf.shapes.internal.spec.common.parser

import amf.aml.client.scala.model.document.Dialect
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Fragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.parser.domain.{Declarations, FragmentRef, FutureDeclarations, SearchScope}
import amf.shapes.client.scala.model.document.DataTypeFragment
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.common.error.ErrorNamedExample
import amf.shapes.internal.spec.jsonschema.parser.document.NameExtraction
import amf.shapes.internal.spec.jsonschema.ref.JsonReference
import org.yaml.model.YPart

object ShapeDeclarations {
  def empty(errorHandler: AMFErrorHandler, futureDeclarations: FutureDeclarations): ShapeDeclarations = {
    new ShapeDeclarations(errorHandler = errorHandler, futureDeclarations = futureDeclarations)
  }
}

class ShapeDeclarations(
    val alias: Option[String] = None,
    errorHandler: AMFErrorHandler,
    futureDeclarations: FutureDeclarations
) extends Declarations(Map.empty, Map.empty, Map.empty, errorHandler, futureDeclarations) {

  var shapes: Map[String, Shape]                                  = Map()
  var extensions: Map[String, Dialect]                            = Map.empty
  var examples: Map[String, Example]                              = Map.empty
  var externalShapes: Map[String, AnyShape]                       = Map()
  var externalLibs: Map[String, Map[String, AnyShape]]            = Map()
  var documentFragments: Map[String, (Shape, Map[String, Shape])] = Map()

  def registerExternalRef(external: (String, AnyShape)): ShapeDeclarations = { // particular case for jsonschema # fragment
    externalShapes = externalShapes + (external._1 -> external._2)
    this
  }

  def registerExternalLib(url: String, content: Map[String, AnyShape]): ShapeDeclarations = { // particular case for jsonschema # fragment
    externalLibs = externalLibs + (url -> content)
    this
  }

  def findInExternals(url: String): Option[AnyShape] = externalShapes.get(url)

  def findInExternalsLibs(lib: String, name: String): Option[AnyShape] = externalLibs.get(lib).flatMap(_.get(name))

  def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit = {
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
  }

  def withExtensions(extensions: Map[String, Dialect]): ShapeDeclarations = {
    this.extensions = extensions
    this
  }

  def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit] = None
  ): Option[CreativeWork] =
    findForType(key, Map.empty, scope) match {
      case Some(u: CreativeWork) => Some(u)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type DocumentationItem")
        )
        None
      case _ => None
    }

  def findNamedExampleOrError(ast: YPart)(key: String): Example = findNamedExample(key) match {
    case Some(result) => result
    case _ =>
      error(s"NamedExample '$key' not found", ast.location)
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

  def findDeclaredTypeInDocFragment(doc: String, name: String): Option[Shape] = {
    documentFragments.get(doc).flatMap { case (_, declared) =>
      declared.get(name)
    }
  }

  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape] =
    findForType(key, _.asInstanceOf[ShapeDeclarations].shapes, scope) match {
      case Some(anyShape: AnyShape) => Some(anyShape)
      case Some(other) if scope == SearchScope.Fragments =>
        error.foreach(
          _(s"Fragment of type ${other.getClass.getSimpleName} does not conform to the expected type DataType")
        )
        None
      case _ => None
    }

  def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    findForType(key, _.asInstanceOf[ShapeDeclarations].examples, scope) collect { case e: Example =>
      e
    }

  def +=(extension: Map[String, Dialect]): ShapeDeclarations = {
    extensions = extensions ++ extension
    this
  }

  def +=(doc: (String, (Shape, Map[String, Shape]))): this.type = {
    documentFragments += doc
    this
  }

  protected def addSchema(s: Shape): Unit = {
    futureDeclarations.resolveRef(aliased(s.name.value()), s)
    shapes = shapes + (s.name.value() -> s)
  }

  def aliased(name: String): String = alias match {
    case Some(prefix) => s"$prefix.$name"
    case None         => name
  }

  override def +=(element: DomainElement): ShapeDeclarations = {
    // future declarations are used for shapes, and therefore only resolved for that case
    element match {
      case s: Shape =>
        addSchema(s)
      case ex: Example =>
        examples = examples + (ex.name.value() -> ex)
      case _ => super.+=(element)
    }
    this
  }

  def copy(): ShapeDeclarations = {
    val next = new ShapeDeclarations(alias, errorHandler, futureDeclarations)
    copy(next)
  }

  def copy[T <: ShapeDeclarations](next: T): T = {
    next.shapes = shapes
    next.extensions = extensions
    next.examples = examples
    next.externalShapes = externalShapes
    next.externalLibs = externalLibs
    next.libraries = libraries
    next.fragments = fragments
    next.annotations = annotations
    next.promotedFragments = promotedFragments
    next
  }
}
