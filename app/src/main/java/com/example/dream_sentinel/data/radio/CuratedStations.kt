package com.example.dream_sentinel.data.radio

/**
 * Ręcznie utowrzona lista startowa.
 *
 * Wszystkie strumienie są publiczne i nadają po HTTPS.
 * Lista jest zapisywana do DataStore tylko przy pierwszym uruchomieniu;
 * później użytkownik może ją dowolnie modyfikować (dodawać/usuwać stacje).
 */
object CuratedStations {

    val all: List<RadioStation> = listOf(
        RadioStation(
            id = "somafm-groovesalad",
            name = "Groove Salad",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            genre = "Chillout",
        ),
        RadioStation(
            id = "somafm-dronezone",
            name = "Drone Zone",
            streamUrl = "https://ice1.somafm.com/dronezone-128-mp3",
            genre = "Ambient",
        ),
        RadioStation(
            id = "somafm-deepspaceone",
            name = "Deep Space One",
            streamUrl = "https://ice1.somafm.com/deepspaceone-128-mp3",
            genre = "Space ambient",
        ),
        RadioStation(
            id = "somafm-spacestationsoma",
            name = "Space Station Soma",
            streamUrl = "https://ice1.somafm.com/spacestation-128-mp3",
            genre = "Ambient",
        ),
        RadioStation(
            id = "somafm-lush",
            name = "Lush",
            streamUrl = "https://ice1.somafm.com/lush-128-mp3",
            genre = "Vocal chill",
        ),
        RadioStation(
            id = "somafm-fluid",
            name = "Fluid",
            streamUrl = "https://ice1.somafm.com/fluid-128-mp3",
            genre = "Instrumental hip-hop",
        ),
        RadioStation(
            id = "radio-paradise-main",
            name = "Radio Paradise",
            streamUrl = "https://stream.radioparadise.com/mp3-128",
            genre = "Eclectic",
        ),
        RadioStation(
            id = "radio-paradise-mellow",
            name = "Radio Paradise Mellow",
            streamUrl = "https://stream.radioparadise.com/mellow-128",
            genre = "Mellow mix",
        ),
        RadioStation(
            id = "radio-smooth-70s",
            name = "Radio Smooth 70s",
            streamUrl = "https://ice3.securenetsystems.net/S70S",
            genre = "Jazz",
        ),
        RadioStation(
            id = "radio-rmf",
            name = "Radio RMF",
            streamUrl = "https://rs102-krk.rmfstream.pl/rmf_polski_rock",
            genre = "Polish rock",
        ),
    )
}
