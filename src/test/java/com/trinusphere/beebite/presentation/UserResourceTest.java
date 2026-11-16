package com.trinusphere.beebite.presentation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @Test
    void shouldReturn401WhenNoToken() {
        given().when().get("/api/v1/users").then().statusCode(401);
    }

    @Test
    void shouldListUsersWithAdminToken() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).when().get("/api/v1/users").then().statusCode(200).body("$",
                not(empty()));
    }

    @Test
    void shouldCreateUserAndReturn201() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body("""
                {
                  "name": "Gerente Teste",
                  "login": "gerente_test_%d",
                  "password": "gerente123",
                  "role": "GERENTE",
                  "permissions": ["pedidos", "clientes"]
                }
                """.formatted(System.currentTimeMillis())).when().post("/api/v1/users").then().statusCode(201)
                .body("name", equalTo("Gerente Teste")).body("role", equalTo("GERENTE")).body("id", notNullValue());
    }

    @Test
    void shouldReturn400WhenCreateUserWithMissingFields() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body("""
                { "name": "", "login": "", "password": "", "role": null }
                """).when().post("/api/v1/users").then().statusCode(400);
    }

    @Test
    void shouldReturn404WhenUserNotFound() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).when()
                .get("/api/v1/users/00000000-0000-0000-0000-999999999999").then().statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentUser() {
        String token = AuthResourceTest.getAdminToken();
        given().header("Authorization", "Bearer " + token).when()
                .delete("/api/v1/users/00000000-0000-0000-0000-999999999999").then().statusCode(404);
    }
}
