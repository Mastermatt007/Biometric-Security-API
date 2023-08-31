import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableWebSecurity
public class BiometricAPI extends WebSecurityConfigurerAdapter {
    private final NotificationService notificationService;

    public BiometricAPI(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BiometricAPI.class, args);
    }

    @PostMapping("/authenticate")
    public String authenticateUser(@RequestBody BiometricData data) {
        // Authenticate the user using the biometric data
        if (authenticateBiometrics(data)) {
            return "Authentication successful";
        } else {
            notificationService.notifyOwner(data);
            return "Authentication failed";
        }
    }

    private boolean authenticateBiometrics(BiometricData data) {
        // Implement biometric authentication logic here
        // Return true if authentication is successful, false otherwise
        return true;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Add an in-memory user for authentication
        auth.userDetailsService(userDetailsService());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure security settings for the API
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().httpBasic();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        // Define an in-memory user for authentication
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("USER")
                        .build());
    }

    @Bean
    public NotificationService notificationService(JavaMailSender mailSender) {
        return new NotificationServiceImpl(mailSender);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        // Configure the JavaMailSender here
        return null;
    }
}

interface NotificationService {
    void notifyOwner(BiometricData data);
}

@Component
class NotificationServiceImpl implements NotificationService {
    private final JavaMailSender mailSender;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void notifyOwner(BiometricData data) {
        // Send an email to the owner with the biometric data
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("owner@example.com");
        message.setSubject("Unauthorized access detected");
        message.setText("An unauthorized user attempted to access the application with the following biometric data: " + data.toString());
        mailSender.send(message);

        // Send a notification to the owner's biometric device with the biometric data (if applicable)
        // ...
    }
}

class BiometricData {
    // Define the fields for the biometric data here

    @Override
    public String toString() {
        // Convert the biometric data to a string representation here
        return "";
    }
}