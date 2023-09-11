# Template: Spring Boot Service

This repository can be used as a template for common _Spring Boot_ services written in _Kotlin_ and build using _Gradle_.

## TODOs

- Change project name in [settings.gradle.kts](settings.gradle.kts).
- Change project name in AsciiDoc files:
  - [api.adoc](src/docs/asciidoc/api.adoc)
  - [domain.adoc](src/docs/asciidoc/domain.adoc)
  - [index.adoc](src/docs/asciidoc/index.adoc)
- Change project name in [application.yml](src/main/resources/application.yml).
- Check if you actually need any of the default additional beans defined in the [ApplicationConfiguration](src/main/kotlin/service/ApplicationConfiguration.kt) class.
- Replace basic-auth with whatever authentication method should actually be used in the [WebSecurityConfiguration](src/main/kotlin/service/config/security/WebSecurityConfiguration.kt) class.
  - Also configure your specific domain scopes, roles etc.
  - Replace in-memory users with something externalized.
- Check if all dependencies in [build.gradle.kts](build.gradle.kts) are actually needed for your domain.
- Optional:
  - Rename root source package structure to reflect your domain.
- **Finally**: replace this `README.md` file's content with your own content.
