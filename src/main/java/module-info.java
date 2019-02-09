module net.bjoernpetersen.shutdownserver {
    requires kotlin.stdlib;
    requires kotlin.reflect;

    requires org.slf4j;
    requires kotlin.logging;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;

    requires cfg4k.core;
    requires cfg4k.yaml;

    requires ST4;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.module.kotlin;

    requires javax.inject;
    requires dagger;

    requires vertx.core;
    requires vertx.web;
    requires vertx.lang.kotlin;
}
