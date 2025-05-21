Planifia est une application Android de gestion de tâches et d'événements qui aide les utilisateurs à organiser leurs activités quotidiennes.

# Prérequis:
1. Android Studio
2. JDK 11
3. Gradle version 7.3.3
4. Compte Firebase


# Installation
1. Clonez le dépôt
git clone https://github.com/ayamte/Planifia.git  

2. Ouvrez le projet dans Android Studio
File > Open > [chemin vers le dossier Planifia]

3. Configurez Firebase
 Créez un projet Firebase sur la console Firebase
 Ajoutez une application Android avec le package com.example.planifia
 Téléchargez le fichier google-services.json et placez-le dans le dossier app/
 Assurez-vous d'activer les services Firebase suivants:
    Authentication 
    Realtime Database
    Storage

# Configuration
1.  Vérifiez que les dépendances Firebase sont correctement configurées dans le fichier app/build.gradle 
2. Assurez-vous que le SDK Android est configuré correctement 
3. Vérifiez la configuration minimale requise 

# Exécution
1. Connectez un appareil Android ou configurez un émulateur
2. Cliquez sur "Run" (▶️) dans Android Studio
3. Sélectionnez l'appareil cible et confirmez


# Fonctionnalités
1. Authentification utilisateur: Connexion et inscription sécurisées via Firebase
2. Tableau de bord intuitif: Gestion et organisation efficace des tâches
3. Création d'événements: Création et planification d'événements
4. Suivi des tâches: Surveillance de la progression des tâches
5. Catégorisation des tâches: Organisation des tâches par catégories

# Licence
Ce projet est sous licence MIT.

