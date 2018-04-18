package kolejkadoegzaminu;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.awt.Image;
import java.io.*;
import java.net.*;


class ObrazekIGeneratorZdarzen extends Canvas implements Runnable{
  int SZEROKOSC, WYSOKOSC;
  // łączność z opcjami
  KolejkaDoEgzaminu oknoMatka;
  // obiekty kolejkowe:
  Kolejka kol;
  Egzaminator egzaminator = new Egzaminator();

  int pojemnoscKolejki;
  int liczbaZdarzen;
  int liczbaWygenerowanychZdarzen;
  long maxZegar;
  // pomocnicze zmienne:
  long czas_o; // czas rozpoczecia obslugi studenta
  long czas_g; // czas pojawienia sie nowego studenta
  // obiekty wątkowe do animacji i udawania, Se się jest zegarem:
  Thread zegarek = new Thread(this);
  int opoznienie;
  boolean biegnie;
  int[][]tablicaPr;
    int[][][] tablicaWr; //egzaminator,liczba porzadkowa, [bsluzeni=0, zdani=1]
  double[] tablicaSr; // znacznik

  int znacznik;
  mieszanka wynik; //pomocnicza zmienna 
  
  // konstruktor
  public ObrazekIGeneratorZdarzen(int W,int H,int N,KolejkaDoEgzaminu m){
    SZEROKOSC = W;
    WYSOKOSC = H;
    oknoMatka = m;
    pojemnoscKolejki = N;
    czas_o = 0;
    czas_g = 0;
    liczbaWygenerowanychZdarzen = 0;
    biegnie = false;
    kol = new Kolejka(N);
    tablicaWr = new int[5][1000][2];
   
     tablicaSr = new double[1000];
   
    znacznik = 0;
    }
  
  // ta metoda generuje zdarzenie co takt zegara
 // ta metoda generuje zdarzenie co takt zegara
  public void run(){
    mieszanka wynik;
    Petent[] egzaminowani;
    egzaminowani = new Petent[5]; //tablica przypisania petentow do egzaminatorow
    znacznik = 0;    
    int przeegzaminowani;
    int [] minuta; // macierz ktora informuje ktory petent jak dlugo bedzie zdawal, jest ona wyliczona na podstawie prawdopodobienstwa (fukcja - zdawalnosc)
    minuta = new int[5];
            
    int [][] praw;
    praw = new int[5][3];
    int licz = 0;
    //zerowanie tablic do rysowania
    for(int j=0; j<5; j++){for(int i=0; i<1000; i++){
        tablicaWr[j][i][0]=0;
        tablicaWr[j][i][1]=0;
    }
    }
     for(int i=0; i<1000; i++){
        tablicaSr[i]=0;
                    
    }
    
    
    try {
      while (true) {
        /**/
          
         

        if (kol.pusta() == false || egzaminator.wolni()== false ){
            if (egzaminator.zajeci()==false ){
            licz = egzaminator.wolny();
            if (kol.pusta() == false){
              egzaminowani[licz] = kol.Pierwszy();
              praw[licz] = tablicaPr[egzaminowani[licz].nrEgzaminu - 1];
              minuta[licz] = egzaminator.dlugoscEgzaminu(praw[licz]);
              wyswietlInformacje("\n" + (znacznik ) + " min,  petent podchodzi do egz. " + (egzaminowani[licz].nrEgzaminu) + " raz\n");
              kol.usunZPrzodu();
              egzaminator.czyPusto[licz] = false; 
              }
            }

            znacznik ++;
          }
        else{
            
            
          wyswietlInformacje("\nKONIEC - obsluzono wszystkich petentow\n");
          zegarek.suspend();  
          }      


        wynik = egzaminator.odswiez(minuta);
        
       
         //wpisujemy w tablice wykresu
        for(int l=0; l<5; l++){
         tablicaWr[l][znacznik][1]=tablicaWr[l][znacznik-1][1];
        tablicaWr[l][znacznik][0]=tablicaWr[l][znacznik-1][0];
        }
        tablicaSr[znacznik] = tablicaSr[znacznik-1];
        
        //------------------------------
        
        
        for (int i = 0; i<wynik.ilu; i++)
          {
            
          kol.obsluzonych[wynik.kto[i]]++;
          if (wynik.zaliczyl[wynik.kto[i]] == true){
          wyswietlInformacje("\nEgzaminator nr " + (wynik.kto[i]+1) + " zaliczył po " + (wynik.czas[i]) + "minutach\n");  
          kol.zdali[wynik.kto[i]]++;
          tablicaWr[wynik.kto[i]][znacznik][1]=tablicaWr[wynik.kto[i]][znacznik-1][1]+1; 
          tablicaWr[wynik.kto[i]][znacznik][0]=tablicaWr[wynik.kto[i]][znacznik-1][0]+1; 
 
          }
          else {
              wyswietlInformacje("\nEgzaminator nr " + (wynik.kto[i]+1) + " oblał petenta po " + (wynik.czas[i]) + "minutach\n"); 
                tablicaWr[wynik.kto[i]][znacznik][0]=tablicaWr[wynik.kto[i]][znacznik-1][0]+1; 
  
          }
          
          
          przeegzaminowani = 0;
        for(int j=0; j<5; j++){
        przeegzaminowani = przeegzaminowani + (int)kol.obsluzonych[j];
        } 
          kol.sredni = kol.sredni + (wynik.czas[i] -kol.sredni)/przeegzaminowani;
               tablicaSr[znacznik]=kol.sredni;
          }
        
//----------------------------
        for ( int i = 0; i<5; i++){
          if ( wynik.awaria[i] == true && wynik.cz_awarii[i] == 0){
            wyswietlInformacje("\nEgzaminator nr " + (i+1) + " ma zepsuty samochód \n");    
            }  
          }
       
         
      
        repaint();
        zegarek.sleep(opoznienie);    
        }
       

      }catch(InterruptedException e){};
    }

