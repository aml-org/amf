package amf.domain

import amf.metadata.domain.UserDocumentationModel

/**
  *
  */
case class UserDocumentation(fields: Fields, annotations: Annotations) extends DomainElement {

  /** Call after object has been adopted by specified parent. */
  def title: String   = fields(UserDocumentationModel.Title)
  def content: String = fields(UserDocumentationModel.Content)

  override def adopted(parent: String): UserDocumentation.this.type = withId(parent + "/" + title)

  def withTitle(title: String): this.type     = set(UserDocumentationModel.Title, title)
  def withContent(content: String): this.type = set(UserDocumentationModel.Content, content)

}

object UserDocumentation {

  def apply(): UserDocumentation = apply(Annotations())

  def apply(annotations: Annotations): UserDocumentation = new UserDocumentation(Fields(), annotations)

}