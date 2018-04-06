package amf.core.emitter

import amf.plugins.document.graph.parser.ScalarEmitter
import amf.client.render.{RenderOptions => ClientRenderOptions}

/**
  * Render options
  */
class RenderOptions {

  private var sources: Boolean                     = false
  private var customEmitter: Option[ScalarEmitter] = None

  /** Include source maps when rendering to graph. */
  def withSourceMaps: RenderOptions = {
    sources = true
    this
  }

  /** Include source maps when rendering to graph. */
  def withoutSourceMaps: RenderOptions = {
    sources = false
    this
  }

  def withCustomEmitter(emitter: ScalarEmitter): RenderOptions = {
    customEmitter = Some(emitter)
    this
  }

  def isWithSourceMaps: Boolean = sources

  def getCustomEmitter: Option[ScalarEmitter] = customEmitter
}

object RenderOptions {
  def apply(): RenderOptions = new RenderOptions()

  def apply(client: ClientRenderOptions): RenderOptions = {
    if (client.isWithSourceMaps)
      new RenderOptions().withSourceMaps
    else
      new RenderOptions().withoutSourceMaps
  }
}
