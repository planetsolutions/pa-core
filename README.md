![master build](https://github.com/planetsolutions/pa-core/workflows/master%20build/badge.svg) ![test build](https://github.com/planetsolutions/pa-core/workflows/test%20build/badge.svg?branch=test)

# PlanetArchive Core Services

## Build requirements
 - Java 8 (any OpenJDK-compliant distribution)
 - [Apache Maven 3](http://maven.apache.org)
 - [Docker](https://www.docker.com/) (uses [OpenContainers](https://www.opencontainers.org/))
 
## Build procedure
To build the project locally, execute the following command from the repository
root folder:
```
mvn clean package
```
By default, it builds Spring Boot application (`spring_boot_jar` profile).

After the build, get the runnable JAR: `jooq-spring-boot/target/jooq_spring_boot.jar` 