# Автотесты OrbitaMarket

Для запуска необходимо:
1. Запустить инфраструктуру: `docker-compose up -d`
2. Запустить сервисы Payments, Orders, Gateway (из IDE или собранные JAR)
3. Выполнить `gradle test allureReport`