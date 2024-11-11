# Bigraph Model Provider

This project provides the core module of the Bigraph Model Provider infrastructure, a flexible library designed for native bigraphical applications. 
The Bigraph Model Provider infrastructure offers a set of standardized interfaces for querying and managing and integrating bigraphical models, making it easy to connect with various data sources and sinks.

### Features
- Unified Interface for Bigraph Management: A simple and consistent interface for querying and managing bigraphical models.
- Custom Provider Support: Create custom providers by subclassing specific abstract classes or interfaces for specialized bigraph data handling needs.
- Data Source Integration: Attach providers to various data access technologies, including databases, web services, message queues, protocols, or other data storage and retrieval solutions.
- Composable Model Providers: Modular design is supported by combining smaller models into larger, complex structures.

## Overview

The Bigraph Model Provider library supports querying, and storing of bigraphical models, which can represent complex systems and interactions. 
It provides a generic, extensible class hierarchy that allows users to integrate a wide range of data sources.

Key parts include:
- Signatures (Syntax): Represent the types and structure of bigraphical model elements.
- Host Bigraphs (Data): Allow for the representation of individual bigraph instances.
- Rules (Behavior): Define constrained transformations applied to bigraphs.

Some model providers can even be composed to construct larger models from smaller sub-models.