"use strict";
var Poker = Poker || {};

/**
 * This creates a sound source. The differences between WebAudio and Html Audio
 * sounds require different models for loading and playing sound data. This class
 * handles some of that.
 *
 * @type {Poker.SoundSource}
 */
Poker.SoundSource = function (url, audioModel, context) {

    this.url = url;
    this.context = context;
    this.source = {};

    this.play = function () {
        this.playSource();
    };

    this.setGain = function(gain) {
        this.source.volume = gain;
    };

    this.playSource = function() {
        this.source.play();
    };

    this.load = function (audioModel, context) {
        var url = this.url;
        switch (audioModel) {
            case "Audio":
                console.log("Audio sound created.");
                this.source = new Audio([url]);
            break;
            case "webkitAudioContext":
                this.loadXHR(url, this.source, context);

                this.source.volume = 1;
                var source = this.source;

                this.source.play = function() {
                    // TODO: Check behaviour on low bandwidth. May behave unpleasantly.
                    if (!source.soundBuffer) return;
                    var sourceNode = context.createBufferSource();
                    sourceNode.buffer = source.soundBuffer;
                    sourceNode.gain.value = source.volume;
                    sourceNode.connect(context.destination);
                    sourceNode.noteOn(0);
                };

            break;
        }

    };

    // Use XHR for loading to the WebAudio player - superior quality on all supported devices
    this.loadXHR = function(url, source, context) {
        source.url = url;

        source[url] = new XMLHttpRequest();
        source[url].open('GET', source.url, true);
        source[url].responseType = 'arraybuffer';

        // Decode asynchronously
        var request = source[url];

        source[url].onload = function() {
            var onError = function() { console.log("Failed decoding sound " + url) };

            context.decodeAudioData(request.response, function(buffer) {
                source.soundBuffer = buffer;
            }, onError)
        };

        source[url].onError = function() {
            console.log("load Error!: "+url)
        };

        source[url].send();
    };

    this.load(audioModel, context);
};

