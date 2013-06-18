"use strict";
var Poker = Poker || {};

/**
 * The sound player is responsible for playing sounds in the client.
 *
 * @type {Poker.SoundPlayer}
 */
Poker.SoundPlayer = Class.extend({

    /**
    * @type Poker.SoundRepository
    */
    soundsRepository:null,


    init:function (soundRepository) {
        this.soundsRepository = soundRepository;
    },

    play:function (soundData, selection) {
        var sound = this.soundsRepository.getSound(soundData.id, selection);
        if (sound) {
            sound.play();
        } else {
            console.log("No sound found.");
        }
    }
});