package amf.plugins.domain.webapi.models

trait ServerContainer {
  def servers: Seq[Server]
  def withServers(servers: Seq[Server]): this.type
  def removeServers(): Unit
}