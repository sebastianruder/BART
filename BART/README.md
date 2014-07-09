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

Dieses [GitHub repository](https://github.com/sebastianruder/BART) muss zunächst geklont werden.

Es sollte Eclipse mit den Plugins [EGit](http://www.eclipse.org/egit/) (zur Versionskontrolle)
und [IvyDE](http://ant.apache.org/ivy/ivyde/) (zum _dependency management_)
verwendet werden. Die Dependenzen gehen aus [ivy.xml](ivy.xml) hervor, während die Einstellungen
in [ivysettings.xml](ivysettings.xml) gespeichert sind.

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

* [src/elkfed/coref](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref):
Hier befindet sich das Interface [CorefResolver](src/elkfed/coref/CorefResolver.java)
* [src/elkfed/coref/algorithms/sieve](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/algorithms/sieve):
Hier befinden sich eine [Utility-Klasse](src/elkfed/coref/algorithms/sieve/SieveUtilities.java),
eine [Factory-Klasse](src/elkfed/coref/algorithms/sieve/SieveFactory.java) und alle Sieb-Klassen
  1. [SpeakerIdentificationSieve](src/elkfed/coref/algorithms/sieve/SpeakerIdentificationSieve.java)
  2. [StringMatchSieve](src/elkfed/coref/algorithms/sieve/StringMatchSieve.java)
  3. [RelaxedStringMatchSieve](src/elkfed/coref/algorithms/sieve/RelaxedStringMatchSieve.java)
  4. [PreciseConstructSieve](src/elkfed/coref/algorithms/sieve/PreciseConstructSieve.java)
  5. [StrictHeadMatchASieve](src/elkfed/coref/algorithms/sieve/StrictHeadMatchASieve.java)
  6. [StrictHeadMatchBSieve](src/elkfed/coref/algorithms/sieve/StrictHeadMatchBSieve.java)
  7. [StrictHeadMatchCSieve](src/elkfed/coref/algorithms/sieve/StrictHeadMatchCSieve.java)
  8. [ProperHeadNounMatchSieve](src/elkfed/coref/algorithms/sieve/ProperHeadNounMatchSieve.java)
  9. [RelaxedHeadMatchSieve](src/elkfed/coref/algorithms/sieve/RelaxedHeadMatchSieve.java)
  10. [PronounMatchSieve](src/elkfed/coref/algorithms/sieve/PronounMatchSieve.java)
* [src/elkfed/coref/algorithms/soon](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/algorithms/soon):
Hier befinden sich die verschiedenen Soon-Algorithmen von BART
* [src/elkfed/coref/discourse_entities](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/discourse_entities):
Hier ist die [neue](src/elkfed/coref/discourse_entities/DiscourseEntity.java) sowie die [alte](src/elkfed/coref/discourse_entities/DiscourseEntity.java)
Implementation der DiscourseEntity
* [src/elkfed/coref/eval](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/eval):
Hier ist der [MUCScorer](src/elkfed/coref/eval/MUCScorer.java), der zur Evaluation verwendet wird
* [src/elkfed/coref/features/pairs](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/features/pairs):
Hier befinden sich einige Feature-Extraktoren, die wir verwendeten (aufzählen)
* [src/elkfed/coref/mentions](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/features/mentions):
Hier befindet sich die [Mention-Klasse](src/elkfed/coref/mentions/Mention.java) sowie die [MentionFactory](src/elkfed/coref/mentions/AbstractMentionFactory.java)
* [src/elkfed/coref/processors](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/coref/features/processors):
Hier befindet sich der [AnnotationProcessor](src/elkfed/coref/processors/AnnotationProcessor.java)
* [src/elkfed/lang](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/lang):
Hier sind die verschiedenen LanguagePlugins und LinguisticConstants
* [src/elkfed/main](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/main):
Hier befindet sich das Hauptprogramm, [SieveAnnotator](src/elkfed/main/SieveAnnotator.java)
* [src/elkfed/nlp/util](https://github.com/sebastianruder/BART/tree/master/BART/src/elkfed/nlp/util):
Hier befindet sich die [Gender-Klasse](src/elkfed/nlp/util/Gender.java)
* [config](https://github.com/sebastianruder/BART/tree/master/BART/config):
Hier befindet sich [config.properties](config/config.properties), in der die Dateipfade angegeben werden
* [documents](https://github.com/sebastianruder/BART/tree/master/BART/documents):
Hier liegen unsere Präsentationsfolien
* [names](https://github.com/sebastianruder/BART/tree/master/BART/names):
Hier befinden sich sprachspezifische Listen
* [tuebadz-MMAX2](https://github.com/sebastianruder/BART/tree/master/BART/tuebadz-MMAX2):
Hier befinden sich unsere Korpora sowie [ein Skript](tuebadz-MMAX2/xml2txt.py) zur Konversion in _plain text_