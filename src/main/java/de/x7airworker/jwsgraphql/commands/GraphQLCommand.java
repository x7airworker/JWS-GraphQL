package de.x7airworker.jwsgraphql.commands;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.javawebstack.abstractdata.AbstractMapper;
import org.javawebstack.command.Command;
import org.javawebstack.command.CommandResult;
import org.javawebstack.command.CommandSystem;

import java.util.List;
import java.util.Map;

public class GraphQLCommand implements Command {
    private final AbstractMapper abstractMapper = new AbstractMapper();

    public CommandResult execute(CommandSystem commandSystem, List<String> list, Map<String, List<String>> map) {
        GraphQL graphQL = commandSystem.getInjector().getInstance(GraphQL.class);
        String query = String.join("", list);
        ExecutionResult result = graphQL.execute(query);
        if (result.getErrors().size() > 0)
            return CommandResult.error(abstractMapper.toAbstract(result.getErrors()).toJson().getAsString());
        System.out.println(abstractMapper.toAbstract(result.getData()).toJson());
        return CommandResult.success();
    }
}
