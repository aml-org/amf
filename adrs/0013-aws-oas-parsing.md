# 13. AWS OAS parsing

Date: 2023-08-07

## Status

Accepted

## Context

AWS API Gateway produces exports of the gateway interface as an extended OAS 3.0.0 document. At first we attempted 
parsing this document using the standard OAS 3.0.0 parser with _semantic extensions_. This was successful for most of 
the extensions except for two:
* x-amazon-apigateway-any-method
* x-amazon-apigateway-integration (using `$ref`)


### x-amazon-apigateway-any-method
This extension should be parsed as a regular OAS 3.0.0 operation. We do not have a way to call the OAS 3.0.0 
operation parser from a semantic extension.

### x-amazon-apigateway-integration
This extension might use a `$ref` to a component declared using the _x-amazon-apigateway-integrations_ extension. 
We don't have a way to declare re-usable semantic extensions.

## Decision

Implement a custom AWS OAS 3.0.0 parser. The parser uses a hybrid mechanism between native parsing and semantic extension
parsing.

### Parsing _x-amazon-apigateway-any-method_
1. Marks the _x-amazon-apigateway-any-method_ extension as a Well Known Extension to avoid the default extension parsing
2. Extends the OAS 3.0.0 endpoint parser calling the operation parser with the value of the extension

**Note**: it is not necessary to declare an extension & annotation mapping for _x-amazon-apigateway-any-method_ in the 
Dialect because we handle the extension manually.

### Parsing _x-amazon-apigateway-integration_
1. Marks the _x-amazon-apigateway-integration_ extension as a Well Known Extension to avoid the default extension parsing
2. Parses the _x-amazon-apigateway-integrations_ extension using semex
3. Indexes the parsed integrations by their name in a custom AWS OAS Declarations class
4. Extends the OAS 3.0.0 operation parser to parse handling the extension:
   1. If the value of the extension is a `$ref` lookup the parsed and indexed extension declaration
   2. If the value of the extension is not a `$ref` parse the value using semantic extensions

**Note**: it should not be necessary to declare an annotation mapping & extension for _x-amazon-apigateway-integration_ 
in the Dialect because we handle the extension manually. However, doing so allows to reuse much of the semantic
extension parsing code (e.g. indexing and finding annotation mappings). 

### Parsing other extensions

Done with semantic extensions.

## Consequences

None so far.