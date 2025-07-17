# 16. Agent Card Specification schema in AMF

Date: 2025-07-15

## Status

Accepted

## Context

AMF is adopting the agent specifications: MCP, Agent Fabric, and Agent 2 Agent.

AMF aims to remain as faithful as possible to the definitions of these specifications without making any changes to
them. In the future, the goal is to host the specifications in a separate repository so they can evolve independently,
and AMF will simply consume them.

The inability to add our own fields to the specifications limits our ability to extend the functionality of each API.
However, since most are JSON Schemas, they support additional properties.

A specific complication with Agent 2 Agent is that if we want to parse only an Agent Card, we don’t have an
IDEntry (a unique identifier for this type of API) to distinguish it from others. In some places, such as in
AMFConfiguration or in amf-service, there is logic that attempts to “guess” the specification type based on some
identifier.

## Decision

For the Agent 2 Agent case, we decided to parse
an [Agent Card](https://github.com/a2aproject/A2A/blob/main/specification/json/a2a.json) (`a2a.json`) and even define a
reduced schema from it, since the file includes definitions that don’t belong to the Agent Card object itself. Based on
this, we created the current `schema.json` located in the amf-agent-card module.

This schema will not include an identifier and will return 1.0 by default if necessary, to avoid breaking parsing and
guessing algorithms.
