# Midi Parser
A robust midi parser which can parse and validate any midi files.

## How to use in your project
The easiest way to use this library is to clone the repo and put the jar file in your local maven 
repository using the command
```bash
./gradlew publishToMavenLocal
```
Next you can easily include the project in gradle with
```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.ejrp.midi:MidiParser:1.0")
}
```
or in maven with
```xml
<dependency>
    <groupId>com.ejrp.midi</groupId>
    <artifactId>MidiParser</artifactId>
    <version>1.0</version>
</dependency>
```

## Parsing midi files
To parse a midi file use the `StandardMidiFileClass`
```kotlin
val file = File("input.mid")
val midiFile = StandardMidiFile.fromInputStream(file.inputStream())
```
### Error handling
Different exceptions will be thrown if the structure of the midi file is invalid. Look inside the 
<tt> MidiExceptions.kt </tt> file for every exception that can be thrown by the parser.

## Writing midi files
To write a midi file, create a `StandardMidiFileClass` using its constructor. You will be working
directly with header/tracks chunks and midi events to create your midi file. Check out the links below
to read more about the midi specs if you have never worked with it.

> **Warning**: You can create invalid midi files this way

Here is an example of a basic midi files which plays a C-Major chord
```kotlin
val midiFile = StandardMidiFile(
    HeaderChunk(0u, 1u, TicksPerQuarterNoteDivision(96u)),
    MidiSequence(listOf(
        TrackChunk(listof(
            NoteOn(
                VariableLengthQuantity(0u), 0, MidiKey.C4, MidiVelocity.Forte
            ),
            NoteOn(
                VariableLengthQuantity(0u), 0, MidiKey.E4, MidiVelocity.Forte
            ),
            NoteOn(
                VariableLengthQuantity(0u), 0, MidiKey.G4, MidiVelocity.Forte
            ),
            // a delta-time of 96 is a quarter note since there are 96 ticks per quarter note
            NoteOn(
                VariableLengthQuantity(96u), 0, MidiKey.C4, MidiVelocity.Niente
            ),
            NoteOn(
                VariableLengthQuantity(96u), 0, MidiKey.E4, MidiVelocity.Niente
            ),
            NoteOn(
                VariableLengthQuantity(96u), 0, MidiKey.G4, MidiVelocity.Niente
            ),
            EndOfTrack(VariableLengthQuantity(0u))
        ))
    ))
)
```
To write the midi file to a file simple do as such:
```kotlin
val file = File("output.mid")
midiFile.toOutputStream(file.outputStream())
```

## Notes Regarding the parser
Despite what the midi specification says, this parser does not support midi file formats other than 0, 
1, and 2. It also will not parse the file if the track chunk has any other length than 6. This means that 
if you are using an extended specification of the midi file format this parser will need to be modified
to support it.

## Links
Official midi specs: https://www.midi.org/specifications  
Old Midi File Format Tutorial Page: http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html#BM2_3  
