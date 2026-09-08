package com.cricklocal;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.dto.InningsResponse;
import com.cricklocal.entity.InningsState;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.dto.ScorecardResponse;
import com.cricklocal.dto.CareerStatsResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import com.cricklocal.enums.DismissalEnd;
import com.cricklocal.enums.InningsStatus;
import com.cricklocal.enums.MatchTeamSide;
import com.cricklocal.enums.MatchFormat;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.repository.InningsStateRepository;
import com.cricklocal.repository.MatchResultRepository;
import com.cricklocal.service.DeliveryService;
import com.cricklocal.service.InningsService;
import com.cricklocal.service.ScorecardService;
import com.cricklocal.service.CareerStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cricklocal.enums.PlayerRole;
import com.cricklocal.service.LeaderboardService;
import com.cricklocal.exception.ResourceNotFoundException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CricklocalBackendApplicationTests {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

	@Autowired
	private MatchTeamRepository matchTeamRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

    @Autowired
    private InningsRepository inningsRepository;

	@Autowired
	private InningsStateRepository inningsStateRepository;

    @Autowired
    private InningsService inningsService;

    @Autowired
    private DeliveryService deliveryService;

	@Autowired
	private ScorecardService scorecardService;

	@Autowired
	private CareerStatisticsService careerStatisticsService;

	@Autowired
	private LeaderboardService leaderboardService;

	@Test
	void leaderboardShouldReturnTopRunScorersFromCompletedMatches() {

			String testId = String.valueOf(System.currentTimeMillis());

			Team teamA = new Team();
			teamA.setName("Module 10 Leaderboard Team A " + testId);
			teamA.setShortName("M10A" + testId);
			teamA.setCity("Nellore");
			teamA = teamRepository.save(teamA);

			Team teamB = new Team();
			teamB.setName("Module 10 Leaderboard Team B " + testId);
			teamB.setShortName("M10B" + testId);
			teamB.setCity("Nellore");
			teamB = teamRepository.save(teamB);

			Player batter1 = createPlayer(
					"Leaderboard",
					"Batter1 " + testId,
					"Leaderboard Batter1 " + testId,
					PlayerRole.BATTER);

			Player batter2 = createPlayer(
					"Leaderboard",
					"Batter2 " + testId,
					"Leaderboard Batter2 " + testId,
					PlayerRole.BATTER);

			Player bowler = createPlayer(
					"Leaderboard",
					"Bowler " + testId,
					"Leaderboard Bowler " + testId,
					PlayerRole.BOWLER);

			Match match = new Match();
			match.setName("Module 10 Leaderboard Match " + testId);
			match.setMatchNumber(1);
			match.setFormat(MatchFormat.T20);
			match.setTotalOvers(1);
			match.setMaxPlayersPerTeam(3);
			match.setScheduledAt(Instant.now());
			match.setStatus(MatchStatus.COMPLETED);
			match = matchRepository.save(match);

			MatchTeam matchTeamA = new MatchTeam();
			matchTeamA.setMatch(match);
			matchTeamA.setTeam(teamA);
			matchTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(matchTeamA);

			MatchTeam matchTeamB = new MatchTeam();
			matchTeamB.setMatch(match);
			matchTeamB.setTeam(teamB);
			matchTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(matchTeamB);

			createLineup(match, teamA, batter1, 1);
			createLineup(match, teamA, batter2, 2);
			createLineup(match, teamB, bowler, 1);

			StartInningsRequest startInningsRequest =
					new StartInningsRequest();

			startInningsRequest.setBattingTeamId(teamA.getId());
			startInningsRequest.setBowlingTeamId(teamB.getId());
			startInningsRequest.setInningsNumber(1);

			InningsResponse inningsResponse =
					inningsService.startInnings(
							match.getId(),
							startInningsRequest);

			Innings innings =
					inningsRepository.findById(
							inningsResponse.getId())
							.orElseThrow();

			createInningsState(
					innings,
					batter1,
					bowler,
					batter2);

			RecordDeliveryRequest firstDelivery =
					createDeliveryRequest(
							batter1,
							batter2,
							bowler,
							6);

			deliveryService.recordDelivery(
					innings.getId(),
					firstDelivery);

			RecordDeliveryRequest secondDelivery =
					createDeliveryRequest(
							batter1,
							batter2,
							bowler,
							4);

			deliveryService.recordDelivery(
					innings.getId(),
					secondDelivery);

			RecordDeliveryRequest thirdDelivery =
					createDeliveryRequest(
							batter1,
							batter2,
							bowler,
							3);

			deliveryService.recordDelivery(
					innings.getId(),
					thirdDelivery);

			innings.setStatus(InningsStatus.COMPLETED);
			innings.setTotalRuns(13);
			inningsRepository.save(innings);

			var leaderboard =
					leaderboardService.getTopRunScorers();

			assertEquals(
					"TOP_RUN_SCORERS",
					leaderboard.getCategory());

			assertFalse(
					leaderboard.getEntries().isEmpty());

			var entry =
					leaderboard.getEntries().stream()
							.filter(item ->
									item.getPlayerId()
											.equals(batter1.getId()))
							.findFirst()
							.orElseThrow();

			assertEquals(
					13L,
					entry.getPrimaryValue());
	}

	@Test
	void leaderboardShouldReturnTopWicketTakersFromCompletedMatches() {

			String testId = String.valueOf(System.currentTimeMillis());

			Team teamA = new Team();
			teamA.setName("Module 10 Wicket Team A " + testId);
			teamA.setShortName("M10WA" + testId);
			teamA.setCity("Nellore");
			teamA = teamRepository.save(teamA);

			Team teamB = new Team();
			teamB.setName("Module 10 Wicket Team B " + testId);
			teamB.setShortName("M10WB" + testId);
			teamB.setCity("Nellore");
			teamB = teamRepository.save(teamB);

			Player batter1 = createPlayer(
					"Wicket",
					"Batter1 " + testId,
					"Wicket Batter1 " + testId,
					PlayerRole.BATTER);

			Player batter2 = createPlayer(
					"Wicket",
					"Batter2 " + testId,
					"Wicket Batter2 " + testId,
					PlayerRole.BATTER);

			Player bowler = createPlayer(
					"Wicket",
					"Bowler " + testId,
					"Wicket Bowler " + testId,
					PlayerRole.BOWLER);

			Match match = new Match();
			match.setName("Module 10 Wicket Match " + testId);
			match.setMatchNumber(1);
			match.setFormat(MatchFormat.T20);
			match.setTotalOvers(1);
			match.setMaxPlayersPerTeam(3);
			match.setScheduledAt(Instant.now());
			match.setStatus(MatchStatus.COMPLETED);
			match = matchRepository.save(match);

			MatchTeam matchTeamA = new MatchTeam();
			matchTeamA.setMatch(match);
			matchTeamA.setTeam(teamA);
			matchTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(matchTeamA);

			MatchTeam matchTeamB = new MatchTeam();
			matchTeamB.setMatch(match);
			matchTeamB.setTeam(teamB);
			matchTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(matchTeamB);

			createLineup(match, teamA, batter1, 1);
			createLineup(match, teamA, batter2, 2);
			createLineup(match, teamB, bowler, 1);

			StartInningsRequest startInningsRequest =
					new StartInningsRequest();

			startInningsRequest.setBattingTeamId(teamA.getId());
			startInningsRequest.setBowlingTeamId(teamB.getId());
			startInningsRequest.setInningsNumber(1);

			InningsResponse inningsResponse =
					inningsService.startInnings(
							match.getId(),
							startInningsRequest);

			Innings innings =
					inningsRepository.findById(
							inningsResponse.getId())
							.orElseThrow();

			createInningsState(
					innings,
					batter1,
					bowler,
					batter2);

			RecordDeliveryRequest wicketRequest =
					createDeliveryRequest(
							batter1,
							batter2,
							bowler,
							0);

			wicketRequest.setWicket(true);
			wicketRequest.setWicketType(WicketType.BOWLED);
			wicketRequest.setDismissedPlayerId(batter1.getId());
			wicketRequest.setDismissalEnd(DismissalEnd.STRIKER);

			deliveryService.recordDelivery(
					innings.getId(),
					wicketRequest);

			innings.setStatus(InningsStatus.COMPLETED);
			innings.setTotalRuns(0);
			inningsRepository.save(innings);

			var leaderboard =
					leaderboardService.getTopWicketTakers();

			assertEquals(
					"TOP_WICKET_TAKERS",
					leaderboard.getCategory());

			assertFalse(
					leaderboard.getEntries().isEmpty());

			var entry =
					leaderboard.getEntries().stream()
							.filter(item ->
									item.getPlayerId()
											.equals(bowler.getId()))
							.findFirst()
							.orElseThrow();

			assertEquals(
					1L,
					entry.getPrimaryValue());
	}

	@Test
	void leaderboardShouldReturnTopFieldingPlayersFromCompletedMatches() {

			String testId = String.valueOf(System.currentTimeMillis());

			Team teamA = new Team();
			teamA.setName("Module 10 Fielding Team A " + testId);
			teamA.setShortName("M10FA" + testId);
			teamA.setCity("Nellore");
			teamA = teamRepository.save(teamA);

			Team teamB = new Team();
			teamB.setName("Module 10 Fielding Team B " + testId);
			teamB.setShortName("M10FB" + testId);
			teamB.setCity("Nellore");
			teamB = teamRepository.save(teamB);

			Player batter = createPlayer(
					"Fielding",
					"Batter " + testId,
					"Fielding Batter " + testId,
					PlayerRole.BATTER);

			Player nonStriker = createPlayer(
					"Fielding",
					"NonStriker " + testId,
					"Fielding NonStriker " + testId,
					PlayerRole.BATTER);

			Player fielder = createPlayer(
					"Fielding",
					"Fielder " + testId,
					"Fielding Fielder " + testId,
					PlayerRole.ALL_ROUNDER);

			Player bowler = createPlayer(
					"Fielding",
					"Bowler " + testId,
					"Fielding Bowler " + testId,
					PlayerRole.BOWLER);

			Match match = new Match();
			match.setName("Module 10 Fielding Match " + testId);
			match.setMatchNumber(1);
			match.setFormat(MatchFormat.T20);
			match.setTotalOvers(1);
			match.setMaxPlayersPerTeam(3);
			match.setScheduledAt(Instant.now());
			match.setStatus(MatchStatus.COMPLETED);
			match = matchRepository.save(match);

			MatchTeam matchTeamA = new MatchTeam();
			matchTeamA.setMatch(match);
			matchTeamA.setTeam(teamA);
			matchTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(matchTeamA);

			MatchTeam matchTeamB = new MatchTeam();
			matchTeamB.setMatch(match);
			matchTeamB.setTeam(teamB);
			matchTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(matchTeamB);

			createLineup(match, teamA, batter, 1);
			createLineup(match, teamA, nonStriker, 2);
			createLineup(match, teamB, fielder, 1);
			createLineup(match, teamB, bowler, 2);

			StartInningsRequest startInningsRequest =
					new StartInningsRequest();

			startInningsRequest.setBattingTeamId(teamA.getId());
			startInningsRequest.setBowlingTeamId(teamB.getId());
			startInningsRequest.setInningsNumber(1);

			InningsResponse inningsResponse =
					inningsService.startInnings(
							match.getId(),
							startInningsRequest);

			Innings innings =
					inningsRepository.findById(
							inningsResponse.getId())
							.orElseThrow();

			createInningsState(
					innings,
					batter,
					bowler,
					nonStriker);

			RecordDeliveryRequest wicketRequest =
					createDeliveryRequest(
							batter,
							nonStriker,
							bowler,
							0);

			wicketRequest.setWicket(true);
			wicketRequest.setWicketType(WicketType.CAUGHT);
			wicketRequest.setDismissedPlayerId(batter.getId());
			wicketRequest.setDismissalEnd(DismissalEnd.STRIKER);
			wicketRequest.setFielderId(fielder.getId());

			deliveryService.recordDelivery(
					innings.getId(),
					wicketRequest);

			innings.setStatus(InningsStatus.COMPLETED);
			innings.setTotalRuns(0);
			inningsRepository.save(innings);

			var leaderboard =
					leaderboardService.getTopFieldingPlayers();

			assertEquals(
					"TOP_FIELDING_PLAYERS",
					leaderboard.getCategory());

			assertFalse(
					leaderboard.getEntries().isEmpty());

			var entry =
					leaderboard.getEntries().stream()
							.filter(item ->
									item.getPlayerId()
											.equals(fielder.getId()))
							.findFirst()
							.orElseThrow();

			assertEquals(
					1L,
					entry.getPrimaryValue());

			assertEquals(
					1L,
					entry.getSecondaryValue());
	}

	@Test
    void careerStatisticsShouldAggregateCompletedMatchBattingBowlingAndFielding() {

			String testId = String.valueOf(System.currentTimeMillis());

			Team teamA = new Team();
			teamA.setName("Module 9 Career Team A " + testId);
			teamA.setShortName("M9A" + testId);
			teamA.setCity("Nellore");
			teamA = teamRepository.save(teamA);

			Team teamB = new Team();
			teamB.setName("Module 9 Career Team B " + testId);
			teamB.setShortName("M9B" + testId);
			teamB.setCity("Nellore");
			teamB = teamRepository.save(teamB);

			Player batterA1 = createPlayer(
					"Career", "Batter A1 " + testId,
					"Career Batter A1 " + testId,
					PlayerRole.BATTER);

			Player batterA2 = createPlayer(
					"Career", "Batter A2 " + testId,
					"Career Batter A2 " + testId,
					PlayerRole.BATTER);

			Player batterA3 = createPlayer(
					"Career", "Batter A3 " + testId,
					"Career Batter A3 " + testId,
					PlayerRole.BATTER);

			Player batterB1 = createPlayer(
					"Career", "Batter B1 " + testId,
					"Career Batter B1 " + testId,
					PlayerRole.BATTER);

			Player batterB2 = createPlayer(
					"Career", "Batter B2 " + testId,
					"Career Batter B2 " + testId,
					PlayerRole.BATTER);

			Player batterB3 = createPlayer(
					"Career", "Batter B3 " + testId,
					"Career Batter B3 " + testId,
					PlayerRole.BATTER);

			Match match = new Match();
			match.setName("Module 9 Career Match " + testId);
			match.setMatchNumber(1);
			match.setFormat(MatchFormat.T20);
			match.setTotalOvers(1);
			match.setMaxPlayersPerTeam(3);
        match.setScheduledAt(Instant.now());
			match.setStatus(MatchStatus.SCHEDULED);
			match = matchRepository.save(match);

			MatchTeam matchTeamA = new MatchTeam();
			matchTeamA.setMatch(match);
			matchTeamA.setTeam(teamA);
			matchTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(matchTeamA);

			MatchTeam matchTeamB = new MatchTeam();
			matchTeamB.setMatch(match);
			matchTeamB.setTeam(teamB);
			matchTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(matchTeamB);

			createLineup(match, teamA, batterA1, 1);
			createLineup(match, teamA, batterA2, 2);
			createLineup(match, teamA, batterA3, 3);

			createLineup(match, teamB, batterB1, 1);
			createLineup(match, teamB, batterB2, 2);
			createLineup(match, teamB, batterB3, 3);

			StartInningsRequest innings1Request =
                new StartInningsRequest();

        innings1Request.setBattingTeamId(teamA.getId());
        innings1Request.setBowlingTeamId(teamB.getId());
        innings1Request.setInningsNumber(1);

        inningsService.startInnings(
                match.getId(),
                innings1Request);

			Innings innings1 =
					inningsRepository.findByMatchAndInningsNumber(
							match, 1)
							.orElseThrow();

			createInningsState(
					innings1,
					batterA1,
					batterB1,
					batterA2);

			RecordDeliveryRequest scoringDelivery =
					createDeliveryRequest(
							batterA1,
							batterA2,
							batterB1,
																		4);

			deliveryService.recordDelivery(
					innings1.getId(),
					scoringDelivery);

			RecordDeliveryRequest wicketDelivery =
					createDeliveryRequest(
							batterA1,
							batterA2,
							batterB1,
							0);

			wicketDelivery.setWicket(true);
			wicketDelivery.setWicketType(WicketType.CAUGHT);
			wicketDelivery.setDismissedPlayerId(batterA1.getId());
			wicketDelivery.setFielderId(batterB2.getId());
			wicketDelivery.setDismissalEnd(DismissalEnd.STRIKER);
			wicketDelivery.setNewBatterId(batterA3.getId());

			deliveryService.recordDelivery(
					innings1.getId(),
					wicketDelivery);

			inningsService.declareInnings(innings1.getId());

			StartInningsRequest innings2Request =
                new StartInningsRequest();

        innings2Request.setBattingTeamId(teamB.getId());
        innings2Request.setBowlingTeamId(teamA.getId());
        innings2Request.setInningsNumber(2);

        inningsService.startInnings(
                match.getId(),
                innings2Request);

			Innings innings2 =
					inningsRepository.findByMatchAndInningsNumber(
							match, 2)
							.orElseThrow();

			createInningsState(
                                    innings2,
                                    batterB1,
                                    batterA2,
                                    batterB2);

			RecordDeliveryRequest targetDelivery =
					createDeliveryRequest(
							batterB1,
							batterB2,
							batterA2,
																		5);

			deliveryService.recordDelivery(
					innings2.getId(),
					targetDelivery);

			Match completedMatch =
					matchRepository.findById(match.getId())
							.orElseThrow();

			assertEquals(
					MatchStatus.COMPLETED,
					completedMatch.getStatus());

			CareerStatsResponse batterStats =
					careerStatisticsService.getCareerStats(
							batterA1.getId());

			assertEquals(batterA1.getId(), batterStats.getPlayerId());
			assertEquals(
					1L,
					batterStats.getBatting().getMatches());
			assertEquals(
					1L,
					batterStats.getBatting().getInnings());
			assertEquals(
                                    4L,
					batterStats.getBatting().getRuns());
			assertEquals(
					2L,
					batterStats.getBatting().getBallsFaced());
			assertEquals(
					1L,
					batterStats.getBatting().getFours());
			assertEquals(
					0L,
					batterStats.getBatting().getSixes());
			assertEquals(
					1L,
					batterStats.getBatting().getDismissals());
			assertEquals(
                                    4L,
					batterStats.getBatting().getHighestScore());
			assertEquals(
                                    4.00,
					batterStats.getBatting().getAverage().doubleValue(),
					0.001);
			assertEquals(
                                    200.00,
					batterStats.getBatting().getStrikeRate().doubleValue(),
					0.001);

			CareerStatsResponse bowlerStats =
					careerStatisticsService.getCareerStats(
							batterB1.getId());

			assertEquals(
					1L,
					bowlerStats.getBowling().getMatches());
			assertEquals(
					1L,
					bowlerStats.getBowling().getInnings());
			assertEquals(
					2L,
					bowlerStats.getBowling().getBallsBowled());
			assertEquals(
                                    4L,
					bowlerStats.getBowling().getRunsConceded());
			assertEquals(
					1L,
					bowlerStats.getBowling().getWickets());
			assertEquals(
                                    12.00,
					bowlerStats.getBowling().getEconomy().doubleValue(),
					0.001);
			assertEquals(
                                    4.00,
					bowlerStats.getBowling().getAverage().doubleValue(),
					0.001);

			CareerStatsResponse fielderStats =
					careerStatisticsService.getCareerStats(
							batterB2.getId());

			assertEquals(
					1L,
					fielderStats.getFielding().getMatches());
			assertEquals(
					1L,
					fielderStats.getFielding().getFieldingDismissals());
			assertEquals(
					1L,
					fielderStats.getFielding().getCatches());
			assertEquals(
					0L,
					fielderStats.getFielding().getRunOuts());
			assertEquals(
					0L,
					fielderStats.getFielding().getStumpings());
	}


    @Test
    void careerStatisticsShouldIgnoreNonCompletedMatches() {

        String testId = String.valueOf(System.currentTimeMillis());

        Team teamA = new Team();
        teamA.setName("Module 9 Filter Team A " + testId);
        teamA.setShortName("F9A" + testId);
        teamA.setCity("Nellore");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Module 9 Filter Team B " + testId);
        teamB.setShortName("F9B" + testId);
        teamB.setCity("Nellore");
        teamB = teamRepository.save(teamB);

        Player batter = createPlayer(
                "Filter", "Batter " + testId,
                "Filter Batter " + testId,
                PlayerRole.BATTER);

        Player nonStriker = createPlayer(
                "Filter", "NonStriker " + testId,
                "Filter NonStriker " + testId,
                PlayerRole.BATTER);

        Player bowler = createPlayer(
                "Filter", "Bowler " + testId,
                "Filter Bowler " + testId,
                PlayerRole.BOWLER);

        Match completedMatch = new Match();
        completedMatch.setName("Module 9 Filter Completed " + testId);
        completedMatch.setMatchNumber(1);
        completedMatch.setFormat(MatchFormat.T20);
        completedMatch.setTotalOvers(1);
        completedMatch.setMaxPlayersPerTeam(2);
        completedMatch.setScheduledAt(Instant.now());
        completedMatch = matchRepository.save(completedMatch);

        MatchTeam completedTeamA = new MatchTeam();
        completedTeamA.setMatch(completedMatch);
        completedTeamA.setTeam(teamA);
        completedTeamA.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(completedTeamA);

        MatchTeam completedTeamB = new MatchTeam();
        completedTeamB.setMatch(completedMatch);
        completedTeamB.setTeam(teamB);
        completedTeamB.setSide(MatchTeamSide.TEAM_B);
        matchTeamRepository.save(completedTeamB);

        createLineup(completedMatch, teamA, batter, 1);
        createLineup(completedMatch, teamA, nonStriker, 2);
        createLineup(completedMatch, teamB, bowler, 1);

        StartInningsRequest completedRequest =
                new StartInningsRequest();

        completedRequest.setBattingTeamId(teamA.getId());
        completedRequest.setBowlingTeamId(teamB.getId());
        completedRequest.setInningsNumber(1);

        inningsService.startInnings(
                completedMatch.getId(),
                completedRequest);

        Innings completedInnings =
                inningsRepository.findByMatchAndInningsNumber(
                        completedMatch, 1)
                        .orElseThrow();

        createInningsState(
                completedInnings,
                batter,
                bowler,
                nonStriker);

        deliveryService.recordDelivery(
                completedInnings.getId(),
                createDeliveryRequest(
                        batter,
                        nonStriker,
                        bowler,
                        4));

        inningsService.declareInnings(
                completedInnings.getId());

		completedMatch.setStatus(MatchStatus.COMPLETED);
				matchRepository.save(completedMatch);

        Match liveMatch = new Match();
        liveMatch.setName("Module 9 Filter Live " + testId);
        liveMatch.setMatchNumber(2);
        liveMatch.setFormat(MatchFormat.T20);
        liveMatch.setTotalOvers(1);
        liveMatch.setMaxPlayersPerTeam(2);
        liveMatch.setScheduledAt(Instant.now());
        liveMatch = matchRepository.save(liveMatch);

        MatchTeam liveTeamA = new MatchTeam();
        liveTeamA.setMatch(liveMatch);
        liveTeamA.setTeam(teamA);
        liveTeamA.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(liveTeamA);

        MatchTeam liveTeamB = new MatchTeam();
        liveTeamB.setMatch(liveMatch);
        liveTeamB.setTeam(teamB);
        liveTeamB.setSide(MatchTeamSide.TEAM_B);
        matchTeamRepository.save(liveTeamB);

        createLineup(liveMatch, teamA, batter, 1);
        createLineup(liveMatch, teamA, nonStriker, 2);
        createLineup(liveMatch, teamB, bowler, 1);

        StartInningsRequest liveRequest =
                new StartInningsRequest();

        liveRequest.setBattingTeamId(teamA.getId());
        liveRequest.setBowlingTeamId(teamB.getId());
        liveRequest.setInningsNumber(1);

        inningsService.startInnings(
                liveMatch.getId(),
                liveRequest);

        Innings liveInnings =
                inningsRepository.findByMatchAndInningsNumber(
                        liveMatch, 1)
                        .orElseThrow();

        createInningsState(
                liveInnings,
                batter,
                bowler,
                nonStriker);

        deliveryService.recordDelivery(
                liveInnings.getId(),
                createDeliveryRequest(
                        batter,
                        nonStriker,
                        bowler,
                        6));

        Match savedLiveMatch =
                matchRepository.findById(liveMatch.getId())
                        .orElseThrow();

        assertEquals(
                MatchStatus.SCHEDULED,
                savedLiveMatch.getStatus());

        CareerStatsResponse stats =
                careerStatisticsService.getCareerStats(
                        batter.getId());

        assertEquals(
                1L,
                stats.getBatting().getMatches());

        assertEquals(
                1L,
                stats.getBatting().getInnings());

        assertEquals(
                4L,
                stats.getBatting().getRuns());

        assertEquals(
                1L,
                stats.getBatting().getFours());

        assertEquals(
                1L,
                stats.getBatting().getBallsFaced());
    }

	@Test
    void careerStatisticsShouldRejectUnknownPlayer() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> careerStatisticsService.getCareerStats(999999999L));
    }

    @Test
    void scoringEngineShouldRecordLegalScoringDelivery() {

		String testId =
        String.valueOf(System.currentTimeMillis());
        // ---------------------------------------------------------
        // 1. Create teams
        // ---------------------------------------------------------

        Team battingTeam = new Team();
        battingTeam.setName("Module 4 Test Batting Team " + testId);
        battingTeam.setShortName("M4B" + testId);
        battingTeam.setCity("Nellore");

        battingTeam = teamRepository.save(battingTeam);

        Team bowlingTeam = new Team();
        bowlingTeam.setName("Module 4 Test Bowling Team " + testId);
        bowlingTeam.setShortName("M4W" + testId);
        bowlingTeam.setCity("Nellore");

        bowlingTeam = teamRepository.save(bowlingTeam);

        // ---------------------------------------------------------
        // 2. Create players
        // ---------------------------------------------------------

        Player striker = createPlayer(
				"Module4",
				"Striker",
				"M4 Striker " + testId,
				PlayerRole.BATTER);

		Player nonStriker = createPlayer(
				"Module4",
				"NonStriker",
				"M4 Non Striker " + testId,
				PlayerRole.BATTER);

		Player bowler = createPlayer(
				"Module4",
				"Bowler",
				"M4 Bowler " + testId,
				PlayerRole.BOWLER);

        // ---------------------------------------------------------
        // 3. Create match
        // ---------------------------------------------------------

        Match match = new Match();

        match.setName("Module 4.17 Scoring Integration Test " + testId);

        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(1);
        match.setMaxPlayersPerTeam(2);
        match.setScheduledAt(Instant.now());
        match.setVenue("Test Ground");

        match = matchRepository.save(match);

		MatchTeam battingMatchTeam = new MatchTeam();
		battingMatchTeam.setMatch(match);
		battingMatchTeam.setTeam(battingTeam);
		battingMatchTeam.setSide(MatchTeamSide.TEAM_A);

		MatchTeam bowlingMatchTeam = new MatchTeam();
		bowlingMatchTeam.setMatch(match);
		bowlingMatchTeam.setTeam(bowlingTeam);
		bowlingMatchTeam.setSide(MatchTeamSide.TEAM_B);

		matchTeamRepository.save(battingMatchTeam);
		matchTeamRepository.save(bowlingMatchTeam);

        // ---------------------------------------------------------
        // 4. Create match lineups
        // ---------------------------------------------------------

        createLineup(
                match,
                battingTeam,
                striker,
                1);

        createLineup(
                match,
                battingTeam,
                nonStriker,
                2);

        createLineup(
                match,
                bowlingTeam,
                bowler,
                1);

        // ---------------------------------------------------------
        // 5. Start innings
        // ---------------------------------------------------------

        StartInningsRequest inningsRequest =
                new StartInningsRequest();

        inningsRequest.setBattingTeamId(
                battingTeam.getId());

        inningsRequest.setBowlingTeamId(
                bowlingTeam.getId());

        inningsRequest.setInningsNumber(1);

        InningsResponse inningsResponse =
                inningsService.startInnings(
                        match.getId(),
                        inningsRequest);

        long inningsId =
                inningsResponse.getId();

		Innings innings =
        inningsRepository
                .findById(inningsId)
                .orElseThrow();

		InningsState inningsState =
				new InningsState();

		inningsState.setInnings(innings);
		inningsState.setStriker(striker);
		inningsState.setNonStriker(nonStriker);
		inningsState.setCurrentBowler(bowler);
		inningsState.setCurrentOver(1);
		inningsState.setLegalBallsInOver(0);

		inningsStateRepository.save(inningsState);

        assertEquals(
                InningsStatus.LIVE,
                inningsResponse.getStatus());

        assertEquals(
                0,
                inningsResponse.getTotalRuns());

        assertEquals(
                0,
                inningsResponse.getLegalBalls());

        // ---------------------------------------------------------
        // 6. Record one legal 2-run delivery
        // ---------------------------------------------------------

        RecordDeliveryRequest deliveryRequest =
                new RecordDeliveryRequest();

        deliveryRequest.setBatterId(
                striker.getId());

        deliveryRequest.setNonStrikerId(
                nonStriker.getId());

        deliveryRequest.setBowlerId(
                bowler.getId());

        deliveryRequest.setRunsOffBat(2);

        deliveryRequest.setExtraType(
                ExtraType.NONE);

        deliveryRequest.setExtraRuns(0);

        deliveryRequest.setWicket(false);

        var delivery =
                deliveryService.recordDelivery(
                        inningsId,
                        deliveryRequest);

        // ---------------------------------------------------------
        // 7. Verify delivery
        // ---------------------------------------------------------

        assertTrue(
                delivery.getLegalDelivery());

        assertEquals(
                2,
                delivery.getRunsOffBat());

        assertEquals(
                0,
                delivery.getExtraRuns());

        assertEquals(
                2,
                delivery.getTotalRuns());

        assertFalse(
                delivery.getWicket());

        // ---------------------------------------------------------
        // 8. Verify innings score
        // ---------------------------------------------------------

        Innings savedInnings =
                inningsRepository
                        .findById(inningsId)
                        .orElseThrow();

        assertEquals(
                2,
                savedInnings.getTotalRuns());

        assertEquals(
                0,
                savedInnings.getWickets());

        assertEquals(
                1,
                savedInnings.getLegalBalls());

        assertEquals(
                InningsStatus.LIVE,
                savedInnings.getStatus());
    }

	@Test
	void undoShouldRollbackAutomaticallyCompletedMatch() {

				String testId =
								String.valueOf(System.currentTimeMillis());

				// ---------------------------------------------------------
				// 1. Create teams
				// ---------------------------------------------------------

				Team teamA = new Team();
				teamA.setName(
								"Module 4 Undo Team A " + testId);
				teamA.setShortName(
								"U4A" + testId);
				teamA.setCity("Nellore");
				teamA = teamRepository.save(teamA);

				Team teamB = new Team();
				teamB.setName(
								"Module 4 Undo Team B " + testId);
				teamB.setShortName(
								"U4B" + testId);
				teamB.setCity("Nellore");
				teamB = teamRepository.save(teamB);

				// ---------------------------------------------------------
				// 2. Create players
				// ---------------------------------------------------------

				Player batterA = createPlayer(
								"Undo",
								"BatterA",
								"Undo Batter A " + testId,
								PlayerRole.BATTER);

				Player bowlerA = createPlayer(
								"Undo",
								"BowlerA",
								"Undo Bowler A " + testId,
								PlayerRole.BOWLER);

				Player batterB = createPlayer(
								"Undo",
								"BatterB",
								"Undo Batter B " + testId,
								PlayerRole.BATTER);

				Player bowlerB = createPlayer(
								"Undo",
								"BowlerB",
								"Undo Bowler B " + testId,
								PlayerRole.BOWLER);

				// ---------------------------------------------------------
				// 3. Create match
				// ---------------------------------------------------------

				Match match = new Match();

				match.setName(
								"Module 4 Undo Match Completion Test "
												+ testId);
				match.setFormat(MatchFormat.T20);
				match.setTotalOvers(1);
				match.setMaxPlayersPerTeam(2);
				match.setScheduledAt(Instant.now());
				match.setVenue("Test Ground");

				match = matchRepository.save(match);

				MatchTeam matchTeamA = new MatchTeam();
				matchTeamA.setMatch(match);
				matchTeamA.setTeam(teamA);
				matchTeamA.setSide(MatchTeamSide.TEAM_A);

				MatchTeam matchTeamB = new MatchTeam();
				matchTeamB.setMatch(match);
				matchTeamB.setTeam(teamB);
				matchTeamB.setSide(MatchTeamSide.TEAM_B);

				matchTeamRepository.save(matchTeamA);
				matchTeamRepository.save(matchTeamB);

				// ---------------------------------------------------------
				// 4. Create lineups
				// ---------------------------------------------------------

				createLineup(
								match,
								teamA,
								batterA,
								1);

				createLineup(
								match,
								teamA,
								bowlerA,
								2);

				createLineup(
								match,
								teamB,
								batterB,
								1);

				createLineup(
								match,
								teamB,
								bowlerB,
								2);

				// ---------------------------------------------------------
				// 5. Start innings 1
				// ---------------------------------------------------------

				StartInningsRequest innings1Request =
								new StartInningsRequest();

				innings1Request.setBattingTeamId(
								teamA.getId());

				innings1Request.setBowlingTeamId(
								teamB.getId());

				innings1Request.setInningsNumber(1);

				InningsResponse innings1Response =
								inningsService.startInnings(
												match.getId(),
												innings1Request);

				Innings innings1 =
								inningsRepository
												.findById(
																innings1Response.getId())
												.orElseThrow();

				createInningsState(
								innings1,
								batterA,
								bowlerB,
								bowlerA);

				// ---------------------------------------------------------
				// 6. Record 1 run in innings 1
				// ---------------------------------------------------------

				RecordDeliveryRequest innings1Delivery =
								createDeliveryRequest(
												batterA,
												bowlerA,
												bowlerB,
												1);

				deliveryService.recordDelivery(
								innings1.getId(),
								innings1Delivery);

				// Declare innings 1 so innings 2 can start.
				inningsService.declareInnings(
								innings1.getId());

				// ---------------------------------------------------------
				// 7. Start innings 2
				// ---------------------------------------------------------

				StartInningsRequest innings2Request =
								new StartInningsRequest();

				innings2Request.setBattingTeamId(
								teamB.getId());

				innings2Request.setBowlingTeamId(
								teamA.getId());

				innings2Request.setInningsNumber(2);

				InningsResponse innings2Response =
								inningsService.startInnings(
												match.getId(),
												innings2Request);

				Innings innings2 =
								inningsRepository
												.findById(
																innings2Response.getId())
												.orElseThrow();

				createInningsState(
								innings2,
								batterB,
								bowlerA,
								bowlerB);

				// ---------------------------------------------------------
				// 8. Score 1 run and complete the chase
				// ---------------------------------------------------------

				RecordDeliveryRequest innings2Delivery =
						createDeliveryRequest(
								batterB,
								bowlerB,
								bowlerA,
								2);

				deliveryService.recordDelivery(
								innings2.getId(),
								innings2Delivery);

				Match savedMatch =
								matchRepository
												.findById(match.getId())
												.orElseThrow();

				assertEquals(
								MatchStatus.COMPLETED,
								savedMatch.getStatus());

				assertTrue(
								matchResultRepository
												.existsByMatch(savedMatch));

				// ---------------------------------------------------------
				// 9. Undo the winning delivery
				// ---------------------------------------------------------

				deliveryService.undoLastDelivery(
								innings2.getId());

				// ---------------------------------------------------------
				// 10. Verify innings rollback
				// ---------------------------------------------------------

				Innings rolledBackInnings =
								inningsRepository
												.findById(innings2.getId())
												.orElseThrow();

				assertEquals(
								0,
								rolledBackInnings.getTotalRuns());

				assertEquals(
								0,
								rolledBackInnings.getLegalBalls());

				assertEquals(
								InningsStatus.LIVE,
								rolledBackInnings.getStatus());

				// ---------------------------------------------------------
				// 11. Verify match rollback
				// ---------------------------------------------------------

				Match rolledBackMatch =
								matchRepository
												.findById(match.getId())
												.orElseThrow();

				assertEquals(
								MatchStatus.LIVE,
								rolledBackMatch.getStatus());

				assertFalse(
								matchResultRepository
												.existsByMatch(
																rolledBackMatch));
	}

		@Test
	void scorecardShouldAssembleInningsBattingAndBowlingData() {

				String testId =
						String.valueOf(System.currentTimeMillis());

				// ---------------------------------------------------------
				// 1. Create teams
				// ---------------------------------------------------------

				Team battingTeam = new Team();
				battingTeam.setName(
						"Module 8 Scorecard Batting Team " + testId);
				battingTeam.setShortName("S8B" + testId);
				battingTeam.setCity("Nellore");
				battingTeam = teamRepository.save(battingTeam);

				Team bowlingTeam = new Team();
				bowlingTeam.setName(
						"Module 8 Scorecard Bowling Team " + testId);
				bowlingTeam.setShortName("S8W" + testId);
				bowlingTeam.setCity("Nellore");
				bowlingTeam = teamRepository.save(bowlingTeam);

				// ---------------------------------------------------------
				// 2. Create players
				// ---------------------------------------------------------

				Player striker = createPlayer(
						"Scorecard",
						"Striker",
						"S8 Striker " + testId,
						PlayerRole.BATTER);

				Player nonStriker = createPlayer(
						"Scorecard",
						"NonStriker",
						"S8 Non Striker " + testId,
						PlayerRole.BATTER);

				Player bowler = createPlayer(
						"Scorecard",
						"Bowler",
						"S8 Bowler " + testId,
						PlayerRole.BOWLER);

				// ---------------------------------------------------------
				// 3. Create match
				// ---------------------------------------------------------

				Match match = new Match();
				match.setName(
						"Module 8 Scorecard Integration Test " + testId);
				match.setFormat(MatchFormat.T20);
				match.setTotalOvers(1);
				match.setMaxPlayersPerTeam(2);
				match.setScheduledAt(Instant.now());
				match.setVenue("Test Ground");
				match = matchRepository.save(match);

				MatchTeam battingMatchTeam = new MatchTeam();
				battingMatchTeam.setMatch(match);
				battingMatchTeam.setTeam(battingTeam);
				battingMatchTeam.setSide(MatchTeamSide.TEAM_A);
				matchTeamRepository.save(battingMatchTeam);

				MatchTeam bowlingMatchTeam = new MatchTeam();
				bowlingMatchTeam.setMatch(match);
				bowlingMatchTeam.setTeam(bowlingTeam);
				bowlingMatchTeam.setSide(MatchTeamSide.TEAM_B);
				matchTeamRepository.save(bowlingMatchTeam);

				// ---------------------------------------------------------
				// 4. Create lineups
				// ---------------------------------------------------------

				createLineup(
						match,
						battingTeam,
						striker,
						1);

				createLineup(
						match,
						battingTeam,
						nonStriker,
						2);

				createLineup(
						match,
						bowlingTeam,
						bowler,
						1);

				// ---------------------------------------------------------
				// 5. Start innings
				// ---------------------------------------------------------

				StartInningsRequest inningsRequest =
						new StartInningsRequest();

				inningsRequest.setBattingTeamId(
						battingTeam.getId());

				inningsRequest.setBowlingTeamId(
						bowlingTeam.getId());

				inningsRequest.setInningsNumber(1);

				InningsResponse inningsResponse =
						inningsService.startInnings(
								match.getId(),
								inningsRequest);

				Innings innings =
						inningsRepository
								.findById(inningsResponse.getId())
								.orElseThrow();

				createInningsState(
						innings,
						striker,
						bowler,
						nonStriker);

				// ---------------------------------------------------------
				// 6. Record a 4-run delivery
				// ---------------------------------------------------------

				RecordDeliveryRequest deliveryRequest =
						createDeliveryRequest(
								striker,
								nonStriker,
								bowler,
								4);

				deliveryService.recordDelivery(
						innings.getId(),
						deliveryRequest);

				// ---------------------------------------------------------
				// 7. Fetch scorecard
				// ---------------------------------------------------------

				ScorecardResponse scorecard =
						scorecardService.getScorecard(
								match.getId());

				// ---------------------------------------------------------
				// 8. Verify match information
				// ---------------------------------------------------------

				assertEquals(
						match.getId(),
						scorecard.getMatchId());

				assertEquals(
						match.getName(),
						scorecard.getMatchName());

				assertEquals(
						1,
						scorecard.getInnings().size());

				// ---------------------------------------------------------
				// 9. Verify innings information
				// ---------------------------------------------------------

				var inningsScorecard =
						scorecard.getInnings().get(0);

				assertEquals(
						innings.getId(),
						inningsScorecard.getInningsId());

				assertEquals(
						1,
						inningsScorecard.getInningsNumber());

				assertEquals(
						battingTeam.getId(),
						inningsScorecard.getBattingTeamId());

				assertEquals(
						bowlingTeam.getId(),
						inningsScorecard.getBowlingTeamId());

				assertEquals(
						4,
						inningsScorecard.getTotalRuns());

				assertEquals(
						0,
						inningsScorecard.getWickets());

				assertEquals(
						1,
						inningsScorecard.getLegalBalls());

				assertEquals(
						InningsStatus.LIVE.toString(),
						inningsScorecard.getStatus());

				// ---------------------------------------------------------
				// 10. Verify batting scorecard
				// ---------------------------------------------------------

				assertEquals(
						1,
						inningsScorecard.getBatting().size());

				var strikerResponse =
						inningsScorecard.getBatting()
								.stream()
								.filter(batting ->
										batting.getPlayerId()
												.equals(striker.getId()))
								.findFirst()
								.orElseThrow();

				assertEquals(
						4,
						strikerResponse.getRuns());

				assertEquals(
						1,
						strikerResponse.getBallsFaced());

				assertEquals(
						1,
						strikerResponse.getFours());

				assertEquals(
						0,
						strikerResponse.getSixes());

				                            // ---------------------------------------------------------
                            // 11. Verify bowling scorecard
                            // ---------------------------------------------------------

                            assertEquals(
                                            1,
                                            inningsScorecard.getBowling().size());

                            var bowlerResponse =
                                            inningsScorecard.getBowling().get(0);

                            assertEquals(
                                            bowler.getId(),
                                            bowlerResponse.getPlayerId());

                            assertEquals(
                                            1,
                                            bowlerResponse.getBallsBowled());

                            assertEquals(
                                            4,
                                            bowlerResponse.getRunsConceded());

                            assertEquals(
                                            0,
                                            bowlerResponse.getWickets());

                            assertEquals(
                                            "0.1",
                                            bowlerResponse.getOvers());

                            // ---------------------------------------------------------
                            // 12. Record a caught wicket
                            // ---------------------------------------------------------

                            Player fielder = createPlayer(
                                            "Scorecard",
                                            "Fielder",
                                            "S8 Fielder " + testId,
                                            PlayerRole.ALL_ROUNDER);

                            createLineup(
                                            match,
                                            bowlingTeam,
                                            fielder,
                                            2);

                            RecordDeliveryRequest wicketRequest =
                                            createDeliveryRequest(
                                                            striker,
                                                            nonStriker,
                                                            bowler,
                                                            0);

                            wicketRequest.setWicket(true);
                            wicketRequest.setWicketType(WicketType.CAUGHT);
                            wicketRequest.setDismissedPlayerId(
                                            striker.getId());
                            wicketRequest.setDismissalEnd(
                                            DismissalEnd.STRIKER);
                            wicketRequest.setFielderId(
                                            fielder.getId());

                            deliveryService.recordDelivery(
                                            innings.getId(),
                                            wicketRequest);

                            // ---------------------------------------------------------
                            // 13. Fetch scorecard after wicket
                            // ---------------------------------------------------------

                            ScorecardResponse wicketScorecard =
                                            scorecardService.getScorecard(
                                                            match.getId());

                            var wicketInningsScorecard =
                                            wicketScorecard.getInnings().get(0);

                            assertEquals(
                                            1,
                                            wicketInningsScorecard.getWickets());

                            assertEquals(
                                            1,
                                            wicketInningsScorecard
                                                            .getFallOfWickets()
                                                            .size());

                            var fowResponse =
                                            wicketInningsScorecard
                                                            .getFallOfWickets()
                                                            .get(0);

                            assertEquals(
                                            striker.getId(),
                                            fowResponse.getDismissedPlayerId());

                            assertEquals(
                                            striker.getDisplayName(),
                                            fowResponse.getDismissedPlayerName());

                            assertEquals(
                                            WicketType.CAUGHT,
                                            fowResponse.getWicketType());

                            assertEquals(
                                            1,
                                            wicketInningsScorecard
                                                            .getFieldingEvents()
                                                            .size());

                            var fieldingResponse =
                                            wicketInningsScorecard
                                                            .getFieldingEvents()
                                                            .get(0);

                            assertEquals(
                                            fielder.getId(),
                                            fieldingResponse.getFielderId());

                            assertEquals(
                                            fielder.getDisplayName(),
                                            fieldingResponse.getFielderName());

                            assertEquals(
                                            striker.getId(),
                                            fieldingResponse.getDismissedPlayerId());

                            assertEquals(
                                            striker.getDisplayName(),
                                            fieldingResponse.getDismissedPlayerName());

                            assertEquals(
                                            WicketType.CAUGHT,
                                            fieldingResponse.getWicketType());
    }


	private void createInningsState(
				Innings innings,
				Player striker,
				Player bowler,
				Player nonStriker) {

			InningsState state =
					new InningsState();

			state.setInnings(innings);
			state.setStriker(striker);
			state.setNonStriker(nonStriker);
			state.setCurrentBowler(bowler);
			state.setCurrentOver(1);
			state.setLegalBallsInOver(0);

			inningsStateRepository.save(state);
	}

	private RecordDeliveryRequest createDeliveryRequest(
				Player batter,
				Player nonStriker,
				Player bowler,
				int runs) {

			RecordDeliveryRequest request =
					new RecordDeliveryRequest();

			request.setBatterId(
					batter.getId());

			request.setNonStrikerId(
					nonStriker.getId());

			request.setBowlerId(
					bowler.getId());

			request.setRunsOffBat(runs);

			request.setExtraType(
					ExtraType.NONE);

			request.setExtraRuns(0);

			request.setWicket(false);

			return request;
	}

    private Player createPlayer(
			String firstName,
			String lastName,
			String displayName,
			PlayerRole role) {

		Player player = new Player();

		player.setFirstName(firstName);
		player.setLastName(lastName);
		player.setDisplayName(displayName);
		player.setRole(role);

		return playerRepository.save(player);
	}

    private void createLineup(
            Match match,
            Team team,
            Player player,
            int jerseyNumber) {

        MatchLineup lineup =
                new MatchLineup();

        lineup.setMatch(match);
        lineup.setTeam(team);
        lineup.setPlayer(player);
        lineup.setJerseyNumber(jerseyNumber);
        lineup.setPlaying(true);
        lineup.setCaptain(false);
        lineup.setWicketKeeper(false);

        matchLineupRepository.save(lineup);
    }
}
