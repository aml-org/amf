package amf.framework.plugins

trait AMFPlugin {
  val ID: String
  def dependencies(): Seq[AMFPlugin]
}
