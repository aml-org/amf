package amf.core.metamodel.domain

import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.{Field, Obj}
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * Reification of a link between elements in the model. Used when we want to capture the structure of the source document
  * in the graph itself. Linkable elements are just replaced by regular links after resolution.
  */
trait LinkableElementModel extends Obj {

  /**
    * Uri of the linked element
    */
  val TargetId = Field(Iri, Namespace.Document + "link-target")

  /**
    * Label for the type of link
    */
  val Label    = Field(Str, Namespace.Document + "link-label")

}

object LinkableElementModel extends LinkableElementModel {

  // 'Static' values, we know the element schema before parsing
  // If the domain element is dynamic, the value from the model,
  // not the meta-model, should be retrieved instead

  override val `type`: List[ValueType] = List(Namespace.Document + "Linkable")

  override val fields: List[Field] = List(TargetId, Label)

}
