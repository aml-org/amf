package amf.client

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
}

object GenerationOptions {
  def apply(): GenerationOptions = new GenerationOptions()
}
