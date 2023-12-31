package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.SequenceListe;
import Structures.FAPListe;

import java.math.BigInteger;
import java.util.*;

import static Modele.Niveau.*;

class IAResolveur extends IA {
    private HashMap<BigInteger, byte[][]> instances;
    private int[][] carte;
    private byte[][] caisses;
    private Position posPousseur;
    private int l, c;
    private int nb_buts, nb_caisses;
    private ArrayList<Position> buts;
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
        buts = new ArrayList<>();
        //supprime la 1ere ligne, la derni�re ligne, la 1ere colonne, la derni�re colonne de cases
        for(int i = 1; i < l+1; i++) {
            for (int j = 1; j < c + 1; j++) {
                if(cases[i][j] == VIDE){
                }
                if (((cases[i][j] & MUR) != 0 || (cases[i][j] & VIDE) != 0)) {
                    carte[i - 1][j - 1] = cases[i][j];
                    //ajout de 0 � la fin de la chaine binaire
                }
                else if((cases[i][j] & BUT) != 0){
                    carte[i - 1][j - 1] = BUT;
                    this.buts.add(new Position(i - 1, j - 1));
                    if (((cases[i][j] & CAISSE) != 0) || ((cases[i][j] & CAISSE_BLOQUEE_TEMP ) != 0)){
                        this.nb_caisses++;
                        this.caisses[i - 1][j - 1] = CAISSE;
                    }
                    this.nb_buts++;
                    if((cases[i][j] & POUSSEUR) != 0) {
                        posPousseur = new Position(i - 1, j - 1);
                    }
                }
                else if (((cases[i][j] & CAISSE ) != 0) || ((cases[i][j] & CAISSE_BLOQUEE_TEMP ) != 0)) {
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

        instances = new HashMap<>();
        nb_instances = 0;
        nb_buts = 0;
        nb_caisses = 0;
        niveauToCarte(niveau);
        if (nb_buts != nb_caisses) {
            Configuration.erreur("Niveau impossible � r�soudre : le nombre de caisses est diff�rent du nombre de buts.");
            return null;
        }
        //initialiseButDansCoin();
        //afficheCaisses(butDansCoin);
        startTime_total = System.currentTimeMillis();
        chemins = calcul_chemin(posPousseur, caisses);
        endTime_total = System.currentTimeMillis();
        duration = (endTime_total - startTime_total);
        System.out.println("Temps total : " + duration + " ms");
/*
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
*/
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
        SequenceListe<Position> cheminCopie = new SequenceListe<Position>();
        SequenceListe<SequenceListe<Position>> chemins_caisse_buts = new SequenceListe<SequenceListe<Position>>();
        FAPListe<ArbreChemins> queue = new FAPListe<ArbreChemins>();
        ArbreChemins arbreCourant = null;
        Instance instanceCourante = null;
        Position posCaisseFutur = null;
        Position posCaissePresent = null;
        int poids = 0; int ancien_poids = 0;
        //byte[][] butDansCoin = initialiseButDansCoin();
        //int nombreCaisseDansCoin = 0;

        ajouterInstance(posPousseur, caisses, instances);
        Instance instanceDepart = new Instance(posPousseur, caisses);
        ArbreChemins arbreCheminsTete = new ArbreChemins(instanceDepart, null, null, nb_caisses);

        queue.insere(arbreCheminsTete);

        while(!queue.estVide()){
            /*
            if(queue.taille()%50000==0) {
                System.out.println("queue taille: " + queue.taille());
                System.out.println("nb instances: " + nb_instances);
            }
             */
            ArbreChemins arbreCheminsAvant = arbreCheminsTete;
            //System.out.println("\n\n ///// NOUVELLE QUEUE ///// \n\n");
            arbreCheminsTete = queue.extrait();//ArbreChemins

            //r�cup�re l'instance courante qui contient la position du pousseur et les caisses
            posPousseur = arbreCheminsTete.getCourant().getPosPousseur();
            caisses = arbreCheminsTete.getCourant().getCaisses();

            //r�cup�re les chemins possibles pour le pousseur depuis l'instance courante
            FAPListe<SequenceListe<Position>> cheminsPousseurCaisse = dijkstra(posPousseur, caisses);

            //System.out.println("cheminsPousseurCaisse taille: " + cheminsPousseurCaisse.taille());

            //pour chaque chemin possible du pousseur � une caisse
            while(!cheminsPousseurCaisse.estVide()){

                cheminCourant = cheminsPousseurCaisse.extrait();//on r�cup�re le chemin courant SequenceListe<Position>

                posCaisseFutur = cheminCourant.extraitQueue();//derni�re position du chemin courant (future position de la caisse d�plac�e)
                posCaissePresent = cheminCourant.extraitQueue();//avant-derni�re position du chemin courant (position de la caisse � d�placer)

                //int nombreCaisseDansCoinAvant = nombreCaisseCoin(butDansCoin,caisses);
                //butDansCoin = actualiseButDansCoin(posCaisseFutur.getL(),posCaisseFutur.getC(),butDansCoin);

                byte[][] caissesNew = pousserCaisse(posCaissePresent, posCaisseFutur, caisses);

                /*
                SequenceListe<SequenceListe<Position>> chemins_caisse_buts = cheminsCaisseButs(posCaissePresent, posCaisseFutur, caissesNew, buts);
                if(!chemins_caisse_buts.estVide()) {
                    SequenceListe<Position> cheminCourant_2 = chemins_caisse_buts.extraitTete();
                    if(cheminCourant_2.estVide()){
                        System.out.println("==================== cheminCourant_2 est null ====================");
                    }else{
                        cheminCourant.insereQueue(posCaissePresent);
                        chemin.add(cheminCourant);
                    }
                    cheminCourant_2.insereTete(posCaissePresent);
                    chemin.add(cheminCourant_2);
                    return chemin;
                }else{
                    return null;
                }
                 */
                //int nombreCaisseDansCoinApres = nombreCaisseCoin(butDansCoin,caissesNew);

                Position posPousseurNew = posCaissePresent;//position de la caisse avant qu'elle soit pouss�e

                if(!estInstance(posPousseurNew, caissesNew, instances)){
                    cheminCourant.insereQueue(posPousseurNew);//on ajoute la nouvelle position du pousseur apr�s avoir pouss� la caisse
                    instanceCourante = new Instance(posPousseurNew, caissesNew);
                    int nb_caisses_sur_but = nbCaissesSurBut(caissesNew);

                    if(nb_caisses_sur_but == nb_caisses){
                        System.out.println(niveau.nom()+" : =================== Toutes les caisses sont sur les buts ===================");
                        arbreCourant = new ArbreChemins(instanceCourante, cheminCourant, arbreCheminsTete, 0);
                        while (!instanceCourante.estInstance(instanceDepart)) {
                            chemin.add(cheminCourant);
                            instanceCourante = arbreCourant.getPere().getCourant();
                            cheminCourant = arbreCourant.getPere().getChemin();
                            arbreCourant = arbreCourant.getPere();
                        }//cheminCourant est maintenant null, puisque c'est le chemin null qui a �t� ajout� en premier
                        ArrayList<SequenceListe<Position>> cheminInverse = new ArrayList<SequenceListe<Position>>();
                        for (int j = chemin.size() - 1; j >= 0; j--) {
                            cheminInverse.add(chemin.get(j));
                        }
                        taille_file = queue.taille();
                        return cheminInverse;
                    } else {
                        ajouterInstance(posPousseurNew, caissesNew, instances);
                        /*
                        cheminCopie = new SequenceListe<Position>();
                        cheminCopie = cheminCourant;//SequenceListe<Position>
                        chemins_caisse_buts = cheminsCaisseButs(posCaissePresent, posCaisseFutur, caissesNew, buts);
                        while(!chemins_caisse_buts.estVide()){
                            SequenceListe<Position> cheminCourant_2 = chemins_caisse_buts.extraitTete();
                            if(!cheminCourant_2.estVide()) {
                                //cheminCourant_2.insereTete(posCaissePresent);
                                //cheminCopie.insereQueue(posCaissePresent);
                                while(!cheminCourant_2.estVide()){//on ajoute le nouveau chemin de la caisse au but au chemin actuel
                                    cheminCopie.insereQueue(cheminCourant_2.extraitTete());
                                }
                                System.out.println("cheminCopie : ");
                                cheminCopie=afficheChemin(cheminCopie);
                                poids = nb_caisses - nb_caisses_sur_but - 1;
                                ArbreChemins arbreEnfile = new ArbreChemins(instanceCourante, cheminCopie, arbreCheminsTete, poids);
                                queue.insere(arbreEnfile);
                            }
                            cheminCopie = new SequenceListe<Position>();
                            cheminCopie = cheminCourant;//SequenceListe<Position>
                        }
                        */
                        poids = nb_caisses - nb_caisses_sur_but;
                        /*
                        if(nombreCaisseDansCoinAvant<nombreCaisseDansCoinApres){
                            poids-=(nombreCaisseDansCoinApres*5);
                        }
                         */
                        ancien_poids = arbreCheminsAvant.getPoids();
                        ArbreChemins arbreEnfile = new ArbreChemins(instanceCourante, cheminCourant, arbreCheminsTete, poids);
                        if (poids < ancien_poids) {
                            queue.insere(arbreEnfile);
                        } else {
                            queue.insereQueue(arbreEnfile);
                        }
                    }
                }
            }//pas de solution pour ce chemin
        }
        return chemin;
    }

    public int rapprocheCaisseBut(Position posCaissePresent, Position posCaisseFutur, byte[][] caisses){
        int lignePresent = posCaissePresent.getL();
        int colonnePresent = posCaissePresent.getC();
        int ligneFutur = posCaisseFutur.getL();
        int colonneFutur = posCaisseFutur.getC();
        SequenceListe<Position> sequenceButs = new SequenceListe<Position>();
        for(int i=0;i<caisses.length;i++){//pour chaque but libre
            for(int j=0;j<caisses[0].length;j++){
                if(caisses[i][j] != CAISSE && (carte[i][j] & BUT) != 0){
                    sequenceButs.insereQueue(new Position(i,j));
                }
            }
        }
        int rapproche = 0;
        while(!sequenceButs.estVide()){
            Position posBut = sequenceButs.extraitTete();
            int ligneBut = posBut.getL();
            int colonneBut = posBut.getC();
            if((Math.abs(ligneBut - lignePresent) + Math.abs(colonneBut - colonnePresent))>(Math.abs(ligneBut - ligneFutur) + Math.abs(colonneBut - colonneFutur))) { // si le present est plus loin que le futur
                rapproche++;
            }else{
                rapproche--; // On diminue meme si egale car perte de temps
            }
        }
        if(rapproche>0){
            return 1;
        }
        return 0;
    }

    boolean estButDansCoin(int i,int j,byte[][] butDansCoin){
        return butDansCoin[i][j]==1;
    }

    byte[][] initialiseButDansCoin(){
        byte[][] butDansCoin = new byte[carte.length][carte[0].length];
        for(int i=0;i< carte.length;i++){
            for(int j=0;j<carte[0].length;j++){
                if(!estCaseHorsMap(i,j)){
                    if(carte[i][j]==BUT){
                        if(estCaseHorsMap(i-1,j)){
                            if(estCaseHorsMap(i,j+1)) butDansCoin[i][j] = 1;
                            if(estCaseHorsMap(i,j-1)) butDansCoin[i][j] = 1;
                        }
                        else if(estCaseHorsMap(i+1,j)){
                            if(estCaseHorsMap(i,j+1)) butDansCoin[i][j] = 1;
                            if(estCaseHorsMap(i,j-1)) butDansCoin[i][j] = 1;
                        }
                        else if(!estCaseHorsMap(i,j-1)&&!estCaseHorsMap(i,j+1)){
                            if (carte[i-1][j]==MUR && (carte[i][j+1]==MUR||carte[i][j-1]==MUR)) butDansCoin[i][j] = 1;
                            if (carte[i+1][j]==MUR && (carte[i][j+1]==MUR||carte[i][j-1]==MUR)) butDansCoin[i][j] = 1;
                        }
                    }
                }
            }
        }
        return butDansCoin;
    }

    byte[][] actualiseButDansCoin(int i,int j,byte[][] butDansCoin){ // on vient de pousser une caisse sur la case i j
        if(estButDansCoin(i,j,butDansCoin)){
            //butDansCoin[i][j] = 0;
            if(!estCaseHorsMap(i,j+1) && carte[i][j+1]==BUT) butDansCoin[i][j+1] = 1;
            if(!estCaseHorsMap(i,j-1) && carte[i][j-1]==BUT) butDansCoin[i][j-1] = 1;
            if(!estCaseHorsMap(i+1,j) && carte[i+1][j]==BUT) butDansCoin[i+1][j] = 1;
            if(!estCaseHorsMap(i-1,j) && carte[i-1][j]==BUT) butDansCoin[i-1][j] = 1;
        }
        return butDansCoin;
    }

    int nombreCaisseCoin(byte[][] butDansCoin,byte[][] caisses){
        int nb=0;
        for(int i=0;i< butDansCoin.length;i++) {
            for (int j = 0; j < butDansCoin[0].length; j++) {
                if(caisses[i][j]==CAISSE) nb+=butDansCoin[i][j];
            }
        }
        return nb;
    }

    private SequenceListe<Position> afficheChemin(SequenceListe<Position> chemin) {
        SequenceListe<Position> chemin_copy = new SequenceListe<>();

        while(!chemin.estVide()){
            Position pos = chemin.extraitTete();
            chemin_copy.insereQueue(pos);
            System.out.println("pos: " + pos.affiche());
        }

        System.out.println("----------------");
        return chemin_copy;
    }

    public boolean estBut(Position p){
        return (carte[p.getL()][p.getC()] & BUT) != 0;
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
        int i = p.getL();
        int j = p.getC();
        int distSuivante = distance[i][j]-1;
        //System.out.println("Distance suivante : " + distSuivante);
        ArrayList<PositionPoids> casesAdjacentes = new ArrayList<>();
        PositionPoids posNord, posSud, posEst, posOuest;
        if(!estCaseHorsMap(i-1, j) && distance[i-1][j] == distSuivante) {
            posNord = new PositionPoids(i-1, j, distance[i-1][j]);
            casesAdjacentes.add(posNord);
        }
        if(!estCaseHorsMap(i+1, j) && distance[i+1][j] == distSuivante) {
            posSud = new PositionPoids(i+1, j, distance[i+1][j]);
            casesAdjacentes.add(posSud);
        }
        if(!estCaseHorsMap(i, j+1) && distance[i][j+1] == distSuivante) {
            posEst = new PositionPoids(i, j+1, distance[i][j+1]);
            casesAdjacentes.add(posEst);
        }
        if(!estCaseHorsMap(i, j-1) && distance[i][j-1] == distSuivante) {
            posOuest = new PositionPoids(i, j-1, distance[i][j-1]);
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

    public FAPListe<SequenceListe<Position>> dijkstra(Position pos, byte[][] caisses){
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
        SequenceListe<PositionPoids> queue = new SequenceListe<>();
        queue.insereTete(pousseur);

        //on met � jour le tableau des distances et les caisses d�pla�ables si on en trouve
        while(!queue.estVide()){
            //extrait le sommet de distance minimale
            PositionPoids p = queue.extraitTete();

            //mise � jour des caisses d�pla�ables
            caisses_deplacables_temp = caissesDeplacables(p.getPos(), caisses);//liste les caisses d�pla�ables depuis la position p
            //contient la position p, la position de la caisse, et la future position de la caisse
            while(!caisses_deplacables_temp.estVide()){
                ArrayList<Position> caisse = caisses_deplacables_temp.extraitTete();
                caisses_deplacables.insereQueue(caisse);
            }
            //mise � jour des distances
            SequenceListe<PositionPoids> cases_accessibles = casesAccessibles(p, caisses);//renvoie les cases accessibles � c�t� du pousseur
            while(!cases_accessibles.estVide()) {
                PositionPoids q = cases_accessibles.extraitTete();
                int i = q.getL();
                int j = q.getC();
                if (!visite[i][j]) {//si la case accessible n'a pas �t� visit�e
                    visite[i][j] = true;
                    int d = distanceMin(distance, i, j) + 1;
                    distance[i][j] = d;
                    q.setPoids(d);
                    queue.insereQueue(q);
                }
            }
        }
        //on a maintenant le tableau des distances et les caisses d�pla�ables
        FAPListe<SequenceListe<Position>> sequence = new FAPListe<>();
        SequenceListe<Position> chemin = new SequenceListe<>();
        PositionPoids caseSuivante;
        ArrayList<Position> tete = null;

        while(!caisses_deplacables.estVide()){//reconstruction du chemin de la fin (position de la caisse) au d�but (position du pousseur au d�part)
            tete = caisses_deplacables.extraitTete();//nouveau chemin vers une caisse d�pla�able
            chemin.insereTete(tete.get(2));//la case sur laquelle sera d�plac�e la caisse est ajout�e au chemin courant
            chemin.insereTete(tete.get(1));//la case sur laquelle est la caisse est ajout�e au chemin courant
            chemin.insereTete(tete.get(0));//la case � c�t� de la caisse est ajout�e au chemin courant
            caseSuivante = parcourtDistances(tete.get(0), distance);
            if(caseSuivante != null){//si le pousseur n'est pas bloqu� (entour� de caisses par exemple)
                chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
                while (caseSuivante.getPoids() != 0) {
                    int i = caseSuivante.getL();
                    int j = caseSuivante.getC();
                    caseSuivante = parcourtDistances(new Position(i, j), distance);
                    chemin.insereTete(caseSuivante.getPos());
                }
            }
            sequence.insere(chemin);
            chemin = new SequenceListe<>();
        }
        endTime = System.currentTimeMillis();
        duration += (endTime - startTime);
        nb_total_chemins += sequence.taille();
        return sequence;
    }

    public int distanceManatthan(Position a, Position b){
        return Math.abs(b.getL() - a.getL()) + Math.abs(b.getC() - a.getC());
    }

    public SequenceListe<Position> aEtoileCaisseBut(Position posCaisse, Position posPousseur, Position posBut, byte[][] caisses){
        if(posCaisse.egal(posBut)) return null;

        //System.out.println("pousseur : " + posPousseur.affiche());
        //System.out.println("caisse : " + posCaisse.affiche());
        PositionPoids caisse = new PositionPoids(posCaisse.getL(), posCaisse.getC(), 0);
        PositionPoids pousseur = new PositionPoids(posPousseur.getL(), posPousseur.getC(), 0);

        boolean fin = false;
        int[][] distance = new int[l][c];
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                distance[i][j] = INFINI;
            }
        }
        boolean[][] visite = new boolean[l][c];
        for(int i = 0; i < visite.length; i++){
            for(int j = 0; j < visite[0].length; j++){
                visite[i][j] = false;
            }
        }
        distance[caisse.getL()][caisse.getC()] = 0;
        visite[caisse.getL()][caisse.getC()] = true;
        visite[pousseur.getL()][pousseur.getC()] = true;
        SequenceListe<ArrayList<PositionPoids>> queue = new SequenceListe<>();
        ArrayList<PositionPoids> q = new ArrayList<>();
        q.add(caisse);
        q.add(pousseur);
        queue.insereTete(q);

        while(!queue.estVide() && !fin){
            q = queue.extraitTete();
            caisse = q.get(0);
            posPousseur = q.get(1).getPos();

            Position caissePos = new Position(caisse.getL(),caisse.getC());
            byte[][] caisses_actuel = new byte[caisses.length][caisses[0].length];
            caisses_actuel[caissePos.getL()][caissePos.getC()] = CAISSE;
            //mise � jour des distances
            SequenceListe<PositionPoids> casesAccessibles = casesAccessiblesManatthan(caisse, posPousseur, posBut, caisses_actuel);//renvoie les cases accessibles non bloquantes � c�t� de la caisse

            while(!casesAccessibles.estVide()){
                PositionPoids caseAccessible = casesAccessibles.extraitTete();
                int i = caseAccessible.getL();
                int j = caseAccessible.getC();

                if (!visite[i][j]) {//si la case accessible � c�t� de la caisse n'a pas �t� visit�e
                    visite[i][j] = true;
                    int d = distanceMin(distance, i, j) + 1;
                    distance[i][j] = d;
                    caseAccessible.setPoids(d);
                    ArrayList<PositionPoids> a = new ArrayList<>();
                    a.add(caseAccessible);
                    a.add(caisse);
                    queue.insereQueue(a);
                }
                if (posBut.getL() == i && posBut.getC() == j){
                    fin = true;
                }
            }
        }
        if(distance[posBut.getL()][posBut.getC()] == INFINI) return null;
        //on a maintenant le tableau des distances de la caisse jusqu'au but
        SequenceListe<Position> sequence = new SequenceListe<>();
        PositionPoids caseSuivante = parcourtDistances(posBut, distance);
        if(caseSuivante != null){
            sequence.insereTete(posBut);
            while(caseSuivante != null && caseSuivante.getPoids() != 0){
                int i = caseSuivante.getL();
                int j = caseSuivante.getC();
                caseSuivante = parcourtDistances(new Position(i, j), distance);
                if(!estCaseBloquante_V2(-1,-1,i,j,caisses,posPousseur)) sequence.insereTete(new Position(i, j));
            }
        }else{//pas de chemin existant de la caisse au but
            return null;
        }
        return sequence;
    }

    public SequenceListe<Position> dijkstraPousseurDerriereCaisse(Position posPousseur, Position dest, byte[][] caisses){
        PositionPoids p = new PositionPoids(posPousseur.getL(), posPousseur.getC(), 0);
        boolean fin = false;
        int[][] distance = new int[l][c];
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                distance[i][j] = INFINI;
            }
        }
        boolean[][] visite = new boolean[l][c];
        for(int i = 0; i < visite.length; i++){
            for(int j = 0; j < visite[0].length; j++){
                visite[i][j] = false;
            }
        }
        distance[posPousseur.getL()][posPousseur.getC()] = 0;
        visite[posPousseur.getL()][posPousseur.getC()] = true;
        SequenceListe<PositionPoids> queue = new SequenceListe<>();
        queue.insereTete(p);

        while(!queue.estVide() && !fin){
            p = queue.extraitTete();

            //mise � jour des distances
            SequenceListe<PositionPoids> cases_accessibles = casesAccessiblesPousseurDerriereCaisseManatthan(p.getPos(), dest, caisses);//renvoie les cases accessibles � c�t� du pousseur
            while(!cases_accessibles.estVide()){
                PositionPoids caseAccessible = cases_accessibles.extraitTete();
                int i = caseAccessible.getL();
                int j = caseAccessible.getC();
                //System.out.println("Pousseur i: "+posPousseur.getL()+" j: "+posPousseur.getC());
                //System.out.println("case accessible i: "+i+ " j: "+j);
                if (!visite[i][j]) {//si la case accessible � c�t� de la caisse n'a pas �t� visit�e
                    visite[i][j] = true;
                    int d = distanceMin(distance, i, j) + 1;
                    distance[i][j] = d;
                    caseAccessible.setPoids(d);
                    queue.insereQueue(caseAccessible);
                }
                if(dest.egal(new Position(i, j))){
                    fin = true;
                }
            }
        }
        //on a maintenant le tableau des distances du pousseur jusqu'� la destination
        SequenceListe<Position> sequence = new SequenceListe<>();
        PositionPoids caseSuivante = parcourtDistances(dest, distance);
        if(caseSuivante != null){
            sequence.insereTete(dest);
            while(caseSuivante != null && caseSuivante.getPoids() != 0){
                int i = caseSuivante.getL();
                int j = caseSuivante.getC();
                caseSuivante = parcourtDistances(new Position(i, j), distance);
                sequence.insereTete(new Position(i, j));
            }
        }else{//pas de chemin existant du pousseur � la destination
            return null;
        }
        return sequence;
    }

