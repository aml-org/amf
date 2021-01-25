package amf.client.`new`

import amf.core.errorhandling.ErrorHandler

trait ErrorHandlerProvider {

  // Returns a new instance of error handler to collect results
  def errorHandler(): ErrorHandler
}
