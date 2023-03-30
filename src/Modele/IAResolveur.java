package Modele;

import Global.Configuration;
import Structures.ElementAvecPoids;
import Structures.Sequence;
import Structures.SequenceListe;
import Structures.FAPListe;
import java.util.*;

import static Modele.Niveau.*;

class IAResolveur extends IA {
    private HashMap<Integer, byte[][]> instances;
    private int[][] carte;
    private byte[][] caisses;
    private Position posPousseur;
    private int l, c;
    private int nb_buts, nb_caisses;
    private int profondeur = 0, nb_instances = 0;
    // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
    final static int VERT = 0x00CC00;
    final static int MARRON = 0xBB7755;
    private static final int INFINI = Integer.MAX_VALUE;
    private long startTime=0,endTime=0, duration=0, startTime_total=0, endTime_total=0;
    private int nb_total_chemins=0, nb_fois_Dijkstra=0, taille_file=0;
    private double nb_moyen_chemins=0.0;

    public IAResolveur() {
    }
    public void niveauToCarte(Niveau n){
        int[][] cases = n.getCases();
        l = n.lignes()-2;
        c = n.colonnes()-2;
        carte = new int[l][c];
        caisses = new byte[l][c];
        //supprime la 1ere ligne, la derni�re ligne, la 1ere colonne, la derni�re colonne de cases
        for(int i = 1; i < l+1; i++) {
            for (int j = 1; j < c + 1; j++) {
                if (((cases[i][j] & MUR) != 0 || (cases[i][j] & VIDE) != 0)) {
                    carte[i - 1][j - 1] = cases[i][j];
                }
                else if((cases[i][j] & BUT) != 0){
                    carte[i - 1][j - 1] = BUT;
                    if ((cases[i][j] & CAISSE) != 0){
                        this.nb_caisses++;
                        this.caisses[i - 1][j - 1] = CAISSE;
                    }
                    this.nb_buts++;
                }
                else if ((cases[i][j] & CAISSE) != 0) {
                    carte[i - 1][j - 1] = VIDE;
                    this.nb_caisses++;
                    this.caisses[i - 1][j - 1] = CAISSE;
                }
                else if((cases[i][j] & POUSSEUR) != 0) {
                    carte[i - 1][j - 1] = VIDE;
                    posPousseur = new Position(i - 1, j - 1);
                }
            }
        }
    }

    @Override
    public Sequence<Coup> joue() {
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        Coup coup = null;
        ArrayList<SequenceListe<Position>> chemins = null;
        /*
        instances = new HashMap<>();
        nb_instances = 0;
        nb_buts = 0;
        nb_caisses = 0;
        niveauToCarte(niveau);
        if (nb_buts != nb_caisses) {
            Configuration.erreur("Niveau impossible � r�soudre : le nombre de caisses est diff�rent du nombre de buts.");
            return null;
        }
        chemins = calcul_chemin(posPousseur, caisses);
        System.out.println("chemins.size() : " + chemins.size());
         */

        int taille_totale_file = 0;
        int duree_totale_Dijkstra = 0;
        int nb_fois_Dijkstra_total = 0;
        int temps_total_total = 0;
        int nb_instances_total = 0;

        double taille_moyenne_file = 0.0;
        double duree_totale_moyenne_Dijkstra = 0.0;
        double nb_chemins_moyen_total = 0.0;
        double nb_fois_Dijkstra_total_moyen = 0.0;
        double temps_total_total_moyen = 0.0;
        double nb_instances_total_moyen = 0.0;

        double nb_tests = 10.0;
        for(int i=0; i<nb_tests; i++) {
            startTime=0;endTime=0;duration=0;startTime_total=0;endTime_total=0;
            nb_total_chemins=0;nb_fois_Dijkstra=0;
            nb_moyen_chemins=0.0;
            taille_file=0;
            System.out.println("Essai " + i);
            instances = new HashMap<>();
            nb_instances = 0;
            nb_buts = 0;
            nb_caisses = 0;
            niveauToCarte(niveau);
            if (nb_buts != nb_caisses) {
                Configuration.erreur("Niveau impossible � r�soudre : le nombre de caisses est diff�rent du nombre de buts.");
                return null;
            }
            startTime_total = System.currentTimeMillis();
            chemins = calcul_chemin(posPousseur, caisses);
            endTime_total = System.currentTimeMillis();
            taille_totale_file += taille_file;
            duree_totale_Dijkstra += duration;
            nb_moyen_chemins = (double) nb_total_chemins / (double) nb_fois_Dijkstra;
            nb_chemins_moyen_total += nb_moyen_chemins;
            nb_fois_Dijkstra_total += nb_fois_Dijkstra;
            temps_total_total += (endTime_total - startTime_total);
            nb_instances_total += nb_instances;
        }
        taille_moyenne_file = (double) taille_totale_file / nb_tests;
        duree_totale_moyenne_Dijkstra = (double) duree_totale_Dijkstra / nb_tests;
        nb_chemins_moyen_total = (double) nb_chemins_moyen_total / nb_tests;
        nb_fois_Dijkstra_total_moyen = (double) nb_fois_Dijkstra_total / nb_tests;
        temps_total_total_moyen = (double) temps_total_total / nb_tests;
        nb_instances_total_moyen = (double) nb_instances_total / nb_tests;
        System.out.println("taille moyenne file : " + taille_moyenne_file);
        System.out.println("dur�e totale moyenne Dijkstra : " + duree_totale_moyenne_Dijkstra + " ms");
        nb_chemins_moyen_total = Math.round(nb_chemins_moyen_total * 100.0) / 100.0;
        System.out.println("nb moyen chemins : " + nb_chemins_moyen_total);
        System.out.println("nb moyen fois Dijkstra : " + nb_fois_Dijkstra_total_moyen);
        System.out.println("temps total moyen : " + temps_total_total_moyen + " ms");
        System.out.println("nb moyen instances : " + nb_instances_total_moyen);
        System.exit(0);

        for(int i=0; i<chemins.size(); i++){
            SequenceListe<Position> chemin = chemins.get(i);
            chemin.extraitTete();//on enl�ve la position du pousseur puisqu'il est d�j� � cette position
            while(!(chemin == null) && !chemin.estVide()){
                Position pos = chemin.extraitTete();
                //System.out.println("pos: " + pos.affiche());
                int dl = pos.getL() - posPousseur.getL();
                int dc = pos.getC() - posPousseur.getC();
                coup = niveau.deplace(dl, dc);
                resultat.insereQueue(coup);
                posPousseur = pos;
            }
        }
        return resultat;
    }

