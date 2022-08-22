package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.{AmfObject, AmfScalar}
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.metamodel.Type.{Iri, Scalar, Str}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDScalar(override val value: Any, val dataType: String) extends AmfScalar(value) with JsonLDElement {

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.virtual()
}
