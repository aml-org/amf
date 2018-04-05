package amf.client.convert

object WebApiClientConverters
    extends WebApiBaseConverter
    with DataShapesBaseConverter
    with WebApiBaseClientConverter
    with DataShapesBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
}
