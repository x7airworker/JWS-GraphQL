package de.x7airworker.jwsgraphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractMapper;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.abstractdata.util.QueryString;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.httpserver.helper.HttpMethod;
import org.javawebstack.injector.Injector;

import javax.servlet.http.HttpServletRequest;

public class HttpGraphQLHandler implements RequestHandler {
    private final AbstractMapper abstractMapper = new AbstractMapper();
    private final GraphQL graphQL;

    protected HttpGraphQLHandler (Injector injector) {
        graphQL = injector.getInstance(GraphQL.class);
    }

    @Override
    public Object handle(Exchange exchange) {
        if (exchange.getMethod() != HttpMethod.POST && exchange.getMethod() != HttpMethod.GET) {
            exchange.status(405);
            return "GraphQL only supports GET/POST operations.";
        }
        HttpServletRequest raw = exchange.rawRequest();
        String query = getQuery(exchange);
        if (query != null) {
            ExecutionResult result = graphQL.execute(ExecutionInput.newExecutionInput()
                    .query(query)
                    .context(raw)
                    .build());
            if (result.getErrors().size() > 0) {
                exchange.status(400);
                return abstractMapper.toAbstract(result.getErrors()).toJson();
            }
            return abstractMapper.toAbstract(result.getData()).toJson();
        }
        return "Query missing.";
    }

    private String getQuery(Exchange exchange) {
        String query = null;
        if (exchange.getMethod() == HttpMethod.GET) {
            query = new QueryString(exchange.rawRequest().getQueryString()).get("query");
            if (query == null)
                exchange.status(400);
        } else if (exchange.getMethod() == HttpMethod.POST) {
            AbstractElement element = exchange.body(AbstractObject.class).get("query");
            if (element != null)
                query = element.string();
            if (query == null)
                exchange.status(500);
        }
        return query;
    }
}
