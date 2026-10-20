package shop.le_paysan.backend.contact;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.contact.dto.ContactMessageRequest;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

/** Cf. docs/02-specifications-fonctionnelles.md UC-12 : formulaire de contact / support. */
@Service
public class ContactService {

    private final ContactMessageRepository repository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ContactService(
            ContactMessageRepository repository, UserRepository userRepository, NotificationService notificationService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void submit(User sender, ContactMessageRequest request) {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setUser(sender);
        contactMessage.setFullName(request.fullName());
        contactMessage.setEmail(request.email());
        contactMessage.setSubject(request.subject());
        contactMessage.setMessage(request.message());
        repository.save(contactMessage);

        // Pas encore de back-office dedie aux messages de contact (cf. docs/02 UC-12, UC-14) : pas de
        // lien pour l'instant, l'admin consulte la base le temps que cet ecran soit construit.
        for (User admin : userRepository.findByRoles_Name(Role.ADMIN)) {
            notificationService.notify(
                    admin,
                    "CONTACT_MESSAGE_RECEIVED",
                    "Nouveau message de contact",
                    "\"" + request.subject() + "\" — envoyé par " + request.fullName(),
                    null);
        }
    }
}
