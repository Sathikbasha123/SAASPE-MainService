package saaspe.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import saaspe.entity.UserLoginDetails;

@Service
public class AuthenticationUserDetailService implements UserDetailsService {

    @Autowired
    private UserLoginService userLoginService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserLoginDetails> user = userLoginService.loadUserByUsername(username);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException(username);
        }
        return new User(user.get().getEmailAddress(), user.get().getPassword(), Collections.emptyList());
    }

}