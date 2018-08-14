package amf.core.resolution.stages
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject, UriAnnotation}
import amf.core.parser.{Annotations, ErrorHandler, Value}

import scala.collection.mutable

class UrlShortenerStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    shorten(model)
    model
  }

  def shorten(element: AmfElement): Unit = {
    element match {
      case o: AmfObject =>
        o.withId(shortener.shorten(o.id))
        o.fields.foreach {
          case (_, value: Value) =>
            shorten(value.value)
            shorten(value.annotations)
        }
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
    private var c: Int = 0

    def shorten(uri: String): String =
      if (uri.nonEmpty) {
        dictionary.getOrElseUpdate(uri, {
          c = c + 1
          val result = "amf://" + c
          dictionary.put(uri, result)
          result
        })
      } else uri
  }

}