    public ArrayList<SequenceListe<Position>> calcul_chemin(Position posPousseur, byte[][] caisses){
        Position posCourante = null;
        ArrayList<SequenceListe<Position>> chemin = new ArrayList<SequenceListe<Position>>();
        SequenceListe<ArrayList<Position>> caissesDepl = new SequenceListe<ArrayList<Position>>();
        ArrayList<Position> caisseDeplCourante = new ArrayList<Position>();
        SequenceListe<Position> cheminCourant = new SequenceListe<Position>();
        LinkedList<ArbreChemins> queue = new LinkedList <ArbreChemins>();
        ArbreChemins arbreCourant = null;
        Instance instanceCourante = null;
        Position posCaisseFutur = null;
        Position posCaissePresent = null;

        ajouterInstance(posPousseur, caisses, instances);
        Instance instanceDepart = new Instance(posPousseur, caisses);
        ArbreChemins arbreCheminsTete = new ArbreChemins(instanceDepart, null, null);

        queue.add(arbreCheminsTete);

        while(!queue.isEmpty()){
            arbreCheminsTete = queue.poll();//ArbreChemins

            //r�cup�re l'instance courante qui contient la position du pousseur et les caisses
            posPousseur = arbreCheminsTete.getCourant().getPosPousseur();
            caisses = arbreCheminsTete.getCourant().getCaisses();

            //r�cup�re les chemins possibles pour le pousseur depuis l'instance courante
            LinkedList<SequenceListe<Position>> cheminsPousseurCaisse = Dijkstra(posPousseur, caisses);

            //pour chaque chemin possible du pousseur � une caisse
            while(!cheminsPousseurCaisse.isEmpty()){
                //System.out.println("taille cheminCourant : " + cheminCourant.taille());
                //System.exit(0);
                cheminCourant = cheminsPousseurCaisse.poll();//on r�cup�re le chemin courant SequenceListe<Position>

                posCaisseFutur = cheminCourant.extraitQueue();//derni�re position du chemin courant (future position de la caisse d�plac�e)
                posCaissePresent = cheminCourant.extraitQueue();//avant-derni�re position du chemin courant (position de la caisse � d�placer)
                posPousseur = cheminCourant.getQueue();//position du pousseur � c�t� de la caisse � d�placer

                byte[][] caissesNew = pousserCaisse(posCaissePresent, posCaisseFutur, caisses);
                Position posPousseurNew = posCaissePresent;//position de la caisse avant qu'elle soit pouss�e

                if(!estInstance(posPousseurNew, caissesNew, instances)){
                    cheminCourant.insereQueue(posPousseurNew);//on ajoute la nouvelle position du pousseur apr�s avoir pouss� la caisse
                    instanceCourante = new Instance(posPousseurNew, caissesNew);
                    int nb_caisses_sur_but = nbCaissesSurBut(caissesNew);
                    if(nb_caisses_sur_but == nb_caisses){
                        System.out.println("=========================== Toutes les caisses sont sur les buts ===========================");
                        arbreCourant = new ArbreChemins(instanceCourante, cheminCourant, arbreCheminsTete);
                        while(!instanceCourante.estInstance(instanceDepart)){
                            chemin.add(cheminCourant);
                            instanceCourante = arbreCourant.getPere().getCourant();
                            cheminCourant = arbreCourant.getPere().getChemin();
                            arbreCourant = arbreCourant.getPere();
                        }//cheminCourant est maintenant null, puisque c'est le chemin null qui a �t� ajout� en premier
                        ArrayList<SequenceListe<Position>> cheminInverse = new ArrayList<SequenceListe<Position>>();
                        for(int j=chemin.size()-1; j>=0; j--){
                            cheminInverse.add(chemin.get(j));
                        }
                        taille_file=queue.size();
                        return cheminInverse;
                    }else{
                        ajouterInstance(posPousseurNew, caissesNew, instances);
                        int rapprocheCaisseBut = rapprocheCaisseBut(posCaissePresent, posCaisseFutur, caissesNew);
                        queue.add(new ArbreChemins(instanceCourante, cheminCourant, arbreCheminsTete));
                    }
                }
            }//pas de solution pour ce chemin
            //System.out.println("nb instances: " + nb_instances);
        }
        return chemin;
    }

