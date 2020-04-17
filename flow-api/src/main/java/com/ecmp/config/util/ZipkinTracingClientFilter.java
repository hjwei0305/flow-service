package com.ecmp.config.util;

import brave.Span;
import brave.Tracer;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.RuntimeType.CLIENT;

/**
 * <strong>实现功能:</strong>
 * <p>jax-rs客户端过滤器</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-04-27 8:31
 */
@Provider
@ConstrainedTo(CLIENT)
public class ZipkinTracingClientFilter implements ClientRequestFilter, ClientResponseFilter {
    static final Propagation.Setter<MultivaluedMap, String> SETTER = (carrier, key, value) -> carrier.putSingle(key, value);
    final Tracer tracer;
    final HttpClientHandler<ClientRequestContext, ClientResponseContext> handler;
    final TraceContext.Injector<MultivaluedMap> injector;

    /**
     * spring bean id
     */
    public static final String BEAN_ID = "zipkinTracingClientFilter";

    /**
     * 构造函数
     *
     * @param httpTracing 跟踪
     */
    public ZipkinTracingClientFilter(HttpTracing httpTracing) {
        if (httpTracing == null) {
            throw new NullPointerException("HttpTracing == null");
        }
        tracer = httpTracing.tracing().tracer();
        handler = HttpClientHandler.create(httpTracing, new HttpAdapter());
        injector = httpTracing.tracing().propagation().injector(SETTER);
    }

    /**
     * Filter method called before a request has been dispatched to a client
     * transport layer.
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     *
     * @param requestContext request context.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Span span = handler.handleSend(injector, requestContext.getHeaders(), requestContext);
        requestContext.setProperty(Tracer.SpanInScope.class.getName(), tracer.withSpanInScope(span));
    }

    /**
     * Filter method called after a response has been provided for a request
     * (either by a {@link ClientRequestFilter request filter} or when the
     * HTTP invocation returns.
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     *
     * @param requestContext  request context.
     * @param responseContext response context.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Span span = tracer.currentSpan();
        if (span == null) {
            return;
        }
        ((Tracer.SpanInScope) requestContext.getProperty(Tracer.SpanInScope.class.getName())).close();
        handler.handleReceive(responseContext, null, span);
    }

    static final class HttpAdapter
            extends brave.http.HttpClientAdapter<ClientRequestContext, ClientResponseContext> {

        @Override
        public String method(ClientRequestContext request) {
            return request.getMethod();
        }

        @Override
        public String path(ClientRequestContext request) {
            return request.getUri().getPath();
        }

        @Override
        public String url(ClientRequestContext request) {
            return request.getUri().toString();
        }

        @Override
        public String requestHeader(ClientRequestContext request, String name) {
            return request.getHeaderString(name);
        }

        @Override
        public Integer statusCode(ClientResponseContext response) {
            return response.getStatus();
        }
    }
}
