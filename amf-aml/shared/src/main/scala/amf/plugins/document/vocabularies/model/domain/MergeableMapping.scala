package amf.plugins.document.vocabularies.model.domain
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.metamodel.domain.MergeableMappingModel

trait MergeableMapping extends  MergeableMappingModel { this: DomainElement =>
  def mergePolicy: StrField = fields.field(MergePolicy)

  def withMergePolicy(mergePolicy: String): MergeableMapping = {
    if (ALLOWED_MERGE_POLICY.contains(mergePolicy)) {
      set(MergePolicy, mergePolicy)
    } else {
      throw new Exception(s"Unknown merging policy: '$mergePolicy'")
    }
  }
}
