FROM    openjdk:8 as builder
ARG     APP_JAR
ARG     MAVEN_REPO_URL
ARG     MAVEN_REPO_USER
ARG     MAVEN_REPO_PASS
ARG     APP_VER
RUN     curl --create-dirs -u ${MAVEN_REPO_USER}:${MAVEN_REPO_PASS} \
        "${MAVEN_REPO_URL}/libs-release-local/ru/doccloud/jooq-spring-boot/${APP_JAR}" -o /webapps/jooq_spring_boot.jar \
        && mkdir -p /BOOT-INF/classes/static \
        && echo "{\"version\":\"${APP_VER}\"}">/BOOT-INF/classes/static/version.json \
        && jar -uf /webapps/jooq_spring_boot.jar /BOOT-INF/classes/static/version.json

FROM    openjdk:8-jre       
COPY --from=builder /webapps/jooq_spring_boot.jar /webapps/jooq_spring_boot.jar
RUN apt-get update && apt-get install -y --allow-downgrades --allow-remove-essential --allow-change-held-packages --no-install-recommends dcmtk && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
CMD     ["java","-jar","/webapps/jooq_spring_boot.jar"]
