# Simple Midi Parser
A simple midi parser easily modifiable to add functionality or to quickly use to debug midi files

## How to use in your project
Copy the SimpleMidiParser.kt file into your codebase. You can then modify the file at your will 
or simply use it as it is. 

## Notes Regarding the parser
The parser splits the stream into arrays which contains the header chunk and all the track chunks.
If you want to modify how the midi events are parsed you will need to look into the function 
`extractMidiEventsFromByteArray`. There is a switch case which looks at the first 4 bit of the 
status byte of an event and parse it according to its type. 

## Links
Official midi specs: https://www.midi.org/specifications  
Old Midi File Format Tutorial Page: http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html#BM2_3  
MuseScore forum post about 0 velocity note on event: https://musescore.org/en/node/115586  
Old blog post about 0 velocity note on events: https://www.kvraudio.com/forum/viewtopic.php?p=4167096

## Warning! The SMPTE division has not been tested. Coder discretion is advised.
