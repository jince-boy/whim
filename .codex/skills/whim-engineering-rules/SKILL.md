---
name: whim-engineering-rules
description: Engineering rules for the Whim Java/Spring Boot backend. Use when Codex edits, reviews, refactors, or generates code in the Whim project, especially service, mapper, XML SQL, authentication, permission, role, user, MyBatis-Plus, Sa-Token, and Spring Boot modules.
---

# Whim Engineering Rules

## Core Style

Write code that a person on the project can understand quickly. Prefer direct, boring, readable code over clever abstraction.

Do not make code look like generated AI code:

- Do not split a small method into many private helpers unless the extracted method has a clear business meaning or removes real complexity.
- Do not introduce generic function parameters, streams, strategy wrappers, adapters, or indirection just to avoid a few repeated lines.
- Keep the main business flow visible in the public method.
- A little straightforward duplication is better than hard-to-follow abstraction.

## Methods

Write method comments for important code. Keep comments short and clear. Say what the method does; do not write long template comments.

When adding or changing methods:

- Do not add defensive null checks unless the current call chain can actually pass null.
- Do not catch conversion exceptions unless invalid input is realistic for that method.
- Trust strong project invariants when they are already guaranteed by login, validation, database constraints, or framework flow.
- Use early returns only when they make the method simpler.
- Keep method bodies readable before extracting helpers.

## Performance First

Prefer speed, performance, and safety when choosing an implementation.

For SQL, performance is the highest priority:

- Start from the most selective condition, usually IDs such as `user_id`, `role_id`, or tenant-scoped keys.
- Use the minimum required joins for the requested data.
- Do not join tables only to "double check" data unless the business rule needs that check.
- Prefer `INNER JOIN` when only valid matched rows are needed.
- Do not use `DISTINCT`, sorting, trimming, filtering, or de-duplication unless the data model can actually produce duplicates or dirty values.
- Check existing indexes before writing complex SQL.
- Add or adjust composite indexes when a query depends on a hot access path.
- Keep mapper XML simple and readable; do not hide expensive behavior behind broad queries.

## Spring And Java

Follow the repository rules:

- Use JDK 25+ and Spring Boot 4.1+ style.
- Use Lombok where it removes boilerplate.
- Use `jakarta.*`, never `javax.*`.
- Use `@RequiredArgsConstructor` for bean injection.
- Prefer explicit, simple services over broad "manager" or "auth query" classes when the behavior belongs to user, role, permission, or another domain service.
- Do not use `record`.

## Review Before Editing

Before changing code, read the surrounding service, mapper, XML, entity, and table definition when relevant. Let the existing domain model decide the shape of the change.

When the user criticizes complexity, simplify the code instead of defending unnecessary abstraction.
