package guru.springframework.sfgrestbrewery.functional;


import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.controller.NotFoundException;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import static guru.springframework.sfgrestbrewery.functional.BeerRouterConfig.BEER_PATH_V2;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService;
    private final Validator validator;

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

        Mono<BeerDto> beerToSave = serverRequest.bodyToMono(BeerDto.class).doOnNext(this::validate);

        return beerService.saveBeerMono(beerToSave)
                .flatMap(beerDto -> {
                    return ServerResponse
                            .noContent()
                            .header("location", BEER_PATH_V2 + "/" + beerDto.getId())
                            .build();
                });
    }

    public Mono<ServerResponse> updateBeer(ServerRequest serverRequest) {


        Integer beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));
        return serverRequest
                .bodyToMono(BeerDto.class)
                .doOnNext(this::validate)
                .flatMap(beerToUpdate -> {
                            return beerService.updateBeer(beerId, beerToUpdate);
                        }
                )
                .flatMap(savedBeerDto -> {
                    if (savedBeerDto.getId() != null) {
                        return ServerResponse
                                .noContent()
                                .build();
                    } else {
                        log.debug("Beer ID not found {} ", beerId);
                        return ServerResponse
                                .notFound()
                                .build();
                    }
                });
    }


    public Mono<ServerResponse> deleteBeer(ServerRequest serverRequest) {

        Integer beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));

        return beerService.deleteBeerByIdReactive(beerId)
                .flatMap(monoVoid ->{
                        return ServerResponse.noContent().build();  //ok
                })
                .onErrorResume(e -> e instanceof NotFoundException, e->ServerResponse.notFound().build());


    }

    private void validate(BeerDto beerDto) {
        Errors errors = new BeanPropertyBindingResult(BeerDto.class, "beerDto");
        validator.validate(beerDto, errors);
        if (errors.hasErrors()) {
            System.out.println(errors.toString());
            throw new ServerWebInputException(errors.toString());
        }
    }
}
