# 16. GRPC Protobuf 3 support in AMF

Date: 2025-12-29

## Status

Accepted

## Context

AMF is adding support for protobuf 3 APIs in its stack:

- Syntactic parsing Protobuf3 APIs with ANTLR
  using [this version](https://github.com/aml-org-emu/amf-antlr-ast/blob/master/grammars/Protobuf3.g4) of the grammar.
- Semantic parsing with AMF using the `GRPCConfiguration`.
- Transformation / Resolution with AMF.
- Validation with the standard [`protoc` validator](https://github.com/protocolbuffers/protobuf).

NOTES:

- only the APIs starting with `syntax = "proto3";` will be accepted by the `GRPCConfiguration`
- all other versions (e.g. `proto2` or the newer `edition = "2023";`) will be parsed as an `ExternalFragment`

## Restrictions

In addition to the protoc validations, we have the following AMF validations that we require:

- we require that the API defines a `package` property, or we add a default one

NOTE:
We have an outdated GRPC Grammar that we need to update, as of now, we don't support the `optional` property, as it
was originally removed when proto3 first launched (making all fields "implicit" where 0/empty was the default),
but was reintroduced in Protobuf v3.15 (released in early 2021).

## Mappings

Although a Protobuf 3 is not exactly an API, it shares similar concepts, and we will map it to the AMF models as follows:

| gRPC/Protobuf Concept | AMF Model Object | Specific object fields                                                                                                                                                                                                                                    |
|-----------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Service               | [EndPoint](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#endpoint) | The specific operation method (post, publish, suscribe, pubsub) is determined via [the table below](#operation-methods)                                                                                                                                                      |
| Service rpc           | [Operation](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#operation) | the input is a [Request](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#request) and the output a [Response](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#response)                                     |        |
| Message               | [NodeShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#nodeshape) | each field, map or oneof is contained in a [PropertyShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#propertyshape) where the `range` property contains the value (see below for each one)                                   |
| Message Field         | [PropertyShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#propertyshape) | it's options are mapped to the `customDomainProperties` property, the `serializationOrder` contains de number, and the `range` property contains the value                                                                                                |
| Message one of        | [AnyShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#anyshape) | PropertyShape that contains an [AnyShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#anyshape) in it's `range` and the oneof values are inside the `xone` property of that shape                                              |
| Message enum          | [ScalarShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#scalarshape) | PropertyShape that contains a [ScalarShape](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#scalarshape) where `values` property contains the enum values, and it's options are saved inside the `customDomainProperties` property |
| Option                | [DomainExtension](https://github.com/aml-org-emu/amf/blob/develop/documentation/model.md#domainextension) |                                                                                                                                                                                                                                                           |



The BaseUnit that results from parsing is a `Document` with a `declares` property that will contain all the messages and
enums declared.
The `package` property of the GRPC/Protobuf API is mapped to the `package` property of the AMF `Document`.
The `Document` also has an `encodes` property that contains the `WebApi` that encodes the Protobuf 3 API.

### Operation Methods

In gRPC, RPC methods can use streaming for requests and/or responses. AMF maps these different streaming patterns to specific operation methods based on the communication pattern they represent.

The operation method is determined by whether the request and response use streaming:

| Request Type | Response Type | AMF Operation Method | Description |
|--------------|---------------|---------------------|-------------|
| Unary (no stream) | Unary (no stream) | `post` | Client sends a single request and receives a single response (standard request-response) |
| Stream | Unary (no stream) | `publish` | Client sends a stream of messages and receives a single response (client streaming) |
| Unary (no stream) | Stream | `subscribe` | Client sends a single request and receives a stream of responses (server streaming) |
| Stream | Stream | `pubsub` | Client and server both send streams of messages (bidirectional streaming) |

#### Example

The following protobuf service demonstrates all four operation methods:

```protobuf
// Service definition maps to EndPoint in AMF model
service Greeter {
  option deprecated = false;
  
  // Unary RPC: single request → single response (maps to 'post')
  rpc PostExample (RequestMessage) returns (ResponseMessage) {
    option deprecated = true;
  }
  
  // Client streaming: stream of requests → single response (maps to 'publish')
  rpc PublishExample (stream RequestMessage) returns (ResponseMessage) {}
  
  // Server streaming: single request → stream of responses (maps to 'subscribe')
  rpc SubscribeExample (RequestMessage) returns (stream ResponseMessage) {}
  
  // Bidirectional streaming: stream of requests → stream of responses (maps to 'pubsub')
  rpc PubSubExample (stream RequestMessage) returns (stream ResponseMessage) {}
}
```