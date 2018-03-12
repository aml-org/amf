package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.Obj
import amf.core.utils.Strings
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.ExternalModel
import amf.plugins.document.vocabularies.metamodel.domain.ExternalModel._
import org.yaml.model.YMap

case class External(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = ExternalModel
  override def adopted(parent: String): External.this.type = Option(alias) match {
    case Some(alias) => withId(parent + "/externals/" + alias.urlEncoded)
    case None        => throw new Exception("Cannot set ID of external without alias")
  }

  def withAlias(alias: String): this.type = set(Alias, alias)
  def withBase(base: String): this.type = set(Base, base)

  def alias: String = fields(Alias)
  def base: String = fields(Base)
}

object External {

  def apply(): External = apply(Annotations())

  def apply(ast: YMap): External = apply(Annotations(ast))

  def apply(annotations: Annotations): External = External(Fields(), annotations)
}
