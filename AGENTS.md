# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build, run, and test

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
- `whim-bom`: internal BOM used for dependency alignment across the project

Several framework modules register themselves through `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, so when tracing behavior, check module auto-configurations before assuming beans are declared in `whim-start`.

### Auth flow

Authentication is intentionally abstracted in two layers:

- `whim-core` defines `AuthenticationContext`, which is the business-facing API for “who is the current user?”
- `whim-satoken` provides the actual implementation (`AuthContext`) and request interception (`SaTokenConfigure`)

`StpAuthManager` is the central facade for Sa-Token multi-account handling. Even though only the `SYSTEM` account type is currently registered, new account systems are meant to be added there. Business and infrastructure code should depend on `AuthenticationContext`, not directly on Sa-Token APIs, unless they are inside the auth framework module itself.

### Persistence conventions

`whim-mybatisplus` centralizes database behavior:

- `MybatisPlusConfiguration` installs pagination, optimistic locking, and block-attack protection
- `BaseEntity` defines shared audit fields (`createBy/createTime/updateBy/updateTime/deleteBy/deleteTime`)
- `AutoFillFieldHandler` fills those audit fields from `AuthenticationContext`

If you change persistence behavior, review both the entity base class and the meta-object handler together; audit-field behavior is split across those two pieces.

### Web/API conventions

`whim-web` owns cross-cutting web behavior rather than controllers inside `whim-start`:

- global exception mapping
- string-to-`LocalDateTime` conversion
- servlet filter/XSS request wrapping

The Sa-Token interceptor protects `/system/**` by default and allows unauthenticated access only to paths configured under `sa-token.exclude-paths` in `application.yml`.

### Business modules

`whim-modules/whim-system` is the current business module. It depends on the auth and redis framework modules and is where system-domain controllers/services/entities should live. Right now the module appears to be in an early stage, with only a placeholder controller present.

## Configuration notes

- Default runtime profile comes from Maven resource filtering: `spring.profiles.active: @profiles_active@`
- Root Maven profiles define `dev` as the default and `prod` as the alternative
- Main application config is in `whim-start/src/main/resources/application.yml`
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