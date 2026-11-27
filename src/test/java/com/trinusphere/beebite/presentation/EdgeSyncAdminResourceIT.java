package com.trinusphere.beebite.presentation;

import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EdgeSyncAdminResourceIT {

    @Inject
    StoreRepository storeRepository;

    @Test
    void shouldAllowCaixaOnHealthButForbidCaixaAndUnprivilegedGerenteOnAdminEndpoints() {
        String adminToken = AuthResourceTest.getAdminToken();

        given().header("Authorization", "Bearer " + adminToken)
                .when().get("/api/v1/sync/health")
                .then().statusCode(200)
                .body("syncMode", notNullValue());

        given().header("Authorization", "Bearer " + adminToken)
                .when().get("/api/v1/sync/failed")
                .then().statusCode(200);

        String caixaLogin = "caixa_sync_" + System.currentTimeMillis();
        given().header("Authorization", "Bearer " + adminToken).contentType(ContentType.JSON).body("""
                {
                  "name": "Caixa Sync",
                  "login": "%s",
                  "password": "caixa123",
                  "role": "CAIXA",
                  "permissions": ["pedidos", "caixa"]
                }
                """.formatted(caixaLogin)).post("/api/v1/users").then().statusCode(201);

        String caixaToken = given().contentType(ContentType.JSON).body("""
                { "login": "%s", "password": "caixa123" }
                """.formatted(caixaLogin)).post("/api/v1/auth/login").then().statusCode(200).extract().path("token");

        given().header("Authorization", "Bearer " + caixaToken)
                .when().get("/api/v1/sync/health")
                .then().statusCode(200);

        given().header("Authorization", "Bearer " + caixaToken)
                .when().get("/api/v1/sync/failed")
                .then().statusCode(403);

        String gerenteLogin = "gerente_no_int_" + System.currentTimeMillis();
        given().header("Authorization", "Bearer " + adminToken).contentType(ContentType.JSON).body("""
                {
                  "name": "Gerente Sem Integracoes",
                  "login": "%s",
                  "password": "gerente123",
                  "role": "GERENTE",
                  "permissions": ["pedidos"]
                }
                """.formatted(gerenteLogin)).post("/api/v1/users").then().statusCode(201);

        String gerenteToken = given().contentType(ContentType.JSON).body("""
                { "login": "%s", "password": "gerente123" }
                """.formatted(gerenteLogin)).post("/api/v1/auth/login").then().statusCode(200).extract().path("token");

        given().header("Authorization", "Bearer " + gerenteToken)
                .when().get("/api/v1/sync/failed")
                .then().statusCode(403);

        // Endpoint interno de reconciliação na nuvem valida X-Edge-Device-Token
        Store edgeStore = new Store("Loja Reconcile HTTP");
        edgeStore.configureEdgeSync("EDGE", "device-token-xyz");
        edgeStore = storeRepository.save(edgeStore);

        given().contentType(ContentType.JSON)
                .header("X-Edge-Device-Token", "wrong")
                .body("""
                        { "storeId": "%s", "from": "2026-11-20", "to": "2026-11-26", "digests": [] }
                        """.formatted(edgeStore.getId()))
                .when().post("/api/v1/internal/edge-sync/reconcile/buckets")
                .then().statusCode(401);

        given().contentType(ContentType.JSON)
                .header("X-Edge-Device-Token", "device-token-xyz")
                .body("""
                        { "storeId": "%s", "from": "2026-11-20", "to": "2026-11-26", "digests": [] }
                        """.formatted(edgeStore.getId()))
                .when().post("/api/v1/internal/edge-sync/reconcile/buckets")
                .then().statusCode(200);
    }
}

