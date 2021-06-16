package amf.apicontract.client.scala.model.domain

trait ServerContainer {
  def servers: Seq[Server]
  def withServers(servers: Seq[Server]): this.type
  def removeServers(): Unit
}
