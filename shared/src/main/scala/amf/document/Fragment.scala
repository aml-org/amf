package amf.document

import amf.domain.DomainElement
import amf.remote.URL

/**
  * RAML Fragments
  */
object Fragment {

  /** Units encoding domain fragments */
  sealed trait Fragment extends BaseUnit with EncodesModel {

    /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
    override def references(): Seq[URL] = Nil
  }

  case class DocumentationItem(location: URL, encodes: DomainElement) extends Fragment

  case class DataType(location: URL, encodes: DomainElement) extends Fragment

  case class NamedExample(location: URL, encodes: DomainElement) extends Fragment

  case class ResourceType(location: URL, encodes: DomainElement) extends Fragment

  case class Trait(location: URL, encodes: DomainElement) extends Fragment

  case class AnnotationTypeDeclaration(location: URL, encodes: DomainElement) extends Fragment

  case class Library(location: URL, encodes: DomainElement) extends Fragment

  case class Overlay(location: URL, encodes: DomainElement) extends Fragment

  case class Extension(location: URL, encodes: DomainElement) extends Fragment

  case class SecurityScheme(location: URL, encodes: DomainElement) extends Fragment

  case class Default(location: URL, encodes: DomainElement) extends Fragment
}

trait EncodesModel {

  /** Encoded [[DomainElement]] described in the document element. */
  def encodes(): DomainElement
}
