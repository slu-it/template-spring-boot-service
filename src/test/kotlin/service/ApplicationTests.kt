package service

import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationTests {

    @BeforeEach
    fun setupRestAssured(@LocalServerPort port: Int) {
        RestAssured.port = port
    }

    @Nested
    inner class ActuatorSecurity {

        val publicEndpoints = setOf("/actuator/info", "/actuator/health")
        val privateEndpoints = setOf("/actuator/metrics")

        @TestFactory
        fun `without credentials only public endpoints are available`() =
            dynamicTests(publicEndpoints to OK, privateEndpoints to UNAUTHORIZED) { endpoint, status ->
                assertThatBasicAuthUserReturnsStatus(endpoint, status)
            }

        @TestFactory
        fun `with credentials of user with ACTUATOR scope all endpoints are available`() =
            dynamicTests(publicEndpoints to OK, privateEndpoints to OK) { endpoint, status ->
                assertThatBasicAuthUserReturnsStatus(endpoint, status, "actuator")
            }

        @TestFactory
        fun `with credentials of user without ACTUATOR scope only public endpoints are available`() =
            dynamicTests(publicEndpoints to OK, privateEndpoints to FORBIDDEN) { endpoint, status ->
                assertThatBasicAuthUserReturnsStatus(endpoint, status, "user")
            }

        private fun dynamicTests(
            vararg expectations: Pair<Set<String>, HttpStatus>,
            block: (String, HttpStatus) -> Unit
        ) = mapOf(*expectations)
            .flatMap { (endpoints, status) -> endpoints.map { it to status } }
            .map { (endpoint, status) -> DynamicTest.dynamicTest("$endpoint >> $status") { block(endpoint, status) } }

        private fun assertThatBasicAuthUserReturnsStatus(path: String, status: HttpStatus, username: String? = null) =
            Given {
                if (username != null) auth().preemptive().basic(username, username.reversed()) else this
            } When {
                get(path)
            } Then {
                statusCode(status.value())
            }
    }
}
