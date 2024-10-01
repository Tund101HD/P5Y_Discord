# P5Y Discord Bot

## Was ist der P5Y Discord Bot?

P5Y Discord Bot soll das Bilden von Squads für unsere CW-Runden erleichtern, indem er automatisch registrierte Benutzer
den Teams zuordnet und in die Kanäle zieht. Ein Squadleader kann am Anfang einer Session ganz einfach alle wichtigen Daten
wie Aktivität und das zu spielende Battle Rating eingeben und der Bot kümmert sich um den Rest.
Nutzer, die dem Wartebereich beitreten, werden automatisch getrackt und sobald ein Platz frei wird automatisch in die richtigen
Calls gezogen. Des Weiteren soll der Bot eine Objektive Auswahl zwischen den Spielern, anhand der vergangenen Leistung, Aktivität und 
der Teilnahme an vergangenen Trainings treffen. 

## Was trackt der Discord Bot?

+ Spieler Aktivität in CW - Wie viele aktive Runden hast du mitgespielt?
+ Bereitschaft für CW - Wie lange warst du bereit warst/aktiv gewartet hast
+ Spieler Aktivität in Trainings - Wie oft hast du an Trainings teilgenommen?
+ Spieler Rating in den Runden (optional) - Wie gut hast du in den Runden performt?
+ Spieler Daten - Was ist dein präferiertes BR, welche BRs hast du, welche Units spielst du, etc..

All das soll dafür sorgen, dass der Bot die optimale Entscheidung bei der Auswahl der Spieler trifft und nicht ungeignete Spieler
z.B. aufgrund von zu hoher Aktivität oder weil sie das passende BR nicht haben.

## Umgang mit dem Bot
### Für Squadleader:
+ Wie kann ich eine Session starten?
> /startsession **(br)*** (min-activity) (min-priority) (exclude_users)

+ Wie kann ich meine Session mit anderen Anforderungen auffüllen lassen?
>/fillsession (min-activity) (min-priority) (exclude_users)

+ Wie kann ich Nutzer zu einer anderen Session übertragen?
> /move **(Nutzer)*** **(session_id)***

+ Wie kann ich meine Session beenden?
> /endsession
    
+ Wie zähle ich alle Sessions auf? 
> /listsessions

+ Wie werde ich Squad 1?
> Es gilt, wer zuerst kommt, mahlt zuerst.

Form und Funktion der Optionen:
    
    br: Das Battle-Rating auf dem die Runden gespielt werden
    min-activity: Die Mindestaktivität die ein Benutzer braucht, um mitzuspielen
    min-priority: Die Mindestpriorität, die ein Benutzer braucht, um mitzuspielen
    exclude_users: Komma-Separierte-Liste (Nutzer1,Nutzer2,NutzerN...) an gebannten Discord Nutzern
> [!NOTE]
> Der Bot pingt und benachrichtigt Benutzer aus der Datenbank automatisch, wenn sie geeignet sind und noch Leute fehlen!

> [!NOTE]
> Angaben die fett und mit einem * markiert sind, sind **_nicht_** optional! Alle anderen schon.

> [!TIP]
> Der Bot hat häufige Eingaben, wie z.B. alle BRs, als Automcomplete zurückgelegt.

### Für Squadmember:
+ Wie kann ich mich registrieren, um mitzuspielen?
> /register

Bitte befolge nach Eingabe die Anweisungen in deinen DMs!

+ Wie trete ich einer Session bei?
>/join 

Bitte beachte, dass du eigentlich immer automatisch in den Call gezogen wirst, wenn du bereits wartest und geeignet bist.
Der Command ist für Leute gedacht, die zu dem Zeitpunkt der Erstellung noch nicht online waren. Es kann passieren dass du 
auf der Warteliste landest.

+ Ab wann werde ich als wartend getrackt?
> Sobald du in den Kanal "Warteraum" betritts wirst du als wartend gekennzeichnet alternativ kannst du auch
> `/join` eingeben, wenn du in einem anderen Kanal bist.

> [!NOTE]
> Solange du dich nicht gehör-stummst, den AFK-Kanal betrittst oder in einem Sprachkanal bist, wirst du weiterhin als wartend
> getrackt. D.h. du kannst ruhig auch in die anderen Kanäle rein um zuzuschauen oder mit anderen zu Spielen bis du dran bist.
> Du wirst benachrichtigt, wenn du auf die Warteliste gesetzt wirst und wenn du von dieser entfernt wirst.

