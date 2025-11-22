package com.supplyboost.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UserJourneySimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080/api")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Performance Test")

  // Scenario: Complete user purchase journey
  val purchaseJourney = scenario("User Purchase Journey")
    .exec(http("Register User")
      .post("/identity/users/register")
      .body(StringBody("""{"username":"test_${__UUID()}","email":"test_${__UUID()}@example.com","password":"Test123!"}"""))
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("userId")))
    .pause(1)
    .exec(http("Login")
      .post("/identity/auth/login")
      .body(StringBody("""{"username":"test_${userId}","password":"Test123!"}"""))
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken")))
    .pause(1)
    .exec(http("Browse Products")
      .get("/catalog/products")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200))
      .check(jsonPath("$[0].id").saveAs("productId")))
    .pause(2)
    .exec(http("Add to Cart")
      .post("/cart/items")
      .header("Authorization", "Bearer ${authToken}")
      .body(StringBody("""{"productId":"${productId}","quantity":1}"""))
      .check(status.is(200)))
    .pause(1)
    .exec(http("View Cart")
      .get("/cart")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(2)
    .exec(http("Create Order")
      .post("/orders")
      .header("Authorization", "Bearer ${authToken}")
      .body(StringBody("""{"shippingAddress":"123 Main St","paymentMethod":"CREDIT_CARD"}"""))
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("orderId")))
    .pause(1)
    .exec(http("Check Order Status")
      .get("/orders/${orderId}")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))

  // Load test scenarios
  val loadTest = scenario("Load Test - Concurrent Users")
    .exec(http("Health Check")
      .get("/identity/actuator/health")
      .check(status.is(200)))
    .pause(100.milliseconds)
    .exec(http("Get Products")
      .get("/catalog/products")
      .check(status.is(200)))

  // Spike test
  val spikeTest = scenario("Spike Test")
    .exec(http("Catalog Search")
      .get("/catalog/products/search?q=test")
      .check(status.is(200)))

  // Setup simulation with different injection profiles
  setUp(
    // Ramp up users gradually
    purchaseJourney.inject(
      rampUsers(50).during(60.seconds),
      constantUsersPerSec(10).during(120.seconds)
    ).protocols(httpProtocol),

    // Load test with steady state
    loadTest.inject(
      constantUsersPerSec(20).during(180.seconds)
    ).protocols(httpProtocol),

    // Spike test
    spikeTest.inject(
      nothingFor(60.seconds),
      atOnceUsers(100)
    ).protocols(httpProtocol)
  ).assertions(
    global.responseTime.max.lt(2000),
    global.responseTime.percentile3.lt(1000),
    global.successfulRequests.percent.gt(95)
  )
}
