# 4. Async message examples are saved in separate fields

Date: 2020-10-27

## Status

Accepted

## Context

After a clarification in the async specification, the examples facet of a message object contains a list of pairs of examples, 
where one is associated to the payload while the other to the header. Due to time restricitions, this change has to be introduced without a breaking change in the model.

## Decision

Within the message model a new field has been added to store header examples. The payload examples are being stored in a separate field.

## Consequences

There is no way to conserve the relation of pairs that one header example can have with a payload example.
ExampleIndex annotation is defined in each example inorder to maintain consistent emission.
