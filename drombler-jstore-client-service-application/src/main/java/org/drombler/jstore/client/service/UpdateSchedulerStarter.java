package org.drombler.jstore.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.incubator.http.HttpClient;
import org.drombler.commons.client.startup.main.BootServiceStarter;
import org.drombler.commons.client.startup.main.DromblerClientConfiguration;
import org.drombler.jstore.client.service.integration.JStoreClient;
import org.drombler.jstore.client.service.jre.oracle.windows.WindowsJRE8Installer2;
import org.drombler.jstore.client.service.model.json.InstalledApplications;
import org.drombler.jstore.protocol.json.ApplicationId;
import org.drombler.jstore.protocol.json.ApplicationVersionInfo;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UpdateSchedulerStarter implements BootServiceStarter {
    /**
     * The property name used to specify an URL to the configuration property file to be used for the created the framework instance.
     */
    public static final String INSTALLED_APPLICATIONS_JSON_PROP = "installedApplications.json.file";
    /**
     * The default name used for the configuration properties file.
     */
    public static final String INSTALLED_APPLICATIONS_JSON_FILE_NAME = "installedApplications.json";

    private final DromblerClientConfiguration configuration;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient httpClient;
    private JStoreClient jStoreClient;

    public UpdateSchedulerStarter(DromblerClientConfiguration configuration) {
        this.configuration = configuration;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public boolean init() throws Exception {
        Path installConfigInstalledApplicationsJsonPath = configuration.getInstallConfigDirPath().resolve(INSTALLED_APPLICATIONS_JSON_FILE_NAME);
        InstalledApplications installedApplications = objectMapper.readValue(installConfigInstalledApplicationsJsonPath.toFile(), InstalledApplications.class);
        CookieManager cookieManager = new CookieManager();
        this.httpClient = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .cookieManager(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.jStoreClient = new JStoreClient(httpClient, objectMapper);
        List<ApplicationId> applicationIdList = new ArrayList<>();
        ApplicationId dromblerJstoreClientServiceApplicationId = new ApplicationId();
        dromblerJstoreClientServiceApplicationId.setVendorId("drombler");
        dromblerJstoreClientServiceApplicationId.setVendorApplicationId("drombler-jstore-client-service");
        applicationIdList.add(dromblerJstoreClientServiceApplicationId);
        ApplicationId dromblerJstoreClientApplicationId = new ApplicationId();
        dromblerJstoreClientApplicationId.setVendorId("drombler");
        dromblerJstoreClientServiceApplicationId.setVendorApplicationId("drombler-jstore-client");

        List<ApplicationVersionInfo> applicationVersionInfos = jStoreClient.searchApplicationVersions(applicationIdList);

        System.out.println(applicationVersionInfos);
//
//        WindowsJRE8Installer windowsJRE8Installer = new WindowsJRE8Installer(httpClient);
//        windowsJRE8Installer.installJRE(Paths.get("target", "jstore", "jre"));

//        JBrowserDriver driver = new JBrowserDriver(Settings.builder()
//                .saveAttachments(true)
//                .logJavascript(true)
//                .timezone(Timezone.EUROPE_ZURICH)
//                .build());

//            System.out.println(driver.attachmentsDir());
//            Path path = driver.attachmentsDir().toPath();
        WindowsJRE8Installer2 windowsJRE8Installer2 = new WindowsJRE8Installer2(httpClient);
            Path installationDirPath = Paths.get("target", "jstore", "jre");
            Files.createDirectories(installationDirPath);

            windowsJRE8Installer2.installJRE(installationDirPath);

//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
//                stream.forEach(filePath -> {
//                    try {
//                        Files.move(filePath, installationDirPath);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }

        return true;
    }

    @Override
    public void startAndWait() throws ExecutionException, InterruptedException {
        Updater updater = new Updater();
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(updater, 0, 24, TimeUnit.HOURS);
        scheduledFuture.get();
    }

    @Override
    public void stop(){
        scheduledExecutorService.shutdown(); // TODO: shutdownNow ?
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String getName() {
        return "Update Scheduler Starter";
    }

    @Override
    public boolean isRunning() {
        return !scheduledExecutorService.isShutdown();
    }
}
