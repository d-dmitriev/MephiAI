# Этап 1: сборка native-приложения и подготовка модели
FROM vegardit/graalvm-maven:25.0.1 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

# Собираем native-бинарник
RUN mvn -Pnative -Pproduction -DskipTests native:compile

# Этап 2: сборка модели
FROM alpine AS model
COPY models/ /build/models/

# Этап 3: финальный образ
FROM redhat/ubi10-minimal:10.0 AS ubi
FROM redhat/ubi10-micro:10.0 AS micro
FROM micro

# Копируем libz (требуется Tribuo / native-image)
COPY --from=ubi /usr/lib64/libz.so.1 /usr/lib64/libz.so.1

WORKDIR /opt/app

# 🔹 СЛОЙ 1: native-бинарник
COPY --from=builder /build/target/MephiML MephiML
# 🔹 СЛОЙ 2: модель — отдельно, чтобы кэшировалась
COPY --from=model /build/models/ /opt/app/models/

EXPOSE 8080
CMD ["/opt/app/MephiML"]