    boolean estBloqueEnHautDijkstra(Position caisseFutur, int gauche, byte[][] caisses){
        if(estCaseLibre(caisseFutur.getL()-1,caisseFutur.getC(),caisses)) return false;
        int ligne = caisseFutur.getL();
        int colonne = caisseFutur.getC();
        int i=0;
        if(gauche==1){
            while(estCaseLibre(ligne,colonne-i,caisses)){
                if(estCaseLibre(ligne-1,colonne-i,caisses)&&estCaseLibre(ligne+1,colonne-i,caisses)) return false;
                i++;
            }
            return true;
        }else{
            while(estCaseLibre(ligne,colonne+i,caisses)){
                if(estCaseLibre(ligne-1,colonne+i,caisses)&&estCaseLibre(ligne+1,colonne+i,caisses)) return false;
                i++;
            }
            return true;
        }
    }

    boolean estBloqueEnBasDijkstra(Position caisseFutur, int gauche, byte[][] caisses){
        if(estCaseLibre(caisseFutur.getL()+1,caisseFutur.getC(),caisses)) return false;
        int ligne = caisseFutur.getL();
        int colonne = caisseFutur.getC();
        int i=0;
        if(gauche==1){
            while(estCaseLibre(ligne,colonne-i,caisses)){
                if(estCaseLibre(ligne+1,colonne-i,caisses)&&estCaseLibre(ligne-1,colonne-i,caisses)) return false;
                i++;
            }
            return true;
        }else{
            while(estCaseLibre(ligne,colonne+i,caisses)){
                if(estCaseLibre(ligne+1,colonne+i,caisses)&&estCaseLibre(ligne-1,colonne+i,caisses)) return false;
                i++;
            }
            return true;
        }
    }

