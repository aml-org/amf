package amf.model

import amf.document
import amf.vocabulary.Namespace

import scala.collection.JavaConverters._

/** Any parsable unit, backed by a source URI. */
trait BaseUnit {

  private[amf] val element: amf.document.BaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  lazy val references: java.util.List[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map {
      case r: document.Module                             => Module(r)
      case dt: document.Fragment.DataType                 => DataType(dt)
      case a: document.Fragment.AnnotationTypeDeclaration => AnnotationTypeDeclaration(a)
      case t: document.Fragment.TraitFragment             => TraitFragment(t)
      case rt: document.Fragment.ResourceTypeFragment     => ResourceTypeFragment(rt)
      case ne: document.Fragment.NamedExample             => NamedExample(ne)
      case df: document.Fragment.DialectFragment          => DialectFragment(df)
      case di: document.Fragment.DocumentationItem        => DocumentationItem(di)
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
