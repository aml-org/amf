package amf.core.plugins

trait AMFPlugin {
  val ID: String
  def dependencies(): Seq[AMFPlugin]
}