    boolean estBloqueDroiteDijkstra(Position caisseFutur, int haut, byte[][] caisses){
        if(estCaseLibre(caisseFutur.getL(),caisseFutur.getC()+1,caisses)) return false;
        int ligne = caisseFutur.getL();
        int colonne = caisseFutur.getC();
        int i=0;
        if(haut==1){
            while(estCaseLibre(ligne-i,colonne,caisses)){
                if(estCaseLibre(ligne-i,colonne+1,caisses)&&estCaseLibre(ligne-i,colonne-1,caisses)) return false;
                i++;
            }
            return true;
        }else{
            while(estCaseLibre(ligne+i,colonne,caisses)){
                if(estCaseLibre(ligne+i,colonne+1,caisses)&&estCaseLibre(ligne+i,colonne-1,caisses)) return false;
                i++;
            }
            return true;
        }
    }


    boolean estBloqueGaucheDijkstra(Position caisseFutur, int haut, byte[][] caisses){
        if(estCaseLibre(caisseFutur.getL(),caisseFutur.getC()-1,caisses)) return false;
        int ligne = caisseFutur.getL();
        int colonne = caisseFutur.getC();
        int i=0;
        if(haut==1){
            while(estCaseLibre(ligne-i,colonne,caisses)){
                //System.out.println("while de estBloqueGaucheDjiskstra, i : "+i);
                if(estCaseLibre(ligne-i,colonne-1,caisses)&&estCaseLibre(ligne-i,colonne+1,caisses)) return false;
                i++;
            }
            return true;
        }else{
            while(estCaseLibre(ligne+i,colonne,caisses)){
                //System.out.println("while de estBloqueGaucheDjiskstra, i : "+i);
                if(estCaseLibre(ligne+i,colonne-1,caisses)&&estCaseLibre(ligne+i,colonne+1,caisses)) return false;
                i++;
            }
            return true;
        }
    }

