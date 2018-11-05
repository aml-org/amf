package amf.core.emitter

import amf.plugins.document.graph.parser.ScalarEmitter
import amf.client.render.{RenderOptions => ClientRenderOptions}
import amf.core.metamodel.Field

/**
  * Render options
  */
class RenderOptions {

  private var sources: Boolean                     = false
  private var customEmitter: Option[ScalarEmitter] = None
  private var compactUris: Boolean                 = false
  private var rawSourceMaps: Boolean               = false
  private var validating: Boolean                  = false
  private var filterFields: Field => Boolean     = (_: Field) => false
  private var amfJsonLdSerialization               = true

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

  def withRawSourceMaps: RenderOptions = {
    rawSourceMaps = true
    this
  }

  def withoutRawSourceMaps: RenderOptions = {
    rawSourceMaps = false
    this
  }

  def withCustomEmitter(emitter: ScalarEmitter): RenderOptions = {
    customEmitter = Some(emitter)
    this
  }

  def withValidation: RenderOptions = {
    validating = true
    this
  }

  def withoutValidation: RenderOptions = {
    validating = false
    this
  }

  def withFilterFieldsFunc(f: Field => Boolean): RenderOptions = {
    filterFields = f
    this
  }

  def withoutAmfJsonLdSerialization: RenderOptions = {
    amfJsonLdSerialization = false
    this
  }

  def withAmfJsonLdSerialization: RenderOptions = {
    amfJsonLdSerialization = true
    this
  }

  def isCompactUris: Boolean                  = compactUris
  def isWithSourceMaps: Boolean               = sources
  def isWithRawSourceMaps: Boolean             = rawSourceMaps
  def getCustomEmitter: Option[ScalarEmitter] = customEmitter
  def isAmfJsonLdSerilization: Boolean        = amfJsonLdSerialization
  def isValidation: Boolean =
    customEmitter.isDefined || validating // I consider that if CustomEmitter is defined, is a validation
  def renderField(field: Field): Boolean = !filterFields(field)
}

object RenderOptions {
  def apply(): RenderOptions = new RenderOptions()

  def apply(client: ClientRenderOptions): RenderOptions = {
    val opts = new RenderOptions()

    if (client.isWithSourceMaps)
      opts.withSourceMaps
    else
      opts.withoutSourceMaps

    if (client.isWithCompactUris)
      opts.withCompactUris
    else
      opts.withoutCompactUris

    if (client.isAmfJsonLdSerilization)
      opts.withAmfJsonLdSerialization
    else
      opts.withoutAmfJsonLdSerialization

    opts
  }
}
