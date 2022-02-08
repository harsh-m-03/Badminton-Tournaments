package com.example.badminton.Modules;

public class IndividualRankings {
    String playerName;
    String tournamentPlayed, tournamentLost, tournamentWon, winningPercentage;
    String weeklyWins;

    public String getWeeklyWins() {
        return weeklyWins;
    }

    public void setWeeklyWins(String weeklyWins) {
        this.weeklyWins = weeklyWins;
    }

    public String getWinningPercentage() {
        return winningPercentage;
    }

    public void setWinningPercentage(String winningPercentage) {
        this.winningPercentage = winningPercentage;
    }

    public String getTournamentWon() {
        return tournamentWon;
    }

    public void setTournamentWon(String tournamentWon) {
        this.tournamentWon = tournamentWon;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTournamentPlayed() {
        return tournamentPlayed;
    }

    public void setTournamentPlayed(String tournamentPlayed) {
        this.tournamentPlayed = tournamentPlayed;
    }

    public String getTournamentLost() {
        return tournamentLost;
    }

    public void setTournamentLost(String tournamentLost) {
        this.tournamentLost = tournamentLost;
    }
}
