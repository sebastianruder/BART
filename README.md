BART
====

Stanford sieve implementation in BART coreference resolution system

BART, das ”Beautiful Anaphora Resolution Toolkit”, wurde beim Projekt ”Exploit-
ing Lexical and Encyclopedic Resources For Entity Disambiguation” am Johns Hopkins

Summer Workshop 2007 erstellt. BART unternimmt automatische Koreferenzresolu-
tion mithilfe einer modularen Pipeline, die aus einer Vorverarbeitungsphase (Daten von

MMAX2-Annotationsebenen werden aggregiert), der Extraktion der NP-Kandidaten,

der Extraktion der NP-Merkmale und der Kandidatenpaare sowie aus einem Resolu-
tionsmodell besteht. BART verwendet momentan einen auf einem Ansatz von Soon

basierenden Resolutionsalgorithmus, der Kandidaten-NPs hinsichtlich ihrer Merkmale

paarweise vergleicht. Statt diesem soll nun das Resolutionssystem der Stanford-NLP-
Gruppe (im Folgenden Stanford-System) implementiert werden, das sich durch seine

Sieb-Architektur auszeichnet. Obwohl es hauptsächlich auf Regeln basiert, konnte es

dennoch das beste Ergebnis beim CoNLL-2011 shared task erzielen. Im Rahmen der

Sieb-Architektur werden nacheinander - absteigend nach ihrer Präzision geordnet - eine

Reihe von deterministischen Koreferenzmodellen angewendet, wobei jedes Modell auf

den Output seines Vorg¨angers aufbaut. Besonders das Entit¨at-zentrische Modell, in bei

dem Merkmale ¨uber alle Vorkommen einer Entit¨at geteilt werden, bietet einen deutlichen

Wissensgewinn, der von Nutzen für BARTs Performanz sein wird.

===

Here goes an example.
