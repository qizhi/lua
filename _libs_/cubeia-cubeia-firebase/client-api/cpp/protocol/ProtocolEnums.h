// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PROTOCOLENUMS_H_60445CD_INCLUDE
#define PROTOCOLENUMS_H_60445CD_INCLUDE

#ifdef REGISTERED
	#undef REGISTERED
#endif

namespace com_cubeia_firebase_io_protocol
{
    namespace ParameterType
    {
        enum Enum { STRING, INT, DATE, ENUM_ERROR = -1 };

        static Enum makeParameterType(int value) {
            switch(value) {
                case 0: return STRING;
                case 1: return INT;
                case 2: return DATE;

                default: return ENUM_ERROR;
            }
        }

        static Enum ParameterTypeError = makeParameterType(ENUM_ERROR);

    }
    namespace ParameterFilter
    {
        enum Enum { EQUALS, GREATER_THAN, SMALLER_THAN, EQUALS_OR_GREATER_THAN, EQUALS_OR_SMALLER_THAN, ENUM_ERROR = -1 };

        static Enum makeParameterFilter(int value) {
            switch(value) {
                case 0: return EQUALS;
                case 1: return GREATER_THAN;
                case 2: return SMALLER_THAN;
                case 3: return EQUALS_OR_GREATER_THAN;
                case 4: return EQUALS_OR_SMALLER_THAN;

                default: return ENUM_ERROR;
            }
        }

        static Enum ParameterFilterError = makeParameterFilter(ENUM_ERROR);

    }
    namespace LobbyType
    {
        enum Enum { REGULAR, MTT, ENUM_ERROR = -1 };

        static Enum makeLobbyType(int value) {
            switch(value) {
                case 0: return REGULAR;
                case 1: return MTT;

                default: return ENUM_ERROR;
            }
        }

        static Enum LobbyTypeError = makeLobbyType(ENUM_ERROR);

    }
    namespace TournamentAttributes
    {
        enum Enum { NAME, CAPACITY, REGISTERED, ACTIVE_PLAYERS, STATUS, ENUM_ERROR = -1 };

        static Enum makeTournamentAttributes(int value) {
            switch(value) {
                case 0: return NAME;
                case 1: return CAPACITY;
                case 2: return REGISTERED;
                case 3: return ACTIVE_PLAYERS;
                case 4: return STATUS;

                default: return ENUM_ERROR;
            }
        }

        static Enum TournamentAttributesError = makeTournamentAttributes(ENUM_ERROR);

    }
    namespace ServiceIdentifier
    {
        enum Enum { NAMESPACE, CONTRACT, ENUM_ERROR = -1 };

        static Enum makeServiceIdentifier(int value) {
            switch(value) {
                case 0: return NAMESPACE;
                case 1: return CONTRACT;

                default: return ENUM_ERROR;
            }
        }

        static Enum ServiceIdentifierError = makeServiceIdentifier(ENUM_ERROR);

    }
    namespace PlayerStatus
    {
        enum Enum { CONNECTED, WAITING_REJOIN, DISCONNECTED, LEAVING, TABLE_LOCAL, RESERVATION, ENUM_ERROR = -1 };

        static Enum makePlayerStatus(int value) {
            switch(value) {
                case 0: return CONNECTED;
                case 1: return WAITING_REJOIN;
                case 2: return DISCONNECTED;
                case 3: return LEAVING;
                case 4: return TABLE_LOCAL;
                case 5: return RESERVATION;

                default: return ENUM_ERROR;
            }
        }

        static Enum PlayerStatusError = makePlayerStatus(ENUM_ERROR);

    }
    namespace ResponseStatus
    {
        enum Enum { OK, FAILED, DENIED, ENUM_ERROR = -1 };

        static Enum makeResponseStatus(int value) {
            switch(value) {
                case 0: return OK;
                case 1: return FAILED;
                case 2: return DENIED;

                default: return ENUM_ERROR;
            }
        }

        static Enum ResponseStatusError = makeResponseStatus(ENUM_ERROR);

    }
    namespace JoinResponseStatus
    {
        enum Enum { OK, FAILED, DENIED, ENUM_ERROR = -1 };

        static Enum makeJoinResponseStatus(int value) {
            switch(value) {
                case 0: return OK;
                case 1: return FAILED;
                case 2: return DENIED;

                default: return ENUM_ERROR;
            }
        }

        static Enum JoinResponseStatusError = makeJoinResponseStatus(ENUM_ERROR);

    }
    namespace WatchResponseStatus
    {
        enum Enum { OK, FAILED, DENIED, DENIED_ALREADY_SEATED, ENUM_ERROR = -1 };

        static Enum makeWatchResponseStatus(int value) {
            switch(value) {
                case 0: return OK;
                case 1: return FAILED;
                case 2: return DENIED;
                case 3: return DENIED_ALREADY_SEATED;

                default: return ENUM_ERROR;
            }
        }

        static Enum WatchResponseStatusError = makeWatchResponseStatus(ENUM_ERROR);

    }
    namespace FilteredJoinResponseStatus
    {
        enum Enum { OK, FAILED, DENIED, SEATING, WAIT_LIST, ENUM_ERROR = -1 };

        static Enum makeFilteredJoinResponseStatus(int value) {
            switch(value) {
                case 0: return OK;
                case 1: return FAILED;
                case 2: return DENIED;
                case 3: return SEATING;
                case 4: return WAIT_LIST;

                default: return ENUM_ERROR;
            }
        }

        static Enum FilteredJoinResponseStatusError = makeFilteredJoinResponseStatus(ENUM_ERROR);

    }
    namespace TournamentRegisterResponseStatus
    {
        enum Enum { OK, FAILED, DENIED, DENIED_LOW_FUNDS, DENIED_MTT_FULL, DENIED_NO_ACCESS, DENIED_ALREADY_REGISTERED, DENIED_TOURNAMENT_RUNNING, ENUM_ERROR = -1 };

        static Enum makeTournamentRegisterResponseStatus(int value) {
            switch(value) {
                case 0: return OK;
                case 1: return FAILED;
                case 2: return DENIED;
                case 3: return DENIED_LOW_FUNDS;
                case 4: return DENIED_MTT_FULL;
                case 5: return DENIED_NO_ACCESS;
                case 6: return DENIED_ALREADY_REGISTERED;
                case 7: return DENIED_TOURNAMENT_RUNNING;

                default: return ENUM_ERROR;
            }
        }

        static Enum TournamentRegisterResponseStatusError = makeTournamentRegisterResponseStatus(ENUM_ERROR);

    }
}

#endif
