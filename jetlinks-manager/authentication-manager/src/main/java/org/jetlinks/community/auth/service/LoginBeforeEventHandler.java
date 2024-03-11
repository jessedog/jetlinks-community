package org.jetlinks.community.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.authorization.events.AuthorizationBeforeEvent;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.codec.digest.DigestUtils.sha1;

/**
 * 功能描述
 *
 * @author: 600
 * @date: 2024年03月08日 09:36
 */
@Component
@Slf4j
public class LoginBeforeEventHandler {


    @Autowired
    private ReactiveUserService userService;
    @EventListener
    public void customLogin(AuthorizationBeforeEvent event) {
        Optional<Object> verifyKeyParam = event.getParameter("verifyKey");
        if (!verifyKeyParam.isPresent()) {

            Optional<Object> keyParam = event.getParameter("key");
            String key = String.valueOf(keyParam.get());
            event.async(
                getUserIdByHashId(key)
                    .doOnNext(event::setAuthorized)
            );
        }

    }

    public Mono<String> getUserIdByHashId(String key) {
        QueryParam queryParam = new QueryParam();
        queryParam.noPaging();
        return userService.findUser(queryParam)
                          .map(user -> {
                              String userId = user.getId();
                              String sha1Hash = sha1(userId);
                              return new AbstractMap.SimpleEntry<>(user, sha1Hash);
                          })
                          .filter(entry -> entry.getValue().equals(key))
                          .next()
                          .flatMap(entry -> userService.findById(entry.getKey().getId()))
                          .map(UserEntity::getId);
    }

    private String sha1(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] result = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHexString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while hashing with SHA-1", e);
        }
    }
    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }




}
