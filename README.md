Запускаем с агентом для анализа зависимостей (здесь в основном нужно для правильного переноса сериализации)

```bach
/Library/Java/JavaVirtualMachines/graalvm-23.jdk/Contents/Home/bin/java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-imagesrc/main/resources/META-INF/native-image -jar target/MephiML-1.0-SNAPSHOT.jar
```