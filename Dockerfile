FROM openjdk:8-jre
COPY /jooq-spring-boot/target/jooq_spring_boot.jar /webapps/jooq_spring_boot.jar
CMD ["java","-jar","/webapps/jooq_spring_boot.jar"]