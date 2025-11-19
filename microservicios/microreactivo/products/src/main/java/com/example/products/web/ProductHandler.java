package com.example.products.web;
import com.example.products.domain.Product; import com.example.products.repo.ProductRepository; import org.springframework.http.MediaType; import org.springframework.stereotype.Component; import org.springframework.web.reactive.function.server.ServerRequest; import org.springframework.web.reactive.function.server.ServerResponse; import reactor.core.publisher.Mono; import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {
  private final ProductRepository repo; public ProductHandler(ProductRepository r){this.repo=r;}

  public Mono<ServerResponse> all(ServerRequest req){ return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(repo.findAll(), Product.class);} 
  public Mono<ServerResponse> byId(ServerRequest req){ var id=Long.parseLong(req.pathVariable("id")); return repo.findById(id).flatMap(p->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(p)).switchIfEmpty(ServerResponse.notFound().build()); }
  public Mono<ServerResponse> create(ServerRequest req){ return req.bodyToMono(Product.class).flatMap(repo::save).flatMap(p->ServerResponse.created(req.uri().resolve("/"+p.id())).body(fromValue(p))); }
  public Mono<ServerResponse> update(ServerRequest req){ var id=Long.parseLong(req.pathVariable("id")); return repo.existsById(id).flatMap(exists-> exists ? req.bodyToMono(Product.class).map(p->new Product(id,p.name(),p.price(),p.stock())).flatMap(repo::save).flatMap(saved->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(saved)) : ServerResponse.notFound().build()); }
  public Mono<ServerResponse> delete(ServerRequest req){ var id=Long.parseLong(req.pathVariable("id")); return repo.deleteById(id).then(ServerResponse.noContent().build()); }
}
