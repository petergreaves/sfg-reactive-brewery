package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebFluxTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    WebTestClient webFluxTest;

    @MockBean
    BeerService  beerService;

    BeerDto validBeer, anotherValidBeer;

    BeerPagedList beerPagedList;

    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder()
                .beerName("Test beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .build();

        anotherValidBeer = BeerDto.builder()
                .beerName("Test beer 2")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .build();
    }

    @Test
    public void getBeerById() throws Exception {

        UUID beerID = UUID.randomUUID();
        given(beerService.getById(any(), any())).willReturn(validBeer);

        webFluxTest.get().uri("/api/v1/beer/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));


    }

    @Test
    public void listBeers() throws Exception {
        beerPagedList = new BeerPagedList(List.of(validBeer, anotherValidBeer));

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(beerPagedList);

        webFluxTest.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.content[0].beerName").isEqualTo("Test beer")
                .jsonPath("$.content[1].beerStyle").isEqualTo("PALE_ALE")
                .jsonPath("$.content.length()").isEqualTo(2);
    }

    @Test
    public void getBeerByUpc() throws Exception {

        String upc  = BeerLoader.BEER_1_UPC;
        given(beerService.getByUpc(any())).willReturn(validBeer);

        webFluxTest.get().uri("/api/v1/beerUpc/" + upc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));

    }

    @Test
    public void saveNewBeer() throws Exception {
    }

    @Test
    public void updateBeerById() throws Exception {
    }

    @Test
    public void deleteBeerById() throws Exception {

        UUID beerID = UUID.randomUUID();

        webFluxTest.delete().uri("/api/v1/beer/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(beerService, times(1)).deleteBeerById(any());

    }
}