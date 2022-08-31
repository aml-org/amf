package amf.apicontract.internal.metamodel.domain.common

import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

/** Version field.
  */
trait VersionField {
  val Version =
    Field(Str, Core + "version", ModelDoc(ModelVocabularies.Core, "version", "Version of the API"))
}

object VersionField extends VersionField
