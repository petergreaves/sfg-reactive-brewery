package guru.springframework.sfgrestbrewery.functional;


import guru.springframework.sfgrestbrewery.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService ;

    public Mono<ServerResponse> getBeerById(ServerRequest serverRequest){

        Integer beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));
        Boolean showInventory = Boolean.valueOf(serverRequest.queryParam("showInventory").orElse("false"));

        return beerService.getById(beerId, showInventory)
                .flatMap(beerDto ->
                    ServerResponse.ok().bodyValue(beerDto))  //ok
                .switchIfEmpty(ServerResponse.notFound().build());//404

    }

    public Mono<ServerResponse> getBeerByUpc(ServerRequest serverRequest){

        String beerUpc = String.valueOf(serverRequest.pathVariable("beerUpc"));

        return beerService.getByUpc(beerUpc)
                .flatMap(beerDto ->
                        ServerResponse.ok().bodyValue(beerDto))  //ok
                .switchIfEmpty(ServerResponse.notFound().build());//404

    }

}