    public SequenceListe<SequenceListe<Position>> cheminsCaisseButs(Position posPousseur, Position posCaisse, byte[][] caisses, ArrayList<Position> buts){
        SequenceListe<Position> cheminCaisse = new SequenceListe<>();
        SequenceListe<Position> cheminCourantCaisseBut = new SequenceListe<>();
        SequenceListe<SequenceListe<Position>> sequence = new SequenceListe<>();
        Position PosDestination = null;
        //pour chaque but, on v�rifie s'il existe un chemin de la caisse � ce but
        for(int i = 0; i < buts.size(); i++){
            cheminCourantCaisseBut = new SequenceListe<>();
            cheminCaisse = aEtoileCaisseBut(posCaisse, posPousseur, buts.get(i), caisses);

            if(cheminCaisse != null){
                while(!cheminCaisse.estVide()){
                    Position courante = cheminCaisse.extraitTete();
                    PosDestination = posDerriere(courante, posCaisse);
                    /*
                    System.out.println("/////////");
                    System.out.println("pos pousseur : " + posPousseur.affiche());
                    System.out.println("caisse : " + posCaisse.affiche());
                    System.out.println("futur pos caisse : " + courante.affiche());
                    System.out.println("la ou le pousseur doit aller : " + PosDestination.affiche());
                     */
                    byte[][] caisses_actuel = new byte[caisses.length][caisses[0].length];
                    caisses_actuel[posCaisse.getL()][posCaisse.getC()] = CAISSE;
                    if(!posPousseur.egal(PosDestination)){
                        if(estCaseLibre(PosDestination.getL(), PosDestination.getC(), caisses_actuel)){
                            SequenceListe<Position> cheminPousseur = dijkstraPousseurDerriereCaisse(posPousseur, PosDestination, caisses_actuel);

                            while(cheminPousseur != null && !cheminPousseur.estVide()){
                                Position pos = cheminPousseur.extraitTete();
                                //System.out.println("chemin : " + pos.affiche());
                                cheminCourantCaisseBut.insereQueue(pos);
                            }
                        }else {
                            //System.out.println(">>>>> la case ou le pousseur doit aller est bloqu�e <<<<<");
                            cheminCourantCaisseBut = null;
                            break;
                        }
                    }
                    //System.out.println("chemin 1 : " + posCaisse.affiche());
                    cheminCourantCaisseBut.insereQueue(posCaisse);
                    posPousseur = new Position(posCaisse.getL(), posCaisse.getC());
                    posCaisse = new Position(courante.getL(), courante.getC());
                }
                //System.out.println("chemin 2 : " + posCaisse.affiche());
                //sequence.insereQueue(posCaisse);
            }
            if(cheminCourantCaisseBut != null && !cheminCourantCaisseBut.estVide()) {
                sequence.insereQueue(cheminCourantCaisseBut);
            }
        }
        return sequence;
    }

    public Position pousse(Position caisse, Position pousseur){
        //la case pousseur pousse la case caisse
        return new Position(caisse.getL() + (caisse.getL() - pousseur.getL()), caisse.getC() + (caisse.getC() - pousseur.getC()));
    }

