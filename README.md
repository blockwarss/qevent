# questevent

Plugin Paper **1.21.x**, **Java 21**. Événement : `/qevent start <durée>` choisit un **item aléatoire** parmi `config.yml`. 
Chaque joueur tente d'atteindre l'**objectif** (quantité) après le départ. Les **3 premiers** à atteindre l'objectif sont classés et reçoivent des récompenses (commandes). 
**Messages** configurables dans `messages.yml` (préfixe: `&a&lꜱᴜʀᴠɪᴇ&c&lᴛɪᴋᴛᴏᴋ&f | `).

## Build (Maven)
```bash
mvn -B package
```
Le JAR est produit dans `target/questevent-1.0.0-shaded.jar`.

## Commande
- `/qevent start <durée>` (ex : `30m`, `1h`, `1h30m`, `90s`) — permission `questevent.admin`.

## Config
- `config.yml` : liste `items` (`material`, `required`), blocs `rewards.first/second/third` avec commandes (placeholder `{player}`).
- `messages.yml` : tous les messages + `prefix`.

## Fonctionnement
- Début : sélection aléatoire d'un item dans `config.yml`.
- Comptage : **uniquement** les items ramassés *après* le départ (pickup). 
- Classement : ordre d'arrivée des 3 premiers joueurs à atteindre l'objectif.
- Récompenses : exécute les commandes depuis la console en remplaçant `{player}`.
- Fin : annonce du top 3 puis réinitialisation.

## CI GitHub Actions
Compilation automatique Java 21 → artefact du JAR.

## API ciblée
- Paper API `1.21-R0.1-SNAPSHOT`, `api-version: '1.21'`.
