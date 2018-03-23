package amf.client.convert

object VocabulariesClientConverter extends VocabulariesBaseConverter with VocabulariesBaseClientConverter {
  // Overriding to match type
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
}
