package amf.core.resolution.stages
import amf.core.metamodel.Type.Iri
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.parser.{Annotations, ErrorHandler, FieldEntry, Value}
import amf.core.vocabulary.Namespace

import scala.collection.mutable

class UrlShortenerStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    val ids: Set[String] = Set(model.id) ++ model.references.map(_.id)
    shorten(model, ids)
    model.withId(base)
  }

  def shorten(element: AmfElement, ids: Set[String]): Unit = {
    element match {
      case o: AmfObject =>
        val shorthenId = shortener.shorten(o.id)
        if (!shorthenId.equals(o.id)) {
          o.withId(shortener.shorten(o.id))
          val fieldEntries = o match {
            case d: DynamicDomainElement => d.meta.fields.flatMap(f => d.valueForField(f).map(v => FieldEntry(f, v)))
            case _                       => o.fields.fields()
          }
          fieldEntries.foreach {
            case FieldEntry(f, value: Value) if f == LinkableElementModel.Target =>
              value.value match {
                case o: AmfObject => o.withId(shortener.shorten(o.id))
                case _            => // ignore
              }
            case FieldEntry(f, value: Value) if f.`type` == Iri =>
              shorten(value.annotations)
              val v = value.value.toString
              if (ids.exists(i => v.startsWith(i)))
                value.value = AmfScalar(shortener.shorten(v), value.value.annotations)
            case FieldEntry(_, value: Value) =>
              shorten(value.value, ids)
              shorten(value.annotations)
          }
        }
      case a: AmfArray =>
        a.values.foreach { v =>
          shorten(v, ids)
        }
      case _ => // ignore
    }
    shorten(element.annotations)
  }

  private def isKnowNamespace(value: String): Boolean = {
    value
      .split("#")
      .headOption
      .flatMap(Namespace.find)
      .isEmpty
  }
  def shorten(annotations: Annotations): Unit = {
    annotations.map {
      case a: UriAnnotation => a.shorten(shortener.shorten)
      case other            => other
    }
  }

  private val shortener = Shortener()

  private case class Shortener(dictionary: mutable.Map[String, String] = mutable.Map()) {
    private var c: Int = -1

    def shorten(uri: String): String =
      if (uri.nonEmpty && !uri.startsWith(s"$base#")) {
        dictionary.getOrElseUpdate(uri, {
          c = c + 1
          s"$base#" + c
        })
      } else uri
  }

  private val base = "amf://id"

}
