package com.orbitamarket.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

public class EndToEndTests {

    private static String PAYMENTS_URL = "http://localhost:8081";
    private static String ORDERS_URL = "http://localhost:8082";

    @BeforeAll
    static void setup() {
        // При необходимости можно настроить через Gateway
        // PAYMENTS_URL = "http://localhost:8080";
        // ORDERS_URL = "http://localhost:8080";
    }

    @Test
    void fullFlowSuccess() {
        String userId = "user-42";

        // Создаём счёт
        given()
            .header("X-User-Id", userId)
            .post(PAYMENTS_URL + "/api/v1/payments/accounts")
            .then().statusCode(200);

        // Пополняем на 1000
        given()
            .header("X-User-Id", userId)
            .contentType(ContentType.JSON)
            .body(Map.of("amount", 1000))
            .post(PAYMENTS_URL + "/api/v1/payments/accounts/top-up")
            .then().statusCode(200);

        // Создаём заказ на 120
        String orderId = given()
            .header("X-User-Id", userId)
            .contentType(ContentType.JSON)
            .body("{\"product_type\":\"ARCHIVE\",\"price\":120,\"payload\":{\"aoi\":\"POLYGON((...))\"}}")
            .post(ORDERS_URL + "/api/v1/orders")
            .then().statusCode(201)
            .extract().path("order_id");

        // Ждём статус PAID
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("X-User-Id", userId)
                .get(ORDERS_URL + "/api/v1/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("status", equalTo("PAID"));
        });

        // Проверяем баланс
        given()
            .header("X-User-Id", userId)
            .get(PAYMENTS_URL + "/api/v1/payments/accounts/balance")
            .then()
            .body("balance", equalTo(880));
    }

    
}