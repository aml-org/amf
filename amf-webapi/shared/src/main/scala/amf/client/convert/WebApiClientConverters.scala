package amf.client.convert

import amf.client.convert.shapeconverters.{ShapesBaseClientConverter, ShapesBaseConverter}

object WebApiClientConverters
    extends WebApiBaseConverter
    with ShapesBaseConverter
    with WebApiBaseClientConverter
    with ShapesBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientLoader    = CoreClientConverters.ClientLoader
  override type ClientReference = CoreClientConverters.ClientReference
}
