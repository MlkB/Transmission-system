#!/usr/bin/env python3
"""
Script pour afficher toutes les courbes de sondes et de TEB
avec des graduations et des légendes améliorées.

Ce script lit tous les fichiers CSV générés par le simulateur Java
et affiche les courbes correspondantes avec matplotlib.
"""

import os
import glob
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import argparse


def plot_sonde_files():
    """
    Affiche toutes les courbes des sondes (fichiers sonde_*.csv)
    """
    sonde_files = glob.glob("sonde_*.csv")

    if not sonde_files:
        print("Aucun fichier de sonde trouvé")
        return

    print(f"Fichiers de sondes trouvés : {len(sonde_files)}")

    for file in sorted(sonde_files):
        try:
            # Lire le fichier CSV
            data = pd.read_csv(file)

            # Extraire le nom de la sonde du nom de fichier
            sonde_name = file.replace("sonde_", "").replace(".csv", "").replace("_", " ")

            # Créer une nouvelle figure pour chaque sonde
            fig, ax = plt.subplots(figsize=(12, 4))

            # Tracer la courbe
            ax.plot(data['index'], data['valeur'], linewidth=1.5, color='blue')
            ax.set_xlabel('Index', fontsize=11, fontweight='bold')
            ax.set_ylabel('Valeur', fontsize=11, fontweight='bold')
            ax.set_title(f'{sonde_name}', fontsize=13, fontweight='bold')
            ax.grid(True, alpha=0.3, linestyle='--')

            # Ajouter les graduations
            ax.tick_params(axis='both', which='major', labelsize=10)

            plt.tight_layout()
            print(f"  - {file}: {len(data)} points")

        except Exception as e:
            print(f"Erreur lors de la lecture de {file}: {e}")


def plot_teb_files():
    """
    Affiche toutes les courbes de TEB (fichiers TEB_*.csv)
    """
    teb_files = glob.glob("TEB_*.csv")

    if not teb_files:
        print("\nAucun fichier de TEB trouvé")
        return

    print(f"\nFichiers de TEB trouvés : {len(teb_files)}")

    # Grouper les fichiers par type d'analyse
    analyses = {}
    for file in teb_files:
        # Extraire le type d'analyse du nom de fichier
        analysis_type = file.replace("TEB_", "").replace(".csv", "")

        # Grouper les analyses de codeur ensemble
        if "Codeur" in analysis_type:
            key = "Codeur"
        else:
            key = analysis_type

        if key not in analyses:
            analyses[key] = []
        analyses[key].append(file)

    # Créer une figure pour chaque type d'analyse
    for analysis_type, files in analyses.items():
        fig, ax = plt.subplots(figsize=(10, 6))

        for file in sorted(files):
            try:
                # Lire le fichier CSV
                data = pd.read_csv(file)

                # Obtenir les noms des colonnes
                x_col = data.columns[0]
                y_col = data.columns[1]

                # Tracer la courbe
                if "Codeur" in file:
                    if "Sans" in file:
                        ax.plot(data[x_col], data[y_col], 'o-', linewidth=2, markersize=6,
                               label='Sans codeur', color='red')
                    else:
                        ax.plot(data[x_col], data[y_col], 's-', linewidth=2, markersize=6,
                               label='Avec codeur', color='green')
                else:
                    label = analysis_type.replace("_", " ")
                    ax.plot(data[x_col], data[y_col], 'o-', linewidth=2, markersize=6,
                           label=label)

                print(f"  - {file}: {len(data)} points")

            except Exception as e:
                print(f"Erreur lors de la lecture de {file}: {e}")

        # Configuration du graphique
        ax.set_xlabel(data.columns[0], fontsize=12, fontweight='bold')
        ax.set_ylabel('TEB (Taux d\'Erreur Binaire)', fontsize=12, fontweight='bold')
        ax.set_title(f'TEB = f({analysis_type})', fontsize=14, fontweight='bold')
        ax.grid(True, alpha=0.3, linestyle='--')
        ax.legend(fontsize=10)
        ax.tick_params(axis='both', which='major', labelsize=10)

        # Utiliser une échelle logarithmique pour le TEB si nécessaire
        if data[y_col].min() > 0 and data[y_col].max() / data[y_col].min() > 100:
            ax.set_yscale('log')
            ax.set_ylabel('TEB (échelle log)', fontsize=12, fontweight='bold')

        plt.tight_layout()


