package guru.springframework.sfgrestbrewery.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BeerRouterConfig {


    public static final String BEER_PATH_V2 = "api/v2/beer";

    @Bean
    public RouterFunction<ServerResponse> beerRouterV2(BeerHandlerV2 beerHandlerV2){

        return route().GET(BEER_PATH_V2+"/{beerId}", accept(APPLICATION_JSON), beerHandlerV2::getBeerById).build();
    }
}
