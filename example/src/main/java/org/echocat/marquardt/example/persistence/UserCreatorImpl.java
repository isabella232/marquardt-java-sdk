package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.persistence.UserCreator;
import org.echocat.marquardt.example.domain.CustomSignUpAccountData;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class UserCreatorImpl implements UserCreator<PersistentUser, UserCredentials, CustomSignUpAccountData> {

    private final UserRepository _userRepository;
    private final PasswordEncoder _passwordEncoder;

    @Autowired
    public UserCreatorImpl(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
    }

    @Override
    public PersistentUser createFrom(final CustomSignUpAccountData accountData) {
        final UserCredentials credentials = accountData.getCredentials();
        final PersistentUser persistentUserToCreate = new PersistentUser();
        persistentUserToCreate.setEmail(credentials.getIdentifier());
        persistentUserToCreate.setFirstName(accountData.getFirstName());
        persistentUserToCreate.setLastName(accountData.getLastName());
        persistentUserToCreate.setEncodedPassword(_passwordEncoder.encode(credentials.getPassword()));
        persistentUserToCreate.setUserId(UUID.randomUUID());
        persistentUserToCreate.setRoles(Collections.<ExampleRoles>emptySet());
        return _userRepository.save(persistentUserToCreate);
    }
}