    public SequenceListe<ArrayList<Position>> caissesDeplacables(Position p, byte[][] caisses){
        //renvoie la liste des caisses d�pla�ables avec la position p, la position de la caisse, et la future position de la caisse
        SequenceListe<ArrayList<Position>> caissesDep = new SequenceListe<>();
        ArrayList<Position> caisseDeplacee = new ArrayList<>();
        Position pCaisse;
        pCaisse = getPosCaisse(p.getL()+1, p.getC(), caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.getL()+1, pCaisse.getC()) && estCaseLibre(pCaisse.getL()+1, pCaisse.getC(), caisses) && !estCaseBloquante_V2(pCaisse.getL(),pCaisse.getC(),pCaisse.getL()+1, pCaisse.getC(), supprimeCaisse(pCaisse, caisses), p)){
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.getL()+1, pCaisse.getC()));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = getPosCaisse(p.getL()-1, p.getC(), caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.getL()-1, pCaisse.getC()) && estCaseLibre(pCaisse.getL()-1, pCaisse.getC(), caisses) && !estCaseBloquante_V2(pCaisse.getL(),pCaisse.getC(),pCaisse.getL()-1, pCaisse.getC(), supprimeCaisse(pCaisse, caisses), p)){
                caisseDeplacee = new ArrayList<>();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.getL()-1, pCaisse.getC()));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = getPosCaisse(p.getL(), p.getC()+1, caisses);//si la caisse est � droite du pousseur
        if(pCaisse != null) {
            if (!estCaseHorsMap(pCaisse.getL(), pCaisse.getC() + 1) && estCaseLibre(pCaisse.getL(), pCaisse.getC() + 1, caisses) && !estCaseBloquante_V2(pCaisse.getL(), pCaisse.getC(), pCaisse.getL(), pCaisse.getC() + 1, supprimeCaisse(pCaisse, caisses), p)) {
                caisseDeplacee = new ArrayList<>();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.getL(), pCaisse.getC() + 1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = getPosCaisse(p.getL(), p.getC()-1, caisses);//si la caisse est � gauche du pousseur
        if(pCaisse != null) {
            if (!estCaseHorsMap(pCaisse.getL(), pCaisse.getC() - 1) && estCaseLibre(pCaisse.getL(), pCaisse.getC() - 1, caisses) && !estCaseBloquante_V2(pCaisse.getL(), pCaisse.getC(), pCaisse.getL(), pCaisse.getC() - 1, supprimeCaisse(pCaisse, caisses), p)) {
                caisseDeplacee = new ArrayList<>();
                caisseDeplacee.add(p);
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.getL(), pCaisse.getC() - 1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        return caissesDep;
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

    boolean estBloqueeSiPousse(Position caisse, int depart, byte[][] caisses){
        //0 : vers bas
        //1 : vers haut
        //2 : vers droite
        //3 : vers gauche
        boolean a = false;
        if(depart == 0){
            if(!estCaseHorsMap(caisse.getL(), caisse.getC() - 1) && aMur(caisse.getL(), caisse.getC() - 1)){
                a= estBloqueGaucheDijkstra(caisse, 0, caisses);
                return a;
            }
            if(!estCaseHorsMap(caisse.getL(), caisse.getC() + 1) && aMur(caisse.getL(), caisse.getC() + 1)){
                a= estBloqueDroiteDijkstra(caisse, 0, caisses);
                return a;
            }
        }
        if(depart == 1){
            if(!estCaseHorsMap(caisse.getL(), caisse.getC() - 1) && aMur(caisse.getL(), caisse.getC() - 1)){
                a= estBloqueGaucheDijkstra(caisse, 1, caisses);
                return a;
            }
            if(!estCaseHorsMap(caisse.getL(), caisse.getC() + 1) && aMur(caisse.getL(), caisse.getC() + 1)){
                a= estBloqueDroiteDijkstra(caisse, 1, caisses);
                return a;
            }
        }
        if(depart == 2){
            if(!estCaseHorsMap(caisse.getL() - 1, caisse.getC()) && aMur(caisse.getL() - 1, caisse.getC())){
                a= estBloqueEnHautDijkstra(caisse, 0, caisses);
                return a;
            }
            if(!estCaseHorsMap(caisse.getL() + 1, caisse.getC()) && aMur(caisse.getL() + 1, caisse.getC())){
                a= estBloqueEnBasDijkstra(caisse, 0, caisses);
                return a;
            }
        }
        if(depart == 3){
            if(!estCaseHorsMap(caisse.getL() - 1, caisse.getC()) && aMur(caisse.getL() - 1, caisse.getC())){
                a= estBloqueEnHautDijkstra(caisse, 1, caisses);
                return a;
            }
            if(!estCaseHorsMap(caisse.getL() + 1, caisse.getC()) && aMur(caisse.getL() + 1, caisse.getC())){
                a= estBloqueEnBasDijkstra(caisse, 1, caisses);
                return a;
            }
        }
        return false;
    }

    public SequenceListe<PositionPoids> casesAccessiblesManatthan(PositionPoids posCourante, Position pousseur, Position but, byte[][] caisses){
        SequenceListe<PositionPoids> casesAccessibles = new SequenceListe<>();
        Position posBas = new Position(posCourante.getL() + 1, posCourante.getC());
        Position posHaut = new Position(posCourante.getL() - 1, posCourante.getC());
        Position posDroite = new Position(posCourante.getL(), posCourante.getC() + 1);
        Position posGauche = new Position(posCourante.getL(), posCourante.getC() - 1);
        int minimum = INFINI;
        if(!posBas.egal(pousseur) && !estBloqueeSiPousse(posBas, 0, caisses)){
            if (estCaseLibre(posCourante.getL() + 1, posCourante.getC(), caisses) && !estCaseBloquante_V2(posCourante.getL(), posCourante.getC(), posCourante.getL() + 1, posCourante.getC(), caisses, pousseur)) {
                Position position = new Position(posCourante.getL() + 1, posCourante.getC());
                minimum = distanceManatthan(position, but);
                casesAccessibles.insereTete(new PositionPoids(position.getL(), position.getC(), minimum));
                //System.out.println(posBas.affiche() + " : pas bloquee si poussee");
            }

        }else{
            //System.out.println(posBas.affiche() + " : bloquee si poussee ou posPousseur");
        }
        if(!posHaut.egal(pousseur) && !estBloqueeSiPousse(posHaut, 1, caisses)){
            if(estCaseLibre(posCourante.getL() - 1, posCourante.getC(), caisses) && !estCaseBloquante_V2(posCourante.getL(), posCourante.getC(), posCourante.getL() - 1, posCourante.getC(), caisses, pousseur)) {
                Position position = new Position(posCourante.getL() - 1, posCourante.getC());
                int distance = distanceManatthan(position, but);
                if (distance < minimum) {
                    minimum = distance;
                    casesAccessibles = new SequenceListe<>();
                    PositionPoids p = new PositionPoids(position.getL(), position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                } else if (distance == minimum) {
                    PositionPoids p = new PositionPoids(position.getL() - 1, position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                }
                //System.out.println(posHaut.affiche() + " : pas bloquee si poussee");
            }
        }else{
            //System.out.println(posHaut.affiche() + " : bloquee si poussee ou posPousseur");
        }
        if(!posDroite.egal(pousseur) && !estBloqueeSiPousse(posDroite, 2, caisses)){
            if(estCaseLibre(posCourante.getL(), posCourante.getC() + 1, caisses) && !estCaseBloquante_V2(posCourante.getL(), posCourante.getC(), posCourante.getL(), posCourante.getC() + 1, caisses, pousseur)) {
                Position position = new Position(posCourante.getL(), posCourante.getC() + 1);
                int distance = distanceManatthan(position, but);
                if (distance < minimum) {
                    minimum = distance;
                    casesAccessibles = new SequenceListe<>();
                    PositionPoids p = new PositionPoids(position.getL(), position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                } else if (distance == minimum) {
                    PositionPoids p = new PositionPoids(position.getL(), position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                }
                //System.out.println(posDroite.affiche() + " : pas bloquee si poussee");
            }
        }else{
            //System.out.println(posDroite.affiche() + " : bloquee si poussee ou posPousseur");
        }
        if(!posGauche.egal(pousseur) && !estBloqueeSiPousse(posGauche, 3, caisses)){
            if(estCaseLibre(posCourante.getL(), posCourante.getC() - 1, caisses) && !estCaseBloquante_V2(posCourante.getL(), posCourante.getC(), posCourante.getL(), posCourante.getC() - 1, caisses, pousseur)) {
                Position position = new Position(posCourante.getL(), posCourante.getC() - 1);
                int distance = distanceManatthan(position, but);
                if (distance < minimum) {
                    minimum = distance;
                    casesAccessibles = new SequenceListe<>();
                    PositionPoids p = new PositionPoids(position.getL(), position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                } else if (distance == minimum) {
                    PositionPoids p = new PositionPoids(position.getL(), position.getC(), minimum);
                    casesAccessibles.insereTete(p);
                }
                //System.out.println(posGauche.affiche() + " : pas bloquee si poussee");
            }
        }else{
            //System.out.println(posGauche.affiche() + " : bloquee si poussee ou posPousseur");
        }
        return casesAccessibles;
    }

    public SequenceListe<PositionPoids> casesAccessiblesPousseurDerriereCaisseManatthan(Position dep, Position dest, byte[][] caisses){
        SequenceListe<PositionPoids> casesAccessibles = new SequenceListe<>();
        int minimum = INFINI;
        if(estCaseLibre(dep.getL()+1,dep.getC(), caisses)){
            Position position = new Position(dep.getL()+1,dep.getC());
            minimum = distanceManatthan(position,dest);
            casesAccessibles.insereTete(new PositionPoids(position.getL(),position.getC(),minimum));
        }
        if(estCaseLibre(dep.getL()-1,dep.getC(), caisses)){
            Position position = new Position(dep.getL()-1,dep.getC());
            int distance = distanceManatthan(position,dest);
            if(distance < minimum){
                minimum = distance;
                casesAccessibles = new SequenceListe<>();
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }else if(distance == minimum){
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }
            else{
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereQueue(p);
            }
        }
        if(estCaseLibre(dep.getL(),dep.getC()+1, caisses)){
            Position position = new Position(dep.getL(),dep.getC()+1);
            int distance = distanceManatthan(position,dest);
            if(distance < minimum){
                minimum = distance;
                casesAccessibles = new SequenceListe<>();
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }else if(distance == minimum){
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }
            else{
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereQueue(p);
            }
        }
        if(estCaseLibre(dep.getL(),dep.getC()-1, caisses)){
            Position position = new Position(dep.getL(),dep.getC()-1);
            int distance = distanceManatthan(position,dest);
            if(distance < minimum){
                minimum = distance;
                casesAccessibles = new SequenceListe<>();
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }else if(distance == minimum){
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereTete(p);
            }
            else{
                PositionPoids p = new PositionPoids(position.getL(),position.getC(),minimum);
                casesAccessibles.insereQueue(p);
            }
        }
        return casesAccessibles;
    }

    public SequenceListe<PositionPoids> casesAccessibles(PositionPoids posCourante, byte[][] caisses){
        SequenceListe<PositionPoids> cases = new SequenceListe<>();
        if(estCaseLibre(posCourante.getL()+1, posCourante.getC(), caisses)){
            cases.insereTete(new PositionPoids(posCourante.getL()+1,posCourante.getC(), posCourante.getPoids()+1));
        }
        if(estCaseLibre(posCourante.getL()-1,posCourante.getC(), caisses)){
            cases.insereTete(new PositionPoids(posCourante.getL()-1,posCourante.getC(), posCourante.getPoids()+1));
        }
        if(estCaseLibre(posCourante.getL(),posCourante.getC()+1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.getL(),posCourante.getC()+1, posCourante.getPoids()+1));
        }
        if(estCaseLibre(posCourante.getL(),posCourante.getC()-1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.getL(),posCourante.getC()-1, posCourante.getPoids()+1));
        }
        cases.insereQueue(posCourante);//la case courante est accessible puisque le pousseur est d�j� dessus
        return cases;
    }

    boolean estCaseHorsMap(int l, int c){
        return (l < 0 || l > this.l-1 || c < 0 || c > this.c-1);
    }

    public Position posDerriere(Position pousseur,Position caisse){
        int diff_l = caisse.l-pousseur.l;
        int diff_c = caisse.c-pousseur.c;
        return new Position(pousseur.l+diff_l*2, pousseur.c+diff_c*2);
    }

    boolean estCaseLibre(int l, int c, byte[][] caisses){
        if(!aMurOuHorsMap(l,c) && getPosCaisse(l,c,caisses)==null){
            return true;
        }else{
            return false;
        }
    }

    boolean aMur(int l, int c) {
        return (carte[l][c] & MUR) != 0;
    }

    boolean aMurOuHorsMap(int l, int c) {
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

    public void ajouterInstance(Position p, byte[][] caisses, HashMap<BigInteger, byte[][]> instances){
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR ;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<instanceCopie.length; i++){
            for(int j=0; j<instanceCopie[0].length; j++){
                if(instanceCopie[i][j]==CAISSE){
                    sb.append("1");
                }else{
                    if(instanceCopie[i][j]==POUSSEUR){
                        sb.append("2");
                    }else{
                        sb.append("0");
                    }
                }
            }
        }
        BigInteger cleInstance = new BigInteger(sb.toString(), 3);

        if(!instances.containsKey(cleInstance)){
            instances.put(cleInstance, instanceCopie);
            nb_instances++;
        }
    }

    public boolean estInstance(Position p, byte[][] caisses, HashMap<BigInteger, byte[][]> instances){
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<instanceCopie.length; i++){
            for(int j=0; j<instanceCopie[0].length; j++){
                if(instanceCopie[i][j]==CAISSE){
                    sb.append("1");
                }else{
                    if(instanceCopie[i][j]==POUSSEUR){
                        sb.append("2");
                    }else{
                        sb.append("0");
                    }
                }
            }
        }
        BigInteger cleInstance = new BigInteger(sb.toString(), 3);

        if(!instances.containsKey(cleInstance)){
            return false;
        }else{
            return true;//Arrays.deepEquals(instanceCopie,instances.get(cleInstance));
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
        return aMurOuHorsMap(l+1,c)|| aMurOuHorsMap(l-1,c)|| aMurOuHorsMap(l,c+1)|| aMurOuHorsMap(l,c-1);
    }
    boolean aBloqueeAutour(int l, int c){
        return aCaisseBloquee(l+1,c,caisses)||aCaisseBloquee(l-1,c,caisses)||aCaisseBloquee(l,c+1,caisses)||aCaisseBloquee(l,c-1,caisses);
    }
    boolean aAccesBut(int l, int c,byte[][] caisses){
        int i=0;
        boolean gauche=false,droite=false,bas=false,haut=false;
        while(!aMurOuHorsMap(l,c+i) && droite==false && !aCaisseBloquee(l,c+i,caisses)) { // � droite
            if (!estCaseHorsMap(l, c - 1)) { // pas sur
                if (estBut(new Position(l,c+i)) && carte[l][c - 1] != MUR && caisses[l][c+i] != CAISSE) droite = true;
            }
            i++;
        }
        i=0;
        while(!aMurOuHorsMap(l,c-i) && gauche==false && !aCaisseBloquee(l,c-i,caisses)){ // � gauche
            if(!estCaseHorsMap(l,c+1)){ // pas sur
                if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR && caisses[l][c-i] != CAISSE) gauche = true;
            }
            i++;
        }
        i=0;
        while(!aMurOuHorsMap(l-i,c) && haut==false && !aCaisseBloquee(l-i,c,caisses)){ // en haut
            if(!estCaseHorsMap(l+1,c)){ // pas sur
                if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR && caisses[l-i][c] != CAISSE) haut = true;
            }
            i++;
        }
        i=0;
        while(!aMurOuHorsMap(l+i,c) && droite==false && !aCaisseBloquee(l+i,c,caisses)){ // en bas
            if(!estCaseHorsMap(l-1,c)){// pas sur
                if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR && caisses[l+i][c] != CAISSE) bas = true;
            }
            i++;
        }
        i=0;
        return gauche||droite||bas||haut;
    }

    boolean aCaisseNonBloquante(int l,int c,byte[][] caisses){
        return (caisses[l][c]==CAISSE || caisses[l][c]==12 || caisses[l][c]==CAISSE_BLOQUEE_TEMP);
    }

    boolean bloqueeVerticalDroit(int l,int c, int nord, byte[][] caisses){
        if(aAccesBut(l,c,caisses)) return false;
        if(aMurOuHorsMap(l,c)) return true;
        int i=0;
        if(nord==1){
            while(!aMurOuHorsMap(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l+1,c+i)) return true;
                if(estCaseHorsMap(l-1,c+i)){
                    if(estBut(new Position(l,c+i)) && carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR && caisses[l][c+i] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l-1,c+i,caisses)|| estBut(new Position(l,c+i))||aCaisseNonBloquante(l-1,c+i,caisses))&& carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR && caisses[l][c+i] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMurOuHorsMap(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l-1,c+i)) return true;
                if(estCaseHorsMap(l+1,c+i)){
                    if(estBut(new Position(l,c+i)) && carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR && caisses[l][c+i] != CAISSE) return false;
                }
                else{
                    if((estCaseLibre(l+1,c+i,caisses)|| estBut(new Position(l,c+i))||aCaisseNonBloquante(l+1,c+i,caisses))&& carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR && caisses[l][c+i] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeVerticalGauche(int l,int c, int nord,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMurOuHorsMap(l,c)) return true;
        if(nord==1){
            while(!aMurOuHorsMap(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l+1,c-i)) return true;
                if(estCaseHorsMap(l-1,c-i)){
                    if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR && caisses[l][c-i] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l-1,c-i,caisses)|| estBut(new Position(l,c-i))||aCaisseNonBloquante(l-1,c-i,caisses))&& carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR && caisses[l][c-i] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMurOuHorsMap(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l-1,c-i)) return true;
                if(estCaseHorsMap(l+1,c-i)){
                    if(estBut(new Position(l,c-i)) && carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR && caisses[l][c-i] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l+1,c-i,caisses) || estBut(new Position(l,c-i))||aCaisseNonBloquante(l+1,c-i,caisses))&& carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR && caisses[l][c-i] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalHaut(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMurOuHorsMap(l,c)) return true;
        if(ouest==1){
            while(!aMurOuHorsMap(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c+1)) return true;
                if(estCaseHorsMap(l-i,c-1)){
                    if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR && caisses[l-i][c] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l-i,c-1,caisses)||estBut(new Position(l-i,c))||aCaisseNonBloquante(l-i,c-1,caisses)) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR && caisses[l-i][c] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMurOuHorsMap(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c-1)) return true;
                if(estCaseHorsMap(l-i,c+1)){
                    if(estBut(new Position(l-i,c)) && carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR && caisses[l-i][c] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l-i,c+1,caisses) || estBut(new Position(l-i,c))||aCaisseNonBloquante(l-i,c+1,caisses))&& carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR && caisses[l-i][c] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalBas(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMurOuHorsMap(l,c)) return true;
        if(ouest==1){
            while(!aMurOuHorsMap(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c+1)) return true;
                if(estCaseHorsMap(l+i,c-1)){
                    if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR && caisses[l+i][c] != CAISSE) return false;
                }
                else{
                    if((estCaseLibre(l+i,c-1,caisses) || estBut(new Position(l+i,c))||aCaisseNonBloquante(l+i,c-1,caisses))&& carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR && caisses[l+i][c] != CAISSE) return false;
                }

                i++;
            }
            return true;
        }else{
            while(!aMurOuHorsMap(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c-1)) return true;
                if(estCaseHorsMap(l+i,c+1)){
                    if(estBut(new Position(l+i,c)) && carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR && caisses[l+i][c] != CAISSE) return false;
                }else{
                    if((estCaseLibre(l+i,c+1,caisses) || estBut(new Position(l+i,c))||aCaisseNonBloquante(l+i,c+1,caisses))&& carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR && caisses[l+i][c] != CAISSE) return false;
                }
                i++;
            }
            return true;
        }
    }

    boolean pourra_bouger_vertical(int l,int c,byte[][] caisses){
        return ((estCaseLibre(l-1,c,caisses)||caisses[l-1][c]==VIDE||caisses[l-1][c]==CAISSE)&&(caisses[l+1][c]!=CAISSE_BLOQUEE&&carte[l+1][c]!=MUR))||((estCaseLibre(l+1,c,caisses)||caisses[l+1][c]==VIDE||caisses[l+1][c]==CAISSE)&&(caisses[l-1][c]!=CAISSE_BLOQUEE&&carte[l-1][c]!=MUR));
    }
    boolean pourra_bouger_horizontal(int l,int c,byte[][] caisses){
        return ((estCaseLibre(l,c-1,caisses)||caisses[l][c-1]==VIDE||caisses[l][c-1]==CAISSE)&&(caisses[l][c+1]!=CAISSE_BLOQUEE&&carte[l][c+1]!=MUR))||((estCaseLibre(l,c+1,caisses)||caisses[l][c+1]==VIDE||caisses[l][c+1]==CAISSE)&&(caisses[l][c-1]!=CAISSE_BLOQUEE&&carte[l][c-1]!=MUR));
    }
    boolean gestionPlusieurTemp(int l, int c,byte[][] caisses){
        if(estCaisseBloqueeTemp(l+1,c,caisses)&&(!aMurAutour(l+1,c)||((caisses[l+1][c+1]==VIDE || caisses[l+1][c+1]==CAISSE)&&(caisses[l+1][c-1]==VIDE || caisses[l+1][c-1]==CAISSE))||pourra_bouger_horizontal(l,c,caisses))) return false;
        if(estCaisseBloqueeTemp(l-1,c,caisses)&&(!aMurAutour(l-1,c)||((caisses[l-1][c+1]==VIDE || caisses[l-1][c+1]==CAISSE)&&(caisses[l-1][c-1]==VIDE || caisses[l-1][c-1]==CAISSE))||pourra_bouger_horizontal(l,c,caisses))) return false;
        if(estCaisseBloqueeTemp(l,c+1,caisses)&&(!aMurAutour(l,c+1)||((caisses[l-1][c+1]==VIDE || caisses[l-1][c+1]==CAISSE)&&(caisses[l+1][c+1]==VIDE || caisses[l+1][c+1]==CAISSE))||pourra_bouger_vertical(l,c,caisses))) return false;
        if(estCaisseBloqueeTemp(l,c-1,caisses)&&(!aMurAutour(l,c-1)||((caisses[l+1][c-1]==VIDE || caisses[l+1][c-1]==CAISSE)&&(caisses[l-1][c-1]==VIDE || caisses[l-1][c-1]==CAISSE))||pourra_bouger_vertical(l,c,caisses))) return false;

        if(estCaseHorsMap(l,c) || estCaseHorsMap(l,c+1) || estCaseHorsMap(l,c-1) || estCaseHorsMap(l+1,c) || estCaseHorsMap(l-1,c)) return true;

        return estCaisseBloqueeTemp(l,c+1,caisses) || estCaisseBloqueeTemp(l,c-1,caisses) || estCaisseBloqueeTemp(l+1,c,caisses) || estCaisseBloqueeTemp(l-1,c,caisses) || caisses[l][c+1]==CAISSE_BLOQUEE || caisses[l][c-1]==CAISSE_BLOQUEE || caisses[l+1][c]==CAISSE_BLOQUEE || caisses[l-1][c]==CAISSE_BLOQUEE;
    }

    public boolean estCaseDisponible(int l, int c,byte[][] caisses) {
        if(estCaseHorsMap(l,c)) return false;
        return (caisses[l][c] & (MUR | CAISSE | CAISSE_BLOQUEE_TEMP | CAISSE_BLOQUEE)) == 0;
    }
    boolean estBloqueeEnCarre(int l,int c,byte[][] caisses){
        //caisse en haut � droite
        if(!estCaseDisponible(l+1,c,caisses)&&!estCaseDisponible(l+1,c-1,caisses)&&!estCaseDisponible(l,c-1,caisses)) return true;
        //caisse en bas � droite
        if(!estCaseDisponible(l-1,c,caisses)&&!estCaseDisponible(l-1,c-1,caisses)&&!estCaseDisponible(l,c-1,caisses)) return true;
        //caisse en haut � gauche
        if(!estCaseDisponible(l+1,c,caisses)&&!estCaseDisponible(l+1,c+1,caisses)&&!estCaseDisponible(l,c+1,caisses)) return true;
        //caisse en bas � gauche
        if(!estCaseDisponible(l-1,c,caisses)&&!estCaseDisponible(l-1,c+1,caisses)&&!estCaseDisponible(l,c+1,caisses)) return true;

        return false;
    }

    void actualiseUneCaisseSpecial(int c,int l,byte[][] caisses){
        if(estCaseHorsMap(l,c)) return;
        if (aCaisse(l,c,caisses)){
            if(caisses[l][c]==CAISSE_BLOQUEE) return;
            if(estBut(new Position(l,c))){
                return;
            }
            if (estCaisseBloqueeTemp(l,c,caisses)) {
                caisses[l][c] = CAISSE_BLOQUEE_TEMP;
                return;
            }
            caisses[l][c] = CAISSE;
        }
    }
    void actualiseCoteSpecial(int l,int c,byte[][] caisses,Position sokoban_pos){
        if(sokoban_pos.l+1!=l) actualiseUneCaisseSpecial(sokoban_pos.l+1,sokoban_pos.c,caisses);
        if(sokoban_pos.l-1!=l) actualiseUneCaisseSpecial(sokoban_pos.l-1,sokoban_pos.c,caisses);
        if(sokoban_pos.c+1!=c) actualiseUneCaisseSpecial(sokoban_pos.l,sokoban_pos.c+1,caisses);
        if(sokoban_pos.c-1!=c) actualiseUneCaisseSpecial(sokoban_pos.l,sokoban_pos.c-1,caisses);
    }

    boolean estCaisseBloquee(int l, int c,byte[][] caisses,Position sokoban_pos){
        actualiseCoteSpecial(l,c,caisses,sokoban_pos);
        if(estBloqueeEnCarre(l,c,caisses)) return true;
        if(aMurOuHorsMap(l,c) || estCaseLibre(l,c,caisses)) return false;
        if(!estBut(new Position(l,c))) {
            // CAS CAISSE TEMPORAIRE
            if(estCaisseBloqueeTemp(l,c,caisses)){
                if(!aMurAutour(l,c)) return false;//&&!aBloqueeAutour(l,c)) return false;
                return gestionPlusieurTemp(l,c,caisses);
            }

            // CAS CONDUITS
            if(aMurOuHorsMap(l-1,c)&& aMurOuHorsMap(l+1,c)) return (bloqueeVerticalDroit(l,c,0,caisses)||bloqueeVerticalDroit(l,c,1,caisses)) && (bloqueeVerticalGauche(l,c,0,caisses)||bloqueeVerticalGauche(l,c,1,caisses)); //conduit horizontale
            if(aMurOuHorsMap(l,c-1)&& aMurOuHorsMap(l,c+1)) return (bloqueeHorizontalBas(l,c,0,caisses)||bloqueeHorizontalBas(l,c,1,caisses)) && (bloqueeHorizontalHaut(l,c,0,caisses)||bloqueeHorizontalHaut(l,c,1,caisses)); //conduit verical

            // CAS GENERALS
            if(aMurOuHorsMap(l-1,c)) return bloqueeVerticalDroit(l,c,1,caisses)&&bloqueeVerticalGauche(l,c,1,caisses); // mur dessus de la caisse
            if(aMurOuHorsMap(l+1,c)) return bloqueeVerticalDroit(l,c,0,caisses)&&bloqueeVerticalGauche(l,c,0,caisses); // mur dessous de la caisse
            if(aMurOuHorsMap(l,c-1)) return bloqueeHorizontalHaut(l,c,1,caisses)&&bloqueeHorizontalBas(l,c,1,caisses); // mur a gauche de la caisse
            if(aMurOuHorsMap(l,c+1)) return bloqueeHorizontalHaut(l,c,0,caisses)&&bloqueeHorizontalBas(l,c,0,caisses); // mur a droite de la caisse
        }
        return false;
    }
    boolean estCaisseBloqueeTemp(int l,int c,byte[][] caisses){
        if(estCaseHorsMap(l,c)) return false;
        if(caisses[l][c]!=CAISSE && caisses[l][c]!=CAISSE_BLOQUEE_TEMP && carte[l][c]!=MUR && caisses[l][c]!=VIDE) return false;
        if(aMurOuHorsMap(l,c)) return false;
        if(estCaseLibre(l,c,caisses))return false;
        if(!estBut(new Position(l,c))){
            if(!estCaseLibre(l-1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMurOuHorsMap(l-1,c) && (aMurOuHorsMap(l,c-1)||(aMurOuHorsMap(l,c+1)))) return false;
                return true;
            }
            if(!estCaseLibre(l+1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMurOuHorsMap(l+1,c) && (aMurOuHorsMap(l,c-1)||(aMurOuHorsMap(l,c+1)))) return false;
                return true;
            }
        }
        return false;
    }

    boolean estCaseBloquante_V2(int l_initial, int c_initial, int l, int c,byte[][] caisses, Position sokoban_pos){
        if(estCaseHorsMap(l,c)) return true;
        if(caisses[l][c]!=VIDE && caisses[l][c]!=BUT) return true;
        if(aMurOuHorsMap(l,c)) return true;

        byte[][] saveCaisses = copieByte(caisses);
        //System.out.println("l: "+l+" c: "+c+" cases[l][c]: "+ cases[l][c]);
        saveCaisses[l][c] = CAISSE;
        if(l_initial!=-1 && c_initial!=-1) saveCaisses[l_initial][c_initial] = VIDE;
        actualiseToutesCaisses(saveCaisses,sokoban_pos);

        if(saveCaisses[l][c]==16){
            return true;
        }
        return false;
    }
    public boolean aCaisse(int l, int c,byte[][] caisses){
        return (caisses[l][c] & CAISSE) != 0  || (caisses[l][c] & CAISSE_BLOQUEE_TEMP) != 0; // || (cases[l][c] & CAISSE_BLOQUEE) != 0;
    }
    void actualiseUneCaisse(int l, int c,byte[][] caisses,Position sokoban_pos){
        if (aCaisse(l,c,caisses)){
            //if(cases[l][c]==CAISSE_BLOQUEE) return;
            if(estBut(new Position(l,c))){
                return;
            }
            if (estCaisseBloquee(l,c,caisses,sokoban_pos)){
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
    public void actualiseToutesCaisses(byte[][] caisses, Position sokoban_pos){
        for(int l=0;l<caisses.length;l++){
            for(int c=0;c<caisses[0].length;c++){
                actualiseUneCaisse(l,c,caisses,sokoban_pos);
            }
        }
    }
}
