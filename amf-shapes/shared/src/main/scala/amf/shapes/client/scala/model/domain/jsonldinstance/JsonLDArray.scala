package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, DomainElement}
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}

import scala.collection.mutable

class JsonLDArray() extends AmfArray(Nil) with JsonLDElement {

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.virtual()

  def +=(value: JsonLDElement): Unit = {
    values = values :+ value
  }

  def jsonLDElements: Seq[JsonLDElement] = values.collect({ case e: JsonLDElement => e })

//  override def cloneElement(branch: mutable.Map[AmfObject, AmfObject]): AmfElement = {
//    val cloned = new JsonLDArray()
//    values.map(_.cloneElement(branch)).foreach({ case v:JsonLDElement => cloned += v})
//    cloned
//  }
}
