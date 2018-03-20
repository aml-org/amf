package amf.client.render

/**
  * Render options
  */
class RenderOptions {

  private var sources: Boolean = false

  /** Include source maps when rendering to graph. */
  def withSourceMaps: RenderOptions = {
    sources = true
    this
  }

  def isWithSourceMaps: Boolean = sources
}

object RenderOptions {
  def apply(): RenderOptions = new RenderOptions()
}
