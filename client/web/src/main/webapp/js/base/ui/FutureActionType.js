"use strict";
var Poker = Poker || {};

Poker.FutureActionType = {
    CHECK : {
        id : "check",
        text : "Check"
    },
    CHECK_OR_FOLD : {
       id : "check-or-fold",
       text : "Check/fold"
   },
   CALL_CURRENT_BET : {
        id : "call-current-bet",
        text : "Call {{amount}}"
   },
   CHECK_OR_CALL_ANY : {
      id : "check-or-call-any",
      text : "Check/Call any"
  },
  CALL_ANY : {
      id : "call-any",
      text : "Call any"
  },
  FOLD : {
      id : "fold",
      text : "Fold"
  },
  RAISE : {
      id : "raise",
      text : "Raise {{amount}}"
  },
  RAISE_ANY : {
      id : "raise-any",
      text : "Raise any"
  }
};