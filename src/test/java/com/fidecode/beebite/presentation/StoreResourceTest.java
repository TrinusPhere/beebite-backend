package com.fidecode.beebite.presentation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class StoreResourceTest {

    @Test
    void shouldReturn401WhenNoToken() {
        given().when().get("/api/v1/store").then().statusCode(401);
    }

    @Test
    void shouldReturnStoreWithValidToken() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).when().get("/api/v1/store").then().statusCode(200)
                .body("name", notNullValue()).body("id", notNullValue());
    }

    @Test
    void shouldUpdateStoreAsAdmin() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body("""
                {
                  "name": "BiteFlow Atualizado",
                  "logoUrl": null,
                  "isOpen": true,
                  "openingHours": "10:00 - 22:00",
                  "address": "Rua Teste, 999"
                }
                """).when().put("/api/v1/store").then().statusCode(200).body("name", equalTo("BiteFlow Atualizado"))
                .body("isOpen", equalTo(true));
    }

    @Test
    void shouldReturn403WhenNonAdminTriesToUpdateStore() {
        // Cria usuário GERENTE
        String adminToken = AuthResourceTest.getAdminToken();
        String login = "gerente_store_" + System.currentTimeMillis();
        given().header("Authorization", "Bearer " + adminToken).contentType(ContentType.JSON).body("""
                {
                  "name": "Gerente Store",
                  "login": "%s",
                  "password": "gerente123",
                  "role": "GERENTE",
                  "permissions": ["pedidos"]
                }
                """.formatted(login)).post("/api/v1/users").then().statusCode(201);

        String gerenteToken = given().contentType(ContentType.JSON).body("""
                { "login": "%s", "password": "gerente123" }
                """.formatted(login)).post("/api/v1/auth/login").then().statusCode(200).extract().path("token");

        given().header("Authorization", "Bearer " + gerenteToken).contentType(ContentType.JSON).body("""
                { "name": "Loja Hackeada", "isOpen": false, "logoUrl": null,
                  "openingHours": null, "address": null }
                """).when().put("/api/v1/store").then().statusCode(403);
    }
}
