package it.intre.tradingsystem.elaboration;

import it.intre.messagedispatcher.consumer.Consumer;
import it.intre.messagedispatcher.consumer.KafkaConsumer;
import it.intre.messagedispatcher.model.KafkaConfiguration;
import it.intre.messagedispatcher.model.Record;
import it.intre.tradingsystem.common.Constants;
import it.intre.tradingsystem.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

public class ThreadConsumerKafka implements Runnable {

    private static final Logger logger = LogManager.getLogger(ThreadConsumerKafka.class);

    private String host;
    private String port;
    private String nameTopic;
    private Class typeClass;

    public ThreadConsumerKafka(String host, String port, String nameTopic, Class typeClass) {
        this.host = host;
        this.port = port;
        this.nameTopic = nameTopic;
        this.typeClass = typeClass;
    }

    private Consumer createConsumer(String host, String port, String NameTopic, Class typeClass) {
        KafkaConfiguration inputConfiguration = new KafkaConfiguration(host, port, Constants.GROUP_ID, Constants.CLIENT_ID, NameTopic);
        return new KafkaConsumer<>(inputConfiguration, String.class, typeClass);
    }

    @Override
    public void run() {
        Consumer consumer = createConsumer(this.host, this.port, this.nameTopic, this.typeClass);
        Session session = HibernateUtil.getSessionFactory().openSession();

        while (true) {
            List<Record> recordsList = consumer.receive();
            for (Record record : recordsList) {
                logger.debug("record {}", record.getValue().toString());

                session.beginTransaction();
                session.save(record.getValue());
                session.getTransaction().commit();
            }
            consumer.commit();
        }
    }
}
