package com.bloatit.framework.rest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Map.Entry;
import java.util.Set;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.FatalErrorException;
import com.bloatit.framework.rest.annotations.REST;
import com.bloatit.framework.rest.exception.RestException;
import com.bloatit.framework.utils.HttpParameter;
import com.bloatit.framework.utils.Parameters;
import com.bloatit.framework.webserver.masters.HttpResponse;
import com.bloatit.framework.webserver.masters.HttpResponse.StatusCode;
import com.bloatit.framework.xcgiserver.HttpHeader;
import com.bloatit.framework.xcgiserver.HttpPost;
import com.bloatit.framework.xcgiserver.XcgiProcessor;

/**
 * <p>
 * Rest server is an abstract class describing the functions of a server
 * handling RPC calls via ReST
 * </p>
 * <p>
 * <b>Note</b>: This server is exclusive. It will consume any request part of
 * his {@link #getResourcesDirectories()}. Therefore if you want to have 2
 * different rest servers consuming different rest requests, ensure they handle
 * different rest directories. Otherwise the first server will always assume he
 * is the one that should handle the request, and return an error when he
 * doesn't know how to handle it properly.
 * </p>
 */
public abstract class RestServer implements XcgiProcessor {
    private static final String UTF_8 = "UTF-8";

    public enum RequestMethod {
        GET, POST, PUT, DELETE;
    }

    /**
     * <p>
     * Constructs a new server in charge of handling rest requests
     * </p>
     */
    public RestServer() {
    }

    /**
     * <p>
     * Processes requests. If requests is a request to a <code>ReST</code>
     * resource, writes this resource to the <code>response</code>
     * </p>
     * 
     * @return <code>true</code> if the request was a ReST request,
     *         <code>false</code> otherwise
     * @throws
     */
    @Override
    public final boolean process(HttpHeader httpHeader, HttpPost post, HttpResponse response) throws IOException {
        String scriptName = "";
        if (httpHeader.getScriptName().startsWith("/") && httpHeader.getScriptName().length() > 1) {
            scriptName = URLDecoder.decode(httpHeader.getScriptName().substring(1), UTF_8);
        }

        boolean found = false;
        for (String directory : getResourcesDirectories()) {
            if (scriptName.equals(directory)) {
                found = true;
            }
        }
        if (!found) {
            return false;
        }

        RestHeader header = new RestHeader(httpHeader);

        String restResource = header.getResourceName();
        Log.rest().trace("Received a rest request for resource: " + restResource);

        RequestMethod requestMethod;
        String requestMethodString = httpHeader.getRequestMethod();
        if (requestMethodString == null) {
            Log.web().warn("Received a rest request with no method. Should be either GET, POST, PUT or DELETE");
            return true;
        }

        try {
            requestMethod = RequestMethod.valueOf(requestMethodString);
        } catch (IllegalArgumentException e) {
            Log.web().warn("Received a rest request with an invalid method: " + requestMethodString + ". Should be either GET, POST, PUT or DELETE");
            return true;
        }

        final Parameters parameters = header.getParameters();

        try {
            Object result = doProcess(restResource, requestMethod, parameters);
            RestResource rr = new RestResource(result, httpHeader.getRequestUri(), getJAXClasses());
            response.writeRestResource(rr);
        } catch (RestException e) {
            response.writeRestError(e);

        }
        return true;
    }

    /**
     * <p>
     * Indicates the list of <i>directories</i> that can contain a
     * <code>ReST</code> resource
     * </p>
     * <p>
     * Example: if structure is
     * <code>http://example.com/rest/{@code <resource>}</code> or
     * <code>http://example.com/ws/{@code <resource>}</code> then this method
     * will return a set containing {"rest", "ws"}.
     * </p>
     * 
     * @return a list of directories that can contain ReST resources
     */
    protected abstract Set<String> getResourcesDirectories();

    /**
     * <p>
     * Determines the class to used for a given resource
     * </p>
     * 
     * @param forResource the resource
     * @return the <code>Class</code> that holds results for
     *         <code>forResource</code> request
     */
    protected abstract Class<?> getClass(String forResource);

    /**
     * @return the list of classes that can be marhalled using JAX
     */
    protected abstract Class<?>[] getJAXClasses();

