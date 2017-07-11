package amf.model

import java.util

import amf.builder.WebApiBuilder

import scala.collection.JavaConverters._

/**
  * JVM WebApi class.
  */
case class WebApi(private val fs: Fields) extends BaseWebApi(fs) {

  val schemesArray: util.List[String]     = schemes.asJava
  val endPointsArray: util.List[EndPoint] = endPoints.asJava

  override def toBuilder: WebApiBuilder = new WebApiBuilder().copy(fs)
}
