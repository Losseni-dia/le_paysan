package shop.le_paysan.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi d'emails transactionnels via SMTP (Gmail en dev, cf.
 * docs/04-architecture-technique.md §6). Canal complementaire aux
 * notifications in-app (obligatoires, non traitees ici).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        send(
                to,
                "Le Paysan - Reinitialisation de mot de passe",
                "Vous avez demande la reinitialisation de votre mot de passe.\n\n"
                        + "Cliquez sur le lien suivant (valable 30 minutes) :\n" + resetLink + "\n\n"
                        + "Si vous n'etes pas a l'origine de cette demande, ignorez cet email.");
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Email = canal complementaire seulement (cf. notifications in-app obligatoires) :
            // un echec d'envoi ne doit jamais faire planter le flux applicatif.
            log.warn("Echec d'envoi d'email a {} : {}", to, e.getMessage());
        }
    }
}
