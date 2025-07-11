package amf.agentfabric.internal.convert

import amf.core.internal.convert.CoreClientConverters

object AgentFabricClientConverters extends AgentFabricBaseConverter with AgentFabricBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientLoader    = CoreClientConverters.ClientLoader
  override type ClientReference = CoreClientConverters.ClientReference
}
