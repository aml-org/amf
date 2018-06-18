package amf.core.model.document

import amf.core.metamodel.document.DocumentModel.{Declares => _, Location => _, References => _, Usage => _}
import amf.core.metamodel.document.ModuleModel
import amf.core.metamodel.document.ModuleModel._
import amf.core.model.domain.{AmfObject, DomainElement}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YDocument

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(References)

  /** Declared DomainElements that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields(Declares)

  /** Meta data for the document */
  override def meta = ModuleModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

trait DeclaresModel extends AmfObject {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares: Seq[DomainElement]

  def withDeclares(declarations: Seq[DomainElement]): this.type = setArrayWithoutId(Declares, declarations)

  def withDeclaredElement(element: DomainElement): this.type = add(Declares, element)
}

object Module {
  def apply(): Module = apply(Annotations())

  def apply(ast: YDocument): Module = apply(Annotations(ast))

  def apply(annotations: Annotations): Module = apply(Fields(), annotations)
}