    public int rapprocheCaisseBut(Position posCaissePresent, Position posCaisseFutur, byte[][] caisses){
        //Dijkstra pour trouver le chemin le plus court entre la caisse et le but
        return 0;
    }

    private void afficheChemin(SequenceListe<Position> chemin) {
        while(!chemin.estVide()){
            Position pos = chemin.extraitTete();
            System.out.println("pos: " + pos.affiche());
        }
    }

    public boolean estBut(Position p){
        return (carte[p.l][p.c] & BUT) != 0;
    }

    public int nbCaissesSurBut(byte[][] caisses){
        int nbCaisseBut = 0;
        for(int i=0;i<caisses.length;i++){
            for(int j=0;j<caisses[0].length;j++){
                if(caisses[i][j] == CAISSE && carte[i][j]==BUT){
                    nbCaisseBut++;
                }
            }
        }
        return nbCaisseBut;
    }

    public byte[][] pousserCaisse(Position present, Position futur, byte[][] caisses){
        byte[][] caissesNew = copieByte(caisses);
        caissesNew[present.getL()][present.getC()] = VIDE;//on supprime la caisse de sa position actuelle
        caissesNew[futur.getL()][futur.getC()] = CAISSE;//on ajoute la caisse � sa nouvelle position
        return caissesNew;
    }

    public PositionPoids parcourtDistances(Position p, int[][] distance){
        int distSuivante = distance[p.l][p.c]-1;
        //System.out.println("Distance suivante : " + distSuivante);
        ArrayList<PositionPoids> casesAdjacentes = new ArrayList<>();
        PositionPoids posNord, posSud, posEst, posOuest;
        if(!estCaseHorsMap(p.l-1, p.c) && distance[p.l-1][p.c] == distSuivante) {
            posNord = new PositionPoids(p.l-1, p.c, distance[p.l-1][p.c]);
            casesAdjacentes.add(posNord);
        }
        if(!estCaseHorsMap(p.l+1, p.c) && distance[p.l+1][p.c] == distSuivante) {
            posSud = new PositionPoids(p.l+1, p.c, distance[p.l+1][p.c]);
            casesAdjacentes.add(posSud);
        }
        if(!estCaseHorsMap(p.l, p.c+1) && distance[p.l][p.c+1] == distSuivante) {
            posEst = new PositionPoids(p.l, p.c+1, distance[p.l][p.c+1]);
            casesAdjacentes.add(posEst);
        }
        if(!estCaseHorsMap(p.l, p.c-1) && distance[p.l][p.c-1] == distSuivante) {
            posOuest = new PositionPoids(p.l, p.c-1, distance[p.l][p.c-1]);
            casesAdjacentes.add(posOuest);
        }
        if(casesAdjacentes.size() > 1) {
            Collections.shuffle(casesAdjacentes);
        }
        if(casesAdjacentes.size() == 0) {
            return null;
        }else{//renvoie l'une des cases au hasard
            return casesAdjacentes.get(0);
        }
    }