  // jeSeli uSytkownik zaznaczył Checkbox, to wyświetlamy informacje w duSym białym polu tekstowym
  public void wyswietlInformacje(String info){
    if(oknoMatka.dajInfo.getState()) oknoMatka.info.append(info);
    }

  // gdybysmy chcieli symulowac od nowa, to ta metoda przywraca kluczowe zmienne do oryginalnego stanu
  public void odnowZmienne(){
    kol = new Kolejka(pojemnoscKolejki);
   // czas_o = 0;
   // czas_g = 0;
    liczbaWygenerowanychZdarzen = 0;
    egzaminator = new Egzaminator();
    oknoMatka.info.setText("");
    for(int i=0; i<5; i++)
    kol.obsluzonych[i] = 0;
     tablicaWr = new int[5][1000][2];
    for(int j=0; j<5; j++){
    for(int i=0; i<1000; i++){
        tablicaWr[j][i][0]=0;
        tablicaWr[j][i][1]=0;
    }
    }
     tablicaSr = new double[1000];
    for(int i=0; i<1000; i++){
        tablicaSr[i]=0;
                
    
    }
    znacznik = 0;
    }

  // pobieramy parametry z panela z opcjami
  public void pobierzParametry(int op,int ils,int[][] pr){
    opoznienie = op;

    kol = new Kolejka(ils);
    tablicaPr = pr;
    
    
    //losowanie ktory raz petent zdaje egzamin
    double los ;
    int numer = 1;
    /*zakładamy, że 
     * 25% osob podchodzi do egzaminu po raz pierwszy
     * 40% osob podchodzi do egzaminu po raz drugi
     * 20% osob podchodzi do egzaminu po raz trzeci
     * 15% osob podchodzi do egzaminu po raz > 3 (oznaczymy czwarty)
     * */    
    for(int i = 0; i< ils; i++){
        los = Math.random();
        if (los<0.25) numer = 1;
        if (los< 0.65 && los>0.25) numer = 2;
        if (los< 0.85 && los>0.65) numer = 3;
        if (los>0.85) numer = 4;
        
     boolean czySieUdaloDodac = kol.dodaj(new Petent(kol.wygenerowanych,czas_g, numer));
      if(czySieUdaloDodac==false){ // student nie zmiescil sie w kolejce
        wyswietlInformacje("("+liczbaWygenerowanychZdarzen +
        ")PRZEPELNIENIE - student nie zmiescil sie\n");
      //  kol.przepelnienie++;
        }
      }
    }

