# Bigraph Model Provider

The Bigraph Model Provider is a flexible library designed for native bigraphical applications that are based on so-called _world models_. 
This library offers a set of standardized interfaces for querying, managing and integrating bigraphical models, making it easy to connect with various data sources and sinks.

### Features
- Unified Interface for Bigraph Management: A simple and consistent interface for querying and managing bigraphical models.
- Custom Provider Support: Create custom providers by subclassing specific abstract classes or interfaces for specialized bigraph data handling needs.
- Data Source Integration: Attach providers to various data access technologies, including databases, web services, message queues, protocols, or other data storage and retrieval solutions.
- Composable Model Providers: Modular design is supported by combining smaller models into larger, complex structures. Finally, to create large _world models_.

## Overview

The Bigraph Model Provider library supports querying, and storing of bigraphical models.
Bigraphical models are expressive graph models, which can represent, for example, complex system designs or interactions of agent behavior. 
The generic, extensible class hierarchy of this library that allows users to integrate a wide range of data sources.

### Providers

Providers come in different flavors:
- Signatures (Syntax): They specify the types and constraints of a bigraphical model.
- Host Bigraphs (Data): Allow for the representation of individual bigraph instances.
- Rules (Behavior): Define constrained transformations applied to bigraphs.
- Composites: Some model providers can even be composed to construct larger models from smaller sub-models.

Technically,
all providers are signature providers since every bigraphical model
(e.g., a rule, or a host bigraph) is specified over a signature.