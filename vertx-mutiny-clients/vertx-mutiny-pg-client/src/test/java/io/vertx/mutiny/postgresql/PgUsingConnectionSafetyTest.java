package io.vertx.mutiny.postgresql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assume.assumeThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testcontainers.containers.PostgreSQLContainer;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.UsingConnectionSafetyTest;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class PgUsingConnectionSafetyTest extends UsingConnectionSafetyTest {

    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>();

    @BeforeClass
    public static void init() {
        assumeThat(System.getProperty("skipInContainerTests"), is(nullValue()));
        container.start();
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
    }

    Vertx vertx;
    private int maxSize;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();

        PgConnectOptions options = new PgConnectOptions()
                .setPort(container.getMappedPort(5432))
                .setHost(container.getContainerIpAddress())
                .setDatabase(container.getDatabaseName())
                .setUser(container.getUsername())
                .setPassword(container.getPassword());

        maxSize = 5;
        pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(maxSize));
    }

    @Override
    protected int getMaxPoolSize() {
        return maxSize;
    }

    @After
    public void tearDown() {
        pool.closeAndAwait();
        vertx.closeAndAwait();
    }
}
