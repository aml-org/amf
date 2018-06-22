package amf.core.utils

/**
  * Lazy instance
  */
class Lazy[T](private val producer: () => T) {

  private var value: Option[T] = None

  def getOrCreate: T = {
    value = Some(value.fold({
      producer()
    })(r => r))
    value.get
  }

  def option: Option[T] = value
}
