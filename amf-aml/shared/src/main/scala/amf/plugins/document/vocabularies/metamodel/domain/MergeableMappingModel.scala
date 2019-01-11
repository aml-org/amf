package amf.plugins.document.vocabularies.metamodel.domain
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace

trait MergeableMappingModel {
  val MergePolicy = Field(
    Str,
    Namespace.Meta + "mergePolicy",
    ModelDoc(ModelVocabularies.Meta,
             "merge policy",
             "Indication of how to merge this graph node when applying a patch document")
  )

  val ALLOWED_MERGE_POLICY: Set[String] = Set(
    // Nodes: identity by URI           // scalars: identity by value
    "insert", // add or ignore if present         // add or ignore if present
    "delete", // remove or ignore if no present   // remove or ignore if no present
    "update", // recursive merge only if present  // replace
    "upsert", // recursive merge or add           //
    "ignore", // equivalent as not present        // equivalent as not present
    "fail" // fail                             // fail
  )
}