  // odrysowanie kolejki
  public void paint(Graphics g){
    int obsl,zdal, oblali;
    
    String imageFileName = "autko2.jpg";
    URL imageSrc;
    Image myImage;
    myImage = Toolkit.getDefaultToolkit().getImage(imageFileName);
    String imageFileName2 = "autko3.jpg";
    URL imageSrc2;
    Image myImage2;
    myImage2 = Toolkit.getDefaultToolkit().getImage(imageFileName2);
    
    
    g.setColor(Color.white);
    g.fillRect(0,0,SZEROKOSC,WYSOKOSC);
    // kratki kolejki:
    g.setColor(Color.lightGray);
    
    int odstep = 60; //odstep miedzy dsamochodzikami na rysunku
    
    for(int i=1;i< kol.POJEMNOSC ;i++) g.drawRect(10+20*i,40,20,30); // o jeden mniej, bo liczymy miejsce w gabinecie
    // studenci:
    for(int i=0;i< kol.LICZNIK -1 ;i++){ // rysujemy o jednego mniej, bo jeden jest juz w gabinecie
      g.setColor(new Color(0,145,72));
      g.fillOval(12+20*(kol.POJEMNOSC-i-1),48,15,15); // twarz
      g.drawLine(12+20*(kol.POJEMNOSC-i-1),48,12+20*(kol.POJEMNOSC-i-1)+5,48+5); // prawy czułek
      g.drawLine(12+20*(kol.POJEMNOSC-i-1)+9,48+5,12+20*(kol.POJEMNOSC-i-1)+14,48); // lewy czułek
      g.setColor(Color.YELLOW);
      g.fillOval(12+20*(kol.POJEMNOSC-i-1)+3,52,5,5); // oko prawe
      g.fillOval(12+20*(kol.POJEMNOSC-i-1)+8,52,5,5); // oko lewe
      }



   
    // samochodziki

    for (int i=0; i<5; i++){
      g.setColor(Color.black);
      g.drawImage(myImage,20+20*kol.POJEMNOSC,20+(odstep*i),this);
      if (egzaminator.czyAwaria[i] == true) g.drawImage(myImage2,20 +20*kol.POJEMNOSC,20+(odstep*i),this);
      }
      

    // osoba zdawająca egzamin
      for(int i=0; i<5; i++){
      if( egzaminator.czyPusto[i] == false ){
        g.setColor(Color.ORANGE);
        g.fillOval(40+20*kol.POJEMNOSC,25+(i*odstep),15,15); // twarz
        g.drawLine(40+20*kol.POJEMNOSC,25+(i*odstep),40+20*kol.POJEMNOSC+5,25+5+(i*odstep)); // prawy czułek
        g.drawLine(40+20*kol.POJEMNOSC+9,25+5+(i*odstep),40+20*kol.POJEMNOSC+14,25+(i*odstep)); // lewy czułek
        g.setColor(Color.GREEN);
        g.fillOval(40+20*kol.POJEMNOSC+3,29+(i*odstep),5,5); // oko prawe
        g.fillOval(40+20*kol.POJEMNOSC+8,29+(i*odstep),5,5); // oko lewe
        }
      }
   // rysowanie obsluzonych
    for(int i = 0; i< 5; i++){
    for(int j=0;j<kol.zdali[i];j++){
         g.setColor(Color.magenta);
         g.fillOval(120+20*kol.POJEMNOSC + 5*(j%5),20 + 5*(j/5) + (i*odstep) ,4,4);
    }
    for(int j=0;j<kol.obsluzonych[i]-kol.zdali[i];j++){
    g.setColor(Color.cyan); 
         g.fillOval(120+20*kol.POJEMNOSC + 5*(j%5),25 + 5*(j/5) + (i*odstep) ,4,4);}
      }
    
    
    // informacje o kolejce:
    obsl = 0;
    zdal = 0;
    oblali = 0;
    for (int i=0; i<5; i++) {
    obsl += kol.obsluzonych[i];
    zdal += kol.zdali[i];
    oblali = obsl - zdal;
    }
    
    g.setColor(Color.BLACK);
    g.drawString("Obecnie na egzamin oczekuje: "+(kol.LICZNIK-1),10,120);
    g.drawString("Liczba obsluzonych petentow: "+obsl,10,140);
    g.drawString("Liczba obsluzonych które zdały: "+zdal,10,160);
    g.drawString("Liczba obsluzonych które oblały: "+oblali,10,180);
    g.drawString("Sredni czas trwania egzamin:"+(int)kol.sredni+" minut",10,200);
    g.drawString("Petenci, którzy zaliczyli egzamin:",10,220);
    g.drawString("Petenci, którzy nie zaliczyli egzamin:",10,240);
    g.setColor(Color.magenta);
    g.fillOval(184,213,7,7);
    g.setColor(Color.cyan);
    g.fillOval(204,233,7,7);
    
   //wykresik
    
    
    g.setColor(Color.BLACK);
g.drawLine(5,WYSOKOSC-5,SZEROKOSC-5,WYSOKOSC-5);
g.drawLine(5,WYSOKOSC-5,5,WYSOKOSC-250);
// podzialka OX co 10 zdarzen
for(int i=0;i<SZEROKOSC/10 - 1;i++) g.drawLine(5+i*10,WYSOKOSC-7,5+i*10,WYSOKOSC-3);
// podzialka OY co 10 osob
for(int i=0;i<250/10 - 1;i++) g.drawLine(3,WYSOKOSC-5-10*i,7,WYSOKOSC-5-10*i);




g.setColor(Color.RED);
g.fillRect(15,280,20,1);
g.drawString("ilość osób które obsłużył egzaminator 1",50,285);
for(int f=0; f<10; f++)g.fillRect(15+ 2*f,300,1,1);
g.drawString("ilość osób którym zaliczyl egzaminator 1",50,305);
g.setColor(Color.ORANGE);
g.fillRect(15,320,20,1);
g.drawString("2",50,325);
g.setColor(Color.GREEN);
g.fillRect(15,340,20,1);
g.drawString("srednie",50,345);
for(int i=0;i<znacznik;i++){
g.setColor(Color.RED); // ogolem obsluzeni przez pierwszego egzaminatora
g.fillRect(5+2*i,WYSOKOSC-5-15*tablicaWr[0][i][0],2,1);
g.fillRect(5+2*i,WYSOKOSC-5-15*tablicaWr[0][i][1],1,1);
g.setColor(Color.ORANGE); // ogolem obsluzeni przez 2 egzaminatora
g.fillRect(5+2*i,WYSOKOSC-5-15*tablicaWr[1][i][0],2,1);
g.fillRect(5+2*i,WYSOKOSC-5-15*tablicaWr[1][i][1],1,1);
g.setColor(Color.GREEN); // ogolem obsluzeni przez 2 egzaminatora
g.fillRect(5+2*i,WYSOKOSC-5-(int)tablicaSr[i],2,1);

}
  }
  // dzięki tej metodzie obraz będzie mniej migał, co nie znaczy, Se nie będzie migał w ogóle :(
  public void update(Graphics g) {
    Image image = null;
    Graphics buffer = null;
    try{
      image = createImage(SZEROKOSC, WYSOKOSC);
      buffer = image.getGraphics();
      paint(buffer);
      if(image != null) g.drawImage(image,0,0,this);
      }catch(NullPointerException e){};
    }
}


