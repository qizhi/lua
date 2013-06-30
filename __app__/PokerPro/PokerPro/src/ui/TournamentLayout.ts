module ui {
    export class TournamentLayout {
        constructor(public tournamentId: number, public name: string, public registered: boolean, public leaveFunction:()=>void) {
        }
        hideTournamentStatistics(): void {
            //  this.viewElement.find(".tournament-stats").hide();
        }
        updatePlayerList(players: any[]): void {
            /*var template = this.templateManager.getRenderTemplate("tournamentPlayerListItem");
            this.playerListBody.empty();
            var self = this;
            $.each(players, function (i, p) {
                self.playerListBody.append(template.render(p));
            });
            if (players.length == 0) {
                this.playerListBody.append("<td/>").attr("colspan", "3").
                    append(i18n.t("tournament-lobby.players.no-players"));
            }*/
        }
        updateBlindsStructure(blindsStructure: any): void {
            //var blindsTemplate = this.templateManager.getRenderTemplate("tournamentBlindsStructureTemplate");
            //this.viewElement.find(".blinds-structure").html(blindsTemplate.render(blindsStructure));
        }
        updatePayoutInfo(payoutInfo: any): void {
            //var payoutTemplate = this.templateManager.getRenderTemplate("tournamentPayoutStructureTemplate");
            //this.viewElement.find(".payout-structure").html(payoutTemplate.render(payoutInfo));
        }
        updateTournamentStatistics(statistics: any): void {
            //var statsTemplate = this.templateManager.getRenderTemplate("tournamentStatsTemplate");
            //this.viewElement.find(".tournament-stats").show().html(statsTemplate.render(statistics));
        }
        setPlayerRegisteredState(): void {
            //this.loadingButton.hide();
            //this.registerButton.hide();
            //this.unregisterButton.show();
        }
        updateTournamentInfo(info:any): void {
            /*this.viewElement.find(".tournament-name").html(info.tournamentName);
            if (info.maxPlayers == info.minPlayers) {
                $.extend(info, { sitAndGo: true });
            }
            var infoTemplate = this.templateManager.getRenderTemplate("tournamentInfoTemplate");
            this.viewElement.find(".tournament-info").html(infoTemplate.render(info));
            if (!info.sitAndGo) {
                this.viewElement.find(".tournament-start-date").html(info.startTime);
            }*/
        }
        setTournamentNotRegisteringState(registered:boolean): void {
            /*if (registered) {
                this.takeSeatButton.show();
            } else {
                this.takeSeatButton.hide();
            }
            this.loadingButton.hide();
            this.registerButton.hide();
            this.unregisterButton.hide();*/
        }
        setPlayerUnregisteredState(): void {
            //this.loadingButton.hide();
            //this.registerButton.show();
            //this.unregisterButton.hide();
        }
        showBuyInInfo(buyIn:number, fee:number, currency:any, balanceInWallet:any):void {
            //var buyInDialog = new Poker.TournamentBuyInDialog();
            //buyInDialog.show(this.tournamentId, this.name, buyIn, fee, balanceInWallet, currency);
        }

    }
}