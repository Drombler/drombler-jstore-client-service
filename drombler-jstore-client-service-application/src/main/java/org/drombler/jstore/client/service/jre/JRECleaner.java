package org.drombler.jstore.client.service.jre;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Uninstalls all unused JREs.
 */
public class JRECleaner implements Callable<JRECleanInfo> {
    public JRECleaner(Set<String> unusedJREVersions) {

    }

    @Override
    public JRECleanInfo call() throws Exception {
        return null;
    }
}