//-----------------------------------------------------------------------------
// Kolejka ma całą masę parametrów, które są opisane w komentarzach poniSej
//-----------------------------------------------------------------------------
class Kolejka{
  int LICZNIK; // ilu Petentów stoi aktualnie w kolejce
  int POJEMNOSC; // ilu Petentów zmieści się do kolejki
  Petent[] ZAWARTOSC; // tablica Studentów
  // parametry symulacyjne:
  long[] obsluzonych = new long [5] ; // licznik obsluzonych studentow
   long[] zdali = new long[5]; //licznich tych ktorzy zdali
  double sredni; // sredni czas czekania w kolejce
//  long przepelnienie; // licznik zgubionych 
  long wygenerowanych; // licznik wygenerowanych studentow

  // konstruktor klasy Kolejka
  public Kolejka(int poj){ 
    POJEMNOSC = poj;
    ZAWARTOSC = new Petent[POJEMNOSC];
    LICZNIK = 0;
    for(int i = 0; i<5; i++) 
    {obsluzonych[i] = 0;
     zdali[i] = 0;
    }
     sredni = 0.0;
//    przepelnienie = 0;
    wygenerowanych = 0;
    }
  

  //dodanie do kolejki
  public boolean dodaj(Petent nowyPetent){
    if(LICZNIK == POJEMNOSC){
      return false;
      } // nie ma juz miejsca
    else{
      ZAWARTOSC[LICZNIK++] = new Petent(nowyPetent.ID,nowyPetent.CZAS,nowyPetent.nrEgzaminu);
      return true; // udalo sie
      }
    }

  // ktos wchodzi do egzaminatora, inni w kolejce robia kroczek do przodu
  public boolean usunZPrzodu(){
    if(LICZNIK==0)
      return false; // nikogo nie ma w kolejce
    else{
      for(int i=1;i<LICZNIK;i++) ZAWARTOSC[i-1] = ZAWARTOSC[i];
      LICZNIK--;
      return true; // udalo sie
      }
    }
    // zwraca pierwszego petenta z kolejki
  public Petent Pierwszy(){
    if(LICZNIK==0)
      return null; // nikogo nie ma w kolejce wiec zwracamy wartosc pusta
    else{
           return ZAWARTOSC[0]; // zwracamy dane pierwszego petenta
      }
    }
  public boolean pusta()
    {
    if (LICZNIK == 0) return true;
    return false;
    }      
  
  }

//-----------------------------------------------------------------------------
// Petent ma identyfikator (nr kolejny) oraz czas dodania
//-----------------------------------------------------------------------------
class Petent{
  long ID;
  long CZAS;
  int nrEgzaminu;
  public Petent(long id, long czas,int nregzaminu){ // konstruktor klasy Petent
    ID = id;
    CZAS = czas;
    nrEgzaminu = nregzaminu;
    }
  }