+ Wie kann ich meine Angaben überprüfen?
> /me


+ Wie kann ich meine Angaben aus der Registrierung ändern?
> Gib einfach noch einmal `/register` ein.

Bitte achte nochmals auf die Anweisungen in deinen DMs!

+ Wie verlasse ich eine Session?
> Du kannst die Session ganz einfach verlassen, indem du aus allen Sprachkanälen kurz raus gehst. Wenn du ein Mitspieler bist, kannst 
> einfach in den Warteraum joinen um deine Position freizugeben und dich auf die Warteliste zu setzen. Du wirst dabei für 5 Minuten 
> aus der Suche entfernt.

> [!CAUTION]
> Wenn du auf der Warteliste stehst, solltest du bereit sein mitzuspielen. Der Bot zieht automatisch geeignete Spieler in die Kanäle, ohne 
> davor nachzufragen! Stelle sicher, dass du erreichbar bleibst und den Anruf zu jedem Zeitpunk verlassen kannst.

### Für Admins:

+ Wie kann ich die IDs der Kanäle und Rollen einstellen?
> /setid **(Art)*** **(Kanal-ID)***

    Arten:
        - Squad1-Boden (Sprachkanal)
        - Squad1-Luft (Sprachkanal)
        - Squad2-Boden (Sprachkanal)
        - Squad2-Luft (Sprachkanal)
        - Warteraum (Sprachkanal)
        - AFK (Sprachkanal)
        - CW-Rolle (Rolle)
        - SL-Rolle (Rolle)

## Welche Daten erhebt der Bot?

Der Bot speichert wichtige Daten zur auswertung in seiner internen Datenbank ab um sie später aufzurufen. Dabei erhebt der Bot
unterschiedliche Informationen über dich, um seiner Aufgabe gerecht zu werden.
Informationen enthalten:

+ Aktive Spielzeit als Squad-Mitglied (Wenn du Teil des Squads bist)
+ Aktive Wartezeit auf die Teilnahme (Wenn du offiziell als wartend gekennzeichnet bist)
+ Informationen über die Teilnahme an Trainings (Wie oft, Welche Trainings, Wann, Welches BR, etc...)
+ ggf. Thema von Chat Nachrichten im CW-Kanal (z.B. @Kilian bin ready)
+ Alle Informationen, die du dem Bot während deiner Registration bereitstellst.
+ Spielleistung in Trainings und gespielten CW-Runden anhand der in-game Stats
+ Deinen jetzigen Sprachkanal (Wenn du offiziell als wartend gekennzeichnet bist)

Der Bot speichert ***NICHT***

+ Was du in einem Sprachkanal sagst, streamst oder sonst spielst beim Warten.
+ Genaue Nachrichten aus dem CW-Chat (Für den Bot ist nur wichtig, was du vorhast. Also bereit sein oder nicht.)
+ Für den Erfolg der CW-Runden nicht relevante Informationen, wie generelle Aktivität im Chat, Abostand bei PSY oder Ähnliches
+ Automatisch aufgefrischte Informationen über deine in-game stats (z.B. via Thunderskill)

> [!IMPORTANT]
> Mit der Registration stimmst du diesen Bedingungen zu.


## Wie weit ist die Entwicklung des Bots?

- [x] Registrierung und Speicherung von Nutzern
- [x] Internes Gerüst für die Handhabung von Sessions
- [x] /startsession, /fillsession, /listsessions
- [ ] /endsession, /move
- [ ] /join, /me, /setid
- [ ] Vollendung des Nutzertrackings und der Handhabung der Sessions
- [ ] Vollendung der Datenbank 
- [ ] Screenshotverarbeitung zur automatischen Verarbeitung der in-game Stat-Tabelle
- [ ] Schreiben von Dokumentation, Code aufbessern

## Ideen, wünsche oder einen Bug gefunden?

Dann schreib mir entweder direkt eine DM auf Discord unter `Tund_101_HD` oder wende dich an Kilian (`SlatanKiliankowitz`)
Du kannst auch alternativ auf diesem Github-Repository ein Problem erstellen unter https://github.com/Tund101HD/P5Y_Discord/issues/new