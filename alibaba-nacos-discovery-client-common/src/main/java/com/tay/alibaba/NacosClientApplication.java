package com.tay.alibaba;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Nacos服务消费
 *
 * @author chentay
 * @date 2019/04/23
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class NacosClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosClientApplication.class, args);
    }

    @Slf4j
    @RestController
    static class TestController {

        @Autowired
        LoadBalancerClient loadBalancerClient;

        @Autowired
        RestTemplate restTemplate;

        @Autowired
        Client client;

        @GetMapping("/test")
        public String test() {
            // 通过spring cloud common中的负载均衡接口选取服务提供节点实现接口调用
            ServiceInstance serviceInstance = loadBalancerClient.choose("alibaba-nacos-discovery-server");
            String url = serviceInstance.getUri() + "/hello?name=" + "didi";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            return "Invoke : " + url + ", return : " + result;
        }

        /**
         * RestTemplate消费方式
         *
         * @return {@link String}
         */
        @GetMapping("/restTemplate")
        public String restTemplate() {
            String result = restTemplate.getForObject("http://alibaba-nacos-discovery-server/hello?name=didi", String.class);
            return "Return : " + result;
        }


        /**
         * Feign消费方式
         *
         * @return {@link String}
         */
        @GetMapping("/feign")
        public String feign() {
            String result = client.hello("didi");
            return "Return : " + result;
        }

    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @FeignClient("alibaba-nacos-discovery-server")
    interface Client {

        /**
         * 实现内部接口调用
         *
         * @param name name
         * @return {@link String}
         */
        @GetMapping("/hello")
        String hello(@RequestParam(name = "name") String name);

    }
}
