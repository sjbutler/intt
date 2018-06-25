# intt

intt (Identifier Name Tokeniser Tool) is a Java library for splitting 
identifier names into their component tokens.

intt is an aggressive tokeniser that tokenises names using conventional 
typographical boundaries, such as camel case and separators, and 
unconventional boundaries like the upper case to lower case transition 
(e.g. as in HTMLEditorKit where the first boundary is implied by the 
typography). intt also includes a novel approach to the tokenisation of
names containing digits and includes novel algorithms 
for tokenising single case strings composed of words and abbreviations.
 
## Copyright & Licence
intt is Copyright (C) 2010-2015 The Open University with improvements 
Copyright (C) 2017 Simon Butler. intt is released under the terms of 
the Apache Licence v2.

intt v0.8.2 is available from Maven central. Gradle users should use 
the line `compile 'uk.org.facetus:intt:0.8.2'`. 

## Requirements
### Java
intt requires Java v8. 
### Dependencies
* mdsc - intt uses word lists derived from the SCOWL word lists and other 
sources found in mdsc (https://github.com/sjbutler/mdsc).
* SLF4J - intt uses SLF4J for logging and requires the slf4j-api-1.7.x jar file 
to be on the classpath and the relevant slf4j jar file for your chosen logging 
system.

## Documentation
The API is documented in the javadocs, which are in a zip archive in the docs 
folder. The techniques implemented in intt are described in the research paper 
referenced in the "Citation" section below. A more detailed prose description 
of the algorithms will eventually appear in the docs folder.

## Citation
If you use intt in academic research please cite the following paper:

   Simon Butler, Michel Wermelinger, Yijun Yu, and Helen Sharp 
   ‘Improving the tokenisation of identifier names’, 
   Proceedings of the 25th European Conference on Object-Oriented Programming, 
   LNCS 6813, Springer Berlin/Heidelberg, 25–29 Jul 2011, Lancaster, UK.

Also be clear about which version you are using. The open source version of intt 
is based on the version described in the above paper and has a range of 
enhancements including improved dictionaries. There will also have been minor
revisions to the algorithms used. 

## Caveat

intt is research software. The API may not be stable, and there is no guarantee 
that the code will be maintained.


