package amf.apicontract.client.scala.model.document

import amf.apicontract.client.scala.model.domain.common.VersionedAmfObject
import amf.apicontract.internal.metamodel.document.ComponentModuleModel
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.{NamedAmfObject, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.document.ComponentModuleModel._
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.metamodel.domain.DomainElementModel.CustomDomainProperties
import org.yaml.model.YDocument

/** Component Module model class */
class ComponentModule(override val fields: Fields, override val annotations: Annotations)
    extends Module(fields, annotations)
    with NamedAmfObject
    with VersionedAmfObject {

  override def meta = ComponentModuleModel

  override protected def nameField: Field = Name

  override def customDomainProperties: Seq[DomainExtension] = fields.field(CustomDomainProperties)

  override def withCustomDomainProperties(extensions: Seq[DomainExtension]): this.type =
    setArray(CustomDomainProperties, extensions)

  override def withCustomDomainProperty(extensions: DomainExtension): this.type =
    add(CustomDomainProperties, extensions)

}

object ComponentModule {
  def apply(): ComponentModule = apply(Annotations())

  def apply(ast: YDocument): ComponentModule = apply(Annotations(ast))

  def apply(annotations: Annotations): ComponentModule = new ComponentModule(Fields(), annotations)
}
