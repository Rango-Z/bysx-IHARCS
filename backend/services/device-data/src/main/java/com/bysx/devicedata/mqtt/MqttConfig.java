package com.bysx.devicedata.mqtt;

import com.bysx.devicedata.service.DeviceStatusIngestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@EnableConfigurationProperties(MqttProps.class)
public class MqttConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(MqttProps props) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{props.getUrl()});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
        }
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            options.setPassword(props.getPassword().toCharArray());
        }
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttStatusInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(MqttProps props, MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(props.getClientId(), factory, props.getTopic().getStatus());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttStatusInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttStatusInputChannel")
    public MessageHandler statusMessageHandler(DeviceStatusIngestService ingestService, ObjectMapper objectMapper) {
        return message -> {
            String payload = String.valueOf(message.getPayload());
            ingestService.ingest(payload, objectMapper);
        };
    }
}

