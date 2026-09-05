package com.cricklocal;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.dto.InningsResponse;
import com.cricklocal.entity.InningsState;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.ExtraType;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cricklocal.enums.PlayerRole;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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