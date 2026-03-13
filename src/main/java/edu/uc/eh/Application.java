package edu.uc.eh;


import org.apache.catalina.Context;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.apache.catalina.startup.Tomcat;
//import edu.uc.eh.uniprot.UniprotRepositoryH2;

import javax.servlet.ServletException;
import java.io.IOException;


@SpringBootApplication
public class Application {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    @Autowired
//    UniprotRepositoryH2 repository;
    private int maxUploadSizeInMb = 10 * 1024 * 1024; // 10 MB

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        logger.info("Student id 10001 -> {}", repository.findById(10001L));
//
//        logger.info("Inserting -> {}", repository.insert(new Uniprot(10010L, "John", "A1234657")));
//
//        logger.info("Inserting -> {}", repository.insert(new Uniprot(10011L, "John2", "A1234658")));
//
//        logger.info("Update 10003 -> {}", repository.update(new Uniprot(10001L, "Name-Updated", "New-Passport")));
//
//        repository.deleteById(10002L);
//
//        logger.info("All users -> {}", repository.findAll());
//    }
    @Bean
    public EmbeddedServletContainerFactory tomcatEmbedded() {
        return new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // Keep large upload behavior unchanged.
                super.postProcessContext(context);
            }

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(Tomcat tomcat) {
                tomcat.getHost().getPipeline().addValve(new ValveBase() {
                    @Override
                    public void invoke(Request request, Response response) throws IOException, ServletException {
                        String requestUri = request.getRequestURI();

                        if (requestUri != null && (requestUri.equals("/pinet/api") || requestUri.startsWith("/pinet/api/"))) {
                            String target = "/pln" + requestUri.substring("/pinet".length());
                            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                                target += "?" + request.getQueryString();
                            }
                            response.sendRedirect(target);
                            return;
                        }

                        getNext().invoke(request, response);
                    }
                });

                tomcat.getConnector();
                if (tomcat.getConnector().getProtocolHandler() instanceof AbstractHttp11Protocol<?>) {
                    ((AbstractHttp11Protocol<?>) tomcat.getConnector().getProtocolHandler()).setMaxSwallowSize(-1);
                }

                return super.getTomcatEmbeddedServletContainer(tomcat);
            }
        };
    }
}
