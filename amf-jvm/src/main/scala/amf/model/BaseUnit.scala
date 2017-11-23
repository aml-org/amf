package amf.model

import amf.framework.model.document
import amf.vocabulary.Namespace
import amf.plugins.document.webapi.model.{
  AnnotationTypeDeclarationFragment => CoreAnnotationTypeDeclarationFragment,
  DataTypeFragment => CoreDataTypeFragment,
  DialectFragment => CoreDialectFragment,
  DocumentationItemFragment => CoreDocumentationItemFragment,
  ExternalFragment => CoreExternalFragment,
  NamedExampleFragment => CoreNamedExampleFragment,
  ResourceTypeFragment => CoreResourceTypeFragment,
  SecuritySchemeFragment => CoreSecuritySchemeFragment,
  TraitFragment => CoreTraitFragment
}


import scala.collection.JavaConverters._

/** Any parsable unit, backed by a source URI. */
trait BaseUnit {

  private[amf] val element: amf.framework.model.document.BaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  lazy val references: java.util.List[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map {
      case r: document.Module                       => Module(r)
      case dt: CoreDataTypeFragment                 => DataType(dt)
      case a: CoreAnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(a)
      case t: CoreTraitFragment                     => TraitFragment(t)
      case rt: CoreResourceTypeFragment             => ResourceTypeFragment(rt)
      case ne: CoreNamedExampleFragment             => NamedExample(ne)
      case df: CoreDialectFragment                  => DialectFragment(df)
      case di: CoreDocumentationItemFragment        => DocumentationItem(di)
    }
    units.asJava
  }

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = element.location

  def usage: String = element.usage

  def findById(id: String): DomainElement = {
    element.findById(Namespace.uri(id).iri()) match {
      case Some(e: DomainElement) => DomainElement(e)
      case _                      => null
    }
  }

  def findByType(typeId: String): java.util.List[DomainElement] =
    element.findByType(Namespace.expand(typeId).iri()).map(e => DomainElement(e)).asJava
}
