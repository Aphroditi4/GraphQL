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
import org.example.utils.FileUtils;
import org.example.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class Main {

    private static final String SCHEMA_PATH = "/graphql/schema/beer.graphql";
    private static final String DATA_PATH = "/data/beers.json";

    private static final Map<String, Beer> beers = new ConcurrentHashMap<>();
    private static final Map<String, Brewery> breweries = new ConcurrentHashMap<>();

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

            String query = "{ beers { id name style brewery { name country } } }";
            System.out.println("Executing query: " + query);
            ExecutionResult executionResult = graphQL.execute(query);
            System.out.println("Query executed successfully.");
            System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));

            String mutation = "mutation { addBeer(input: { name: \"IPA\", style: \"Ale\", breweryId: \"1\" }) { id name style brewery { name country } } }";
            System.out.println("Executing mutation: " + mutation);
            executionResult = graphQL.execute(mutation);
            System.out.println("Mutation executed successfully.");
            System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadData() {
        List<Map<String, String>> beerData = JsonUtils.loadFromJsonFile("/data/beers.json", new TypeReference<>() {});
        List<Map<String, String>> breweryData = JsonUtils.loadFromJsonFile("/data/breweries.json", new TypeReference<>() {});

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
                        .dataFetcher("brewery", environment -> breweries.get(environment.getArgument("id"))))
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
                            return beer;
                        }))
                .build();
    }

    private static GraphQL createGraphQLEntryPoint(TypeDefinitionRegistry typeDefinitionRegistry, RuntimeWiring runtimeWiring) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }
}
