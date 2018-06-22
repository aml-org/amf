package amf.client.handler

/** Interface that needs to be implemented to handle a given result, or an exception if something went wrong. */
trait Handler[T] {
  def success(result: T)
  def error(exception: Throwable)
}
