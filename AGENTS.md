# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build, run, and test

The commands in this section document how the project is built and run; they are reference information only. Agents must not execute any `java` or `maven` commands, as required by the Core Rules below.

- Build the full multi-module project: `mvn clean package`
- Run all tests: `mvn test`
- Run tests for the application module only: `mvn -pl whim-start test`
- Run a single test class: `mvn -pl whim-start -Dtest=ClassName test`
- Run a single test method: `mvn -pl whim-start -Dtest=ClassName#methodName test`
- Start the application locally: `mvn -pl whim-start spring-boot:run`
- Build without tests: `mvn clean package -DskipTests`

## Repository shape

This is a Maven multi-module Spring Boot 4 project targeting Java 25. The root `pom.xml` aggregates three top-level modules:

- `whim-framework`: reusable infrastructure and auto-configuration modules
- `whim-modules`: business-domain modules
- `whim-start`: the runnable Spring Boot application that assembles framework + business modules

`whim-start` is the entrypoint. `com.whim.WhimApplication` boots the app and depends on `whim-web` plus the business module `whim-system`.

## High-level architecture

The codebase is organized so that most shared behavior lives in framework modules exposed through Spring Boot auto-configuration rather than being wired manually in the app module.

### Framework modules

- `whim-core`: foundational utilities, shared exceptions, authentication abstractions, and thread-pool configuration
- `whim-json`: Jackson customization
- `whim-web`: MVC/web concerns like converters, global exception handling, and XSS filtering
- `whim-redis`: Redisson-based Redis integration plus cache manager setup
- `whim-satoken`: authentication/authorization integration based on Sa-Token
- `whim-mybatisplus`: MyBatis-Plus configuration, pagination/optimistic-lock/block-attack interceptors, and entity auto-fill support
- The root POM manages the versions of internal modules through `dependencyManagement`

