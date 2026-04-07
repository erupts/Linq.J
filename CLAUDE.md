# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Linq.J is a zero-dependency Java library that brings LINQ (Language Integrated Query) capabilities to Java, enabling SQL-like querying of in-memory collections via a fluent, chainable API with lambda expressions.

- **Maven coordinates:** `xyz.erupt:linq.j:0.1.1`
- **Requirements:** JDK 8+, zero production dependencies
- **Test dependencies only:** JUnit 4.13.2, Gson 2.9.1

## Build & Test Commands

```bash
mvn clean compile                              # Compile
mvn clean test                                 # Run all tests
mvn clean test -Dtest=LinqTest                 # Run specific test class
mvn clean test -Dtest=LinqTest#selectId        # Run specific test method
mvn clean package                              # Build JAR
mvn clean install                              # Install locally
```

## Architecture

**Data flow:** `Linq` (entry point) → builds a `Dql` schema → `EruptEngine` executes it → returns results.

### Core layers

| Package | Role |
|---------|------|
| `grammar/` | Six SQL-like interfaces: `Select`, `Join`, `Where`, `GroupBy`, `OrderBy`, `Write` |
| `schema/` | DQL data structures: `Dql` (full query), `Row`, `Column`, `JoinSchema`, `WhereSchema`, `OrderBySchema` |
| `engine/` | `Engine` (abstract) + `EruptEngine` (executes the `Dql`, handles joins, filtering, grouping, sorting) |
| `lambda/` | Lambda parsing: `SFunction` (serializable function), `LambdaSee` (extracts field names from lambdas), `Th` (identity helper) |
| `util/` | `Columns` (aggregation: sum/avg/min/max/count), `RowUtil`, `ReflectField`, `CompareUtil` |
| `consts/` | Enums: `JoinMethod`, `OrderByDirection`, `CompareSymbol`, `JoinExchange` |

### Key design decisions

- `Linq.java` implements all six grammar interfaces; methods mutate a `Dql` instance, returning `this` for chaining.
- Lambda expressions are serialized and parsed at runtime by `LambdaSee` using `SerializedLambda` reflection to extract field names — this is why the `-parameters` compiler flag is required (set in `pom.xml`).
- `EruptEngine` uses hash-based join optimization; columns are represented as `Map<Column, Object>` internally (the `Row` type).
- Results are materialized via `write(Class<T>)`, `writeMap()`, or `writeOne(Class<T>)`.
- Objects must expose getter methods (Lombok `@Getter` recommended).

### Planned features (not yet implemented)

- UNION ALL / UNION / INTERSECT / EXCEPT
- Window functions
