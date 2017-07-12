package amf.model

import java.util

import amf.builder.WebApiBuilder

import scala.collection.JavaConverters._

/**
  * Created by martin.gutierrez on 7/3/17.
  */
case class WebApi(private val fs: Fields) extends BaseWebApi(fs) {

  val schemesArray: util.List[String] = schemes.asJava

  override def toBuilder: WebApiBuilder = new WebApiBuilder().copy(fs)
}
