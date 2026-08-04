package com.basedew;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class AccountResourceTest {
    @Test
    void testHelloEndpoint() {
//        given()
//          .when().get("/api/v1/account")
//          .then()
//             .statusCode(200)
//             .body(is("Hello from Quarkus REST"));
    }

}