//-----------------------------------------------------------------------------
// klasa Egzaminator
//-----------------------------------------------------------------------------
class Egzaminator{
  boolean[] czyPusto , czyAwaria ;
  int[] cz_obslugi , czasNaprawy;
  
  public Egzaminator(){ // konstruktor klasy Dziekan
    czyPusto = new boolean[5];
    cz_obslugi = new int[5];
    czyAwaria = new boolean [5];
    czasNaprawy = new int [5];
    
    for(int i =0; i<5; i++) 
      {
      czyPusto[i] = true;
      cz_obslugi[i] = 0;
      czyAwaria[i] = false;
      czasNaprawy[i] = 0;
      }
    }

  
  //zwraca true gdy wszyscy egzaminatorzy sa zajeci
  //jezeli chociaz jeden jest wolny zwroc false
  public boolean zajeci(){
    for (int i=0; i<5; i++ ) 
      if ( czyPusto[i]==true ) return false;    
    return true;
    }
    //zwraca true gdy wszyscy egzaminatorzy sa wolni 
  //jezeli chociaz jeden jest zajety zwroc false
  public boolean wolni(){
    for (int i=0; i<5; i++ ) 
      if ( czyPusto[i]==false )  return false;    
    return true;
    }
  
  //zwraca numer pierwszego wolnego egzaminatora
  //lub 5 jesli wszyscy zajeci
  public int wolny(){
    int i;
    for ( i=0; i<5; i++ ) 
      if ( czyPusto[i]==true ) return i;
    return i;  
    }
  

  public int dlugoscEgzaminu(int[] prawdopod){
      int t = 1 ;
      int r;
      int [] ma;
      ma = new int[3];
      ma = prawdopod;
        /*
         * zakładamy że czas jest losowany zgodnie z rozkladem jednostajnym
         * */
      r = (int)(Math.random()*100); 
  
      //oblanie na placu 5-12 minut
      if (r <= ma[0]) t = (int)(Math.random()*7) + 5;
      //oblanie na miescie 20-40 minut
     if (r > ma[0] && r <= ma[0] + ma[1]) t = (int)(Math.random()*20+ 20);
      //zaliczenie egzaminu 45-60 minut
    if ( r > ma[0] + ma[1]) t = (int)(Math.random()*15 + 45);
      
      return t;
  }
  //odswierzenie stanu egzaminatorów po uplynieciu jednostki czasu
  public mieszanka odswiez(int[] minuta){      
    Random generator = new Random();
    mieszanka wynik = new mieszanka();
    int[] m;
    m = new int[5]; 
    m = minuta;

    

    for(int i = 0; i<5; i++)
      {
         double r = generator.nextDouble();
         if (czyAwaria[i] == true){
        if(czasNaprawy[i] == 15){  
          czyAwaria[i] = false; 
          czasNaprawy[i] = 0;
          }
        else czasNaprawy[i]++ ;
        
        }
      else if ( r <= 0.003 && czyAwaria[i] == false ){
          czyAwaria[i] = true; 
          czasNaprawy[i] = 0;
          }
      else
        { 
        
        
      if(czyPusto[i] == false && czyAwaria[i] == false )
        {
        cz_obslugi[i]++;   
        
        if( cz_obslugi[i] == minuta[i]){
          
            wynik.kto[wynik.ilu] = i;
            wynik.czas[wynik.ilu] = cz_obslugi[i];
            wynik.ilu++;
            //nie zalicza jezeli jezdzi co najwyzej 40 minut
            if(minuta[i]<41) wynik.zaliczyl[i] = false;
            // zeby zaliczyc egzamin trzeba przejechac 45-60 minut
            else wynik.zaliczyl[i] = true;
            czyPusto[i]=true;
            cz_obslugi[i]=0;
            }
          }
      }
      wynik.awaria[i]= czyAwaria[i];
      wynik.cz_awarii[i] = czasNaprawy[i];
      }

    return wynik;
    }
}
//-----------------------------------------------------------------------------
// klasa kolejka do dziekana
//-----------------------------------------------------------------------------

public class KolejkaDoEgzaminu extends Frame{
    Label podajTakt = new Label("Takt pracy zegara (ms):"); //podajemy domyślny napis na etykiecie
   // Label podajLiczbeZdarzen = new Label("Liczba zdarzeń:");
   // Label podajCzasObslugiStudenta = new Label("Czas obsługi studenta:");
   // Label podajCzasCzekaniaNaStudenta = new Label("Czas czekania na studenta:");
   // Label podajMaxWartoscZegara = new Label("Maksymalna wartość zegara:");
    Label podajPrawdopodobienstwa = new Label("prawdop. zdania [%]:");
    Label raz1 = new Label("pierwszy raz:");
     Label raz2 = new Label("drugi raz:");
      Label raz3 = new Label("trzeci raz:");
       Label raz4 = new Label(">3 raz:");
    Label podajIloscStudentow = new Label("Ilosc petentówdo obsluzenia:");
 
