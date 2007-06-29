HELLO_WORLD_DEMO=$CXF_HOME/samples/hello_world
$JAVA_HOME/bin/java -classpath $CXF_HOME/lib/cxf-manifest-incubator.jar:$CXF_HOME/lib/js-1.6R5.jar:$HELLO_WORLD_DEMO/build/classes:$CLASSPATH -Djava.util.logging.config.file=logger.properties org.mozilla.javascript.tools.shell.Main -f src/helloworld.js
