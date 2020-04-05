FROM adoptopenjdk:11-jre-hotspot
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} testbefund-api.jar
CMD ["java", \
     "-jar", \
      "testbefund-api.jar"]
