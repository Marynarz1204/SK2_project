Projekt zaliczeniowy - Sieci Komputerowe 2 laboratorium

Nazwa: Chatu Chatu
Michał Łopatka 145369
Damian Rzepka 145222

Opis protokołu komunikacyjnego
Wybór akcji użytkownika – użytkownik po uruchomieniu programu może zacząć od
zalogowania się lub utworzenia nowego konta.
Przy próbie logowania użytkownik wysyła sygnał „log <nazwa_konta> <hasło>” może od serwera
otrzymać:
pass – gdy podane dane są poprawne
wrong – gdy podane hasło jest nieprawidłowe
dont exist – kiedy nie istnieje konto użytkownika o podanej nazwie

Przy próbie rejestracji użytkownik wysyła sygnał „sign <nazwa_konta> <hasło>” oraz hasło musi być
wpisane dwukrotnie, może otrzymać:
pass – gdy konto zostało utworzone poprawnie
exist – gdy istnieje wcześniej utworzone konto o takiej nazwie

Po zalogowaniu się użytkownik uzyskuje dostęp do pozostałych funkcjonalności
Przy próbie dodania znajomego użytkownik wysyła sygnał „add <nazwa_konta_przyjaciela>”, w
odpowiedzi serwer może wysłać:
me – gdy użytkownik poda swoją nazwę podczas dodawania użytkownika
dont exist – gdy konto o takiej nazwie nie istnieje
pass – gdy użytkownik zostanie dodany jako nasz znajomy
already a friend – gdy podany użytkownik jest już naszym znajomym

Przy próbie usunięcia znajomego użytkownik wysyła sygnał „delete <nazwa_konta_przyjaciela>”,
który jest naszym znajomym, w odpowiedzi serwer może wysłać:
pass – gdy poprawnie usuniemy znajomego z listy
wrong – gdy konto o takiej nazwie nie jest naszym znajomym

Przy próbie wybrania znajomego, do którego chcemy napisać, wysyłamy do serwera sygnał „show”,
serwer wyślę nam listę naszych znajomych i wyświetli ją w formie rozwijanej listy. Po wybraniu w ten
sposób znajomego z którym chcemy pisać, wyślemy syganł „getFile”, aby otrzymać wcześniejsze
wiadomości z tym użytkownikiem, będziemy mogli wysłać mu wiadomość, za pomocą sygnału „send
to <wiadomosc>”, wiadomość zostanie wpisana do rejestru wiadomości oraz jeśli jesteśmy na jednym
czacie z użytkownikiem, do którego piszemy otrzyma on ją.

Jeśli w którymś momencie wybierzemy opcje Exit to wyślemy do serwera sygnał „quit”, który to
zatrzyma wątek serwera odpowiedzialny za naszego klienta.
Natomiast jeśli wybierzemy opcje Log out, to również wyślemy sygnał „quit”, ale będziemy nadal
połączeni z serwerem.


Opis implementacji (Struktura projektu)
Zarówno klient jak i serwer zostały napisane w języku Java. Używaliśmy standardowych
bibliotek języka Java oraz innych bibliotek potrzebnych do implementacji wątków w standardach
POSIX. Serwer korzysta również z mutexów. Do stworzenia interfejsu graficznego użyty został JavaFX.

Opis sposobu kompilacji i uruchomienia projektu
By uruchomić najlepiej otworzyć projekt w IntelliJ lub innym podobnym programie
obsługującym projekty Java. Zacząć należy od uruchomienia pliku ServerMain.class a następnie
uruchomić jeden lub więcej razy plik Client.class
Poruszanie się po programie umożliwiają opisane swoimi funkcjami przyciski. Przy pierwszym
uruchomieniu programu nie istnieją żadne profile użytkowników, dlatego też zalecane jest
rozpoczęcie użytkowania od utworzenia takowego.
