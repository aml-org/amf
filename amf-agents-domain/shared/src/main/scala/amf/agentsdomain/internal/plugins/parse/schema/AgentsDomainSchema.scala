package amf.agentsdomain.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentsDomainSchema extends JsonSchemaBasedSpecSchema{

  override def schema: String =
    """{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "title": "Agent Factory",
      |  "type": "object",
      |  "required": [
      |    "apiVersion",
      |    "agents",
      |    "agents-domain"
      |  ],
      |  "properties": {
      |    "apiVersion": {
      |      "anyOf": [
      |        {
      |          "type": "string"
      |        },
      |        {
      |          "type": "number"
      |        }
      |      ]
      |    },
      |    "agents": {
      |      "type": "object",
      |      "patternProperties": {
      |        "^[a-zA-Z_][a-zA-Z0-9_.-]*$": {
      |          "$ref": "#/definitions/Agent"
      |        }
      |      }
      |    },
      |    "agents-domain": {
      |      "$ref": "#/definitions/AgentsDomain"
      |    }
      |  },
      |  "additionalProperties": false,
      |  "definitions": {
      |    "AgentSpec": {
      |      "type": "object",
      |      "required": [
      |        "llm",
      |        "instructions"
      |      ],
      |      "properties": {
      |        "llm": {
      |          "type": "string"
      |        },
      |        "instructions": {
      |          "type": "array",
      |          "items": {
      |            "type": "string"
      |          }
      |        },
      |        "tools": {
      |          "type": "array",
      |          "items": {
      |            "type": "object",
      |            "properties": {
      |              "mcp": {
      |                "type": "object",
      |                "required": [
      |                  "server"
      |                ],
      |                "properties": {
      |                  "server": {
      |                    "type": "string"
      |                  },
      |                  "allowed": {
      |                    "type": "array",
      |                    "items": {
      |                      "type": "string"
      |                    }
      |                  }
      |                }
      |              }
      |            }
      |          }
      |        },
      |        "links": {
      |          "type": "array",
      |          "items": {
      |            "type": "object",
      |            "patternProperties": {
      |              "^[a-zA-Z_][a-zA-Z0-9_.-]*$": {
      |                "type": "object",
      |                "properties": {
      |                  "authentication": {
      |                    "$ref": "#/definitions/Authentication"
      |                  }
      |                }
      |              }
      |            }
      |          }
      |        }
      |      }
      |    },
      |    "Agent": {
      |      "type": "object",
      |      "properties": {
      |        "card": {
      |          "$ref": "#/definitions/AgentCard"
      |        },
      |        "spec": {
      |          "$ref": "#/definitions/AgentSpec"
      |        }
      |      },
      |      "required": [
      |        "card",
      |        "spec"
      |      ]
      |    },
      |    "BasicAuth": {
      |      "type": "object",
      |      "title": "Basic Authentication",
      |      "description": "Properties for Basic Authentication.",
      |      "properties": {
      |        "type": {
      |          "const": "basic",
      |          "type": "string"
      |        },
      |        "username": {
      |          "type": "string",
      |          "description": "The username for authentication."
      |        },
      |        "password": {
      |          "type": "string",
      |          "description": "The password for authentication."
      |        }
      |      },
      |      "required": [
      |        "type",
      |        "username",
      |        "password"
      |      ]
      |    },
      |    "AnypointClientCredentialsAuth": {
      |      "type": "object",
      |      "properties": {
      |        "type": {
      |          "const": "apikey-client-credentials",
      |          "type": "string"
      |        },
      |        "clientId": {
      |          "type": "string",
      |          "description": "The client ID."
      |        },
      |        "clientSecret": {
      |          "type": "string",
      |          "description": "The client secret."
      |        }
      |      }
      |    },
      |    "OAuth2ClientCredentialsAuth": {
      |      "type": "object",
      |      "title": "OAuth 2.0 Client Credentials Grant",
      |      "description": "Properties for the OAuth 2.0 Client Credentials Grant Type.",
      |      "properties": {
      |        "type": {
      |          "const": "oauth2-client-credentials",
      |          "type": "string"
      |        },
      |        "clientId": {
      |          "type": "string",
      |          "description": "The client ID."
      |        },
      |        "clientSecret": {
      |          "type": "string",
      |          "description": "The client secret."
      |        },
      |        "tokenUrl": {
      |          "type": "string",
      |          "description": "The URL of the token endpoint to obtain the access token."
      |        },
      |        "scopes": {
      |          "type": "array",
      |          "items": {
      |            "type": "string"
      |          },
      |          "description": "An array of scopes to request."
      |        }
      |      },
      |      "required": [
      |        "type",
      |        "clientId",
      |        "clientSecret",
      |        "tokenUrl"
      |      ]
      |    },
      |    "Authentication": {
      |      "anyOf": [
      |        {
      |          "$ref": "#/definitions/OAuth2ClientCredentialsAuth"
      |        },
      |        {
      |          "$ref": "#/definitions/BasicAuth"
      |        },
      |        {
      |          "$ref": "#/definitions/AnypointClientCredentialsAuth"
      |        }
      |      ]
      |    },
      |    "ExternalAgent": {
      |      "type": "object",
      |      "properties": {
      |        "cardUrl": {
      |          "type": "string"
      |        },
      |        "authentication": {
      |          "$ref": "#/definitions/Authentication"
      |        }
      |      }
      |    },
      |    "EinsteinLLM": {
      |      "type": "object",
      |      "required": [
      |        "clientId",
      |        "clientSecret",
      |        "baseUrl",
      |        "modelName"
      |      ],
      |      "properties": {
      |        "type": {
      |          "type": "string",
      |          "const": "einstein"
      |        },
      |        "clientId": {
      |          "type": "string"
      |        },
      |        "clientSecret": {
      |          "type": "string"
      |        },
      |        "baseUrl": {
      |          "type": "string"
      |        },
      |        "modelName": {
      |          "type": "string"
      |        },
      |        "probability": {
      |          "type": "number"
      |        },
      |        "locale": {
      |          "type": "string"
      |        }
      |      }
      |    },
      |    "SseTransport": {
      |      "required": [
      |        "sse"
      |      ],
      |      "properties": {
      |        "sse": {
      |          "type": "object",
      |          "properties": {
      |            "ssePath": {
      |              "type": "string"
      |            },
      |            "messagesPath": {
      |              "type": "string"
      |            }
      |          }
      |        }
      |      }
      |    },
      |    "StreamableHttpTransport": {
      |      "required": [
      |        "streamableHttp"
      |      ],
      |      "properties": {
      |        "streamableHttp": {
      |          "type": "object",
      |          "properties": {
      |            "path": {
      |              "type": "string"
      |            }
      |          }
      |        }
      |      }
      |    },
      |    "MCPTransport": {
      |      "type": "object",
      |      "oneOf": [
      |        {
      |          "$ref": "#/definitions/SseTransport"
      |        },
      |        {
      |          "$ref": "#/definitions/StreamableHttpTransport"
      |        }
      |      ]
      |    },
      |    "MCPServer": {
      |      "type": "object",
      |      "required": [
      |        "url",
      |        "transport"
      |      ],
      |      "properties": {
      |        "url": {
      |          "type": "string",
      |          "format": "uri"
      |        },
      |        "transport": {
      |          "$ref": "#/definitions/MCPTransport"
      |        },
      |        "tools": {
      |          "type": "object",
      |          "properties": {
      |            "allowed": {
      |              "type": "array",
      |              "items": {
      |                "type": "string"
      |              }
      |            },
      |            "denied": {
      |              "type": "array",
      |              "items": {
      |                "type": "string"
      |              }
      |            }
      |          }
      |        }
      |      }
      |    },
      |    "AgentsDomain": {
      |      "type": "object",
      |      "required": [
      |        "llm-providers"
      |      ],
      |      "properties": {
      |        "external-agents": {
      |          "type": "object",
      |          "patternProperties": {
      |            "^[a-zA-Z_][a-zA-Z0-9_.-]*$": {
      |              "$ref": "#/definitions/ExternalAgent"
      |            }
      |          }
      |        },
      |        "llm-providers": {
      |          "type": "object",
      |          "patternProperties": {
      |            "^[a-zA-Z_][a-zA-Z0-9_.-]*$": {
      |              "$ref": "#/definitions/EinsteinLLM"
      |            }
      |          }
      |        },
      |        "mcp-servers": {
      |          "type": "array",
      |          "items": {
      |            "type": "object",
      |            "patternProperties": {
      |              "^[a-zA-Z_][a-zA-Z0-9_.-]*$": {
      |                "$ref": "#/definitions/MCPServer"
      |              }
      |            }
      |          }
      |        }
      |      }
      |    },
      |    "AgentCard": {
      |      "description": "An AgentCard conveys key information:\n- Overall details (version, name, description, uses)\n- Skills: A set of capabilities the agent can perform\n- Default modalities/content types supported by the agent.\n- Authentication requirements",
      |      "properties": {
      |        "additionalInterfaces": {
      |          "description": "Announcement of additional supported transports. Client can use any of\nthe supported transports.",
      |          "items": {
      |            "$ref": "#/definitions/AgentInterface"
      |          },
      |          "type": "array"
      |        },
      |        "capabilities": {
      |          "$ref": "#/definitions/AgentCapabilities",
      |          "description": "Optional capabilities supported by the agent."
      |        },
      |        "defaultInputModes": {
      |          "description": "The set of interaction modes that the agent supports across all skills. This can be overridden per-skill.\nSupported media types for input.",
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        },
      |        "defaultOutputModes": {
      |          "description": "Supported media types for output.",
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        },
      |        "description": {
      |          "description": "A human-readable description of the agent. Used to assist users and\nother agents in understanding what the agent can do.",
      |          "examples": [
      |            "Agent that helps users with recipes and cooking."
      |          ],
      |          "type": "string"
      |        },
      |        "documentationUrl": {
      |          "description": "A URL to documentation for the agent.",
      |          "type": "string"
      |        },
      |        "iconUrl": {
      |          "description": "A URL to an icon for the agent.",
      |          "type": "string"
      |        },
      |        "name": {
      |          "description": "Human readable name of the agent.",
      |          "examples": [
      |            "Recipe Agent"
      |          ],
      |          "type": "string"
      |        },
      |        "preferredTransport": {
      |          "description": "The transport of the preferred endpoint. If empty, defaults to JSONRPC.",
      |          "type": "string"
      |        },
      |        "protocolVersion": {
      |          "default": "0.2.5",
      |          "description": "The version of the A2A protocol this agent supports.",
      |          "type": "string"
      |        },
      |        "provider": {
      |          "$ref": "#/definitions/AgentProvider",
      |          "description": "The service provider of the agent"
      |        },
      |        "security": {
      |          "description": "Security requirements for contacting the agent.",
      |          "items": {
      |            "additionalProperties": {
      |              "items": {
      |                "type": "string"
      |              },
      |              "type": "array"
      |            },
      |            "type": "object"
      |          },
      |          "type": "array"
      |        },
      |        "securitySchemes": {
      |          "additionalProperties": {
      |            "$ref": "#/definitions/SecurityScheme"
      |          },
      |          "description": "Security scheme details used for authenticating with this agent.",
      |          "type": "object"
      |        },
      |        "skills": {
      |          "description": "Skills are a unit of capability that an agent can perform.",
      |          "items": {
      |            "$ref": "#/definitions/AgentSkill"
      |          },
      |          "type": "array"
      |        },
      |        "supportsAuthenticatedExtendedCard": {
      |          "description": "true if the agent supports providing an extended agent card when the user is authenticated.\nDefaults to false if not specified.",
      |          "type": "boolean"
      |        },
      |        "url": {
      |          "description": "A URL to the address the agent is hosted at. This represents the\npreferred endpoint as declared by the agent.",
      |          "type": "string"
      |        },
      |        "version": {
      |          "description": "The version of the agent - format is up to the provider.",
      |          "examples": [
      |            "1.0.0"
      |          ],
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "capabilities",
      |        "defaultInputModes",
      |        "defaultOutputModes",
      |        "description",
      |        "name",
      |        "protocolVersion",
      |        "skills",
      |        "url",
      |        "version"
      |      ],
      |      "type": "object"
      |    },
      |    "AgentInterface": {
      |      "description": "AgentInterface provides a declaration of a combination of the\ntarget url and the supported transport to interact with the agent.",
      |      "properties": {
      |        "transport": {
      |          "description": "The transport supported this url. This is an open form string, to be\neasily extended for many transport protocols. The core ones officially\nsupported are JSONRPC, GRPC and HTTP+JSON.",
      |          "type": "string"
      |        },
      |        "url": {
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "transport",
      |        "url"
      |      ],
      |      "type": "object"
      |    },
      |    "AgentCapabilities": {
      |      "description": "Defines optional capabilities supported by an agent.",
      |      "properties": {
      |        "extensions": {
      |          "description": "extensions supported by this agent.",
      |          "items": {
      |            "$ref": "#/definitions/AgentExtension"
      |          },
      |          "type": "array"
      |        },
      |        "pushNotifications": {
      |          "description": "true if the agent can notify updates to client.",
      |          "type": "boolean"
      |        },
      |        "stateTransitionHistory": {
      |          "description": "true if the agent exposes status change history for tasks.",
      |          "type": "boolean"
      |        },
      |        "streaming": {
      |          "description": "true if the agent supports SSE.",
      |          "type": "boolean"
      |        }
      |      },
      |      "type": "object"
      |    },
      |    "AgentExtension": {
      |      "description": "A declaration of an extension supported by an Agent.",
      |      "examples": [
      |        {
      |          "description": "Google OAuth 2.0 authentication",
      |          "required": false,
      |          "uri": "https://developers.google.com/identity/protocols/oauth2"
      |        }
      |      ],
      |      "properties": {
      |        "description": {
      |          "description": "A description of how this agent uses this extension.",
      |          "type": "string"
      |        },
      |        "params": {
      |          "additionalProperties": {},
      |          "description": "Optional configuration for the extension.",
      |          "type": "object"
      |        },
      |        "required": {
      |          "description": "Whether the client must follow specific requirements of the extension.",
      |          "type": "boolean"
      |        },
      |        "uri": {
      |          "description": "The URI of the extension.",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "uri"
      |      ],
      |      "type": "object"
      |    },
      |    "AgentProvider": {
      |      "description": "Represents the service provider of an agent.",
      |      "examples": [
      |        {
      |          "organization": "Google",
      |          "url": "https://ai.google.dev"
      |        }
      |      ],
      |      "properties": {
      |        "organization": {
      |          "description": "Agent provider's organization name.",
      |          "type": "string"
      |        },
      |        "url": {
      |          "description": "Agent provider's URL.",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "organization",
      |        "url"
      |      ],
      |      "type": "object"
      |    },
      |    "SecurityScheme": {
      |      "anyOf": [
      |        {
      |          "$ref": "#/definitions/APIKeySecurityScheme"
      |        },
      |        {
      |          "$ref": "#/definitions/HTTPAuthSecurityScheme"
      |        },
      |        {
      |          "$ref": "#/definitions/OAuth2SecurityScheme"
      |        },
      |        {
      |          "$ref": "#/definitions/OpenIdConnectSecurityScheme"
      |        }
      |      ],
      |      "description": "Mirrors the OpenAPI Security Scheme Object\n(https://swagger.io/specification/#security-scheme-object)"
      |    },
      |    "APIKeySecurityScheme": {
      |      "description": "API Key security scheme.",
      |      "properties": {
      |        "description": {
      |          "description": "Description of this security scheme.",
      |          "type": "string"
      |        },
      |        "in": {
      |          "description": "The location of the API key. Valid values are \"query\", \"header\", or \"cookie\".",
      |          "enum": [
      |            "cookie",
      |            "header",
      |            "query"
      |          ],
      |          "type": "string"
      |        },
      |        "name": {
      |          "description": "The name of the header, query or cookie parameter to be used.",
      |          "type": "string"
      |        },
      |        "type": {
      |          "const": "apiKey",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "in",
      |        "name",
      |        "type"
      |      ],
      |      "type": "object"
      |    },
      |    "HTTPAuthSecurityScheme": {
      |      "description": "HTTP Authentication security scheme.",
      |      "properties": {
      |        "bearerFormat": {
      |          "description": "A hint to the client to identify how the bearer token is formatted. Bearer tokens are usually\ngenerated by an authorization server, so this information is primarily for documentation\npurposes.",
      |          "type": "string"
      |        },
      |        "description": {
      |          "description": "Description of this security scheme.",
      |          "type": "string"
      |        },
      |        "scheme": {
      |          "description": "The name of the HTTP Authentication scheme to be used in the Authorization header as defined\nin RFC7235. The values used SHOULD be registered in the IANA Authentication Scheme registry.\nThe value is case-insensitive, as defined in RFC7235.",
      |          "type": "string"
      |        },
      |        "type": {
      |          "const": "http",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "scheme",
      |        "type"
      |      ],
      |      "type": "object"
      |    },
      |    "OAuth2SecurityScheme": {
      |      "description": "OAuth2.0 security scheme configuration.",
      |      "properties": {
      |        "description": {
      |          "description": "Description of this security scheme.",
      |          "type": "string"
      |        },
      |        "flows": {
      |          "$ref": "#/definitions/OAuthFlows",
      |          "description": "An object containing configuration information for the flow types supported."
      |        },
      |        "type": {
      |          "const": "oauth2",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "flows",
      |        "type"
      |      ],
      |      "type": "object"
      |    },
      |    "OAuthFlows": {
      |      "description": "Allows configuration of the supported OAuth Flows",
      |      "properties": {
      |        "authorizationCode": {
      |          "$ref": "#/definitions/AuthorizationCodeOAuthFlow",
      |          "description": "Configuration for the OAuth Authorization Code flow. Previously called accessCode in OpenAPI 2.0."
      |        },
      |        "clientCredentials": {
      |          "$ref": "#/definitions/ClientCredentialsOAuthFlow",
      |          "description": "Configuration for the OAuth Client Credentials flow. Previously called application in OpenAPI 2.0"
      |        },
      |        "implicit": {
      |          "$ref": "#/definitions/ImplicitOAuthFlow",
      |          "description": "Configuration for the OAuth Implicit flow"
      |        },
      |        "password": {
      |          "$ref": "#/definitions/PasswordOAuthFlow",
      |          "description": "Configuration for the OAuth Resource Owner Password flow"
      |        }
      |      },
      |      "type": "object"
      |    },
      |    "AuthorizationCodeOAuthFlow": {
      |      "description": "Configuration details for a supported OAuth Flow",
      |      "properties": {
      |        "authorizationUrl": {
      |          "description": "The authorization URL to be used for this flow. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS",
      |          "type": "string"
      |        },
      |        "refreshUrl": {
      |          "description": "The URL to be used for obtaining refresh tokens. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS.",
      |          "type": "string"
      |        },
      |        "scopes": {
      |          "additionalProperties": {
      |            "type": "string"
      |          },
      |          "description": "The available scopes for the OAuth2 security scheme. A map between the scope name and a short\ndescription for it. The map MAY be empty.",
      |          "type": "object"
      |        },
      |        "tokenUrl": {
      |          "description": "The token URL to be used for this flow. This MUST be in the form of a URL. The OAuth2 standard\nrequires the use of TLS.",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "authorizationUrl",
      |        "scopes",
      |        "tokenUrl"
      |      ],
      |      "type": "object"
      |    },
      |    "ClientCredentialsOAuthFlow": {
      |      "description": "Configuration details for a supported OAuth Flow",
      |      "properties": {
      |        "refreshUrl": {
      |          "description": "The URL to be used for obtaining refresh tokens. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS.",
      |          "type": "string"
      |        },
      |        "scopes": {
      |          "additionalProperties": {
      |            "type": "string"
      |          },
      |          "description": "The available scopes for the OAuth2 security scheme. A map between the scope name and a short\ndescription for it. The map MAY be empty.",
      |          "type": "object"
      |        },
      |        "tokenUrl": {
      |          "description": "The token URL to be used for this flow. This MUST be in the form of a URL. The OAuth2 standard\nrequires the use of TLS.",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "scopes",
      |        "tokenUrl"
      |      ],
      |      "type": "object"
      |    },
      |    "ImplicitOAuthFlow": {
      |      "description": "Configuration details for a supported OAuth Flow",
      |      "properties": {
      |        "authorizationUrl": {
      |          "description": "The authorization URL to be used for this flow. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS",
      |          "type": "string"
      |        },
      |        "refreshUrl": {
      |          "description": "The URL to be used for obtaining refresh tokens. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS.",
      |          "type": "string"
      |        },
      |        "scopes": {
      |          "additionalProperties": {
      |            "type": "string"
      |          },
      |          "description": "The available scopes for the OAuth2 security scheme. A map between the scope name and a short\ndescription for it. The map MAY be empty.",
      |          "type": "object"
      |        }
      |      },
      |      "required": [
      |        "authorizationUrl",
      |        "scopes"
      |      ],
      |      "type": "object"
      |    },
      |    "PasswordOAuthFlow": {
      |      "description": "Configuration details for a supported OAuth Flow",
      |      "properties": {
      |        "refreshUrl": {
      |          "description": "The URL to be used for obtaining refresh tokens. This MUST be in the form of a URL. The OAuth2\nstandard requires the use of TLS.",
      |          "type": "string"
      |        },
      |        "scopes": {
      |          "additionalProperties": {
      |            "type": "string"
      |          },
      |          "description": "The available scopes for the OAuth2 security scheme. A map between the scope name and a short\ndescription for it. The map MAY be empty.",
      |          "type": "object"
      |        },
      |        "tokenUrl": {
      |          "description": "The token URL to be used for this flow. This MUST be in the form of a URL. The OAuth2 standard\nrequires the use of TLS.",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "scopes",
      |        "tokenUrl"
      |      ],
      |      "type": "object"
      |    },
      |    "OpenIdConnectSecurityScheme": {
      |      "description": "OpenID Connect security scheme configuration.",
      |      "properties": {
      |        "description": {
      |          "description": "Description of this security scheme.",
      |          "type": "string"
      |        },
      |        "openIdConnectUrl": {
      |          "description": "Well-known URL to discover the [[OpenID-Connect-Discovery]] provider metadata.",
      |          "type": "string"
      |        },
      |        "type": {
      |          "const": "openIdConnect",
      |          "type": "string"
      |        }
      |      },
      |      "required": [
      |        "openIdConnectUrl",
      |        "type"
      |      ],
      |      "type": "object"
      |    },
      |    "AgentSkill": {
      |      "description": "Represents a unit of capability that an agent can perform.",
      |      "properties": {
      |        "description": {
      |          "description": "Description of the skill - will be used by the client or a human\nas a hint to understand what the skill does.",
      |          "type": "string"
      |        },
      |        "examples": {
      |          "description": "The set of example scenarios that the skill can perform.\nWill be used by the client as a hint to understand how the skill can be used.",
      |          "examples": [
      |            [
      |              "I need a recipe for bread"
      |            ]
      |          ],
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        },
      |        "id": {
      |          "description": "Unique identifier for the agent's skill.",
      |          "type": "string"
      |        },
      |        "inputModes": {
      |          "description": "The set of interaction modes that the skill supports\n(if different than the default).\nSupported media types for input.",
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        },
      |        "name": {
      |          "description": "Human readable name of the skill.",
      |          "type": "string"
      |        },
      |        "outputModes": {
      |          "description": "Supported media types for output.",
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        },
      |        "tags": {
      |          "description": "Set of tagwords describing classes of capabilities for this specific skill.",
      |          "examples": [
      |            [
      |              "cooking",
      |              "customer support",
      |              "billing"
      |            ]
      |          ],
      |          "items": {
      |            "type": "string"
      |          },
      |          "type": "array"
      |        }
      |      },
      |      "required": [
      |        "description",
      |        "id",
      |        "name",
      |        "tags"
      |      ],
      |      "type": "object"
      |    }
      |  }
      |}
      |""".stripMargin

}
