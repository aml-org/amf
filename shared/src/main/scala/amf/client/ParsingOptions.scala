package amf.client

class ParsingOptions {
  private var exceptionOnFailedValidation = true

  def withoutExceptionOnFailedValidation(shouldThrow: Boolean): ParsingOptions = {
    exceptionOnFailedValidation = shouldThrow
    this
  }

  def isWithExceptionOnFailedValidation = exceptionOnFailedValidation
}

object ParsingOptions {
  def apply(): ParsingOptions = new ParsingOptions()
}
