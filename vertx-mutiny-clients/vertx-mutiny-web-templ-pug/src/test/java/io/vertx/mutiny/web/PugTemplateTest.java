package io.vertx.mutiny.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.templ.pug.PugTemplateEngine;

public class PugTemplateTest {

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.closeAndAwait();
    }

    @Test
    public void testTemplate() {
        PugTemplateEngine engine = PugTemplateEngine.create(vertx);
        Buffer buffer = engine.renderAndAwait(new JsonObject().put("foo", "hello"), "template.pug");
        assertThat(buffer.toString()).contains("<p>hello</p>");
    }

}
