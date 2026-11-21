package com.testhakni.backend.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.testhakni.backend.model.PriceType;
import com.testhakni.backend.model.ProviderProfile;
import com.testhakni.backend.model.Role;
import com.testhakni.backend.model.ServiceCategory;
import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceRequest;
import com.testhakni.backend.model.User;
import com.testhakni.backend.repository.ProviderProfileRepository;
import com.testhakni.backend.repository.ServiceOfferingRepository;
import com.testhakni.backend.repository.ServiceRequestRepository;
import com.testhakni.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Donnees de demonstration, inserees au demarrage si la collection users est vide. DB-02
 * Active en profil dev (app.seed.enabled=true). Comptes crees (mot de passe : app.seed.password) :
 * admin@testhakni.dev, client@testhakni.dev, plombier@testhakni.dev, menage@testhakni.dev
 */
@Component
@org.springframework.context.annotation.Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final ProviderProfileRepository profiles;
    private final ServiceOfferingRepository services;
    private final ServiceRequestRepository requests;
    private final PasswordEncoder encoder;
    private final String password;

    public DataSeeder(UserRepository users, ProviderProfileRepository profiles, ServiceOfferingRepository services,
                      ServiceRequestRepository requests, PasswordEncoder encoder,
                      @Value("${app.seed.password}") String password) {
        this.users = users;
        this.profiles = profiles;
        this.services = services;
        this.requests = requests;
        this.encoder = encoder;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            log.info("Seed ignore : la base contient deja des utilisateurs");
            return;
        }
        String hash = encoder.encode(password);

        user("admin@testhakni.dev", hash, "Admin", "Plateforme", Role.ADMIN, "Tunis");
        User client = user("client@testhakni.dev", hash, "Sami", "Ben Ali", Role.CUSTOMER, "Tunis");
        User plombier = user("plombier@testhakni.dev", hash, "Karim", "Trabelsi", Role.PROVIDER, "Tunis");
        User menage = user("menage@testhakni.dev", hash, "Leila", "Mansour", Role.PROVIDER, "Sousse");

        profiles.saveAll(List.of(
                ProviderProfile.builder().userId(plombier.getId()).businessName("Karim Plomberie & Elec")
                        .bio("Plombier et electricien depuis 12 ans. Intervention rapide sur le Grand Tunis.")
                        .categories(List.of(ServiceCategory.PLOMBERIE, ServiceCategory.ELECTRICITE))
                        .skills(List.of("Fuites", "Chauffe-eau", "Tableau electrique"))
                        .city("Tunis").serviceArea("Grand Tunis").yearsOfExperience(12).build(),
                ProviderProfile.builder().userId(menage.getId()).businessName("Leila Clean Services")
                        .bio("Menage a domicile et nettoyage apres travaux, produits fournis.")
                        .categories(List.of(ServiceCategory.MENAGE, ServiceCategory.JARDINAGE))
                        .skills(List.of("Repassage", "Vitres", "Entretien jardin"))
                        .city("Sousse").serviceArea("Sousse et Monastir").yearsOfExperience(6).build()));

        services.saveAll(List.of(
                service(plombier, "Reparation de fuite d'eau", "Detection et reparation de fuites sous evier, WC, douche. Deplacement inclus.",
                        ServiceCategory.PLOMBERIE, "Tunis", "60", PriceType.FIXED),
                service(plombier, "Installation chauffe-eau", "Pose et raccordement d'un chauffe-eau electrique ou gaz, mise en service.",
                        ServiceCategory.PLOMBERIE, "Tunis", "150", PriceType.FIXED),
                service(plombier, "Depannage electrique", "Panne de courant, disjoncteur qui saute, prises et interrupteurs.",
                        ServiceCategory.ELECTRICITE, "Ariana", "35", PriceType.HOURLY),
                service(menage, "Menage complet appartement", "Nettoyage de toutes les pieces, cuisine et salle de bain. Produits fournis.",
                        ServiceCategory.MENAGE, "Sousse", "15", PriceType.HOURLY),
                service(menage, "Nettoyage apres travaux", "Remise en etat complete apres chantier : poussiere, vitres, sols.",
                        ServiceCategory.MENAGE, "Monastir", null, PriceType.ON_QUOTE),
                service(menage, "Entretien de jardin", "Tonte, taille de haies, desherbage et evacuation des dechets verts.",
                        ServiceCategory.JARDINAGE, "Sousse", "40", PriceType.FIXED)));

        requests.save(ServiceRequest.builder().customerId(client.getId())
                .title("Fuite sous l'evier de la cuisine")
                .description("L'eau coule goutte a goutte sous l'evier depuis deux jours.")
                .category(ServiceCategory.PLOMBERIE).city("Tunis").address("Rue de Marseille")
                .preferredDate(LocalDate.now().plusDays(3)).budget(new BigDecimal("80")).build());

        log.info("Seed termine : 4 comptes (mot de passe '{}'), 2 profils, 6 services, 1 demande", password);
    }

    private User user(String email, String hash, String first, String last, Role role, String city) {
        return users.save(User.builder().email(email).passwordHash(hash).firstName(first).lastName(last)
                .phone("+216 20 000 000").city(city).role(role).build());
    }

    private ServiceOffering service(User provider, String title, String description, ServiceCategory category,
                                    String city, String price, PriceType priceType) {
        return ServiceOffering.builder().providerId(provider.getId()).title(title).description(description)
                .category(category).city(city).price(price == null ? null : new BigDecimal(price))
                .priceType(priceType).build();
    }
}
