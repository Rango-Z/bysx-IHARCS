package com.bysx.devicecontrol.mqtt;

import com.bysx.devicecontrol.service.ControlResultConsumeService;
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
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
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
    public MessageChannel mqttResultInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(MqttProps props, MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(props.getClientId() + "_in", factory, props.getTopic().getResult());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttResultInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttResultInputChannel")
    public MessageHandler resultMessageHandler(ControlResultConsumeService consumeService, ObjectMapper objectMapper) {
        return message -> consumeService.consume(String.valueOf(message.getPayload()), objectMapper);
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttProps props, MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(props.getClientId() + "_out", factory);
        handler.setAsync(true);
        handler.setDefaultQos(1);
        handler.setConverter(new DefaultPahoMessageConverter());
        return handler;
    }
}

