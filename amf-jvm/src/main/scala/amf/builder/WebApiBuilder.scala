package amf.builder

import java.util

import amf.model.{WebApi, WebApiModel}

import scala.collection.JavaConverters._

/**
  * Created by martin.gutierrez on 7/3/17.
  */
class WebApiBuilder extends BaseWebApiBuilder {

  def withScheme(scheme: util.List[String]): this.type = super.withScheme(scheme.asScala.toList)
}
