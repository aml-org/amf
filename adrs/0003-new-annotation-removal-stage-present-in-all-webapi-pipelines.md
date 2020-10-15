# 3. New annotation removal stage present in all webapi pipelines

Date: 2020-10-21

## Status

Accepted

## Context

When referencing external files there are certain cases where AMF inlines the parsed content without creating a links (this is because the targeted elements are not present in the references of the base unit). 
For these cases, when a emitting an unresolved model these references are being emitted inlined. 

## Decision

In order to avoid emitting these references inlined for an unresolved model, we must make use of annotation to save the original reference. 
When saving this reference, we must make sure that if the model is resolved this annotation is no longer present so that the emitter does not render references for a flattened model. 
This leads to the creation of a new resolution stage that removes specific annotations from the model that must not be present in a resolved base unit.

## Consequences

References can be emitted when receiving an unresolved model with inlined content. 
This stage iterates over the whole model removing specific annotations which can lead to more processing, however performance tests have been made showing that this processing is insignificant. 
