package amf.core.metamodel.domain

import amf.core.metamodel.{Field, Obj}
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.vocabulary.Namespace.Shacl
import amf.core.vocabulary.{Namespace, ValueType}

trait ExternalSourceElementModel extends Obj {
  val Raw         = Field(Str, Shacl + "raw")
  val ReferenceId = Field(Iri, Namespace.Document + "reference-id")

  override val dynamic = true

}

object ExternalSourceElementModel extends ExternalSourceElementModel {

  override val fields                  = List(Raw, ReferenceId)
  override val `type`: List[ValueType] = List(Namespace.Document + "ExternalSource")

}
