package org.imjs_man.moodleParser.tokenGenerator;

import org.imjs_man.moodleParser.repository.PersonRepository;
import org.imjs_man.moodleParser.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class TokenGenerator {

    @Autowired
    private PersonRepository personRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public String generateNewToken() {
        boolean checkTokenExist = true;
        byte[] randomBytes = new byte[24];
        String newToken = "";
        while(checkTokenExist)
        {
            secureRandom.nextBytes(randomBytes);
            newToken = base64Encoder.encodeToString(randomBytes);
            if(personRepository.findByToken(newToken) == null) {checkTokenExist = false;} //fixme use service
        }

        return newToken;
    }
}
