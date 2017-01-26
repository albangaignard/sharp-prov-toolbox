# SHARP provenance toolbox

## Synopsis
SHARP is a reasearch prototype aimed at harmonizing heterogeneous provenance graphs through reasoning (PROV inferences). [PROV Constraints inferences rules](https://www.w3.org/TR/prov-constraints/) have been implemented through the JENA forward chaining inference engine. 

## Motivations
Several ontologies have been proposed to extend the multi-purpose PROV-O ontology, such as [prov-one](http://vcvcomputing.com/provone/provone.html), [wfprov](http://lov.okfn.org/dataset/lov/vocabs/wfprov). The mutiplication of PROV-extended vocabularies makes it diffcult to link and cross-exploit multi-systems provenance graphs. In addition, even if workflow management systems produce strictly PROV-O entities, there is no guaranty that the same classes and properties are used, which possibly leads to non joinable graphs. 

**With SHARP, our goal is to propose reasoning mechanisms to produce harmonized provenance graphs, that can be more easily linked together.**

## Usage in command line
SHARP is available as a standalone command line interface. 
   java -jar ... -h

## Usage as an API

## License