    /**
     * <p>
     * Indicates whether the <code>string description</code> of the resource is
     * a valid descriptor.
     * </p>
     * 
     * @param resource the string descriptor of the resource we want to access
     * @return <code>true</code> if the resource descriptor is valid,
     *         <code>false</code> otherwise
     */
    protected abstract boolean isValidResource(String resource);

    /**
     * <p>
     * Process a rest request and return the <code>Object</code> resulting of
     * the invocation
     * </p>
     * 
     * @param restResource the resource (eg: members/1/messages)
     * @param requestMethod the http request method (
     *            <code>GET, POST, PUT, DELETE</code>)
     * @param parameters the parameters (a multimap of elements that are after
     *            the <code>?</code>)
     * @return the <code>Object</code> returned by the execution of the
     *         <code>RPC/<code>
     * @throws RestException whenever an error occurs. The detail of the
     *             exception is described in the <code>Status</code> field.
     * @see RestException
     * @see RestException#getStatus()
     */
    private final Object doProcess(String restResource, RequestMethod requestMethod, Parameters parameters) throws RestException {
        String[] pathInfo = restResource.split("/");
        if (pathInfo.length == 0) {
            throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Please specify the resource you want to access");
        }
        if (!isValidResource(pathInfo[0])) {
            throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Request resource [/" + pathInfo[0] + "] does not exist");
        }

        Object result;
        if (pathInfo.length == 1) {
            result = invokeStatic(requestMethod, pathInfo[0], parameters);
            return result;

        }

        int id;
        try {
            id = Integer.valueOf(pathInfo[1]);
        } catch (NumberFormatException e) {
            throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Expected " + pathInfo[0] + "/" + "[id]/" + pathInfo[1] + "; received "
                    + restResource);
        }
        result = invokeStatic(requestMethod, pathInfo[0], id);

