package de.x7airworker.jwsgraphql;

import com.google.gson.Gson;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.javawebstack.abstractdata.AbstractMapper;
import org.javawebstack.abstractdata.util.QueryString;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.httpserver.helper.HttpMethod;
import org.javawebstack.injector.Injector;


public class HttpGraphQLHandler implements RequestHandler {
    private final AbstractMapper abstractMapper = new AbstractMapper();
    private final GraphQL graphQL;

    protected HttpGraphQLHandler(Injector injector) {
        graphQL = injector.getInstance(GraphQL.class);
    }

    @Override
    public Object handle(Exchange exchange) {
        if (exchange.getMethod() != HttpMethod.POST && exchange.getMethod() != HttpMethod.GET) {
            exchange.status(405);
            return "GraphQL only supports GET/POST operations.";
        }
        ExecutionInput input = getQuery(exchange);
        if (input != null) {
            ExecutionResult result = graphQL.execute(input);
            if (result.getErrors().size() > 0) {
                exchange.status(400);
            }
            return abstractMapper.toAbstract(result).toJson().toString();
        }
        return "Query missing.";
    }

    private ExecutionInput getQuery(Exchange exchange) {
        ExecutionInput input = null;
        if (exchange.getMethod() == HttpMethod.GET) {
            String query = new QueryString(exchange.rawRequest().getQueryString()).get("query");
            if (query == null)
                exchange.status(400);
            input = ExecutionInput.newExecutionInput(query).context(exchange).build();
        } else if (exchange.getMethod() == HttpMethod.POST) {
            byte[] bytes = exchange.read();
            if (bytes.length == 0) {
                exchange.status(500);
            } else {
                String in = new String(bytes);
                in = in.replace("\\n", "").replace("\\r", "");
                ExecutionInput.Builder builder = new Gson().fromJson(in, ExecutionInput.Builder.class);
                input = builder.context(exchange).build();
            }
        }
        return input;
    }
}
