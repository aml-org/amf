package amf.client

/**
  *
  */
trait Handler[T] {
  def success(document: T)
  def error(exception: Throwable)
}