        for (int i = 2; i < pathInfo.length; i++) {
            String pathString = pathInfo[i];
            if (i < pathInfo.length - 1) {
                result = invokeMethod(requestMethod, result, pathString);

                if (result == null) {
                    // ERROR Case
                    String request = "";
                    String ignored = "";
                    int j = 0;
                    for (String s : pathInfo) {
                        if (j <= i) {
                            request += s + '/';
                        } else {
                            ignored += s + '/';
                        }
                        j++;
                    }
                    throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "No result to request: [" + requestMethod + " " + request
                            + "]. Ignored end of request: [" + ignored + "]");
                }

            } else {
                result = invokeMethod(requestMethod, result, pathString, parameters);
            }
        }
        return result;

    }

    /**
     * <p>
     * Private method used to invoke a static method on a
     * <code>RestResource</code>
     * </p>
     * <p>
     * This method has to be used when the ReST request doesn't specify an id
     * </p>
     * 
     * @param requestMethod the http request method (
     *            <code>GET, POST, PUT, DELETE</code>)
     * @param path the path of the RestResource
     * @param params the list of parameters
     * @return the object returned by the invocation of the <code>path</code>
     *         method
     * @throws RestException whenever an error occurs
     */
    private Object invokeStatic(RequestMethod requestMethod, String path, Parameters params) throws RestException {
        Class<?> clazz = getClass(path);
        return invoke(requestMethod, clazz, null, path, params);
    }

    /**
     * <p>
     * Private method used to invoke a static method on a
     * <code>RestResource</code>
     * </p>
     * <p>
     * This method has to be used on the <b>root</b> of ReST request that
     * specifies an id. <br />
     * Example of usage:
     * 
     * <pre>
     * Request: GET http://elveos.org/rest/members/1/messages
     * Use: invokeStatic(RequestMethod.GET, members, 1);
     * Return: Members.getById(1);
     * </pre>
     * 
     * </p>
     * 
     * @param requestMethod the http request method (
     *            <code>GET, POST, PUT, DELETE</code>)
     * @param path the <b>root</b> of the ReST request
     * @param id the id asked
     * @return the object resulting of the <code>path</code> method call
     * @throws RestException whenever an error occurs
     */
    private Object invokeStatic(RequestMethod requestMethod, String path, int id) throws RestException {
        Class<?> clazz = getClass(path);
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(REST.class) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == int.class) {
                REST annotation = m.getAnnotation(REST.class);
                if (annotation.name().equalsIgnoreCase(path) && annotation.method().equals(requestMethod)) {
                    try {
                        Log.rest().trace("Invoking " + m);
                        return m.invoke(null, id);
                    } catch (Exception e) {
                        Log.rest().fatal("Encountered an error when invoking [" + path + "/" + id + "]", e);
                        throw new RestException(StatusCode.ERROR_500_INTERNAL_SERVER_ERROR, "Error when invoking [" + path + "/" + id + "]", e);
                    }
                }
            }
        }
        throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Request: [" + path + "/" + id + "] doest not exist");
    }

    /**
     * <p>
     * Invoke the method specified by <code>path</code> on the
     * <code>lookup</code> object
     * </p>
     * 
     * @param requestMethod the http request method (
     *            <code>GET, POST, PUT, DELETE</code>)
     * @param lookup the object on which the method will be used
     * @param path the method to call
     * @return the <code>Object</code> returned by the call of <code>path</code>
     *         on <code>lookup</code>
     * @throws RestException whenever an error occurs
     */
    private Object invokeMethod(RequestMethod requestMethod, Object lookup, String path) throws RestException {
        Class<?> clazz = lookup.getClass();
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(REST.class) && m.getParameterTypes().length == 0) {
                REST annotation = m.getAnnotation(REST.class);
                if (annotation.name().equalsIgnoreCase(path) && annotation.method().equals(requestMethod)) {
                    try {
                        Log.rest().trace("Invoking " + m);
                        return m.invoke(lookup, (Object[]) null);
                    } catch (Exception e) {
                        Log.rest().fatal("Encountered an error when invoking [" + path + "/]", e);
                        throw new RestException(StatusCode.ERROR_500_INTERNAL_SERVER_ERROR, "Error when invoking [" + path + "/]", e);
                    }
                }
            }
        }
        throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Request: [" + requestMethod + " " + path + " on " + lookup.toString()
                + "] does not exist");
    }

    private Object invokeMethod(RequestMethod requestMethod, Object result, String path, Parameters params) throws RestException {
        return invoke(requestMethod, result.getClass(), result, path, params);
    }

    /**
     * <p>
     * Factorization of code
     * </p>
     * 
     * @see #invokeStatic(RequestMethod, String, Parameters)
     * @see #invokeMethod(RequestMethod, Object, String, Parameters)
     */
    private Object invoke(RequestMethod requestMethod, Class<?> clazz, Object lookup, String path, Parameters params) throws RestException {
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(REST.class)) {
                REST annotation = m.getAnnotation(REST.class);
                if (annotation.name().equals(path) && annotation.method().equals(requestMethod) && m.getParameterTypes().length == params.size()
                        && annotation.params().length == params.size()) {
                    boolean correct = true;
                    String[] paramsInOrder = new String[annotation.params().length];
                    int i = 0;
                    for (String param : annotation.params()) {
                        if (!params.containsKey(param)) {
                            correct = false;
                        } else {
                            paramsInOrder[i++] = params.look(param).getSimpleValue();
                        }
                    }
                    if (correct) {
                        try {
                            Log.rest().trace("Invoking " + m);
                            return (m.invoke(lookup, (Object[]) paramsInOrder));
                        } catch (Exception e) {
                            Log.rest().fatal("Encountered an error when invoking [" + path + "/]", e);
                            throw new RestException(StatusCode.ERROR_500_INTERNAL_SERVER_ERROR, "Error when invoking [" + path + "/]", e);
                        }
                    }
                }
            }
        }
        if (lookup != null) {
            Log.rest().warn("Method [" + requestMethod + " " + path + generateParametersString(params) + "] on " + lookup.toString()
                    + " does not exist");
            throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Request: [" + requestMethod + " " + path + generateParametersString(params)
                    + "] on " + lookup.toString() + " does not exist");
        } else {
            Log.rest().warn("Method [" + requestMethod + " " + path + generateParametersString(params) + "] does not exist");
            throw new RestException(StatusCode.ERROR_404_NOT_FOUND, "Request: [" + requestMethod + " " + path + generateParametersString(params)
                    + "] does not exist");
        }
    }

    /**
     * <p>
     * Creates a string that represents a map as a list of parameters
     * </p>
     */
    private String generateParametersString(Parameters params) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Entry<String, HttpParameter> param : params.entrySet()) {
            sb.append("String " + param.getKey() + ", ");
        }
        return sb.substring(0, sb.length() - 2) + ")";
    }
}