    public LinkedList<SequenceListe<Position>> Dijkstra(Position pos, byte[][] caisses){
        startTime = System.currentTimeMillis();
        nb_fois_Dijkstra++;
        PositionPoids pousseur = new PositionPoids(pos.getL(), pos.getC(), 0);
        //une s�quence de caisses avec leur position avant et apr�s avoir �t� pouss�es
        SequenceListe<ArrayList<Position>> caisses_deplacables = new SequenceListe<ArrayList<Position>>();
        SequenceListe<ArrayList<Position>> caisses_deplacables_temp = new SequenceListe<ArrayList<Position>>();
        int[][] distance = new int[l][c];
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                distance[i][j] = INFINI;
            }
        }
        distance[pousseur.getL()][pousseur.getC()] = 0;
        boolean[][] visite = new boolean[l][c];
        for(int i = 0; i < visite.length; i++){
            for(int j = 0; j < visite[0].length; j++){
                visite[i][j] = false;
            }
        }
        visite[pousseur.getL()][pousseur.getC()] = true;
        FAPListe<PositionPoids> queue = new FAPListe<>();
        queue.insere(pousseur);

        //on met � jour le tableau des distances et les caisses d�pla�ables si on en trouve
        while(!queue.estVide()){
            //extrait le sommet de distance minimale
            PositionPoids p = queue.extrait();

            //mise � jour des caisses d�pla�ables
            caisses_deplacables_temp = caissesDeplacables(p.getPos(), caisses);//liste les caisses d�pla�ables depuis la position p
            //contient la position p, la position de la caisse, et la future position de la caisse
            while(!caisses_deplacables_temp.estVide()){
                ArrayList<Position> caisse = caisses_deplacables_temp.extraitTete();
                caisses_deplacables.insereTete(caisse);
            }
            //mise � jour des distances
            SequenceListe<PositionPoids> casesAccessibles = casesAccessibles(p, caisses);//renvoie les cases accessibles � c�t� du pousseur
            while(!casesAccessibles.estVide()){
                PositionPoids q = casesAccessibles.extraitTete();
                if(!visite[q.getL()][q.getC()]){//si la case accessible n'a pas �t� visit�e
                    visite[q.getL()][q.getC()] = true;
                    distance[q.getL()][q.getC()] = distanceMin(distance,q.getL(),q.getC()) + 1;
                    queue.insere(q);
                }
            }
        }
        //on a maintenant le tableau des distances et les caisses d�pla�ables
        FAPListe<SequenceListe<Position>> sequenceChemins = new FAPListe<>();
        FAPListe<SequenceListe<Position>> sequenceCheminsButs = new FAPListe<>();
        LinkedList<SequenceListe<Position>> sequence = new LinkedList<>();
        SequenceListe<Position> chemin = new SequenceListe<>();
        PositionPoids caseSuivante;
        ArrayList<Position> tete = null;

        while(!caisses_deplacables.estVide()){//reconstruction du chemin de la fin (position de la caisse) au d�but (position du pousseur au d�part)
            tete = caisses_deplacables.extraitTete();//nouveau chemin vers une caisse d�pla�able
            chemin.insereTete(tete.get(2));//la case sur laquelle sera d�plac�e la caisse est ajout�e au chemin courant
            chemin.insereTete(tete.get(1));//la case sur laquelle est la caisse est ajout�e au chemin courant
            chemin.insereTete(tete.get(0));//la case � c�t� de la caisse est ajout�e au chemin courant
            caseSuivante = parcourtDistances(tete.get(0), distance);
            if(caseSuivante == null){//si le pousseur est bloqu� (entour� de caisses par exemple)
                //on privil�gie les chemins qui m�nent la caisse sur un but
                ElementAvecPoids<SequenceListe<Position>> element = new ElementAvecPoids<>(chemin, chemin.taille());
                if(estBut(tete.get(2))){//si la position o� sera d�plac�e la caisse est un but, on ajoute le chemin au d�but de la s�quence
                    sequenceCheminsButs.insere(chemin);
                }else{
                    sequenceChemins.insere(chemin);
                }
                endTime = System.currentTimeMillis();
                duration += (endTime - startTime);
                nb_total_chemins += sequenceChemins.taille()+sequenceCheminsButs.taille();
                while(!sequenceCheminsButs.estVide()){
                    sequence.add(sequenceCheminsButs.extrait());
                }
                while(!sequenceChemins.estVide()){
                    sequence.add(sequenceChemins.extrait());
                }
                return sequence;
            }
            chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            while(caseSuivante.poids != 0){
                caseSuivante = parcourtDistances(new Position(caseSuivante.getL(), caseSuivante.getC()), distance);
                chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            }
            //on privil�gie les chemins qui m�nent la caisse sur un but
            ElementAvecPoids<SequenceListe<Position>> element = new ElementAvecPoids<>(chemin, chemin.taille());
            if(estBut(tete.get(2))){//si la position o� sera d�plac�e la caisse est un but, on ajoute le chemin au d�but de la s�quence
                sequenceCheminsButs.insere(chemin);
            }else{
                sequenceChemins.insere(chemin);
            }
            chemin = new SequenceListe<>();
        }
        endTime = System.currentTimeMillis();
        duration += (endTime - startTime);
        nb_total_chemins += sequenceChemins.taille()+sequenceCheminsButs.taille();
        while(!sequenceCheminsButs.estVide()){
            sequence.add(sequenceCheminsButs.extrait());
        }
        while(!sequenceChemins.estVide()){
            sequence.add(sequenceChemins.extrait());
        }
        return sequence;
    }

    public SequenceListe<ArrayList<Position>> caissesDeplacables(Position p, byte[][] caisses){
        //renvoie la liste des caisses d�pla�ables avec la position p, la position de la caisse, et la future position de la caisse
        SequenceListe<ArrayList<Position>> caissesDep = new SequenceListe<>();
        ArrayList<Position> caisseDeplacee = new ArrayList<>();
        Position pCaisse;
        pCaisse = getPosCaisse(p.l+1, p.c, caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l+1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l-1, p.c, caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            boolean bloquante_dessus = estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses));
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l-1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c+1, caisses);//si la caisse est � droite du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l, pCaisse.c+1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c-1, caisses);//si la caisse est � gauche du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l, pCaisse.c-1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        return caissesDep;
    }

    public boolean estAdjacentCaisse(PositionPoids p, byte[][] caisses){
        Position pCaisse;
        pCaisse = getPosCaisse(p.l+1, p.c, caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est en-dessous du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l+1, pCaisse.c)&&estCaseLibre(pCaisse.l+1, pCaisse.c, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l+1, pCaisse.c, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l+1, pCaisse.c));
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l-1, p.c, caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est au-dessus du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l-1, pCaisse.c)&&estCaseLibre(pCaisse.l-1, pCaisse.c, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l-1, pCaisse.c, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l-1, pCaisse.c));
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c+1, caisses);//si la caisse est � droite du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est � droite du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l, pCaisse.c+1)&&estCaseLibre(pCaisse.l, pCaisse.c+1, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l, pCaisse.c+1, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l, pCaisse.c+1));
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c-1, caisses);//si la caisse est � gauche du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est � gauche du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l, pCaisse.c-1)&&estCaseLibre(pCaisse.l, pCaisse.c-1, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l, pCaisse.c-1, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l, pCaisse.c-1));
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        return false;
    }

    private byte[][] supprimeCaisse(Position pCaisse, byte[][] caisses) {
        byte [][] caisses2 = copieByte(caisses);
        caisses2[pCaisse.l][pCaisse.c] = VIDE;
        return caisses2;
    }

    public int distanceMin(int distance[][], int l, int c){
        int posNord, posSud, posEst, posOuest;
        if(l+1 > distance.length-1){
            posSud = INFINI;
            posNord = distance[l-1][c];
        }else{
            posSud = distance[l + 1][c];
            if (l - 1 < 0){
                posNord = INFINI;
            }else{
                posNord = distance[l - 1][c];
            }
        }
        if(c+1 > distance[0].length-1){
            posEst = INFINI;
            int dist =  distance[0].length-1;
            posOuest = distance[l][c-1];
        }else{
            posEst = distance[l][c+1];
            if(c-1 < 0){
                posOuest = INFINI;
            }else{
                posOuest = distance[l][c-1];
            }
        }
        return Math.min(Math.min(posNord, posSud), Math.min(posEst, posOuest));
    }

    public SequenceListe<PositionPoids> casesAccessibles(PositionPoids posCourante, byte[][] caisses){
        SequenceListe<PositionPoids> cases = new SequenceListe<>();
        if(posCourante.l+1 <= l-1 && estCaseLibre(posCourante.l+1,posCourante.c, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l+1,posCourante.c, posCourante.poids+1));
        }
        if(posCourante.l-1 >= 0 && estCaseLibre(posCourante.l-1,posCourante.c, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l-1,posCourante.c, posCourante.poids+1));
        }
        if(posCourante.c+1 <= c-1 && estCaseLibre(posCourante.l,posCourante.c+1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l,posCourante.c+1, posCourante.poids+1));
        }
        if(posCourante.c-1 >= 0 && estCaseLibre(posCourante.l,posCourante.c-1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l,posCourante.c-1, posCourante.poids+1));
        }
        cases.insereQueue(posCourante);//la case courante est accessible puisque le pousseur est d�j� dessus
        return cases;
    }

    boolean estCaseHorsMap(int l, int c){
        return (l < 0 || l > this.l-1 || c < 0 || c > this.c-1);
    }

    boolean estCaseBloquante(int l, int c, byte[][] caisses){
        if(!estCaseHorsMap(l-1,c)&&!estCaseHorsMap(l,c-1)&&!estCaseHorsMap(l,c+1)&&!estCaseHorsMap(l+1,c)) {
            if((!estCaseLibre(l - 1, c, caisses) && (!estCaseLibre(l, c - 1, caisses) || !estCaseLibre(l, c + 1, caisses))) ||
                    (!estCaseLibre(l + 1, c, caisses) && (!estCaseLibre(l, c - 1, caisses) || !estCaseLibre(l, c + 1, caisses)))){
            }
        }
        return false;
    }

    public Position posDerriere(Position pousseur,Position caisse){
        int diff_l = caisse.l-pousseur.l;
        int diff_c = caisse.c-pousseur.c;
        return new Position(pousseur.l+diff_l*2, pousseur.c+diff_c*2);
    }

    public boolean estButDerriere(Position pousseur,Position caisse){
        return estBut(posDerriere(pousseur,caisse));
    }

    boolean estCaseLibre(int l, int c, byte[][] caisses){
        if(!aMur(l,c) && getPosCaisse(l,c,caisses)==null){
            return true;
        }else{
            return false;
        }
    }

    boolean aMur(int l, int c) {
        if(estCaseHorsMap(l,c)) return true;
        return (carte[l][c] & MUR) != 0;
    }

    public byte[][] copieByte( byte[][] caisses){
        byte[][] caisses2 = new byte[caisses.length][caisses[0].length];
        for(int i=0;i<caisses2.length;i++){
            for(int j=0;j<caisses2[0].length;j++){
                caisses2[i][j] = caisses[i][j];
            }
        }
        return caisses2;
    }

    public void ajouterInstance(Position p, byte[][] caisses, HashMap<Integer, byte[][]> instances){
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR ;
        //String posPousseur = p.getL() + "," + p.getC();
        //int clePousseur = posPousseur.hashCode();
        int cleInstance = Arrays.deepHashCode(instanceCopie)+p.affiche().hashCode();
        //int cle = clePousseur+cleInstance;
        //System.out.println("cle = " + cle);
        //afficheCaisses(instanceCopie);
        if(!instances.containsKey(cleInstance)){
            instances.put(cleInstance, instanceCopie);
            nb_instances++;
        }
    }

    public boolean estInstance(Position p, byte[][] caisses, HashMap<Integer, byte[][]> instances) {
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR;
        //String posPousseur = p.getL() + "," + p.getC();
        //int clePousseur = posPousseur.hashCode();
        int cleInstance = Arrays.deepHashCode(instanceCopie)+p.affiche().hashCode();
        //int cle = clePousseur+cleInstance;
        if(!instances.containsKey(cleInstance)){
            return false;
        }else{
            byte[][] instanceTrouvee = instances.get(cleInstance);
            for(int i = 0; i < instanceCopie.length; i++){
                for(int j = 0; j < instanceCopie[0].length; j++){
                    if(instanceCopie[i][j] != instanceTrouvee[i][j]){
                        return false;
                    }
                }
            }
            return true;
        }
    }

    void afficheCaisses(byte[][] caisses){
        for(int i=0; i<caisses.length; i++){
            for(int j=0; j<caisses[0].length; j++){
                System.out.print(caisses[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("-----------");
    }

    public Position getPosCaisse(int l, int c, byte[][] caisses){
        if(estCaseHorsMap(l,c)) return null;
        if(caisses[l][c]==CAISSE){
            return new Position(l, c);
        }
        return null;
    }

    public void afficheDistances(int[][] distance){
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                //si distance i j est �gal � infini
                if(distance[i][j] == INFINI) {
                    System.out.print("+00 ");
                }else{
                    if(distance[i][j]<10) {
                        System.out.print(" " + distance[i][j] + "  ");
                    }else{
                        System.out.print(distance[i][j] + "  ");
                    }
                }
            }
            System.out.println();
        }
        System.out.println("--------------------------------------");
    }

    public void afficheCarte(int[][] carte){
        for (int i = 0; i < carte.length; i++) {
            for (int j = 0; j < carte[i].length; j++) {
                switch (carte[i][j]) {
                    case MUR:
                        System.out.print("#");
                        break;
                    case POUSSEUR:
                        System.out.print("@");
                        break;
                    case CAISSE:
                        System.out.print("$");
                        break;
                    case BUT:
                        System.out.print(".");
                        break;
                    case VIDE:
                        System.out.print(" ");
                        break;
                    default:
                        System.out.println("Erreur de lecture de la carte ligne " + i + " colonne " + j);
                        System.exit(1);
                }
            }
            System.out.println();
        }
    }

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// CASE BLOQUANTE ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    public boolean aCaisseBloquee(int l, int c,byte[][] caisses){
        return (caisses[l][c] & CAISSE_BLOQUEE) != 0;
    }

    boolean aMurAutour(int l,int c){
        return aMur(l+1,c)||aMur(l-1,c)||aMur(l,c+1)||aMur(l,c-1);
    }
    boolean aBloqueeAutour(int l, int c){
        return aCaisseBloquee(l+1,c,caisses)||aCaisseBloquee(l-1,c,caisses)||aCaisseBloquee(l,c+1,caisses)||aCaisseBloquee(l,c-1,caisses);
    }
    boolean aAccesBut(int l, int c,byte[][] caisses){
        int i=0;
        boolean gauche=false,droite=false,bas=false,haut=false;
        while(!aMur(l,c+i) && droite==false && !aCaisseBloquee(l,c+i,caisses)) { // � droite
            if (!estCaseHorsMap(l, c - 1)) { // pas sur
                if (estBut(new Position(l,c+i)) && carte[l][c - 1] != MUR) droite = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l,c-i) && gauche==false && !aCaisseBloquee(l,c-i,caisses)){ // � gauche
            if(!estCaseHorsMap(l,c+1)){ // pas sur
                if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR) gauche = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l-i,c) && haut==false && !aCaisseBloquee(l-i,c,caisses)){ // en haut
            if(!estCaseHorsMap(l+1,c)){ // pas sur
                if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR) haut = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l+i,c) && droite==false && !aCaisseBloquee(l+i,c,caisses)){ // en bas
            if(!estCaseHorsMap(l-1,c)){// pas sur
                if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR) bas = true;
            }
            i++;
        }
        i=0;
        return gauche||droite||bas||haut;
    }
    boolean bloqueeVerticalDroit(int l,int c, int nord, byte[][] caisses){
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        int i=0;
        if(nord==1){
            while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l+1,c+i)) return true;
                if(estCaseHorsMap(l-1,c+i)){
                    if(estBut(new Position(l,c+i)) && carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-1,c+i,caisses)|| estBut(new Position(l,c+i))||caisses[l-1][c+i]==CAISSE)&& carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l-1,c+i)) return true;
                if(estCaseHorsMap(l+1,c+i)){
                    if(estBut(new Position(l,c+i)) && carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR) return false;
                }
                else{
                    if((estCaseLibre(l+1,c+i,caisses)|| estBut(new Position(l,c+i))||caisses[l+1][c+i]==CAISSE)&& carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeVerticalGauche(int l,int c, int nord,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(nord==1){
            while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l+1,c-i)) return true;
                if(estCaseHorsMap(l-1,c-i)){
                    if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-1,c-i,caisses)|| estBut(new Position(l,c-i))||caisses[l-1][c-i]==CAISSE)&& carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l-1,c-i)) return true;
                if(estCaseHorsMap(l+1,c-i)){
                    if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l+1,c-i,caisses) || estBut(new Position(l,c-i))||caisses[l+1][c-i]==CAISSE)&& carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalHaut(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(ouest==1){
            while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c+1)) return true;
                if(estCaseHorsMap(l-i,c-1)){
                    if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-i,c-1,caisses)||estBut(new Position(l-i,c))||caisses[l-i][c-1]==CAISSE) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c-1)) return true;
                if(estCaseHorsMap(l-i,c+1)){
                    if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-i,c+1,caisses) || estBut(new Position(l-i,c))||caisses[l-i][c+1]==CAISSE)&& carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalBas(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(ouest==1){
            while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c+1)) return true;
                if(estCaseHorsMap(l+i,c-1)){
                    if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR) return false;
                }
                else{
                    if((estCaseLibre(l+i,c-1,caisses) || estBut(new Position(l+i,c))||caisses[l+i][c-1]==CAISSE)&& carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR) return false;
                }

                i++;
            }
            return true;
        }else{
            while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c-1)) return true;
                if(estCaseHorsMap(l+i,c+1)){
                    if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l+i,c+1,caisses) || estBut(new Position(l+i,c))||caisses[l+i][c+1]==CAISSE)&& carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean gestionPlusieurTemp(int l, int c,byte[][] caisses){
        if(estCaisseBloqueeTemp(l+1,c,caisses)&&!aMurAutour(l+1,c)) return false;
        if(estCaisseBloqueeTemp(l-1,c,caisses)&&!aMurAutour(l-1,c)) return false;
        if(estCaisseBloqueeTemp(l,c+1,caisses)&&!aMurAutour(l,c+1)) return false;
        if(estCaisseBloqueeTemp(l,c-1,caisses)&&!aMurAutour(l,c-1)) return false;

        return estCaisseBloqueeTemp(l,c+1,caisses) || estCaisseBloqueeTemp(l,c-1,caisses) || estCaisseBloqueeTemp(l+1,c,caisses) || estCaisseBloqueeTemp(l-1,c,caisses); // || cases[l][c+1]==CAISSE_BLOQUEE || cases[l][c-1]==CAISSE_BLOQUEE || cases[l+1][c]==CAISSE_BLOQUEE || cases[l-1][c]==CAISSE_BLOQUEE;
    }

    boolean estCaisseBloquee(int l, int c,byte[][] caisses){
        if(aMur(l,c) || estCaseLibre(l,c,caisses)) return false;
        if(!estBut(new Position(l,c))) {
            // CAS CAISSE TEMPORAIRE
            if(estCaisseBloqueeTemp(l,c,caisses)){
                if(!aMurAutour(l,c)) return false;//&&!aBloqueeAutour(l,c)) return false;
                return gestionPlusieurTemp(l,c,caisses);
            }

            // CAS CONDUITS
            if(aMur(l-1,c)&&aMur(l+1,c)) return (bloqueeVerticalDroit(l,c,0,caisses)||bloqueeVerticalDroit(l,c,1,caisses)) && (bloqueeVerticalGauche(l,c,0,caisses)||bloqueeVerticalGauche(l,c,1,caisses)); //conduit horizontale
            if(aMur(l,c-1)&&aMur(l,c+1)) return (bloqueeHorizontalBas(l,c,0,caisses)||bloqueeHorizontalBas(l,c,1,caisses)) && (bloqueeHorizontalHaut(l,c,0,caisses)||bloqueeHorizontalHaut(l,c,1,caisses)); //conduit verical

            // CAS GENERALS
            if(aMur(l-1,c)) return bloqueeVerticalDroit(l,c,1,caisses)&&bloqueeVerticalGauche(l,c,1,caisses); // mur dessus de la caisse
            if(aMur(l+1,c)) return bloqueeVerticalDroit(l,c,0,caisses)&&bloqueeVerticalGauche(l,c,0,caisses); // mur dessous de la caisse
            if(aMur(l,c-1)) return bloqueeHorizontalHaut(l,c,1,caisses)&&bloqueeHorizontalBas(l,c,1,caisses); // mur a gauche de la caisse
            if(aMur(l,c+1)) return bloqueeHorizontalHaut(l,c,0,caisses)&&bloqueeHorizontalBas(l,c,0,caisses); // mur a droite de la caisse
        }
        return false;
    }
    boolean estCaisseBloqueeTemp(int l,int c,byte[][] caisses){
        if(estCaseHorsMap(l,c)) return false;
        if(caisses[l][c]!=CAISSE && caisses[l][c]!=CAISSE_BLOQUEE_TEMP && carte[l][c]!=MUR && caisses[l][c]!=VIDE) return false;
        if(aMur(l,c)) return false;
        if(estCaseLibre(l,c,caisses))return false;
        if(!estBut(new Position(l,c))){
            if(!estCaseLibre(l-1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMur(l-1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
                return true;
            }
            if(!estCaseLibre(l+1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMur(l+1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
                return true;
            }
        }
        return false;
    }

    boolean estCaseBloquante_V2(int l_initial, int c_initial, int l, int c,byte[][] caisses){
        if(aMur(l,c)) return true;

        byte[][] saveCaisses = caisses;
        //System.out.println("l: "+l+" c: "+c+" cases[l][c]: "+ cases[l][c]);
        saveCaisses[l][c] = CAISSE;
        saveCaisses[l_initial][c_initial] = VIDE;
        actualiseToutesCaisses(saveCaisses);

        if(saveCaisses[l][c]==16){
            return true;
        }
        return false;
    }
    public boolean aCaisse(int l, int c,byte[][] caisses){
        return (caisses[l][c] & CAISSE) != 0  || (caisses[l][c] & CAISSE_BLOQUEE_TEMP) != 0; // || (cases[l][c] & CAISSE_BLOQUEE) != 0;
    }
    void actualiseUneCaisse(int l, int c,byte[][] caisses){
        if (aCaisse(l,c,caisses)){
            //if(cases[l][c]==CAISSE_BLOQUEE) return;
            if(estBut(new Position(l,c))){
                return;
            }
            if (estCaisseBloquee(l,c,caisses)){
                caisses[l][c] = CAISSE_BLOQUEE; //FAUDRA TERMINER L'INSTANCE ! A FAIRE
                return;
            }
            if (estCaisseBloqueeTemp(l,c,caisses)) {
                caisses[l][c] = CAISSE_BLOQUEE_TEMP;
                return;
            }
            caisses[l][c] = CAISSE;
        }
    }

    public void actualiseToutesCaisses(byte[][] caisses){
        for(int l=0;l<caisses.length;l++){
            for(int c=0;c<caisses[0].length;c++){
                actualiseUneCaisse(l,c,caisses);
            }
        }
    }
    void actualiseCaisses(int l, int c,byte[][] caisses){
        actualiseUneCaisse(l+1,c,caisses);
        actualiseUneCaisse(l-1,c,caisses);
        actualiseUneCaisse(l,c+1,caisses);
        actualiseUneCaisse(l,c-1,caisses);
    }






}
