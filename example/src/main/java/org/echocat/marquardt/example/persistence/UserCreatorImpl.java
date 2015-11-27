package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.domain.UserStatus;
import org.echocat.marquardt.authority.persistence.UserCreator;
import org.echocat.marquardt.common.domain.ClientInformation;
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

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

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
    public PersistentUser createEmptyUser() {
        final PersistentUser userToCreate = new PersistentUser();
        userToCreate.setUserId(UUID.randomUUID());
        userToCreate.setRoles(Collections.<ExampleRoles>emptySet());
        userToCreate.setStatus(UserStatus.WITHOUT_CREDENTIALS);
        return _userRepository.save(userToCreate);
    }

    @Override
    public PersistentUser enrichAndUpdateFrom(final PersistentUser user, final CustomSignUpAccountData accountData) {
        final UserCredentials credentials = accountData.getCredentials();
        user.setEmail(credentials.getIdentifier());
        user.setFirstName(accountData.getFirstName());
        user.setLastName(accountData.getLastName());
        user.setEncodedPassword(_passwordEncoder.encode(credentials.getPassword()));
        user.setRoles(asSet(ExampleRoles.ROLE_USER));
        user.setStatus(UserStatus.CONFIRMED);
        return _userRepository.save(user);
    }
}