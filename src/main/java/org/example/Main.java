package org.example;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.example.models.Beer;
import org.example.models.Brewery;
import org.example.models.Review;
import org.example.models.User;
import org.example.utils.FileUtils;
import org.example.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class Main {

    private static final String SCHEMA_PATH = "/graphql/schema/beer.graphql";
    private static final String BEER_PATH = "/graphql/query/beer-query.graphql";
    private static final String REVIEWS_QUERY_PATH = "/graphql/query/reviews-query.graphql";
    private static final String USERS_AND_REVIEWS_QUERY_PATH = "/graphql/query/users-and-reviews-query.graphql";

    private static final String BEERS_DATA_PATH = "data/beers.json";
    private static final String BREWERIES_DATA_PATH = "data/breweries.json";
    private static final String REVIEWS_DATA_PATH = "data/reviews.json";
    private static final String USERS_DATA_PATH = "data/users.json";

    private static final Map<String, Beer> beers = new ConcurrentHashMap<>();
    private static final Map<String, Brewery> breweries = new ConcurrentHashMap<>();
    private static final Map<String, Review> reviews = new ConcurrentHashMap<>();
    private static final Map<String, User> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            System.out.println("Starting application...");
            loadData();
            System.out.println("Data loaded successfully.");

            TypeDefinitionRegistry typeDefinitionRegistry = parseSchema(SCHEMA_PATH);
            System.out.println("Schema parsed successfully.");

            RuntimeWiring runtimeWiring = buildRuntimeWiring();
            System.out.println("Runtime wiring built successfully.");

            GraphQL graphQL = createGraphQLEntryPoint(typeDefinitionRegistry, runtimeWiring);
            System.out.println("GraphQL instance created successfully.");

            executeQueriesAndMutations(graphQL);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeQueriesAndMutations(GraphQL graphQL) {
        // Приклад 1: Отримати всі пива
        String queryAllBeers = FileUtils.readFileContent(BEER_PATH);
        executeAndPrintResult(graphQL, queryAllBeers, "All Beers");

        // Приклад 2: Отримати пиво за ID
        String queryBeerById = "{ beer(id: \"1\") { id name style brewery { name country } } }";
        executeAndPrintResult(graphQL, queryBeerById, "Beer by ID");

        // Приклад 3: Отримати всі пивоварні
        String queryAllBreweries = "{ breweries { id name country beers { name style } } }";
        executeAndPrintResult(graphQL, queryAllBreweries, "All Breweries");

        // Приклад 4: Отримати пивоварню за ID
        String queryBreweryById = "{ brewery(id: \"1\") { id name country beers { name style } } }";
        executeAndPrintResult(graphQL, queryBreweryById, "Brewery by ID");

        // Приклад 5: Отримати всі відгуки
        String queryAllReviews = FileUtils.readFileContent(REVIEWS_QUERY_PATH);
        executeAndPrintResult(graphQL, queryAllReviews, "All Reviews");

        // Приклад 6: Отримати користувачів та їх відгуки
        String queryUsersAndReviews = FileUtils.readFileContent(USERS_AND_REVIEWS_QUERY_PATH);
        executeAndPrintResult(graphQL, queryUsersAndReviews, "Users and Reviews");

        // Приклад 7: Додати нове пиво (мутація)
        String mutationAddBeer = "mutation { addBeer(input: { name: \"IPA\", style: \"Ale\", breweryId: \"1\" }) { id name style brewery { name country } } }";
        executeAndPrintResult(graphQL, mutationAddBeer, "Add Beer");

        // Приклад 8: Додати новий відгук (мутація)
        String mutationAddReview = "mutation { addReview(input: { text: \"Great beer!\", rating: 5, beerId: \"1\", userId: \"1\" }) { id text rating beer { name } user { name } } }";
        executeAndPrintResult(graphQL, mutationAddReview, "Add Review");

        // Приклад 9: Додати нового користувача (мутація)
        String mutationAddUser = "mutation { addUser(input: { name: \"John Doe\", email: \"john@example.com\" }) { id name email } }";
        executeAndPrintResult(graphQL, mutationAddUser, "Add User");
    }

    private static void executeAndPrintResult(GraphQL graphQL, String query, String description) {
        System.out.println("\nExecuting: " + description);
        System.out.println("Query: " + query);
        ExecutionResult executionResult = graphQL.execute(query);
        System.out.println("Result:");
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));
    }

    private static void loadData() {
        List<Map<String, String>> beerData = JsonUtils.loadFromJsonFile(BEERS_DATA_PATH, new TypeReference<>() {});
        List<Map<String, String>> breweryData = JsonUtils.loadFromJsonFile(BREWERIES_DATA_PATH, new TypeReference<>() {});
        List<Map<String, String>> reviewData = JsonUtils.loadFromJsonFile(REVIEWS_DATA_PATH, new TypeReference<>() {});
        List<Map<String, String>> userData = JsonUtils.loadFromJsonFile(USERS_DATA_PATH, new TypeReference<>() {});

        for (Map<String, String> brewery : breweryData) {
            Brewery b = new Brewery(brewery.get("id"), brewery.get("name"), brewery.get("location"));
            breweries.put(b.getId(), b);
        }

        for (Map<String, String> beer : beerData) {
            Brewery brewery = breweries.get(beer.get("breweryId"));
            Beer b = new Beer(beer.get("id"), beer.get("name"), beer.get("style"), brewery);
            beers.put(b.getId(), b);
            if (brewery != null) {
                brewery.getBeers().add(b);
            }
        }

        for (Map<String, String> user : userData) {
            User u = new User(user.get("id"), user.get("name"), user.get("email"));
            users.put(u.getId(), u);
        }

        for (Map<String, String> review : reviewData) {
            Beer beer = beers.get(review.get("beerId"));
            User user = users.get(review.get("userId"));
            if (beer != null && user != null) {
                Review r = new Review(
                        review.get("id"),
                        review.get("text"),
                        Integer.parseInt(review.get("rating")),
                        beer,
                        user
                );
                reviews.put(r.getId(), r);
                beer.getReviews().add(r);
                user.getReviews().add(r);
            }
        }
    }

    private static TypeDefinitionRegistry parseSchema(String schemaPath) {
        String schema = FileUtils.readFileContent(schemaPath);
        SchemaParser schemaParser = new SchemaParser();
        return schemaParser.parse(schema);
    }

    private static RuntimeWiring buildRuntimeWiring() {
        return newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("beers", environment -> new ArrayList<>(beers.values()))
                        .dataFetcher("beer", environment -> beers.get(environment.getArgument("id")))
                        .dataFetcher("breweries", environment -> new ArrayList<>(breweries.values()))
                        .dataFetcher("brewery", environment -> breweries.get(environment.getArgument("id")))
                        .dataFetcher("reviews", environment -> new ArrayList<>(reviews.values()))
                        .dataFetcher("review", environment -> reviews.get(environment.getArgument("id")))
                        .dataFetcher("users", environment -> new ArrayList<>(users.values()))
                        .dataFetcher("user", environment -> users.get(environment.getArgument("id"))))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("addBeer", environment -> {
                            Map<String, String> input = environment.getArgument("input");
                            Brewery brewery = breweries.get(input.get("breweryId"));
                            if (brewery == null) {
                                throw new IllegalArgumentException("Brewery not found for ID: " + input.get("breweryId"));
                            }
                            Beer beer = new Beer(UUID.randomUUID().toString(), input.get("name"), input.get("style"), brewery);
                            beers.put(beer.getId(), beer);
                            brewery.getBeers().add(beer);
                            saveBeersToFile();
                            return beer;
                        })
                        .dataFetcher("addReview", environment -> {
                            Map<String, Object> input = environment.getArgument("input");
                            Beer beer = beers.get(input.get("beerId"));
                            User user = users.get(input.get("userId"));
                            if (beer == null || user == null) {
                                throw new IllegalArgumentException("Beer or User not found");
                            }
                            Review review = new Review(
                                    UUID.randomUUID().toString(),
                                    (String) input.get("text"),
                                    (Integer) input.get("rating"),
                                    beer,
                                    user
                            );
                            reviews.put(review.getId(), review);
                            beer.getReviews().add(review);
                            user.getReviews().add(review);
                            saveReviewsToFile();
                            return review;
                        })
                        .dataFetcher("addUser", environment -> {
                            Map<String, String> input = environment.getArgument("input");
                            User user = new User(UUID.randomUUID().toString(), input.get("name"), input.get("email"));
                            users.put(user.getId(), user);
                            saveUsersToFile();
                            return user;
                        }))
                .build();
    }

    private static GraphQL createGraphQLEntryPoint(TypeDefinitionRegistry typeDefinitionRegistry, RuntimeWiring runtimeWiring) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private static void saveBeersToFile() {
        List<Map<String, String>> beerData = new ArrayList<>();
        for (Beer beer : beers.values()) {
            Map<String, String> beerMap = new HashMap<>();
            beerMap.put("id", beer.getId());
            beerMap.put("name", beer.getName());
            beerMap.put("style", beer.getStyle());
            beerMap.put("breweryId", beer.getBrewery().getId());
            beerData.add(beerMap);
        }
        JsonUtils.saveToJsonFile(BEERS_DATA_PATH, beerData);
    }

    private static void saveReviewsToFile() {
        List<Map<String, String>> reviewData = new ArrayList<>();
        for (Review review : reviews.values()) {
            Map<String, String> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("text", review.getText());
            reviewMap.put("rating", String.valueOf(review.getRating()));
            reviewMap.put("beerId", review.getBeer().getId());
            reviewMap.put("userId", review.getUser().getId());
            reviewData.add(reviewMap);
        }
        JsonUtils.saveToJsonFile(REVIEWS_DATA_PATH, reviewData);
    }

    private static void saveUsersToFile() {
        List<Map<String, String>> userData = new ArrayList<>();
        for (User user : users.values()) {
            Map<String, String> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userData.add(userMap);
        }
        JsonUtils.saveToJsonFile(USERS_DATA_PATH, userData);
    }
}