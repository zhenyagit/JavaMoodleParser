FROM openjdk:10
ENV DB_HOST=localhost
ENV DB_NAME=postgres_db_name
ENV DB_U_NAME=postgres_username
ENV DB_U_PASS=postgres_password
ENV EL_U_NAME=elastic_username
ENV EL_U_PASS=elastic_password
ENV EL_HOSTNAME=localhost
COPY ./target/JavaMoodleParser.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]