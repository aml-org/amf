package amf.model

import java.util

import amf.builder.WebApiBuilder
import scala.collection.JavaConverters._

/**
  * Created by martin.gutierrez on 7/3/17.
  */
case class WebApi(name: String,
                  description: String,
                  host: String,
                  schemeList: List[String],
                  basePath: String,
                  accepts: String,
                  contentType: String,
                  version: String,
                  termsOfService: String,
                  provider: Organization,
                  license: License,
                  documentation: CreativeWork)
    extends WebApiModel {

  override protected def createBuilder(): WebApiBuilder = new WebApiBuilder

  val scheme: util.List[String] = schemeList.asJava
}
