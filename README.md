# Midi Parser
A robust MIDI parser which can parse and validate any MIDI files.

## How to use in your project
The easiest way to use this library is to clone the repo and put the jar file in your local Maven 
repository using the command
```bash
./gradlew publishToMavenLocal
```
Next, you can easily include the project in Gradle with
```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.ejrp.midi:MidiParser:1.0")
}
```
or in Maven with
```xml
<dependency>
    <groupId>com.ejrp.midi</groupId>
    <artifactId>MidiParser</artifactId>
    <version>1.0</version>
</dependency>
```

## Parsing MIDI files
To parse a MIDI file, use the `StandardMidiFileClass`
```kotlin
val file = File("input.mid")
val midiFile = StandardMidiFile.fromInputStream(file.inputStream())
```
### Error handling
Different exceptions will be thrown if the structure of the MIDI file is invalid. Look inside the 
<tt> MidiExceptions.kt </tt> file for possible exceptions thrown by the parser.

## Writing MIDI files
To write a MIDI file, create a `StandardMidiFileClass` using its constructor. You will be working
directly with header/tracks chunks and MIDI events to make your MIDI file. Check out the links below
to read more about the MIDI specs if you have never worked with it.

> **Warning**: You can create invalid MIDI files this way

Here is an example of a basic MIDI file which plays a C-Major chord
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
To write the MIDI file to a file:
```kotlin
val file = File("output.mid")
midiFile.toOutputStream(file.outputStream())
```

## Notes Regarding the parser
Despite what the MIDI specification says, this parser does not support MIDI file formats other than 0, 
1, and 2. It also will not parse the file if the track chunk has any length other than 6. This means that 
if you are using an extended specification of the MIDI file format, this parser will need to be modified
to support it.

## Links
Official MIDI specs: https://www.midi.org/specifications  
Old MIDI File Format Tutorial Page: http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html#BM2_3  
