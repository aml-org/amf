package amf.plugins.document.webapi.contexts

trait SpecAwareContext {
  val factory: SpecVersionFactory
}
trait SpecVersionFactory {}