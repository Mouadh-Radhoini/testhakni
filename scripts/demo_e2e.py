"""Exercise the French marketplace UI on a disposable, seeded Compose test project."""
import os
import secrets
import shutil
from datetime import date, timedelta
from uuid import uuid4

from playwright.sync_api import expect, sync_playwright


def main():
    if os.environ.get("APP_DATABASE") != "testhakni_test":
        raise SystemExit("Set APP_DATABASE=testhakni_test and use the isolated test stack.")
    origin = "http://127.0.0.1:" + str(int(os.environ.get("WEB_PORT", "8088")))
    suffix = uuid4().hex[:10]
    password = "Qa!" + secrets.token_hex(16) + "7"
    title = "Plomberie " + suffix
    comment = "Travail soigné " + suffix

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(executable_path=shutil.which("chromium") or shutil.which("google-chrome"))

        def page():
            return browser.new_context(base_url=origin, viewport={"width": 1050, "height": 800}).new_page()

        def login(tab, email, secret):
            tab.goto("/login")
            tab.locator("#email").fill(email)
            tab.locator("#password").fill(secret)
            tab.locator("form button[type=submit]").click()
            expect(tab.get_by_role("button", name="Déconnexion")).to_be_visible()

        def register(role, name):
            tab = page()
            email = f"{name}-{suffix}@example.test"
            tab.goto("/register/" + role)
            for field, value in {"firstName": name, "lastName": "Test", "city": "Tunis", "email": email,
                                 "password": password, "phone": "+216 20 123 456"}.items():
                tab.locator("#" + field).fill(value)
            if role == "provider":
                tab.locator("#businessName").fill(name + " " + suffix)
                tab.get_by_label("Plomberie", exact=True).check()
            tab.locator("form button[type=submit]").click()
            tab.get_by_role("button", name="Déconnexion").click()
            login(tab, email, password)
            return tab

        customer = register("customer", "Client")
        provider = register("provider", "Artisan")
        competitor = register("provider", "Concurrent")
        provider.goto("/provider/profile")
        expect(provider.locator("#businessName")).to_have_value("Artisan " + suffix)
        provider.locator("#bio").fill("Plombier spécialisé dans les réparations domestiques.")
        provider.get_by_role("button", name="Enregistrer le profil").click()
        expect(provider.locator(".alert.success")).to_be_visible()
        provider.goto("/provider/services/new")
        provider.locator("#title").fill(title)
        provider.locator("#description").fill("Réparation complète de plomberie.")
        provider.locator("#category").select_option("PLOMBERIE")
        provider.locator("#city").fill("Tunis")
        provider.locator("#priceType").select_option("FIXED")
        provider.locator("#price").fill("80")
        provider.locator("form button[type=submit]").click()
        expect(provider).to_have_url(origin + "/provider/services")

        customer.goto("/services")
        customer.locator("#q").fill(title)
        customer.get_by_role("button", name="Rechercher").click()
        customer.get_by_role("heading", name=title, exact=True).click()
        expect(customer.get_by_role("link", name="Faire une demande")).to_be_visible()
        service_url = customer.url
        customer.get_by_role("link", name="Faire une demande").click()
        customer.locator("#description").fill("Réparer une fuite sous mon évier.")
        customer.locator("#address").fill("Adresse de démonstration")
        customer.get_by_role("button", name="Envoyer la demande").click()
        customer.get_by_role("link", name="Comparer les offres").click()
        for tab, price in [(provider, "75"), (competitor, "90")]:
            tab.goto("/provider/requests")
            card = tab.locator("article").filter(has=tab.get_by_role("heading", name=title, exact=True))
            card.get_by_role("button", name="Proposer une offre").click()
            card.get_by_label("Prix (DT)").fill(price)
            card.get_by_label("Date proposée").fill(str(date.today() + timedelta(days=2)))
            card.get_by_label("Description").fill("Intervention sur place et pièces comprises")
            card.get_by_role("button", name="Envoyer l’offre").click()
            expect(tab.get_by_text("Offre envoyée.")).to_be_visible()
        customer.reload()
        expect(customer.locator("article")).to_have_count(2)
        customer.locator("article").filter(has_text="Artisan " + suffix).get_by_role("button", name="Accepter cette offre").click()
        expect(customer.get_by_role("heading", name="Détail de la réservation")).to_be_visible()
        booking_url = customer.url
        provider.goto("/bookings")
        expect(provider.get_by_role("link", name="Voir la réservation")).to_have_count(1)
        provider.get_by_role("link", name="Voir la réservation").click()
        provider.get_by_role("button", name="Démarrer le travail").click()
        provider.get_by_role("button", name="Déclarer le travail terminé").click()
        expect(provider.get_by_text("À confirmer", exact=False).first).to_be_visible()
        customer.reload()
        customer.get_by_role("button", name="Confirmer la fin des travaux").click()
        customer.get_by_label("Commentaire").fill(comment)
        customer.get_by_role("button", name="Publier l’avis").click()
        expect(customer.get_by_text("Avis publié.")).to_be_visible()
        customer.goto(service_url)
        expect(customer.get_by_text(comment, exact=True)).to_be_visible()
        customer.get_by_role("link", name="Voir le profil du prestataire").click()
        expect(customer.get_by_text(comment, exact=True)).to_be_visible()
        customer.get_by_role("button", name="Signaler cet avis").click()
        customer.get_by_label("Motif du signalement").fill("Vérification " + suffix)
        customer.get_by_role("button", name="Envoyer le signalement").click()
        expect(customer.get_by_text("Signalement enregistré.")).to_be_visible()
        for tab, text in [(competitor,"Une autre offre a été retenue"),(provider,"Vous avez reçu un avis"),
                          (customer,"Nouvelle offre pour " + title)]:
            tab.goto("/notifications")
            card = tab.locator("article").filter(has_text=text).first
            card.get_by_role("button", name="Marquer comme lue").click()
            expect(card.get_by_text("Lue", exact=True)).to_be_visible()
        competitor.goto(booking_url)
        expect(competitor.get_by_role("alert")).to_be_visible()
        customer.goto("/admin/dashboard")
        expect(customer).to_have_url(origin + "/forbidden")
        admin = page()
        login(admin, "admin@testhakni.dev", os.environ["SEED_PASSWORD"])
        admin.goto("/admin/dashboard")
        expect(admin.get_by_role("heading", name="Réservations")).to_be_visible()
        admin.goto("/admin/reviews")
        card = admin.locator("article").filter(has_text=comment)
        card.get_by_role("button", name="Masquer").click()
        card.get_by_label("Motif de la décision").fill("Modération de démonstration")
        card.get_by_role("button", name="Masquer").click()
        expect(card.get_by_role("button", name="Rendre visible")).to_be_visible()
        admin.goto("/admin/reports")
        card = admin.locator("article").filter(has_text="Vérification " + suffix)
        card.get_by_role("button", name="Résoudre").click()
        card.get_by_label("Motif de la décision").fill("Avis masqué")
        card.get_by_role("button", name="Résoudre").click()
        expect(card.get_by_text("Avis masqué", exact=False)).to_be_visible()
        customer.goto(service_url)
        expect(customer.get_by_text("Aucun avis publié.")).to_be_visible()
        admin.goto("/admin/services")
        service_card = admin.locator("article").filter(has=admin.get_by_role("heading", name=title, exact=True))
        service_card.get_by_role("button", name="Suspendre").click()
        service_card.get_by_label("Motif de la décision").fill("Contrôle de publication")
        service_card.get_by_role("button", name="Suspendre").click()
        expect(service_card.get_by_role("button", name="Activer")).to_be_visible()
        customer.reload()
        expect(customer.get_by_text("Service introuvable")).to_be_visible()
        service_card.get_by_role("button", name="Activer").click()
        service_card.get_by_label("Motif de la décision").fill("Publication validée")
        service_card.get_by_role("button", name="Activer").click()
        expect(service_card.get_by_role("button", name="Suspendre")).to_be_visible()
        customer.reload()
        expect(customer.get_by_role("heading", name=title, exact=True)).to_be_visible()
        admin.goto("/admin/users")
        account = admin.locator("article").filter(has_text=f"Concurrent-{suffix}@example.test".lower())
        account.get_by_role("button", name="Suspendre").click()
        account.get_by_label("Motif de la décision").fill("Vérification du compte")
        account.get_by_role("button", name="Suspendre").click()
        expect(account.get_by_role("button", name="Activer")).to_be_visible()
        competitor.goto("/provider/offers")
        expect(competitor.get_by_role("button", name="Déconnexion")).to_have_count(0)
        account.get_by_role("button", name="Activer").click()
        account.get_by_label("Motif de la décision").fill("Compte vérifié")
        account.get_by_role("button", name="Activer").click()
        expect(account.get_by_role("button", name="Suspendre")).to_be_visible()
        login(competitor,f"Concurrent-{suffix}@example.test", password)
        browser.close()
    print("Passed: registration, login, profile, service, search, two offers, booking, completion, review, notifications, moderation and authorization.")


if __name__ == "__main__":
    main()
