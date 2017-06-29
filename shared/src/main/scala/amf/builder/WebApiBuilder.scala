package amf.builder

import amf.model.WebApi

/**
  * Created by martin.gutierrez on 6/29/17.
  */
class WebApiBuilder extends Builder[WebApi] {
  var name: String            = ""
  var description: String     = ""
  var host: String            = ""
  var protocols: List[String] = List()

  def withProtocols(protocols: List[String]): this.type = {
    this.protocols = protocols
    this
  }

  def withHost(host: String): this.type = {
    this.host = host
    this
  }

  def withDescription(description: String): this.type = {
    this.description = description
    this
  }

  def withName(name: String): this.type = {
    this.name = name
    this
  }

  override def build: WebApi = new WebApi(name, description, host, protocols)
}

object WebApiBuilder {
  def apply(): WebApiBuilder = new WebApiBuilder()
}
