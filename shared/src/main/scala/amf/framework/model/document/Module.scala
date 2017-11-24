package amf.framework.model.document

import amf.framework.metamodel.document.DocumentModel.{Declares => _, Location => _, References => _, Usage => _}
import amf.framework.metamodel.document.ModuleModel
import amf.framework.metamodel.document.ModuleModel._
import amf.framework.model.domain.{AmfObject, DomainElement}
import amf.framework.parser.{Annotations, Fields}
import org.yaml.model.YDocument

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {

  override def adopted(parent: String): this.type = withId(parent)

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(References)

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields(Declares)

  /** Returns the usage comment for de element */
  override def usage: String = fields(Usage)

  /** Returns the file location for the document that has been parsed to generate this model */
  override def location: String = fields(Location)

  /** Meta data for the document */
  override def meta = ModuleModel
}

trait DeclaresModel extends AmfObject {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares: Seq[DomainElement]

  def withDeclares(declarations: Seq[DomainElement]): this.type = setArrayWithoutId(Declares, declarations)
}

object Module {
  def apply(): Module = apply(Annotations())

  def apply(ast: YDocument): Module = apply(Annotations(ast))

  def apply(annotations: Annotations): Module = apply(Fields(), annotations)
}
