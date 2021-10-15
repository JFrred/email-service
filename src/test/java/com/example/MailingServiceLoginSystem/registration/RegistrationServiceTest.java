package com.example.MailingServiceLoginSystem.registration;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserService;
import com.example.MailingServiceLoginSystem.email.EmailSender;
import com.example.MailingServiceLoginSystem.registration.token.ConfirmationToken;
import com.example.MailingServiceLoginSystem.registration.token.ConfirmationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {
    @Mock
    private AppUserService appUserService;
    @Mock
    EmailValidator emailValidator;
    @Mock
    ConfirmationTokenService confirmationTokenService;
    @Mock
    EmailSender emailSender;

    @InjectMocks
    RegistrationService registrationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_withValidEmail_shouldSucceed() {
        RegistrationRequest request = new RegistrationRequest(
                "foo", "bar", "pass", "email");
        when(emailValidator.test(anyString())).thenReturn(true);
        when(appUserService.signUpUser(any())).thenReturn("token");


        String token = registrationService.register(
                request);

        assertThat(token).isNotNull();
    }

    @Test
    void register_withInvalidEmail_shouldThrowISE() {
        RegistrationRequest request = new RegistrationRequest(
                "foo", "bar", "pass", "email");
        when(emailValidator.test(anyString())).thenReturn(false);
        Exception exception = assertThrows(IllegalStateException.class,
                () -> registrationService.register(request));

        String expectedMessage = "email not valid";
        String actualMessage = exception.getMessage();

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    void confirmToken_WithValidConfirmationToken_Succeed() {
        ConfirmationToken confirmationToken = new ConfirmationToken("token", LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(60), new AppUser());
        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.of(confirmationToken));

        String token = registrationService.confirmToken("test token");

        assertThat(token).isNotNull();
        assertThat(token).isEqualTo("confirmed");
    }

    @Test
    void confirmToken_tokenNotFound_shouldThrowISE() {
        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalStateException.class,
                () -> registrationService.confirmToken("token"));

        String actualMessage = exception.getMessage();
        String expectedMessage = "token not found";

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    void confirmToken_withEmailAlreadyConfirmed_shouldThrowISE() {
        ConfirmationToken confirmationToken = new ConfirmationToken(
                "token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(1), new AppUser());
        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.of(confirmationToken));

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        Exception exception = assertThrows(IllegalStateException.class,
                () -> registrationService.confirmToken("token"));

        String actualMessage = exception.getMessage();
        String expectedMessage = "email already confirmed";

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    void confirmToken_withTokenExpired_shouldThrowISE() {
        ConfirmationToken confirmationToken = new ConfirmationToken(
                "token", LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(5), new AppUser());
        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.of(confirmationToken));

        Exception exception = assertThrows(IllegalStateException.class,
                () -> registrationService.confirmToken("test token"));

        String expectedMessage = "token expired";
        String actualMessage = exception.getMessage();

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(actualMessage).contains(expectedMessage);
    }


}