package com.example.consumer.config;

import com.example.consumer.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    /**
     * public KeyStoreKeyFactory(Resource resource, char[] password) {
     *         this(resource, password, type(resource));
     *     }
     * 传入密钥库 和密钥 然后自动解析密钥库的类型
     *
     * @param properties:  加载bean 我们使用Enable注解这样就清楚的知道我们使用了什么注解
     * @return [com.system.systemcommon.config.JwtProperties]
     */

    @Bean
    @Qualifier("com.system.systemcommon.config.JwtProperties")
    public KeyPair keyPair(JwtProperties properties){
        // 获取秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(
                        properties.getLocation(),
                        properties.getPassword().toCharArray());
        //读取钥匙对
        return keyStoreKeyFactory.getKeyPair(
                properties.getAlias(),
                properties.getPassword().toCharArray());
    }

    @Bean
    public JwtTool jwtTool(KeyPair keyPair){
        return new JwtTool(keyPair);
    }
}
