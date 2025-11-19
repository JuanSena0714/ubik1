package com.example.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(GatewayRoutingTest.TestRoutes.class)
class GatewayRoutingTest {
  @Autowired WebTestClient client;
  static DisposableServer stub;

  @BeforeAll static void startStub(){
    stub = HttpServer.create().port(0).route(r -> r
      .get("/ok", (req,res) -> res.sendString(Mono.just("ok")))
    ).bindNow();
  }
  @AfterAll static void stop(){ if(stub!=null) stub.disposeNow(); }

  @Test void routes_through_gateway(){
    client.get().uri("/t/anything").exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("ok");
  }

  @Configuration static class TestRoutes {
    @Bean RouteLocator testRouteLocator(RouteLocatorBuilder b){
      int port = stub.port();
      return b.routes()
        .route("test", r -> r.path("/t/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:"+port))
        .build();
    }
  }
}
