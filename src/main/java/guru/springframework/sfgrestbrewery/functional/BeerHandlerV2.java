package guru.springframework.sfgrestbrewery.functional;


import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static guru.springframework.sfgrestbrewery.functional.BeerRouterConfig.BEER_PATH_V2;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService;

    public Mono<ServerResponse> getBeerById(ServerRequest serverRequest) {

        Integer beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));
        Boolean showInventory = Boolean.valueOf(serverRequest.queryParam("showInventory").orElse("false"));

        return beerService.getById(beerId, showInventory)
                .flatMap(beerDto ->
                        ServerResponse.ok().bodyValue(beerDto))  //ok
                .switchIfEmpty(ServerResponse.notFound().build());//404

    }

    public Mono<ServerResponse> getBeerByUpc(ServerRequest serverRequest) {

        String beerUpc = String.valueOf(serverRequest.pathVariable("beerUpc"));

        return beerService.getByUpc(beerUpc)
                .flatMap(beerDto ->
                        ServerResponse.ok().bodyValue(beerDto))  //ok
                .switchIfEmpty(ServerResponse.notFound().build());//404

    }

    public Mono<ServerResponse> saveBeer(ServerRequest serverRequest) {

        Mono<BeerDto> beerToSave = serverRequest.bodyToMono(BeerDto.class);

        return beerService.saveBeerMono(beerToSave)
                .flatMap(beerDto -> {
                    return ServerResponse
                            .noContent()
                            .header("location", BEER_PATH_V2 + "/" + beerDto.getId())
                            .build();
                });
    }
}
