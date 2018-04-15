package org.drombler.jstore.client.service.jre.windows;

//import jdk.incubator.http.HttpClient;
//import jdk.incubator.http.HttpRequest;
//import jdk.incubator.http.HttpResponse;
import org.drombler.jstore.client.service.jre.JREInstaller;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author puce
 */
public abstract class WindowsJRE8Installer implements JREInstaller {

//    private final HttpClient httpClient;
//
//    public WindowsJRE8Installer(HttpClient httpClient){
//        this.httpClient = httpClient;
//    }
//
//    @Override
//    public void installJRE(Path installationDirPath) {
//        try {
//
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(new URI("http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html"))
//                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
//                    .GET()
//                    .build();
//            CompletableFuture<HttpResponse<String>> completableFuture = httpClient.sendAsync(request, HttpResponse.BodyHandler.asString());
//            HttpResponse<String> stringHttpResponse = completableFuture.get(10, TimeUnit.SECONDS);
//            System.out.println(stringHttpResponse.statusCode());
//            String body = stringHttpResponse.body();
//            System.out.printf(body);
//        } catch (URISyntaxException ex) {
//            Logger.getLogger(WindowsJRE8Installer.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void uninstallJRE(Path installationDirPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