def plot_teb_comparison():
    """
    Affiche les courbes de TEB pour NRZ, NRZT et RZ sur le même graphique
    Détecte automatiquement les comparaisons SNR et nbEch
    """
    teb_files = glob.glob("TEB_*.csv")

    if not teb_files:
        print("\nAucun fichier de TEB trouvé")
        return

    print(f"\nComparaison des types de codage")

    # Séparer les fichiers SNR et nbEch
    # SNR: TEB_NRZ.csv, TEB_NRZT.csv, TEB_RZ.csv
    # nbEch: TEB_NRZ_NbEch.csv, TEB_NRZT_NbEch.csv, TEB_RZ_NbEch.csv

    snr_files = {'NRZ': None, 'NRZT': None, 'RZ': None}
    nbech_files = {'NRZ': None, 'NRZT': None, 'RZ': None}

    for file in teb_files:
        # Fichiers nbEch (contiennent "_NbEch")
        if 'NbEch' in file:
            if 'NRZT' in file:
                nbech_files['NRZT'] = file
            elif 'NRZ' in file:
                nbech_files['NRZ'] = file
            elif 'RZ' in file:
                nbech_files['RZ'] = file
        # Fichiers SNR (ne contiennent pas "_NbEch")
        else:
            if 'NRZT' in file:
                snr_files['NRZT'] = file
            elif 'NRZ' in file:
                snr_files['NRZ'] = file
            elif 'RZ' in file:
                snr_files['RZ'] = file

    colors = {'NRZ': 'blue', 'NRZT': 'red', 'RZ': 'green'}
    markers = {'NRZ': 'o', 'NRZT': 's', 'RZ': '^'}

    # Tracer le graphique SNR si des fichiers existent
    if any(snr_files.values()):
        plot_comparison_graph(snr_files, colors, markers, "SNR_dB",
                            "Comparaison TEB : NRZ vs NRZT vs RZ (SNR)")

    # Tracer le graphique nbEch si des fichiers existent
    if any(nbech_files.values()):
        plot_comparison_graph(nbech_files, colors, markers, "NbEch",
                            "Comparaison TEB : NRZ vs NRZT vs RZ (nbEch)")

    # Vérifier s'il y a des fichiers de comparaison multi-trajets
    nbtrajets_files = {'NRZ': None, 'NRZT': None, 'RZ': None}
    alpha_files = {'NRZ': None, 'NRZT': None, 'RZ': None}
    tau_files = {'NRZ': None, 'NRZT': None, 'RZ': None}

    for file in teb_files:
        if 'NbTrajets' in file:
            if 'NRZT' in file:
                nbtrajets_files['NRZT'] = file
            elif 'NRZ' in file:
                nbtrajets_files['NRZ'] = file
            elif 'RZ' in file:
                nbtrajets_files['RZ'] = file
        elif 'Alpha' in file:
            if 'NRZT' in file:
                alpha_files['NRZT'] = file
            elif 'NRZ' in file:
                alpha_files['NRZ'] = file
            elif 'RZ' in file:
                alpha_files['RZ'] = file
        elif 'Tau' in file:
            if 'NRZT' in file:
                tau_files['NRZT'] = file
            elif 'NRZ' in file:
                tau_files['NRZ'] = file
            elif 'RZ' in file:
                tau_files['RZ'] = file

    # Tracer les graphiques multi-trajets si des fichiers existent
    if any(nbtrajets_files.values()):
        plot_comparison_graph(nbtrajets_files, colors, markers, "NbTrajets",
                            "Comparaison TEB : NRZ vs NRZT vs RZ (Nombre de trajets)")

    if any(alpha_files.values()):
        plot_comparison_graph(alpha_files, colors, markers, "Alpha",
                            "Comparaison TEB : NRZ vs NRZT vs RZ (Alpha)")

    if any(tau_files.values()):
        plot_comparison_graph(tau_files, colors, markers, "Tau",
                            "Comparaison TEB : NRZ vs NRZT vs RZ (Tau)")


