
package okbem.br31.server.util.httplog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    protected void dumpHttp(
        ContentCachingRequestWrapper request,
        ContentCachingResponseWrapper response,
        long startTime
    ) {
        if (!logger.isDebugEnabled())
            return;

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;

        String reqLine = String.format("%s %s",
            request.getMethod(),
            request.getRequestURI()
        );
        if (request.getQueryString() != null)
            reqLine = String.format("%s?%s", reqLine, request.getQueryString());

        byte[] rawReqBody = request.getContentAsByteArray();
        String reqBody;
        try {
            reqBody = new String(rawReqBody, request.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            reqBody = String.format("length=%d", rawReqBody.length);
        }

        int resCode = response.getStatus();

        byte[] rawResBody = response.getContentAsByteArray();
        String resBody;
        try {
            resBody = new String(rawResBody, response.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            resBody = String.format("length=%d", rawResBody.length);
        }

        logger.debug("In  ({} ms) - {} {}", elapsed, reqLine, reqBody);
        logger.debug("Out ({} ms) - {} {}", elapsed, resCode, resBody);
    }


    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper;
        if (request instanceof ContentCachingRequestWrapper)
            requestWrapper = (ContentCachingRequestWrapper)request;
        else
            requestWrapper = new ContentCachingRequestWrapper(request);

        ContentCachingResponseWrapper responseWrapper;
        if (response instanceof ContentCachingResponseWrapper)
            responseWrapper = (ContentCachingResponseWrapper)response;
        else
            responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.nanoTime();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            this.dumpHttp(requestWrapper, responseWrapper, startTime);

            responseWrapper.copyBodyToResponse();
        }
    }

}

