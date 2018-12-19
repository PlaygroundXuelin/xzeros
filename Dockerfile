FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/xzeros-0.0.1-SNAPSHOT-standalone.jar /xzeros/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/xzeros/app.jar"]
