package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;


import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WebFluxTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService  beerService;

    @InjectMocks
    BeerController beerController;

    BeerDto validBeerForPostOrPut, validBeerForGet, anotherValidBeer, updatedBeer, newBeerPreSave, newBeerPostSave;

    BeerPagedList beerPagedList;

    @BeforeEach
    void setUp() {
        validBeerForGet = BeerDto.builder()
                .id(9)
                .quantityOnHand(0)
                .price(new BigDecimal(3))
                .beerName("Test beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .build();

        validBeerForPostOrPut= BeerDto.builder()
                .quantityOnHand(0)
                .price(new BigDecimal(3))
                .beerName("Test beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .build();

        anotherValidBeer = BeerDto.builder()
                .beerName("Test beer 2")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .build();

        updatedBeer = BeerDto.builder()
                .id(1)
                .beerName("Test beer 2 Updated")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .build();

        newBeerPreSave = BeerDto.builder()
                .beerName("Test beer 2 Updated")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .build();

        newBeerPostSave = BeerDto.builder()
                .id(1)
                .beerName("Test beer 2 Updated")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("List all beers")
    public void listBeers() throws Exception {
        beerPagedList = new BeerPagedList(Arrays.asList(validBeerForGet, anotherValidBeer), PageRequest.of(1,1),2);

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(Mono.just(beerPagedList));

        webTestClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.content[0].beerName").isEqualTo("Test beer")
                .jsonPath("$.content[1].beerStyle").isEqualTo("PALE_ALE")
                .jsonPath("$.content.length()").isEqualTo(2);
    }

    @Test
    @DisplayName("Get a beer by ID")
    public void getBeerById() throws Exception {

        given(beerService.getById(any(), any())).willReturn(Mono.just(validBeerForGet));

        webTestClient.get()
                .uri("/api/v1/beer/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeerForGet.getBeerName()));

        verify(beerService, times(1)).getById(any(), any());
    }

    @Test
    @DisplayName("Get a beer by UPC")
    public void getBeerByUpc() throws Exception {

        String upc  = BeerLoader.BEER_1_UPC;
        given(beerService.getByUpc(any())).willReturn(Mono.just(validBeerForGet));

        webTestClient.get()
                .uri("/api/v1/beerUpc/" + upc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeerForGet.getBeerName()));

        verify(beerService, times(1)).getByUpc(any());

    }

    @Test
    @DisplayName("Save new beer")
    public void saveNewBeer() throws Exception {

        Integer beerID = 1;
        newBeerPostSave.setId(beerID);
        given(beerService.saveNewBeer(any())).willReturn(Mono.just(newBeerPostSave));

        webTestClient.post().uri("/api/v1/beer")
                .body(BodyInserters.fromValue(validBeerForPostOrPut))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "http://api.springframework.guru/api/v1/beer/"+beerID.toString())
                .expectBody().isEmpty()
                ;

        verify(beerService, times(1)).saveNewBeer(any());

    }

    @Test
    @DisplayName("Update a beer")
    public void updateBeerById() throws Exception {

        Integer beerID = 1;

        given(beerService.updateBeer(beerID, validBeerForPostOrPut)).willReturn(Mono.just(updatedBeer));

        webTestClient.put().uri("/api/v1/beer/1")
                .body(BodyInserters.fromValue(validBeerForPostOrPut))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

       verify(beerService, times(1)).updateBeer(any(), any());
    }

    @Test
    @DisplayName("Delete a beer by ID")
    public void deleteBeerById() throws Exception {

        Integer beerID = 1;

        webTestClient.delete().uri("/api/v1/beer/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(beerService, times(1)).deleteBeerById(any());

    }
}