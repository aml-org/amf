package amf.document

import amf.domain.{Annotations, DomainElement, Fields}

/**
  * RAML Fragments
  */
object Fragment {

  /** Units encoding domain fragments */
  sealed trait Fragment extends BaseUnit with EncodesModel {

    /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
    override val references: Seq[BaseUnit] = Nil

    override val fields: Fields = new Fields()

    /** Set of annotations for object. */
    override val annotations: Annotations = Annotations()

    override def adopted(parent: String): this.type = withId(parent)
  }

  case class DocumentationItem(location: String, encodes: DomainElement) extends Fragment

  case class DataType(location: String, encodes: DomainElement) extends Fragment

  case class NamedExample(location: String, encodes: DomainElement) extends Fragment

  case class ResourceType(location: String, encodes: DomainElement) extends Fragment

  case class Trait(location: String, encodes: DomainElement) extends Fragment

  case class AnnotationTypeDeclaration(location: String, encodes: DomainElement) extends Fragment

  case class Library(location: String, encodes: DomainElement) extends Fragment

  case class Overlay(location: String, encodes: DomainElement) extends Fragment

  case class Extension(location: String, encodes: DomainElement) extends Fragment

  case class SecurityScheme(location: String, encodes: DomainElement) extends Fragment

  case class Default(location: String, encodes: DomainElement) extends Fragment
}

trait EncodesModel {

  /** Encoded [[DomainElement]] described in the document element. */
  def encodes: DomainElement
}
