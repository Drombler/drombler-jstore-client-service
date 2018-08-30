package org.drombler.jstore.client.agent.startup.download;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DownloadManager {
    private final Path downloadTempDirPath;
    private final HttpClient httpClient;

    public DownloadManager(Path downloadTempDirPath, HttpClient httpClient) {
        this.downloadTempDirPath = downloadTempDirPath;
        this.httpClient = httpClient;
    }

    public <K> DownloadTask<K> downloadFile(DownloadId<K> id, HttpRequest request) {
//        CompletableFuture<HttpResponse<Path>> response = httpClient.sendAsync(request, HttpResponse.BodyHandler.asFileDownload(downloadTempDirPath));
        CompletableFuture<HttpResponse<Path>> response = null;
//        HttpResponse<Path> response = null;
//        try {
        response = httpClient.sendAsync(request, HttpResponse.BodyHandler.asFile(downloadTempDirPath.resolve("jre")));
        return new DownloadTask<K>(id, response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    // TODO: check SecurityManager. See HttpResponse.BodyHandler.asFileDownload
//    public  <K> HttpResponse.BodyHandler<DownloadTask<K>> asDownloadTask(DownloadId<K> id){
//        return new DownloadTaskBodyHandler<>(id, downloadTempDirPath);
//
//    }

}
