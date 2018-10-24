package amf.client.render

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Render options
  */
@JSExportAll
@JSExportTopLevel("render.RenderOptions")
class RenderOptions {

  private var sources: Boolean       = false
  private var compactUris: Boolean   = false
  private var amfJsonLdSerialization = true

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

  def withCompactUris: RenderOptions = {
    compactUris = true
    this
  }

  def withoutCompactUris: RenderOptions = {
    compactUris = false
    this
  }

  /**
    * Emit specific AMF JSON-LD serialization
    *
    * @return
    */
  def withoutAmfJsonLdSerialization: RenderOptions = {
    amfJsonLdSerialization = false
    this
  }

  /**
    * Emit regular JSON-LD serialization
    *
    * @return
    */
  def withAmfJsonLdSerialization: RenderOptions = {
    amfJsonLdSerialization = true
    this
  }
  def isWithCompactUris: Boolean       = compactUris
  def isWithSourceMaps: Boolean        = sources
  def isAmfJsonLdSerilization: Boolean = amfJsonLdSerialization
}

object RenderOptions {
  def apply(): RenderOptions = new RenderOptions()
}
