# orbit

Как собрать и запустить
Поднять инфраструктуру:
``` bash

docker-compose up -d
```


Собрать каждый сервис (из его папки):

```bash
cd payments-service
./gradlew bootJar
cd ../orders-service
./gradlew bootJar
cd ../gateway
./gradlew bootJar
```

Запустить сервисы (в разных терминалах или фоново):


```bash
java -jar payments-service/build/libs/payments-service-0.0.1-SNAPSHOT.jar
java -jar orders-service/build/libs/orders-service-0.0.1-SNAPSHOT.jar
java -jar gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar
```
Проверить работу:
Выполнить curl запросы через Gateway (http://localhost:8080) или напрямую к сервисам.

Запустить автотесты:

```bash
cd tests
gradle test allureReport
```