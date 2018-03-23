package amf.client.convert

object WebApiClientConverters
    extends WebApiBaseConverter
    with DataShapesBaseConverter
    with WebApiBaseClientConverter
    with DataShapesBaseClientConverter {
  // Overriding to match type
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
}
