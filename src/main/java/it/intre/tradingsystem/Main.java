package it.intre.tradingsystem;

import it.intre.tradingsystem.common.Constants;
import it.intre.tradingsystem.elaboration.ThreadConsumerKafka;
import it.intre.tradingsystem.model.Order;
import it.intre.tradingsystem.model.Quote;
import it.intre.tradingsystem.util.HibernateUtil;
import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {
        CommandLine commandLine = getCommandLine(args);
        final String host = commandLine.getOptionValue("host");
        final String port = commandLine.getOptionValue("port");

        HibernateUtil.getSessionFactory();

        Thread threadQuotes = new Thread(new ThreadConsumerKafka(host, port, Constants.INPUT_TOPIC_QUOTES, Quote.class));
        threadQuotes.start();

        Thread threadOrders = new Thread(new ThreadConsumerKafka(host, port, Constants.INPUT_TOPIC_ORDERS, Order.class));
        threadOrders.start();
    }


    private static CommandLine getCommandLine(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        Options options = getOptions();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            helpFormatter.printHelp("DB Service", options);
            System.exit(1);
        }
        return commandLine;
    }

    private static Options getOptions() {
        Options options = new Options();
        Option host = new Option("h", "host", true, "Kafka host");
        host.setRequired(true);
        options.addOption(host);
        Option port = new Option("p", "port", true, "Kafka port");
        port.setRequired(true);
        options.addOption(port);
        return options;
    }
}
