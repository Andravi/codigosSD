package a;

/**
 * Hello world!
 */
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

import java.io.IOException;
import java.util.Properties;

public class MQTTBroker {

    private final Server mqttBroker;
    private final IConfig config;

    public MQTTBroker() {
        this.mqttBroker = new Server();
        this.config = new MemoryConfig(new Properties());
        
        // Configuração básica
        config.setProperty("port", "1883");
        config.setProperty("host", "0.0.0.0");
        config.setProperty("allow_anonymous", "true");
    }

    public void start() throws IOException {
        System.out.println("Iniciando broker MQTT...");
        mqttBroker.startServer(config);
        System.out.println("Broker MQTT iniciado na porta 1883");
        
        // Adiciona shutdown hook para parar o broker corretamente
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        System.out.println("Parando broker MQTT...");
        mqttBroker.stopServer();
        System.out.println("Broker MQTT parado");
    }

    public static void main(String[] args) {
        MQTTBroker broker = new MQTTBroker();
        try {
            broker.start();
            
            // Mantém o broker rodando
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            broker.stop();
        }
    }
}
