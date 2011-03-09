package com.bloatit.framework.webserver.masters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.bloatit.common.Log;
import com.bloatit.framework.rest.RestResource;
import com.bloatit.framework.rest.exception.RestException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.components.writers.IndentedHtmlStream;
import com.bloatit.web.HtmlTools;

public final class HttpResponse {
    /**
     * Describes the error level
     */
    public enum StatusCode {
        OK_200("200"), //
        ERROR_301_MOVED_PERMANENTLY("301"), //
        ERROR_302_FOUND("302"), //
        ERROR_401_UNAUTHORIZED("401"), //
        ERROR_403_FORBIDDEN("403"), //
        ERROR_404_NOT_FOUND("404"), //
        ERROR_405_METHOD_NOT_ALLOWED("405"), //
        ERROR_500_INTERNAL_SERVER_ERROR("500"), //
        ERROR_501_NOT_IMPLEMENTED("501"), //
        ERROR_503_SERVICE_UNAVAILABLE("503");//

        private String code;

        StatusCode(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private final OutputStream output;
    private final IndentedHtmlStream htmlText;
    private StatusCode status = StatusCode.OK_200;

    // TODO: use write line in all file
    public HttpResponse(final OutputStream output) {
        this.output = output;
        this.htmlText = new IndentedHtmlStream(output);
    }

    /**
     * Sets the status (OK, ERROR+type) of the HttpResponse.
     * <p>
     * Default value of the status is OK_200, hence there is no need to call
     * this method when everything is OK. When an error occurs, call this method
     * to set the error status to its new value.
     * </p>
     *
     * @param status the new status
     */
    public void setStatus(final StatusCode status) {
        this.status = status;
    }

    public void writeException(final Exception e) {
        final StringBuilder display = new StringBuilder();
        display.append("Content-type: text/plain\r\n\r\n");
        display.append(e.toString());
        display.append(" :\n");

        for (final StackTraceElement s : e.getStackTrace()) {
            display.append('\t');
            display.append(s);
            display.append('\n');
        }

        try {
            output.write(display.toString().getBytes());
        } catch (final IOException e1) {
            Log.framework().fatal("Cannot send exception through the SCGI soket.", e1);
        }
    }

    public void writeRedirect(final String url) throws IOException {
        writeCookies();
        output.write("Location: ".getBytes());
        output.write(url.getBytes());
        output.write("\r\n".getBytes());
        closeHeaders();
    }

    public void writePage(final Page page) throws IOException {
        writeCookies();
        output.write("Content-Type: text/html\r\n".getBytes());

        closeHeaders();

        page.write(htmlText);
    }

    public void writeResource(final String path, final long size, final String fileName) throws IOException {
        // writeLine("Vary: Accept-Encoding");
        // writeLine("Content-Encoding: gzip");
        // writeLine("ETag: \"3164227128\"");
        // Content-Type: image/png
        // writeLine("Content-Type: text/css");
        // writeLine("Content-Type: image/png");
        // writeLine("Accept-Ranges: bytes");
        // writeLine("Last-Modified: Wed, 16 Feb 2011 22:29:37 GMT");

        // TODO: add all meta info as date, etc...

        writeLine("Content-Disposition: inline; filename=" + fileName);
        writeLine("X-Sendfile2: " + path + " 0-" + (size - 1));

        closeHeaders();
    }

    /**
     * <p>
     * Writes a rest resource into an HttpResponse
     * </p>
     * <p>
     * Will generate either a {@code <rest result="ok">} or a
     * {@code <rest result="fail">} depending on the value of its internal
     * <code>status</code> (which can be set using
     * {@link #setStatus(StatusCode)}. <br />
     * Any value other than OK_200 will result in a fail.
     * </p>
     * <p>
     * Before writing make sure the statusCode has been correctly set. <br />
     * The default value of the status code is OK_200 hence when everything goes
     * well, there is no need to change it. Whenever generating the restResource
     * goes haywire, think to set a correct status using the method
     * {@link #setStatus(StatusCode)}
     * </p>
     *
     * @param resource the resource to write
     * @throws IOException whenever an IO error occurs on the underlying stream
     * @see #setStatus(StatusCode)
     */
    public void writeRestResource(final RestResource resource) throws IOException {
        try {
            final String resourceXml = resource.getXmlString();
            output.write("Content-Type: text/xml\r\n".getBytes());
            closeHeaders();
            htmlText.writeLine("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>");
            htmlText.writeLine("<rest result=\"ok\" request=\"" + HtmlTools.escape(resource.getRequest()) + "\" >");
            htmlText.indent();
            htmlText.writeLine(resourceXml);
            htmlText.unindent();
            htmlText.writeLine("</rest>");
        } catch (final Exception e) {
            Log.rest().fatal("Exception while marshalling RestResource " + resource.getUnderlying(), e);
            writeRestError(StatusCode.ERROR_500_INTERNAL_SERVER_ERROR, "Error while marhsalling the Object", e);
        }
    }

    /**
     * Writes a rest error based on the <code>exception</code>
     *
     * @param exception the exception describing the error
     * @throws IOException when an IO error occurs
     */
    public void writeRestError(final RestException exception) throws IOException {
        writeRestError(exception.getStatus(), exception.getMessage(), exception);
    }

    private void writeLine(final String string) throws IOException {
        final String line = string + "\r\n";
        output.write(line.getBytes());
    }

    /**
     * <p>
     * Writes a rest error
     * </p>
     *
     * @see {@link #writeRestError(RestException)}
     */
    private void writeRestError(final StatusCode status, final String message, final Exception e) throws IOException {
        output.write("Content-Type: text/xml\r\n".getBytes());
        closeHeaders();
        htmlText.writeLine("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>");
        htmlText.indent();

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        final String stackTrace = sw.toString();

        if (stackTrace != null && !stackTrace.isEmpty()) {
            htmlText.writeLine("<error code=\"" + status.toString() + "\" reason=\"" + message + "\" >");
            htmlText.writeLine(HtmlTools.escape(stackTrace));
            htmlText.writeLine("</error>");
        } else {
            htmlText.writeLine("<error reason=\"" + HtmlTools.escape(status.toString()) + "\" />");
        }

        htmlText.unindent();
    }

    private void closeHeaders() throws IOException {
        output.write("\r\n".getBytes());
    }

    private void writeCookies() throws IOException {
        output.write("Set-Cookie: session_key=".getBytes());
        output.write(Context.getSession().getKey().toString().getBytes());
        output.write("; path=/; Max-Age=1296000; Version=1 \r\n".getBytes());
    }
}