    Checkbox dajInfo = new Checkbox("Pokazuj informacje",true); //napis na checkboksie i czy go zaznaczyć
    TextArea info = new TextArea(10,20); //10 kolumn i 20 rzędów (lub na odwrót :/)
    TextField jakiTakt = new TextField("2",7); //”500” to tekst, który ma być wpisany juS od początku ...
  //  TextField liczbaZdarzen = new TextField("100",7); //... a 7 to szerokość pola (w znakach)
   TextField pr1p = new TextField("25",7); //prawdopodobienswo oblania za pierwszym razem na placu (w procentach
   TextField pr1m = new TextField("45",7); //pr oblania za 1 razem na miescie
    TextField pr1z = new TextField("30",7); //pr zdania za 1 razem
     TextField pr2p = new TextField("15",7); //prawdopodobienswo oblania za 2razem na placu (w procentach
   TextField pr2m = new TextField("40",7); //pr oblania za 2 razem na miescie
    TextField pr2z = new TextField("45",7); //pr zdania za 2 razem
     TextField pr3p = new TextField("10",7); //prawdopodobienswo oblania za 3 razem na placu (w procentach
   TextField pr3m = new TextField("50",7); //pr oblania za 3 razem na miescie
    TextField pr3z = new TextField("40",7); //pr zdania za 3 razem
      TextField pr4p = new TextField("8",7); //prawdopodobienswo oblania za 4 razem na placu (w procentach
   TextField pr4m = new TextField("42",7); //pr oblania za 4 razem na miescie
    TextField pr4z = new TextField("50",7); //pr zdania za 4 razem
    
  //  TextField ccStudenta = new TextField("10",7);
 //   TextField maxZegar = new TextField("40000000",7);
    TextField ilStudentow = new TextField("20",7);
    Button start = new Button("Start"); //podajemy domyślny napis na guziku
    Button stop = new Button("Stop");
    Button odNowa = new Button("Od nowa");
    Panel opcje = new Panel(); //panel po lewej
    Panel wizualizacja = new Panel(); //szeroki panel po prawej
    
    
    
    
    ObrazekIGeneratorZdarzen obrazek = new ObrazekIGeneratorZdarzen(800,540,20,this);

    

