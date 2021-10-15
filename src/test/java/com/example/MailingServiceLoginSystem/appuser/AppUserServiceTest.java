package com.example.MailingServiceLoginSystem.appuser;

import com.example.MailingServiceLoginSystem.registration.token.ConfirmationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AppUserServiceTest {
    @Mock
    AppUserRepository appUserRepository;
    @Mock
    ConfirmationTokenService confirmationTokenService;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    AppUserService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_userWithGivenEmailExists_shouldSucceed() {
        Optional<AppUser> appUser = Optional.of(new AppUser());
        when(appUserRepository.findByEmail(anyString())).thenReturn(appUser);

        UserDetails userDetails = service.loadUserByUsername("testemail");

        assertThat(appUser).isNotEmpty();
        assertThat(userDetails).isNotNull();
    }

    @Test
    void loadUserByUsername_userWithGivenEmailDoesNotExist_shouldThrowUsernameNotFoundException() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Exception exception = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("testemail"));

        String expectedMessage = "email";
        String actualMessage = exception.getMessage();

        assertThat(exception).isInstanceOf(UsernameNotFoundException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    void signUpUser_emailIsNotTaken_shouldSucceed() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        String token = service.signUpUser(new AppUser());

        assertThat(token).isNotNull();
    }

    @Test
    void signUpUser_emailIsAlreadyTaken_shouldThrowISE() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(new AppUser()));

        AppUser testUser = new AppUser("foo", "bar", "email", "pass", AppUserRole.USER);
        Exception exception = assertThrows(IllegalStateException.class,
                () -> service.signUpUser(testUser));

        String actualMessage = exception.getMessage();
        String expectedMessage = "email already taken";

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }
}
