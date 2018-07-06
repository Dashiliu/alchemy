package com.dfire.platform.alchemy.web;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;

import com.dfire.platform.alchemy.web.config.DefaultProfileUtil;

import io.github.jhipster.config.JHipsterConstants;

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class})
@EnableCaching
public class AlchemyApp {

    private static final Logger log = LoggerFactory.getLogger(AlchemyApp.class);

    private final Environment env;

    public AlchemyApp(Environment env) {
        this.env = env;
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Throwable {
        SpringApplication app = new SpringApplication(AlchemyApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env=null;
//        try {
             env = app.run(args).getEnvironment();
            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.info(
                "\n----------------------------------------------------------\n\t"
                    + "Application '{}' is running! Access URLs:\n\t" + "Local: \t\t{}://localhost:{}\n\t"
                    + "External: \t{}://{}:{}\n\t"
                    + "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"), protocol, env.getProperty("server.port"), protocol, hostAddress,
                env.getProperty("server.port"), env.getActiveProfiles());
//        }catch (Throwable e){
//            log.error("init error",e);
//        }

    }

    /**
     * Initializes alchemy.
     * <p>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on
     * <a href="https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
            && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run "
                + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
            && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not "
                + "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }
}
