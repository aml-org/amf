# 16. Agent Card Specification schema in AMF

Date: 2025-07-15

## Status

Accepted

## Context

AMF is adopting the agent specifications: MCP, Agent domain, and Agent 2 Agent (Agent Card).

AMF aims to remain as faithful as possible to the definitions of these specifications without making any changes to
them. In the future, the goal is to host the specifications in a separate repository so they can evolve independently,
and AMF will simply consume them.

The inability to add our own fields to the specifications limits our ability to extend the functionality of each API.
However, since most are JSON Schemas, they support additional properties.

A specific complication with Agent 2 Agent is that if we want to parse only an Agent Card, we'll use the
`protocolVersion` as IDEntry (a unique identifier for this type of API) to distinguish it from others. This is not
explicitly said by the specification, but it's a required field so it should always be present.

## Decision

For the Agent 2 Agent case, we decided to parse
an [Agent Card](https://github.com/a2aproject/A2A/blob/main/specification/json/a2a.json) (`a2a.json`) and even define a
reduced schema from it, since the file includes definitions that don’t belong to the Agent Card object itself. Based on
this, we created the current `schema.json` located in the amf-agent-card module, bringing the AgentCard definition and
manually including all necessary dependencies/definitions.

NOTE: this brings an overhead, everytime the `a2a.json` schema changes, we must re-do the manual extraction.
