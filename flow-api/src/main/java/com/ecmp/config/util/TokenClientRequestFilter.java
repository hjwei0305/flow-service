package com.ecmp.config.util;

import com.ecmp.context.ContextUtil;
import com.ecmp.util.IdGenerator;
import com.ecmp.vo.SessionUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * API token检查的客户端过滤器
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2017/10/15 13:10
 */
@Priority(value = 10)
public class TokenClientRequestFilter implements ClientRequestFilter/*, ClientResponseFilter*/ {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenClientRequestFilter.class);

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
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        SessionUser sessionUser = ContextUtil.getSessionUser();
        //从运行环境上下文中获取当前session id
        String token = sessionUser.getAccessToken();
        LOGGER.debug("request AccessToken {}", token);
        if (StringUtils.isNotBlank(token)) {
            headers.putSingle(ContextUtil.AUTHORIZATION_KEY, token);
        }

        String traceId = MDC.get("traceId");
        if (StringUtils.isBlank(traceId)) {
            traceId = IdGenerator.uuid2();
        }
        LOGGER.debug("request traceId {}", traceId);
        headers.putSingle("traceId", traceId);

        LOGGER.debug("request uri {}", requestContext.getUri());

        headers.putSingle("appId", ContextUtil.getAppId());

        String ua = MDC.get("UserAgent");
        if (StringUtils.isBlank(ua)) {
            ua = requestContext.getHeaderString("User-Agent");
        }
        String clientIP = MDC.get("clientIP");
        if (StringUtils.isBlank(clientIP)) {
            clientIP = requestContext.getHeaderString("clientIP");
        }
        if (StringUtils.isNotBlank(ua)) {
            headers.putSingle("UserAgent", ua);
        }
        if (StringUtils.isNotBlank(clientIP)) {
            headers.putSingle("clientIP", clientIP);
        }
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
//    @Override
//    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
//
//    }
}
