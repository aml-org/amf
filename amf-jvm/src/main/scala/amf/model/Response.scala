package amf.model

import scala.collection.JavaConverters._

/**
  * response jvm class
  */
case class Response private[model] (private val response: amf.domain.Response) extends DomainElement {

  val name: String                       = response.name
  val description: String                = response.description
  val statusCode: String                 = response.statusCode
  val headers: java.util.List[Parameter] = response.headers.map(Parameter).asJava
  val payloads: java.util.List[Payload]  = response.payloads.map(Payload).asJava

  override def equals(other: Any): Boolean = other match {
    case that: Response =>
      (that canEqual this) &&
        response == that.response
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Response]

  override private[amf] def element: amf.domain.Response = response
}
