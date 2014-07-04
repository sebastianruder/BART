# Implementation der Stanford-Sieb-Architektur im BART-Koreferenz-Resolutionssystem

## Projektbeschreibung

BART, das _Beautiful Anaphora Resolution Toolkit_, wurde beim Projekt _Exploiting Lexical
and Encyclopedic Resources For Entity Disambiguation_ am Johns Hopkins Summer Workshop 2007
erstellt. BART unternimmt automatische Koreferenzresolution mithilfe einer modularen Pipeline,
die aus einer Vorverarbeitungsphase (Daten von MMAX2-Annotationsebenen werden aggregiert),
der Extraktion der NP-Kandidaten, der Extraktion der NP-Merkmale und der Kandidatenpaare
sowie aus einem Resolutionsmodell besteht. BART verwendet momentan einen auf einem Ansatz
von Soon basierenden Resolutionsalgorithmus, der Kandidaten-NPs hinsichtlich ihrer Merkmale
paarweise vergleicht. Statt diesem soll nun das Resolutionssystem der Stanford-NLP-Gruppe
(im Folgenden Stanford-System) implementiert werden, das sich durch seine Sieb-Architektur
auszeichnet. Obwohl es hauptsäachlich auf Regeln basiert, konnte es dennoch das beste Ergebnis
beim CoNLL-2011 shared task erzielen. Im Rahmen der Sieb-Architektur werden nacheinander -
absteigend nach ihrer Präzision geordnet - eine Reihe von deterministischen Koreferenzmodellen
angewendet, wobei jedes Modell auf den Output seines Vorgäangers aufbaut. Besonders das
Entität-zentrische Modell, in bei dem Merkmale über alle Vorkommen einer Entität geteilt
werden, bietet einen deutlichen Wissensgewinn, der von Nutzen für BARTs Performanz sein wird.

## Inbetriebnahme

In [config.properties](config/config.properties) muss für den Parameter ```testdata```
das Verzeichnis angegeben werden, indem sich der zu verwendende MMAX2-Datensatz auf dem eigenen
System befindet, bspw. auf einem Windows-System:
```
testData=D:/BART/BART/tuebadz-MMAX2/mmax-mini
```

Die main-Methode des [SieveAnnotator](src/de/elkfed/main/SieveAnnotator) muss nun gestartet
werden.