package amf.core.client

/**
  * Generation options
  */
class GenerationOptions {

  private var sources: Boolean = false

  /** Include source maps on generation. */
  def withSourceMaps: GenerationOptions = {
    sources = true
    this
  }

  def isWithSourceMaps: Boolean = sources
}

object GenerationOptions {
  def apply(): GenerationOptions = new GenerationOptions()
}
