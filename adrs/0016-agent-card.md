# 16. Agents Specifications schemas in AMF

Date: 2025-07-15

## Status

Accepted

## Context

AMF is adopting the following agent specifications, there are links to each schema definition:

- [mcp-metadata](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/mcp_metadata.json)
- [Agent Network](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/agent_network.json)
- [Agent 2 Agent](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/a2a.json)
- [other-card](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/other_card.json)
- [agent-metadata](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/agent_metadata.json)
- [llm-metadata](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/llm_metadata.json)

AMF aims to remain as faithful as possible to the definitions of these specifications without making any changes to
them. In the future, the goal is to host the specifications in a separate repository so they can evolve independently,
and AMF will simply consume them.

The inability to add our own fields to the specifications limits our ability to extend the functionality of each API.
However, since most are JSON Schemas, they support additional properties.

A specific complication with Agent 2 Agent is that if we want to parse only an Agent Card, we'll use the
`protocolVersion` as IDEntry (a unique identifier for this type of API) to distinguish it from others. This is not
explicitly said by the specification, but it's a required field so it should always be present.

The most problematic aspect as of now is that we can't reference another files in each schema,
and most of them share definitions. Thus, we must manually include all necessary dependencies/definitions each time we
want to update a schema.

## Decision

We've decided to create a new module for each specification, and include the necessary dependencies/definitions in each.

We'll later see how we can reference the shared definitions in each module
(mainly in the [a2a.json](https://github.com/a2aproject/A2A/blob/main/specification/json/a2a.json) specification).

This is a list of each schema and what dependencies have to be manually included when updating:

- other-card
  - AgentProvider
  - SecurityScheme (and all its inner definitions)
- llm-metadata
  - PolicyRef (from the [references.json](https://github.com/mulesoft-emu/agent-fabric-specification/blob/master/agent-fabric-schema/src/main/resources/references.json) file)
- agent-network
  - this module has all the dependencies from references.json, llm_metadata.json, some from the a2a.json and some from mcp_metadata.json
- agent-card
  - in the schema we add a $ref to the AgentCard definition