# 2. Async uses its editing pipeline as validation pipeline

Date: 2020-09-18

## Status

Accepted

## Context

Currently the async validation pipeline is missing many core resolution stages present in the default and editing pipeline, such as JsonMergePatch and AsyncExamplesPropagation. This is causes missing validations, as well as exceptions in resolution. In many cases fixes are applied to the default and editing pipelines but the validation pipeline is left behind.

## Decision

The async editing pipeline will be used as the validation pipeline.

## Consequences

This will reduce the amount of pipelines that have to be maintained (default and editing), and ensure same results when directly validating an api vs resolving with editing and then validating.
The current async validation pipeline is inheriting many resolution stages that are not relevant in async, so this would also be an optimization.
Using editing pipeline may lead to a larger resulting model, given that the stage ExternalSourceRemovalStage is not present in the async editing pipeline.

