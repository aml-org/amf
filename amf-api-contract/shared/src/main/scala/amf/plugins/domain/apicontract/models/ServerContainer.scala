package amf.plugins.domain.apicontract.models

trait ServerContainer {
  def servers: Seq[Server]
  def withServers(servers: Seq[Server]): this.type
  def removeServers(): Unit
}
