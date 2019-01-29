package amf.plugins.document.vocabularies.model.domain
import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.utils._
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.UnionNodeMappingModel
import amf.plugins.document.vocabularies.metamodel.domain.UnionNodeMappingModel._
import org.yaml.model.YMap

case class UnionNodeMapping(fields: Fields, annotations: Annotations) extends DomainElement with Linkable with MergeableMapping with NodeWithDiscriminator[UnionNodeMapping] with NodeMappable {

  override def meta: Obj = UnionNodeMappingModel

  override def linkCopy(): Linkable = UnionNodeMapping().withId(id)
  override protected def classConstructor: (Fields,Annotations) => Linkable with DomainElement = UnionNodeMapping.apply
  override def componentId: String = "/" + name.value().urlComponentEncoded
}

object UnionNodeMapping {
  def apply(): UnionNodeMapping = apply(Annotations())

  def apply(ast: YMap): UnionNodeMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): UnionNodeMapping = UnionNodeMapping(Fields(), annotations)
}
