package amf.core.resolution.stages
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.parser.{Annotations, ErrorHandler, Value}
import amf.core.vocabulary.Namespace

import scala.collection.mutable

class UrlShortenerStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    shorten(model)
    model.withId(base)
  }

  def shorten(element: AmfElement): Unit = {
    element match {
      case o: AmfObject =>
        o.withId(shortener.shorten(o.id))
        val toReplace = mutable.Map[Field, Value]()
        o.fields.filter({ case (f, v) => f != LinkableElementModel.Target }).foreach {
          case (f, value: Value)
              if f.`type` == Iri && value.value.toString
                .split("#")
                .headOption
                .flatMap(Namespace.find)
                .isEmpty => // todo: how to know if a field.type IRI is id or namespace?
            shorten(value.annotations)
            if (value.value.toString.nonEmpty)
              toReplace.put(f, Value(AmfScalar(shortener.shorten(value.value.toString)), value.annotations))
          case (_, value: Value) =>
            shorten(value.value)
            shorten(value.annotations)
        }
        toReplace.foreach { case (f, v) => o.set(f, v.value, v.annotations) }
      case a: AmfArray => a.values.foreach(shorten)
      case _           => // ignore
    }
    shorten(element.annotations)
  }

  def shorten(annotations: Annotations): Unit = {
    annotations
      .collect({ case a: UriAnnotation => a })
      .foreach { a =>
        annotations.reject(_ == a)
        annotations += a.shorten(shortener.shorten)
      }
  }

  private val shortener = Shortener()

  private case class Shortener(dictionary: mutable.Map[String, String] = mutable.Map()) {
    private var c: Int = -1

    def shorten(uri: String): String =
      if (uri.nonEmpty && !uri.matches("amf://id#[0-9]+")) {
        dictionary.getOrElseUpdate(uri, {
          c = c + 1
          val result = s"$base#" + c
          dictionary.put(uri, result)
          result
        })
      } else uri
  }

  private val base = "amf://id"

}
