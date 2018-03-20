package amf.client.handler

/** Interface that needs to be implemented to handle a success result from writing a file, or an exception if something went wrong. */
trait FileHandler {
  def success()
  def error(exception: Throwable)
}
