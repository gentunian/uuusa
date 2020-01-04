package org.grupolys;

import org.grupolys.samulan.Samulan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        if (springboot(args)) {
            SpringApplication.run(Application.class, args);
        } else {
            Samulan.main(args);
        }
    }

    private static boolean springboot(String[] args) {
        ArgumentParser ap = ArgumentParsers
                .newArgumentParser("Samulan")
                .defaultHelp(true)
                .description("Apply Sentiment Analysis to files");
        ap.addArgument("-springboot", "--springboot")
                .required(false)
                .help("Spring boot the application.")
                .action(Arguments.storeTrue());
        try {
            return ap.parseArgs(args)
                    .getBoolean("springboot");
        } catch (ArgumentParserException e) {
            return false;
        }
    }
}