def plot_comparison_graph(coding_files, colors, markers, expected_xlabel, title):
    """
    Trace un graphique de comparaison pour un ensemble de fichiers
    """
    fig, ax = plt.subplots(figsize=(10, 6))

    x_col = None
    y_col = None
    y_min = float('inf')
    y_max = float('-inf')

    for coding, file in coding_files.items():
        if file:
            try:
                # Lire le fichier CSV
                data = pd.read_csv(file)

                # Obtenir les noms des colonnes (première fois seulement)
                if x_col is None:
                    x_col = data.columns[0]
                    y_col = data.columns[1]

                # Tracer la courbe
                ax.plot(data[x_col], data[y_col], marker=markers[coding],
                       linewidth=2, markersize=6, label=coding, color=colors[coding])

                # Suivre les min/max pour l'échelle log
                y_min = min(y_min, data[y_col].min())
                y_max = max(y_max, data[y_col].max())

                print(f"  - {coding}: {len(data)} points")

            except Exception as e:
                print(f"Erreur lors de la lecture de {file}: {e}")

    # Vérifier qu'au moins un fichier a été lu
    if x_col is None:
        return

    # Configuration du graphique
    ax.set_xlabel(x_col, fontsize=12, fontweight='bold')
    ax.set_ylabel('TEB (Taux d\'Erreur Binaire)', fontsize=12, fontweight='bold')
    ax.set_title(title, fontsize=14, fontweight='bold')
    ax.grid(True, alpha=0.3, linestyle='--')
    ax.legend(fontsize=11)
    ax.tick_params(axis='both', which='major', labelsize=10)

    # Utiliser une échelle logarithmique pour le TEB si nécessaire
    if y_min > 0 and y_max / y_min > 100:
        ax.set_yscale('log')
        ax.set_ylabel('TEB (échelle log)', fontsize=12, fontweight='bold')

    plt.tight_layout()


def main():
    """
    Fonction principale
    """
    # Parser les arguments de ligne de commande
    parser = argparse.ArgumentParser(
        description='Afficher les courbes de sondes et de TEB')
    parser.add_argument('-comparaison', action='store_true',
                       help='Afficher la comparaison des TEB pour NRZ, NRZT et RZ')
    args = parser.parse_args()

    print("="*60)
    if args.comparaison:
        print("Comparaison des types de codage")
    else:
        print("Affichage des courbes de sondes et de TEB")
    print("="*60)

    # Vérifier si des fichiers CSV existent
    csv_files = glob.glob("*.csv")

    if not csv_files:
        print("\nAucun fichier CSV trouvé dans le répertoire courant.")
        print("Assurez-vous d'avoir exécuté le simulateur Java avec l'option -s")
        return

    print(f"\nTotal de fichiers CSV trouvés : {len(csv_files)}\n")

    if args.comparaison:
        # Mode comparaison : afficher uniquement les TEB NRZ, NRZT, RZ
        plot_teb_comparison()
    else:
        # Mode normal : afficher toutes les courbes
        plot_sonde_files()
        plot_teb_files()

    print("\n" + "="*60)
    print("Affichage terminé !")
    print("="*60)

    # Afficher les graphiques
    plt.show()


if __name__ == "__main__":
    main()
