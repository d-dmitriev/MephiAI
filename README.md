# MephiML — Анализ тональности текста на русском языке

Проект `MephiML` реализует REST-сервис для анализа тональности (sentiment analysis) текста на русском языке. Сервис использует обученную модель машинного обучения на основе библиотеки [Tribuo](https://tribuo.org/) и развернут в виде легковесного native-приложения, собранного с помощью GraalVM.

## 📦 Стек технологий

- **Язык**: Java 24
- **Фреймворк**: Spring Boot 3.5.7
- **ML-библиотека**: Tribuo 4.3.0 (линейная SGD-классификация)
- **Сборка native-образа**: GraalVM + Maven Plugin
- **Контейнеризация**: Docker (UBI10 Micro)
- **Оркестрация**: Kubernetes (Minikube)
- **Мониторинг**: Prometheus + Grafana (через kube-prometheus-stack)

## 🚀 Быстрый старт

### 1. Обучение модели

Модель обучается на расширенном наборе данных из 131 примера (60 положительных, 71 отрицательный). Для запуска обучения:

```bash
mvn -Ptrain-model test-compile
```

Обученная модель сохраняется в `models/final_sentiment_model.proto`.

> ⚠️ Убедитесь, что директория `models/` существует перед запуском.

### 2. Сборка native-приложения

```bash
./mvnw clean package -Pnative -Pproduction -DskipTests
```

Результат: исполняемый файл `target/MephiML`.

### 3. Сборка Docker-образа

```bash
docker build -t mephi-tribuo:1.0.4-bin .
```

Или через Minikube (для локального кластера):

```bash
minikube image build -t mephi-tribuo:1.0.4-bin .
```

### 4. Запуск в Kubernetes

Примените манифест:

```bash
kubectl apply -f spring-boot-app.yaml
```

Это создаст:
- Deployment с 3 репликами
- Service типа LoadBalancer
- Ingress (через NGINX Ingress Controller)
- HorizontalPodAutoscaler (CPU ≥ 50%)
- ServiceMonitor для интеграции с Prometheus

### 5. Доступ к сервису

Если используется Minikube:

```bash
minikube service spring-boot-sentiment-service --url
```

Пример запроса:

```bash
curl "http://<EXTERNAL-IP>/api/sentiment?text=Отличный товар!"
```

Ответ:

```json
{
  "sentiment": "positive"
}
```

С подробной информацией (`detailed=true`):

```bash
curl "http://<EXTERNAL-IP>/api/sentiment?text=Ужасное качество&detailed=true"
```

```json
{
  "sentiment": "negative",
  "confidence": 98.7,
  "featuresUsed": 5,
  "text": "Ужасное качество"
}
```

## 📊 Мониторинг

Проект интегрирован с Prometheus и Grafana:

- Эндпоинт метрик: `/actuator/prometheus`
- Health-check эндпоинты:
    - Liveness: `/actuator/health/liveness`
    - Readiness: `/actuator/health/readiness`
- Custom health indicator: проверяет загрузку модели

Для установки стека мониторинга в Minikube:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
  --create-namespace --namespace monitoring \
  --set grafana.adminPassword=admin123
```

Порт-форвардинг Grafana:

```bash
kubectl port-forward svc/prometheus-grafana 3000:80 -n monitoring --address 0.0.0.0
```

Затем откройте `http://localhost:3000`, войдите с логином `admin` / паролем `admin123` и импортируйте дашборд **ID 4701** (JVM Micrometer).

## 🔒 Безопасность контейнера

- `readOnlyRootFilesystem: true`
- `runAsNonRoot: true`
- Все привилегии отозваны (`drop: ALL`)
- Seccomp profile: `RuntimeDefault`
- Временные файлы монтируются в `emptyDir:/tmp`

## 🧪 Тестирование

Проект включает:

- Юнит-тесты (`SentimentServiceIntegrationTest`)
- Нагрузочный скрипт (`test.sh`) — регулирует интервал между запросами для эмуляции нагрузки
- Демо-запуск модели (`SentimentTestApp`)

## 🛠️ Полезные команды

### Генерация конфигурации для GraalVM

Для корректной сериализации Tribuo-модели используйте агент:

```bash
/Library/Java/JavaVirtualMachines/graalvm-23.jdk/Contents/Home/bin/java \
  -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image \
  -jar target/MephiML-1.0-SNAPSHOT.jar
```

### Настройка окружения (Minikube)

Скрипт `minikube.sh` автоматически устанавливает:
- `kubectl`
- `helm`
- `minikube`
- Docker + containerd
- Включает аддоны: `dashboard`, `metrics-server`
- Развертывает Prometheus/Grafana

## 📁 Структура проекта

```
.
├── Dockerfile                 # Multi-stage сборка: builder → model → runtime (UBI Micro)
├── pom.xml                    # Spring Boot + Tribuo + native profile
├── spring-boot-app.yaml       # Kubernetes манифесты
├── models/                    # Обученные модели (.proto)
├── src/
│   ├── main/
│   │   ├── java/              # Контроллер, сервис, health indicator
│   │   └── resources/
│   │       └── META-INF/native-image/  # Reachability metadata для GraalVM
│   └── test/
│       └── java/              # Тренировка модели и интеграционные тесты
├── test.sh                    # Нагрузочный тест
└── minikube.sh                # Скрипт развертывания локального кластера
```