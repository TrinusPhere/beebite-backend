package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.catalog.dto.ModifierGroupRequest;
import com.trinusphere.beebite.domain.catalog.valueobject.ModifierType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class CatalogIntegrationTest {

    private String getToken() {
        return given().contentType(ContentType.JSON).body(Map.of("login", "admin", "password", "admin123")).when()
                .post("/api/v1/auth/login").then().statusCode(200).extract().path("token");
    }

    @Test
    public void testCreateModifierGroup() {
        String token = getToken();

        given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body("""
                    {
                        "name": "Ponto da Carne",
                        "type": "RADIO",
                        "required": true,
                        "minSelections": 1,
                        "maxSelections": 1,
                        "options": [
                            {"name": "Ao Ponto", "price": 0},
                            {"name": "Bem Passado", "price": 0}
                        ]
                    }
                """).when().post("/api/v1/modifier-groups").then().statusCode(201).body("id", notNullValue());
    }
}
