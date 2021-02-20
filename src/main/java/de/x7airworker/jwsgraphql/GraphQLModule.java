package de.x7airworker.jwsgraphql;

import de.x7airworker.jwsgraphql.commands.GraphQLCommand;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.javawebstack.command.CommandSystem;
import org.javawebstack.framework.WebApplication;
import org.javawebstack.framework.module.Module;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.injector.Injector;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class GraphQLModule implements Module {
    private String route;
    private Class<?>[] services;
    private Consumer<GraphQLSchemaGenerator> generatorFunction;

    public GraphQLModule(String route, Class<?>... services) {
        this.route = route;
        this.services = services;
    }

    public GraphQLModule(Class<?>... services) {
        this("/graphql", services);
    }

    public void setupInjection(WebApplication application, Injector injector) {
        GraphQLSchemaGenerator generator = new GraphQLSchemaGenerator();
        for (Class clazz : services) {
            Object instance = injector.getInstance(clazz);
            if (instance == null) {
                try {
                    instance = clazz.getDeclaredConstructor().newInstance();
                    injector.inject(instance);
                    injector.setInstance(clazz, instance);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            generator = generator.withOperationsFromSingleton(instance);
        }

        if (generatorFunction != null)
            generatorFunction.accept(generator);

        GraphQLSchema schema = generator.generate();
        injector.setInstance(GraphQLSchema.class, schema);
        injector.setInstance(GraphQL.class, new GraphQL.Builder(schema).build());
    }

    public GraphQLModule generator (Consumer<GraphQLSchemaGenerator> generatorFunction) {
        this.generatorFunction = generatorFunction;
        return this;
    }

    public void setupServer(WebApplication application, HTTPServer server) {
        server.any(route, new HttpGraphQLHandler(application.getInjector()));
    }

    public void setupCommands(WebApplication application, CommandSystem system) {
        system.addCommand("graphql", new GraphQLCommand());
    }
}
