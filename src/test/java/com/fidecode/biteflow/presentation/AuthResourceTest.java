package com.fidecode.biteflow.presentation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    void shouldLoginWithValidCredentialsAndReturnToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "login": "admin", "password": "admin123" }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", equalTo(86400))
            .body("user.login", equalTo("admin"))
            .body("user.role", equalTo("ADMIN"))
            .body("user.permissions", hasItem("dashboard"))
            .body("user.permissions", hasItem("pedidos"));
    }

    @Test
    void shouldReturn401WhenPasswordIsWrong() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "login": "admin", "password": "wrongpass" }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401)
            .body("status", equalTo(401))
            .body("error", equalTo("Unauthorized"))
            .body("message", notNullValue());
    }

    @Test
    void shouldReturn401WhenLoginNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "login": "naoexiste", "password": "qualquer" }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn400WhenBodyIsInvalid() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "login": "", "password": "" }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn401WhenMeCalledWithoutToken() {
        given()
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn204OnLogout() {
        String token = getAdminToken();
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/api/v1/auth/logout")
        .then()
            .statusCode(204);
    }

    @Test
    void shouldReturnMeDataWithValidToken() {
        String token = getAdminToken();
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(200)
            .body("login", equalTo("admin"))
            .body("role", equalTo("ADMIN"));
    }

    static String getAdminToken() {
        return given()
            .contentType(ContentType.JSON)
            .body("""
                { "login": "admin", "password": "admin123" }
                """)
            .post("/api/v1/auth/login")
            .then().statusCode(200)
            .extract().path("token");
    }
}
