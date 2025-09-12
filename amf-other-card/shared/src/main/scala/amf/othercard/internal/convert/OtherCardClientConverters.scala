package amf.othercard.internal.convert

import amf.core.internal.convert.CoreClientConverters

object OtherCardClientConverters extends OtherCardBaseConverter with OtherCardBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientLoader    = CoreClientConverters.ClientLoader
  override type ClientReference = CoreClientConverters.ClientReference
}
