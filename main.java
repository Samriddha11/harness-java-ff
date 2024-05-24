package io.harness;

import io.harness.cf.client.api.CfClient;
import io.harness.cf.client.dto.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    // API Key - set this as an env variable
    private static String apiKey = System.getenv("HARNESS_API_KEY");
    //private static String apiKey = "API-KEY-HERE";

    // Flag Identifier - Replace special_feature flag by your FF
    private static String flagIdentifier = "special_feature";

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        System.out.println("Harness SDK Getting Started");


        try {
            // Create a Feature Flag Client
            CfClient cfClient = new CfClient(apiKey);
            cfClient.waitForInitialization();

            // Create targets
            List<Target> targets = new ArrayList<>();
            targets.add(Target.builder().identifier("usUsers").name("usUsers").attribute("country", "US").build());
            targets.add(Target.builder().identifier("BetaUsers").name("BetaUsers").attribute("version", "0.0.1").build());
            targets.add(Target.builder().identifier("iosUsers").name("iosUsers").attribute("device", "ios").build());
            targets.add(Target.builder().identifier("ukUsers").name("ukUsers").attribute("country", "UK").build());
            targets.add(Target.builder().identifier("gaUsers").name("gaUsers").attribute("version", "0.0.2").build());
            targets.add(Target.builder().identifier("CustomUser").name("CustomUser").attribute("country", "UK").attribute("version", "0.0.1").build());

            // Schedule a single task to report flag status for all targets
            scheduler.scheduleAtFixedRate(
                    () -> {
                        for (Target target : targets) {
                            boolean result = cfClient.boolVariation(flagIdentifier, target, false);
                            StringBuilder attributes = new StringBuilder();
                            for (Map.Entry<String, Object> entry : target.getAttributes().entrySet()) {
                                attributes.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(", ");
                            }
                            // Remove the last comma and space
                            if (attributes.length() > 0) {
                                attributes.setLength(attributes.length() - 2);
                            }
                            System.out.println("Boolean variation for " + target.getIdentifier() + " (" + attributes + ") is " + result);
                        }
                    },
                    0,
                    10,
                    TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the SDK on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                CfClient.getInstance().close();
                scheduler.shutdown();
            }));
        }
    }
}
