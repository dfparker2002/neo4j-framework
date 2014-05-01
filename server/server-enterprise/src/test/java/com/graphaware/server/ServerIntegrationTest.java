package com.graphaware.server;

import com.graphaware.test.integration.IntegrationTest;
import com.graphaware.test.util.TestUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;

/**
 * Integration test for custom server that wires Spring components.
 */
public class ServerIntegrationTest extends IntegrationTest {

    @Test
    public void componentsShouldBeWired() {
        TestUtils.get("http://localhost:7474/graphaware/greeting", HttpStatus.SC_OK);
    }
}
