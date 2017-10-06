package amf.document

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.document.DocumentModel.{Declares => _, Location => _, References => _, Usage => _}
import amf.metadata.document.FragmentModel
import amf.metadata.document.ModuleModel._
import amf.model.AmfObject
import org.yaml.model.YDocument

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {

  override def adopted(parent: String): this.type = withId(parent)

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(References)

  /** Declared [[amf.domain.DomainElement]]s that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields(Declares)

  /** Returns the usage comment for de element */
  override def usage: String = fields(Usage)

  /** Returns the file location for the document that has been parsed to generate this model */
  override def location: String = fields(Location)
}

trait DeclaresModel extends AmfObject {

  /** Declared [[amf.domain.DomainElement]]s that can be re-used from other documents. */
  def declares: Seq[DomainElement]

  def withDeclares(declarations: Seq[DomainElement]): this.type = setArrayWithoutId(Declares, declarations)
}

object Module {
  def apply(): Module = apply(Annotations())

  def apply(ast: YDocument): Module = apply(Annotations(ast))

  def apply(annotations: Annotations): Module = apply(Fields(), annotations)
}
