package org.drombler.jstore.client.agent.startup.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.drombler.jstore.client.agent.startup.commons.http.StandardHttpHeaderFieldNames;
import org.drombler.jstore.client.agent.startup.commons.http.StandardMimeTypes;
import org.drombler.jstore.client.agent.startup.integration.impl.JacksonRequestBodyPublisher;
import org.drombler.jstore.protocol.json.*;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class JStoreClient {
    private static final String V1_PATH_SEGMENT = "v1";
    public static final String MANAGED_COMPONENTS_PATH_SEGMENT = "managed-components";
    public static final String MANAGED_COMPONENTS_V1_PATH = V1_PATH_SEGMENT + "/" + MANAGED_COMPONENTS_PATH_SEGMENT;
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI endpoint;

    public JStoreClient(HttpClient httpClient, ObjectMapper objectMapper, URI endpoint) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
    }

    public List<UpgradableApplication> searchApplicationVersions(List<SelectedApplication> selectedApplications, SystemInfo systemInfo) throws JStoreClientException {
        ApplicationVersionSearchRequest requestPayload = new ApplicationVersionSearchRequest();
        requestPayload.setSelectedApplications(selectedApplications);
        requestPayload.setSystemInfo(systemInfo);

        String path = MANAGED_COMPONENTS_V1_PATH + "/application-version-search";
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://jstore.drombler.com"))
                .uri(endpoint.resolve(path))
//                    .timeout(Duration.of(10, ChronoUnit.MINUTES))
                .setHeader(StandardHttpHeaderFieldNames.ACCEPT, StandardMimeTypes.APPLICATION_JSON)
                .setHeader(StandardHttpHeaderFieldNames.CONTENT_TYPE, StandardMimeTypes.APPLICATION_JSON) // TODO: required?
                .POST(createJacksonRequestBodyProcessor(requestPayload))
                .build();

        HttpResponse<String> response = send(request);


        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            ApplicationVersionSearchResponse applicationVersionSearchResponse = readResponsePayload(response, ApplicationVersionSearchResponse.class);
            return applicationVersionSearchResponse.getUpgradableApplications();
        } else {
            throw new JStoreClientException(request.method() + " " + path + " call failed! Status code: " + response.statusCode(), response.statusCode());
        }
    }

    public List<UpgradableJRE> startJreVersionSearch(List<SelectedJRE> selectedJREs, SystemInfo systemInfo) throws JStoreClientException {
        JreVersionSearchRequest requestPayload = new JreVersionSearchRequest();
        requestPayload.setSelectedJREs(selectedJREs);
        requestPayload.setSystemInfo(systemInfo);

        String path = MANAGED_COMPONENTS_V1_PATH + "/jre-version-search";
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://jstore.drombler.com"))
                .uri(endpoint.resolve(path))
//                    .timeout(Duration.of(10, ChronoUnit.MINUTES))
                .setHeader(StandardHttpHeaderFieldNames.ACCEPT, StandardMimeTypes.APPLICATION_JSON)
                .setHeader(StandardHttpHeaderFieldNames.CONTENT_TYPE, StandardMimeTypes.APPLICATION_JSON) // TODO: required?
                .POST(createJacksonRequestBodyProcessor(requestPayload))
                .build();

        HttpResponse<String> response = send(request);


        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JreVersionSearchResponse applicationVersionSearchResponse = readResponsePayload(response, JreVersionSearchResponse.class);
            return applicationVersionSearchResponse.getUpgradableJREs();
        } else {
            throw new JStoreClientException(request.method() + " " + path + " call failed! Status code: " + response.statusCode(), response.statusCode());
        }
    }

    public List<UpgradableJRE> getJRE(SelectedJRE selectedJRE, SystemInfo systemInfo) throws JStoreClientException {
        String path = createGetJREPath(selectedJRE, systemInfo);

        HttpCookie oraclelicense = new HttpCookie("oraclelicense", "accept-securebackup-cookie");
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://jstore.drombler.com"))
                .uri(endpoint.resolve(path))
//                    .timeout(Duration.of(10, ChronoUnit.MINUTES))
//                .setHeader(StandardHttpHeaderFieldNames.ACCEPT, StandardMimeTypes.BINARY)
                .setHeader("Cookie", oraclelicense.getName() + "=" + oraclelicense.getValue())
                .GET()
                .build();

        HttpResponse<String> response = send(request);


        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JreVersionSearchResponse applicationVersionSearchResponse = readResponsePayload(response, JreVersionSearchResponse.class);
            return applicationVersionSearchResponse.getUpgradableJREs();
        } else {
            throw new JStoreClientException(request.method() + " " + path + " call failed! Status code: " + response.statusCode(), response.statusCode());
        }
    }

    private String createGetJREPath(SelectedJRE selectedJRE, SystemInfo systemInfo) {
        JreInfo jreInfo = selectedJRE.getJreInfo();

        StringBuilder pathSB = new StringBuilder(MANAGED_COMPONENTS_V1_PATH)
                .append("/jres/")
                .append(URLEncoder.encode(jreInfo.getJreVendorId(), UTF8_CHARSET))
                .append("/")
                .append(URLEncoder.encode(jreInfo.getJavaSpecificationVersion(), UTF8_CHARSET))
                .append("/")
                .append(URLEncoder.encode(systemInfo.getOsName(), UTF8_CHARSET))
                .append("/")
                .append(URLEncoder.encode(systemInfo.getOsArch(), UTF8_CHARSET));
        if ((selectedJRE.getInstalledImplementationVersion() != null && !selectedJRE.getInstalledImplementationVersion().equals(""))
                || (systemInfo.getHeadless() != null && systemInfo.getHeadless())) {
            pathSB.append("?");
            boolean firstQueryParam = true;
            if (selectedJRE.getInstalledImplementationVersion() != null && !selectedJRE.getInstalledImplementationVersion().equals("")) {
                pathSB.append("installedImplementationVersion=").append(URLEncoder.encode(selectedJRE.getInstalledImplementationVersion(), UTF8_CHARSET));
                firstQueryParam = false;
            }
            if (systemInfo.getHeadless() != null && systemInfo.getHeadless()) {
                if (!firstQueryParam) {
                    pathSB.append("&");
                }
                pathSB.append("headless=true");
            }
        }

        return pathSB.toString();
    }

    private <T> T readResponsePayload(HttpResponse<String> response, Class<T> valueType) throws JStoreClientException {
        try {
            return objectMapper.readValue(response.body(), valueType);
        } catch (IOException e) {
            throw new JStoreClientException(e.getMessage(), response.statusCode(), e);
        }
    }

    private HttpResponse<String> send(HttpRequest request) throws JStoreClientException {
        try {
            //       HttpResponse<ApplicationVersionSearchResponse> response = httpClient.send(request, new JacksonBodyHandler<>(objectMapper, ApplicationVersionSearchResponse.class));
            return httpClient.send(request, HttpResponse.BodyHandler.asString());
        } catch (IOException | InterruptedException e) {
            throw new JStoreClientException(e.getMessage(), e);
        }
    }

    private JacksonRequestBodyPublisher createJacksonRequestBodyProcessor(Object requestPayload) throws JStoreClientException {
        try {
            return new JacksonRequestBodyPublisher(objectMapper, requestPayload);
        } catch (JsonProcessingException e) {
            throw new JStoreClientException(e.getMessage(), e);
        }
    }
}
