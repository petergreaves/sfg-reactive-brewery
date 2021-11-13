package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {

    public static final String BASE_URL = "http://localhost:8080";


    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test
    void testGetBeerByID() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BASE_URL + "/" + BeerRouterConfig.BEER_PATH_V2 + "/5")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            Assertions.assertNotNull(beerDto);
            Assertions.assertTrue(beerDto.getBeerName() != null);
            Assertions.assertTrue(beerDto.getBeerStyle() != null);
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testGetBeerByIDNotFound() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BASE_URL + "/" + BeerRouterConfig.BEER_PATH_V2 + "/999")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
                },
                throwable -> {
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


    @Test
    void testGetBeerByUPC() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BASE_URL + "/" + BeerRouterConfig.BEER_BY_UPC_PATH_V2 + "/" + BeerLoader.BEER_1_UPC)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            Assertions.assertNotNull(beerDto);
            Assertions.assertEquals(beerDto.getUpc(), BeerLoader.BEER_1_UPC);
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testGetBeerByUPCNotFound() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BASE_URL + "/" + BeerRouterConfig.BEER_BY_UPC_PATH_V2 + "/abc")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
                },
                throwable -> {
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


    @Test
    public void testSaveNewValidBeer() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto newBeer = BeerDto.builder()
                .beerName("New beer tests")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal(2.0))
                .upc("ABC")
                .build();

        // for some reason, this version (BodyInserters) sends a
        // dto with null fields...
//        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post()
//                .uri(BeerRouterConfig.BEER_PATH_V2)
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(BodyInserters.fromValue(newBeer))
//                .retrieve().toBodilessEntity();

        // but this one works!
        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post()
                .uri(BeerRouterConfig.BEER_PATH_V2)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(newBeer)
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {
            Assertions.assertNotNull(responseEntity.getHeaders().get("location"));
            Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(countDownLatch.getCount(), 0);


    }


    @Test
    public void testSaveNewInvalidBeerThrowsClientError() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto invalidBeer = BeerDto.builder()
                .price(new BigDecimal(2.0))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post()
                .uri(BASE_URL + "/" + BeerRouterConfig.BEER_PATH_V2)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(BodyInserters.fromValue(invalidBeer))
                .retrieve().toBodilessEntity();

        beerResponseMono.subscribe(responseEntity -> {
            // nothing on success
        }, throwable -> {
            if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest")) {
                WebClientResponseException ex = (WebClientResponseException) throwable;

                if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    countDownLatch.countDown();
                }
            }
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);


    }


    @Test
    void testUpdateBeerNotFound() throws InterruptedException {

        final String newBeerName = "JTs Beer";
        final Integer beerId = 999;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webClient.put().uri(BeerRouterConfig.BEER_PATH_V2 + "/" + beerId)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters
                .fromValue(BeerDto.builder()
                        .beerName(newBeerName)
                        .upc("1233455")
                        .beerStyle("PALE_ALE")
                        .price(new BigDecimal("8.99"))
                        .build()))
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {

                        },
                        throwable -> {
                            if (throwable.getClass().getName().
                                    equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")) {
                                WebClientResponseException ex = (WebClientResponseException) throwable;

                                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                                    countDownLatch.countDown();
                                }
                            }
                        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }


    @Test
    void testUpdateBeer() throws InterruptedException {

        final String newBeerName = "JTs Beer";
        final Integer beerId = 1;
        CountDownLatch countDownLatch = new CountDownLatch(2);

        webClient.put().uri(BeerRouterConfig.BEER_PATH_V2 + "/" + beerId)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters
                .fromValue(BeerDto.builder()
                        .beerName(newBeerName)
                        .upc("1233455")
                        .beerStyle("PALE_ALE")
                        .price(new BigDecimal("8.99"))
                        .build()))
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                    assertThat(responseEntity.getStatusCode().is2xxSuccessful());
                    countDownLatch.countDown();
                });

        //wait for update thread to complete
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        webClient.get().uri(BeerRouterConfig.BEER_PATH_V2 + "/" + beerId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class)
                .subscribe(beer -> {
                    assertThat(beer).isNotNull();
                    assertThat(beer.getBeerName()).isNotNull();
                    assertThat(beer.getBeerName()).isEqualTo(newBeerName);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


    @Test
    void testDeleteBeer() {
        Integer beerId = 3;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webClient.delete().uri(BeerRouterConfig.BEER_PATH_V2 +"/"+ beerId)
                .retrieve().toBodilessEntity()
                .flatMap(responseEntity -> {
                    countDownLatch.countDown();

                    return webClient.get().uri(BeerRouterConfig.BEER_PATH_V2 +"/"+ beerId)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve().bodyToMono(BeerDto.class);
                }).subscribe(savedDto -> {

        }, throwable -> {
            countDownLatch.countDown();
        });
    }

    @Test
    void testDeleteBeerNotFound() {
        Integer beerId = 4;

        // delete it
        webClient.delete().uri("/api/v2/beer/" + beerId)
               .retrieve().toBodilessEntity().block();

        // try to delete it again
        assertThrows(WebClientResponseException.NotFound.class, () -> {
            webClient.delete().uri("/api/v2/beer/" + beerId)
                    .retrieve().toBodilessEntity().block();
        });
    }
}

