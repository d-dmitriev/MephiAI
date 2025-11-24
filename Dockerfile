FROM vegardit/graalvm-maven:25.0.1 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn -Pnative -Pproduction -DskipTests native:compile

FROM redhat/ubi10-minimal:10.0 AS ubi

FROM redhat/ubi10-micro:10.0 AS micro

FROM micro
COPY --from=ubi /usr/lib64/libz.so.1 /usr/lib64/libz.so.1
WORKDIR /opt/app
COPY --from=builder /build/target/MephiML MephiML
EXPOSE 8080
CMD ["/opt/app/MephiML"]