    public KolejkaDoEgzaminu() {
      super("Kolejka do egzaminu na prawo jazdy- symulacja");
      // kolorki – podajemy słownie (np. Color.BLACK) albo jako współrzędne RGB (moSna znaleźć w PaintBrushu)
      this.setBackground(Color.BLACK);
      opcje.setBackground(new Color(220,242,255));
      wizualizacja.setBackground(new Color(220,242,255));
      // gdzie beda panele i jaki maja kontener:
      this.setLayout(null);
      opcje.setLayout(null);
      wizualizacja.setLayout(null);
      opcje.setBounds(10,35,300,550);
      wizualizacja.setBounds(320,35,700,550);


      // ustalamy połoSenie i wymiary komponentów, dodajemy komponenty do panela
      podajTakt.setBounds(10,10,170,20); opcje.add(podajTakt);
      //podajLiczbeZdarzen.setBounds(10,30,170,20); opcje.add(podajLiczbeZdarzen);
     // podajCzasObslugiStudenta.setBounds(10,50,170,20); opcje.add(podajCzasObslugiStudenta);
     // podajCzasCzekaniaNaStudenta.setBounds(10,70,170,20);opcje.add(podajCzasCzekaniaNaStudenta);
     // podajMaxWartoscZegara.setBounds(10,90,170,20); opcje.add(podajMaxWartoscZegara);
      podajPrawdopodobienstwa.setBounds(10,30,170,20);opcje.add(podajPrawdopodobienstwa);
      raz1.setBounds(10,50,170,20);opcje.add(raz1);
       raz2.setBounds(10,70,170,20);opcje.add(raz2);
        raz3.setBounds(10,90,170,20);opcje.add(raz3);
         raz4.setBounds(10,110,170,20);opcje.add(raz4);
      podajIloscStudentow.setBounds(10,135,170,20); opcje.add(podajIloscStudentow);
      jakiTakt.setBounds(190,10,70,20); opcje.add(jakiTakt);
      //liczbaZdarzen.setBounds(190,30,70,20); opcje.add(liczbaZdarzen);
      pr1p.setBounds(180,50,25,20); opcje.add(pr1p);
      pr1m.setBounds(210,50,25,20); opcje.add(pr1m);
      pr1z.setBounds(240,50,25,20); opcje.add(pr1z);
       pr2p.setBounds(180,70,25,20); opcje.add(pr2p);
      pr2m.setBounds(210,70,25,20); opcje.add(pr2m);
      pr2z.setBounds(240,70,25,20); opcje.add(pr2z);      
       pr3p.setBounds(180,90,25,20); opcje.add(pr3p);
      pr3m.setBounds(210,90,25,20); opcje.add(pr3m);
      pr3z.setBounds(240,90,25,20); opcje.add(pr3z);
       pr4p.setBounds(180,110,25,20); opcje.add(pr4p);
      pr4m.setBounds(210,110,25,20); opcje.add(pr4m);
      pr4z.setBounds(240,110,25,20); opcje.add(pr4z);
     // ccStudenta.setBounds(190,70,70,20); opcje.add(ccStudenta);
     // maxZegar.setBounds(190,90,70,20); opcje.add(maxZegar);
      ilStudentow.setBounds(190,135,70,20); opcje.add(ilStudentow);
      start.setBounds(10,160,80,20); opcje.add(start);
      stop.setBounds(95,160,80,20); opcje.add(stop);
      stop.setEnabled(false); // guzik stopu ma na początku być nieaktywny
      odNowa.setBounds(180,160,80,20); opcje.add(odNowa);
      dajInfo.setBounds(10,190,120,20); opcje.add(dajInfo);
      info.setBounds(10,210,280,340); opcje.add(info);
      obrazek.setBounds(5,5,700,550);wizualizacja.add(obrazek);
      
      // dodajemy panele do okna:
      add(opcje);
      add(wizualizacja);

      start.addActionListener(new BStartL());
      stop.addActionListener(new BStopL());
      odNowa.addActionListener(new BOdNowaL());
      }
    

    
    
public class BStartL implements ActionListener{
  public void actionPerformed(ActionEvent e){
  int op,ils;
  int [][] prawdop;
  prawdop = new int[4][3];
   
  try{ // pobieramy parametry z TextFieldow
    op = Integer.parseInt(jakiTakt.getText());
    prawdop[0][0] = Integer.parseInt(pr1p.getText());
    prawdop[0][1] = Integer.parseInt(pr1m.getText());
    prawdop[0][2] = Integer.parseInt(pr1z.getText());
    prawdop[1][0] = Integer.parseInt(pr2p.getText());
    prawdop[1][1] = Integer.parseInt(pr2m.getText());
    prawdop[1][2] = Integer.parseInt(pr2z.getText());
    prawdop[2][0] = Integer.parseInt(pr3p.getText());
    prawdop[2][1] = Integer.parseInt(pr3m.getText());
    prawdop[2][2] = Integer.parseInt(pr3z.getText());
    prawdop[3][0] = Integer.parseInt(pr4p.getText());
    prawdop[3][1] = Integer.parseInt(pr4m.getText());
    prawdop[3][2] = Integer.parseInt(pr4z.getText());
    
    ils = Integer.parseInt(ilStudentow.getText());
    // jezeli uzytkownik podal liczby ktore sie nie sumuja do 100% to prawdopodobienstwo
    // zdania jest rowne 100 - pozostale prawdopod.
    for(int j=0; j<4; j++){
        if (prawdop[j][0] + prawdop[j][1] + prawdop[j][2] != 100 )  prawdop[j][2] = 100- (prawdop[j][0] + prawdop[j][1]) ;
    }
    
    obrazek.pobierzParametry(op,ils,prawdop);
    }catch(NumberFormatException wyjatek){
      // jezeli nie wprowadzil liczb tylko jakies bzdury to wpisujemy poczatkowe wartosci i je przesylamy
      jakiTakt.setText("500");
      pr1p.setText ("25");
      pr1m.setText("45");
   pr1z.setText("30"); 
     pr2p.setText("15"); 
   pr2m.setText("40"); //pr oblania za 2 razem na miescie
    pr2z.setText("45"); //pr zdania za 2 razem
     pr3p.setText("10"); //prawdopodobienswo oblania za 3 razem na placu (w procentach
   pr3m.setText("50"); //pr oblania za 3 razem na miescie
    pr3z.setText("40"); //pr zdania za 3 razem
      pr4p.setText("8"); //prawdopodobienswo oblania za 4 razem na placu (w procentach
   pr4m.setText("42"); //pr oblania za 4 razem na miescie
    pr4z.setText("50");
    prawdop[0][0] = 25;
    prawdop[0][1] = 45;
    prawdop[0][2] = 30;
    prawdop[1][0] = 15;
    prawdop[1][1] = 40;
    prawdop[1][2] = 45;
    prawdop[2][0] = 10;
    prawdop[2][1] = 50;
    prawdop[2][2] = 40;
    prawdop[3][0] = 8;
    prawdop[3][1] = 42;
    prawdop[3][2] = 50;
      obrazek.pobierzParametry(500,20,prawdop);
      }
//narozrabiano tu:)
  if(!obrazek.biegnie){
    obrazek.zegarek.start();
    obrazek.biegnie = true;
    }
  else obrazek.zegarek.resume();
    start.setEnabled(false);
    stop.setEnabled(true);
    }
}


public class BStopL implements ActionListener{
  public void actionPerformed(ActionEvent e){
    obrazek.zegarek.suspend();
    start.setEnabled(true);
    stop.setEnabled(false);
    }
  }

public class BOdNowaL implements ActionListener{
  public void actionPerformed(ActionEvent e){
    obrazek.odnowZmienne();
    int op,ils;
    int[][] prawdop;
    prawdop = new int[4][3];
    
    
    try{ // pobieramy parametry z TextFieldow
      op = Integer.parseInt(jakiTakt.getText());
    prawdop[0][0] = Integer.parseInt(pr1p.getText());
    prawdop[0][1] = Integer.parseInt(pr1m.getText());
    prawdop[0][2] = Integer.parseInt(pr1z.getText());
    prawdop[1][0] = Integer.parseInt(pr2p.getText());
    prawdop[1][1] = Integer.parseInt(pr2m.getText());
    prawdop[1][2] = Integer.parseInt(pr2z.getText());
    prawdop[2][0] = Integer.parseInt(pr3p.getText());
    prawdop[2][1] = Integer.parseInt(pr3m.getText());
    prawdop[2][2] = Integer.parseInt(pr3z.getText());
    prawdop[3][0] = Integer.parseInt(pr4p.getText());
    prawdop[3][1] = Integer.parseInt(pr4m.getText());
    prawdop[3][2] = Integer.parseInt(pr4z.getText());
   
      ils = Integer.parseInt(ilStudentow.getText()); 
      obrazek.pobierzParametry(op,ils,prawdop);
      }
    catch(NumberFormatException wyjatek){
      // jeSeli nie wprowadzil liczb tylko jakies bzdury to wpisujemy poczatkowe wartosci i je przesylamy
     jakiTakt.setText("500");
      pr1p.setText ("25");
      pr1m.setText("45");
   pr1z.setText("30"); 
     pr2p.setText("15"); 
   pr2m.setText("40"); //pr oblania za 2 razem na miescie
    pr2z.setText("45"); //pr zdania za 2 razem
     pr3p.setText("10"); //prawdopodobienswo oblania za 3 razem na placu (w procentach
   pr3m.setText("50"); //pr oblania za 3 razem na miescie
    pr3z.setText("40"); //pr zdania za 3 razem
      pr4p.setText("8"); //prawdopodobienswo oblania za 4 razem na placu (w procentach
   pr4m.setText("42"); //pr oblania za 4 razem na miescie
    pr4z.setText("50");
    prawdop[0][0] = 25;
    prawdop[0][1] = 45;
    prawdop[0][2] = 30;
    prawdop[1][0] = 15;
    prawdop[1][1] = 40;
    prawdop[1][2] = 45;
    prawdop[2][0] = 10;
    prawdop[2][1] = 50;
    prawdop[2][2] = 40;
    prawdop[3][0] = 8;
    prawdop[3][1] = 42;
    prawdop[3][2] = 50;
      obrazek.pobierzParametry(500,20,prawdop);
      }
    obrazek.repaint();
    }
  }
    
static class WL extends WindowAdapter{
  public void windowClosing(WindowEvent e){
    System.exit(0);
    }
  }
    
public static void main(String[] args) {
  Frame f=new KolejkaDoEgzaminu(); //deklaracja i inicjalizacja okna
  f.addWindowListener(new WL()); //mówimy oknu, Se ma się zamykać gdy klikniemy „X” w górnym prawym rogu
  f.setSize(1024,600); //ustalamy rozmiar okna
  f.setVisible(true); //okno ma być widoczne
  }
}


class mieszanka{
int ilu; // ewentualnosc gdy obaj egzaminatorzy na raz skoncza egzamin
int [] kto;
int [] czas;
boolean[] zaliczyl;
boolean [] awaria;
int [] cz_awarii;
        
public mieszanka()
  {
  kto = new int[5];
  czas = new int[5];
  ilu = 0;
  zaliczyl = new boolean[5];
  awaria = new boolean[5];
  cz_awarii = new int[5];
  }
}

