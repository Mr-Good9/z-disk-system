package com.good.zdisksystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import javax.xml.transform.Source;

@SpringBootTest
class ZDiskSystemApplicationTests {

    @Test
    void contextLoads() {
        // 生成一个512位长度的密钥
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(key);
        System.out.println(key.getEncoded());
        System.out.println(base64Key);
    }

}
