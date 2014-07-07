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

Die main-Methode des [SieveAnnotator](src/elkfed/main/SieveAnnotator.java) muss nun gestartet
werden.

## Repository Struktur

Relevante Dateien im _repository_:

src/
	elkfed/
		coref/
		Hier ist das Interface CorefResolver
			algorithms/
				sieve/
				Hier sind alle Siebklassen und -methoden
				soon/
				Hier sind die verschiedenen Soon-Algorithmen von BART
			discourse_entities/
			Hier ist die neue sowie die alte Implementation der DiscourseEntity
			eval/
			Hier ist der MUCScorer, der zur Evaluation verwendet wird
			features/
				pairs/
				Hier sind einige Feature-Extraktoren, die wir verwendeten
			mentions/
				Hier ist die Mention-Klasse sowie die MentionFactory
			processors/
				Hier ist der AnnotationProcessor
		lang/
		Hier sind die verschiedenen LanguagePlugins und LinguisticConstants
		main/
		Hier ist das Hauptprogramm, SieveAnnotator
		nlp/
			util/
			Hier ist die Gender-Klasse
config/
Hier ist config.properties, in der die Dateipfade angegeben werden
Dokumente/
Hier liegen unsere Präsentationsfolien
names/
Hier befinden sich sprachspezifische Listen
tuebadz-MMAX2/
Hier befinden sich unsere Korpora sowie ein Skript zur Konversion in plain text
index.html
index.xml
INSTALL
README.md

Klicke [hier](http://htmlpreview.github.io/?https://github.com/sebastianruder/BART/blob/master/BART/index.html), um
zur Webseite zu gelangen.

