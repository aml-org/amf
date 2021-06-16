package amf.apicontract.internal.convert

import amf.core.internal.convert.CoreClientConverters
import amf.shapes.internal.convert.{ShapesBaseClientConverter, ShapesBaseConverter}

object ApiClientConverters
    extends ApiBaseConverter
    with ShapesBaseConverter
    with ApiBaseClientConverter
    with ShapesBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientLoader    = CoreClientConverters.ClientLoader
  override type ClientReference = CoreClientConverters.ClientReference
}
