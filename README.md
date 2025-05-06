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

## Notes Regarding the parser
Despite what the midi specification says, this parser does not support midi file formats other than 0, 
1, and 2. It also will not parse the file if the track chunk has any other length than 6. This means that 
if you are using an extended specification of the midi file format this parser will need to be modified
to support it.

## Links
Official midi specs: https://www.midi.org/specifications  
Old Midi File Format Tutorial Page: http://www.music.mcgill.ca/~ich/classes/mumt306/StandardMIDIfileformat.html#BM2_3  