Several framework modules register themselves through `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, so when tracing behavior, check module auto-configurations before assuming beans are declared in `whim-start`.

### Reuse `whim-core` utilities

Before writing common helper logic, check `whim-framework/whim-core/src/main/java/com/whim/core/utils`. When an existing utility method has the required behavior, use it instead of duplicating its implementation or creating another utility class. Read the method contract and choose the appropriate overload; do not force a utility into a case with different semantics. Add shared functionality to the existing appropriate utility only when it is genuinely missing and belongs in `whim-core`.

The `com.whim.core.utils` package currently contains:

| Utility | Existing capabilities |
| --- | --- |
| `AmountUtils` | Monetary null handling, sign checks, rounding, yuan/cent conversion, parsing, string formatting, and Chinese uppercase amounts. |
| `BCryptUtils` | BCrypt password encoding and verification. |
| `BeanConvertUtils` | Bean-to-bean, list, and map-to-bean conversion. |
| `DateUtils` | Date/time patterns, formatting, parsing, arithmetic, day boundaries, and conversion between Java date/time types. |
| `DesensitizationUtils` | Masking sensitive text by retained prefix/suffix or predefined desensitization type. |
| `IdUtils` | UUID generation, including a 32-character form. |
| `IPUtils` | Client IP extraction and IP location lookup. |
| `RandomStringUtils` | Random alphabetic, numeric, alphanumeric, and custom-character strings. |
| `RestClientUtils` | HTTP requests through `RestClient`, including common verbs and configurable request options. |
| `ServletUtils` | Current servlet request, headers, parameters, pagination parameters, URL parameter encoding, and user-agent information. |
| `SpringUtils` | Spring context, bean, environment, and AOP proxy access where direct injection is unavailable; prefer constructor injection in Spring beans. |
| `ValueParserUtils` | Conversion of values to strings, numbers, booleans, and enums; delimited array parsing; full-width/half-width text conversion. |

### Auth flow

Authentication is intentionally abstracted in two layers:

- `whim-core` defines `AuthenticationContext`, which is the business-facing API for “who is the current user?”
- `whim-satoken` provides the actual implementation (`AuthContext`) and request interception (`SaTokenConfigure`)

`StpAuthManager` is the central facade for Sa-Token multi-account handling. Even though only the `SYSTEM` account type is currently registered, new account systems are meant to be added there. Business and infrastructure code should depend on `AuthenticationContext`, not directly on Sa-Token APIs, unless they are inside the auth framework module itself.

`whim-satoken` also provides system-account annotations in `com.whim.satoken.annotation`: `SystemCheckLogin` checks login, `SystemCheckPermission` checks permission codes, `SystemCheckRole` checks roles, and `SystemCheckSafe` checks secondary authentication. Use these project annotations for the corresponding system-account checks instead of repeating the underlying Sa-Token annotations directly.

`whim-satoken` and `whim-mybatisplus` are independent sibling infrastructure modules. Both may depend on `whim-core`, but they must never depend on each other. Business modules must use the abstractions in `whim-core`; `whim-start` is the composition root that assembles the concrete Sa-Token implementation with business and persistence modules.

### Persistence conventions

`whim-mybatisplus` centralizes database behavior:

- `MybatisPlusConfiguration` installs pagination, optimistic locking, and block-attack protection
- `BaseEntity` defines shared audit fields (`createBy/createTime/updateBy/updateTime/deleteBy/deleteTime`)
- `AutoFillFieldHandler` fills those audit fields from `AuthenticationContext`

If you change persistence behavior, review both the entity base class and the meta-object handler together; audit-field behavior is split across those two pieces.

For ordinary persistence operations, use the methods provided by MyBatis-Plus `IService` and its condition wrappers instead of calling mapper methods directly. Write SQL in mapper XML only when necessary, such as for complex queries that cannot be expressed clearly or efficiently with those methods and wrappers.

Define transaction boundaries at the service layer for business operations that must update multiple tables atomically. When a database change affects cached data, invalidate or refresh the relevant cache after the write succeeds; for transactional writes, coordinate cache changes with transaction commit so a rollback cannot leave the cache inconsistent with the database.

### Web/API conventions

`whim-web` owns cross-cutting web behavior rather than placing it in controllers inside `whim-start`:

- global exception mapping
- string-to-`LocalDateTime` conversion
- servlet filter/XSS request wrapping

Place controllers under `whim-start/src/main/java/com/whim/controller` and group them by business area in subdirectories. The existing `system` directory contains system APIs; future business areas should use their own directories. Controller directory grouping is independent of Maven module boundaries.

The Sa-Token interceptor protects `/system/**` by default and allows unauthenticated access only to paths configured under `sa-token.exclude-paths` in `application.yml`.

Static Controller route segments must use lower camel case and may contain only letters and numbers. Do not use hyphens, underscores, or other special characters in static route segments; use paths such as `/userInfo`, not `/user-info` or `/user_info`. Path-variable placeholders such as `/{userId}` are allowed, and their names must also use lower camel case.

Follow the existing API models: return ordinary API responses through `whim-web`'s `Result<T>`; use `PageQueryDTO` and `PageDataVO<T>` for paginated APIs; validate request DTOs with `jakarta.validation` and `@Valid` where applicable. Reuse the existing exception types and `GlobalExceptionHandler` for errors, keeping HTTP status and `Result.code` consistent instead of creating endpoint-specific error formats or a separate code scheme. Follow the permission-code naming used by related endpoints and apply the appropriate project authorization annotation when a permission check is required.

After any API-related change, including adding, modifying, or removing endpoints, routes, request parameters or bodies, response schemas, authentication or permissions, and error responses, call the Apifox MCP tools to update the API documentation in the `whim` project. Verify that the documentation matches the final implementation before completing the task.

### Business modules

`whim-modules/whim-system` is the current system module. It depends on `whim-core`, `whim-mybatisplus`, and `whim-redis`, but not on the concrete `whim-satoken` implementation. System-domain services, mappers, entities, DTOs, and VOs belong here; system controllers live in `whim-start` under `controller/system`. `whim-modules` may contain additional business modules in the future, such as `whim-business`; do not assume every business domain belongs in `whim-system`.

## Configuration notes

- Default runtime profile comes from Maven resource filtering: `spring.profiles.active: @profiles_active@`
- Root Maven profiles define `dev` as the default and `prod` as the alternative
- Main application config is in `whim-start/src/main/resources/application.yml`
- Flyway migrations live in `whim-start/src/main/resources/db/migration`
- Every database change must use a new, higher Flyway version; never edit, delete, or reuse an already-applied versioned migration
- `V1__baseline.sql` is the initial schema baseline; existing non-empty databases are baselined at version 1 before later migrations run
- The app runs on port `8089`
- Sa-Token exclusion paths are configured in `application.yml`, not hardcoded in the app module

## Current testing state

There are currently no `src/test/java` test classes in the repository. If you add tests, the most natural place for application-level tests is `whim-start`, since that module assembles the full runtime graph.


# Core Rules

- All code must be based on **JDK 25+** and **Spring Boot 4.1+**.
- Always prioritize the latest, cutting-edge modern technologies and optimal solutions recommended for Spring Boot 4.
- Avoid using deprecated, legacy, or obsolete implementation methods.
- Code should meet high-level, professional production-grade standards.
- **Do not** use `record` to define classes.
- Use meaningful naming conventions and avoid abbreviations.
- Do not split a method into multiple methods without a clear need; keep straightforward logic together and extract a method only when it improves clarity or encapsulates meaningful reusable behavior.
- Do not execute any `java` or `maven` commands.
- Do not write any test code unless explicitly requested.
- I expect a complete solution in one go; do not suggest further upgrades in the final step.

# Spring Boot

- Adhere strictly to the best practices recommended for **Spring Boot 4.x**.
- Use `@RequiredArgsConstructor` for Bean injection.
- Utilize **Lombok** to eliminate redundant code where applicable.
- Use the `jakarta.*` package (never use the `javax.*` package).
- Favor `@ConfigurationProperties` over `@Value`.
- Avoid unnecessary `@Component` annotations; prefer explicit Bean configuration.
- Use `@AutoConfiguration` when building reusable modules.
- Prioritize a functional programming style where appropriate.

# Code Structure

- Follow a clear package structure:
    - `controller` (Controller Layer)
    - `service` (Business Logic Layer)
    - `impl` (Business Logic Implementation)
    - `mapper` (Data Access Layer)
    - `config` (Configuration Layer)
    - `model/entity` (Database Entities)
    - `model/dto` (Data Transfer Objects)
    - `model/vo` (View Objects)
- Clearly distinguish between DTO, VO, and Entity.
- Maintain small class granularity and strictly follow the **Single Responsibility Principle**.

# Logging and Exceptions

- Use structured logging (SLF4J is recommended).
- **Prohibit** the use of `System.out.println` for output.
- Provide meaningful log messages.
- Logs and exceptions **must** be in **Chinese** (Important).
- Implement a global exception handling mechanism via `@ControllerAdvice`.

# Documentation and Comments (Important)

## Class Comment Format (Mandatory)

/**
* @author Jince
* @date yyyy/MM/dd
* @description
  */

## Method Comment Rules

- **Every** method **must** include a comment.
- Comments must be concise and clear.
- Comments should describe:
    - The specific functionality of the method (what it does).
    - Parameter descriptions (if necessary).
    - Return value descriptions (if the meaning is not obvious).
    - Complex or non-obvious logic sections.

# Code Generation

- Always generate complete, "ready-to-run" code.
- Include all necessary `import` statements, annotations, and configuration details.
- **Do not** omit any critical code snippets.
- **Do not** mix new and old code styles.

# Style and Quality

- Prioritize clarity and readability over clever "tricks."
- Avoid over-engineering.
- Avoid deeply nested logic.
- Follow consistent code formatting standards.

# Behavioral Guidelines

- If multiple implementation options exist, choose the most modern and officially recommended one.
- Unless explicitly requested, do not suggest outdated alternatives.
