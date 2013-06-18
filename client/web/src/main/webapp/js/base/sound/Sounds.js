"use strict";
var Poker = Poker || {};



Poker.Sounds = {

    DEAL_PLAYER:                  {id:"DEAL_PLAYER",         delay:0,     soundList:[{file:"card_1"      ,gain:1}, {file:"card_2"     ,gain:1}, {file:"card_3"     ,gain:1}, {file:"card_4"     ,gain:1}]},
    DEAL_COMMUNITY:               {id:"DEAL_COMMUNITY",      delay:100,   soundList:[{file:"card_7"      ,gain:1}, {file:"card_8"     ,gain:1}, {file:"card_9"     ,gain:1}]},
    REVEAL:                       {id:"REVEAL:",             delay:60,    soundList:[{file:"cardheavy_1" ,gain:1}, {file:"cardheavy_2",gain:1}, {file:"cardheavy_3",gain:1}]},
    BUY_IN_COMPLETED:             {id:"BUY_IN_COMPLETED",    delay:0,     soundList:[{file:"chippile_5"  ,gain:1}]},
    REQUEST_ACTION:               {id:"REQUEST_ACTION",      delay:0,     soundList:[{file:"clocktick_2" ,gain:1}]},
    MOVE_DEALER_BUTTON:           {id:"MOVE_DEALER_BUTTON",  delay:300,   soundList:[{file:"arp_1"       ,gain:0.2}]},
    POT_TO_PLAYERS:               {id:"POT_TO_PLAYERS",      delay:460,   soundList:[{file:"sweep_6"     ,gain:0.1}]},
    "action-call"                :{id:"CALL"              ,  delay:0,     soundList:[{file:"chippile_6"  ,gain:0.2}]    },
    "action-check"               :{id:"CHECK"             ,  delay:0,     soundList:[{file:"knockcheck_1",gain:0.3}]    },
    "action-fold"                :{id:"FOLD"              ,  delay:0,     soundList:[{file:"sweep_3"     ,gain:0.2}]    },
    "action-bet"                 :{id:"BET"               ,  delay:200,   soundList:[{file:"chippile_5"  ,gain:0.2}]    },
    "action-raise"               :{id:"RAISE"             ,  delay:400,   soundList:[{file:"chippile_6"  ,gain:0.4}]    },
    "action-small-blind"         :{id:"SMALL_BLIND"       ,  delay:600,   soundList:[{file:"chippile_1"  ,gain:0.12}]    },
    "action-big-blind"           :{id:"BIG_BLIND"         ,  delay:350,   soundList:[{file:"chippile_6"  ,gain:0.14}]    },
    "action-join"                :{id:"JOIN"              ,  delay:0,     soundList:[{file:"metaltwang_1",gain:0.06}]    },
    "action-leave"               :{id:"LEAVE"             ,  delay:0,     soundList:[{file:"woodtwang_1" ,gain:0.06}]    },
    "action-sit-out"             :{id:"SIT_OUT"           ,  delay:0,     soundList:[{file:"bellchord_4" ,gain:1}]    },
    "action-sit-in"              :{id:"SIT_IN"            ,  delay:0,     soundList:[{file:"bellchord_3" ,gain:1}]    },
    "entry-bet"                  :{id:"ENTRY_BET"         ,  delay:0,     soundList:[{file:"chipheavy_1" ,gain:1}]    },
    "decline-entry-bet"          :{id:"DECLINE_ENTRY_BET" ,  delay:200,   soundList:[{file:"sweep_3"     ,gain:0.2}]    },
    "wait-for-big-blind"         :{id:"WAIT_FOR_BIG_BLIND",  delay:0,     soundList:[{file:"clocktick_1" ,gain:1}]    },
    "ante"                       :{id:"ANTE"              ,  delay:0,     soundList:[{file:"chip_1"      ,gain:1}]    },
    "dead-small-blind"           :{id:"DEAD_SMALL_BLIND"  ,  delay:0,     soundList:[{file:"chip_2"      ,gain:1}]    },
"big-blind-plus-dead-small-blind":{id:"BIG_BLIND_PLUS_DEAD_SMALL_BLIND", delay:0, soundList:[{file:"chippile_1" ,gain:1}] }

}