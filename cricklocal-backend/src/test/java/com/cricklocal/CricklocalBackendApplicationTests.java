package com.cricklocal;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.controller.PlayerRegistrationController;
import com.cricklocal.dto.InningsResponse;
import com.cricklocal.entity.InningsState;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.dto.ScorecardResponse;
import com.cricklocal.dto.CareerStatsResponse;
import com.cricklocal.dto.PlayerRegistrationInvitationResponse;
import com.cricklocal.dto.PlayerRegistrationImportResponse;
import com.cricklocal.dto.PlayerRegistrationManagementResponse;
import com.cricklocal.dto.PlayerImportResponse;
import com.cricklocal.dto.PlayerRegistrationDetailResponse;
import com.cricklocal.dto.PlayerRegistrationSummaryResponse;
import com.cricklocal.dto.PlayerRegistrationRegenerateResponse;
import com.cricklocal.dto.PlayerRegistrationLinkResponse;
import com.cricklocal.dto.PlayerRegistrationUpdateRequest;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.entity.Series;
import com.cricklocal.entity.SeriesTeam;
import com.cricklocal.entity.AdminEntitlement;
import com.cricklocal.entity.User;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.entity.UserAuthentication;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import com.cricklocal.enums.DismissalEnd;
import com.cricklocal.enums.InningsStatus;
import com.cricklocal.enums.MatchTeamSide;
import com.cricklocal.enums.MatchFormat;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.enums.BattingStyle;
import com.cricklocal.enums.BowlingStyle;
import com.cricklocal.enums.AdminSubscriptionStatus;
import com.cricklocal.enums.UserRole;
import com.cricklocal.enums.PlayerRole;
import com.cricklocal.enums.PlayerRegistrationStatus;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.enums.AuthenticationProvider;
import com.cricklocal.repository.UserRepository;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.repository.InningsStateRepository;
import com.cricklocal.repository.MatchResultRepository;
import com.cricklocal.repository.TeamPlayerRepository;
import com.cricklocal.repository.SeriesTeamRepository;
import com.cricklocal.repository.PlayingXIRepository;
import com.cricklocal.repository.SeriesRepository;
import com.cricklocal.repository.AdminEntitlementRepository;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.UserAuthenticationRepository;
import com.cricklocal.service.DeliveryService;
import com.cricklocal.service.InningsService;
import com.cricklocal.service.ScorecardService;
import com.cricklocal.service.CareerStatisticsService;
import com.cricklocal.service.MatchService;
import com.cricklocal.service.PlayerHistoryService;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.service.PlayerRegistrationInvitationService;
import com.cricklocal.service.PlayerRegistrationManagementService;
import com.cricklocal.service.PlayerRegistrationService;
import com.cricklocal.service.PlayerImportService;
import com.cricklocal.service.PlayerRegistrationSummaryService;
import com.cricklocal.service.PlayerRegistrationDetailService;
import com.cricklocal.service.PlayerRegistrationRegenerateService;
import com.cricklocal.service.PlayerRegistrationExcelExportService;
import com.cricklocal.service.PlayerRegistrationLinkService;
import com.cricklocal.service.PlayerRegistrationInvitationMaintenanceService;
import com.cricklocal.service.PlayerRegistrationInvitationBackfillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.dao.DataIntegrityViolationException;
import com.cricklocal.enums.PlayerRole;
import com.cricklocal.service.LeaderboardService;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.dto.ErrorResponse;
import com.cricklocal.exception.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class CricklocalBackendApplicationTests {

        @Autowired
        private TeamRepository teamRepository;

        @Autowired
        private PlayerRepository playerRepository;

        @Autowired
        private MatchRepository matchRepository;

        @Autowired
        private SeriesTeamRepository seriesTeamRepository;

        @Autowired
        private SeriesRepository seriesRepository;

        @Autowired
        private PlayingXIRepository playingXIRepository;

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
	private TeamPlayerRepository teamPlayerRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PlayerRegistrationInvitationRepository playerRegistrationInvitationRepository;
        
        @Autowired
        private AdminEntitlementRepository adminEntitlementRepository;

        @Autowired
        private UserAuthenticationRepository userAuthenticationRepository;

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

	@Autowired
	private MatchService matchService;

	@Autowired
	private PlayerHistoryService playerHistoryService;


        @Autowired
        private AdminAccessService adminAccessService;

        @Autowired
        private PlayerRegistrationInvitationService playerRegistrationInvitationService;

        @Autowired
        private PlayerRegistrationService playerRegistrationService;

        @Autowired
        private PlayerImportService playerImportService;

        @Autowired
        private PlayerRegistrationSummaryService playerRegistrationSummaryService;

        @Autowired
        private PlayerRegistrationManagementService playerRegistrationManagementService;

        @Autowired
        private PlayerRegistrationDetailService playerRegistrationDetailService;

        @Autowired
        private PlayerRegistrationRegenerateService playerRegistrationRegenerateService;

        @Autowired
        private PlayerRegistrationExcelExportService playerRegistrationExcelExportService;

        @Autowired
        private PlayerRegistrationLinkService playerRegistrationLinkService;

        @Autowired
        private PlayerRegistrationInvitationMaintenanceService playerRegistrationInvitationMaintenanceService;

        @Autowired
        private PlayerRegistrationInvitationBackfillService playerRegistrationInvitationBackfillService;

        @Autowired
        private PlayerRegistrationController playerRegistrationController;

	@Autowired
	private MockMvc mockMvc;

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
                MatchStatus.LIVE,
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
    void backendShouldVerifyCompleteMatchLifecycleAcrossModules() {

                String testId = String.valueOf(System.currentTimeMillis());

                Team teamA = new Team();
                teamA.setName("Module 13 E2E Team A " + testId);
                teamA.setShortName("E13A" + testId);
                teamA.setCity("Nellore");
                teamA = teamRepository.save(teamA);

                Team teamB = new Team();
                teamB.setName("Module 13 E2E Team B " + testId);
                teamB.setShortName("E13B" + testId);
                teamB.setCity("Nellore");
                teamB = teamRepository.save(teamB);

                Player batterA = createPlayer(
                                "E2E", "BatterA " + testId,
                                "E2E Batter A " + testId, PlayerRole.BATTER);

                Player bowlerA = createPlayer(
                                "E2E", "BowlerA " + testId,
                                "E2E Bowler A " + testId, PlayerRole.BOWLER);

                Player batterB = createPlayer(
                                "E2E", "BatterB " + testId,
                                "E2E Batter B " + testId, PlayerRole.BATTER);

                Player bowlerB = createPlayer(
                                "E2E", "BowlerB " + testId,
                                "E2E Bowler B " + testId, PlayerRole.BOWLER);

                Match match = new Match();
                match.setName("Module 13 E2E Match " + testId);
                match.setMatchNumber(1);
                match.setFormat(MatchFormat.T20);
                match.setTotalOvers(1);
                match.setMaxPlayersPerTeam(2);
                match.setScheduledAt(Instant.now());
                match.setVenue("E2E Test Ground");
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

                createLineup(match, teamA, batterA, 1);
                createLineup(match, teamA, bowlerA, 2);
                createLineup(match, teamB, batterB, 1);
                createLineup(match, teamB, bowlerB, 2);

                StartInningsRequest innings1Request = new StartInningsRequest();
                innings1Request.setBattingTeamId(teamA.getId());
                innings1Request.setBowlingTeamId(teamB.getId());
                innings1Request.setInningsNumber(1);

                InningsResponse innings1Response =
                                inningsService.startInnings(match.getId(), innings1Request);

                Innings innings1 =
                                inningsRepository.findById(innings1Response.getId()).orElseThrow();

                createInningsState(innings1, batterA, bowlerB, bowlerA);

                deliveryService.recordDelivery(
                                innings1.getId(),
                                createDeliveryRequest(batterA, bowlerA, bowlerB, 3));

                inningsService.declareInnings(innings1.getId());

                StartInningsRequest innings2Request = new StartInningsRequest();
                innings2Request.setBattingTeamId(teamB.getId());
                innings2Request.setBowlingTeamId(teamA.getId());
                innings2Request.setInningsNumber(2);

                InningsResponse innings2Response =
                                inningsService.startInnings(match.getId(), innings2Request);

                Innings innings2 =
                                inningsRepository.findById(innings2Response.getId()).orElseThrow();

                createInningsState(innings2, batterB, bowlerA, bowlerB);

                deliveryService.recordDelivery(
                                innings2.getId(),
                                createDeliveryRequest(batterB, bowlerB, bowlerA, 4));

                Match completedMatch =
                                matchRepository.findById(match.getId()).orElseThrow();

                assertEquals(MatchStatus.COMPLETED, completedMatch.getStatus());
                assertTrue(matchResultRepository.existsByMatch(completedMatch));

                ScorecardResponse scorecard =
								scorecardService.getScorecard(match.getId());

				assertEquals(match.getId(), scorecard.getMatchId());
				assertEquals("COMPLETED", scorecard.getStatus());
				assertEquals(2, scorecard.getInnings().size());
				assertTrue(scorecard.getResult() != null);
				assertEquals(teamB.getId(), scorecard.getResult().getWinningTeamId());
				assertEquals(1, scorecard.getResult().getMarginWickets());

                CareerStatsResponse careerStats =
                                careerStatisticsService.getCareerStats(batterA.getId());

                assertEquals(batterA.getId(), careerStats.getPlayerId());
                assertEquals(3L, careerStats.getBatting().getRuns());
                assertEquals(1L, careerStats.getBatting().getMatches());

                var leaderboard = leaderboardService.getTopRunScorers();

                final Long e2eBatterAId = batterA.getId();
                final Long e2eMatchId = match.getId();
                var leaderboardEntry =
                                leaderboard.getEntries().stream()
                                                .filter(entry ->
                                                                entry.getPlayerId().equals(e2eBatterAId))
                                                .findFirst()
                                                .orElseThrow();

                assertEquals(3L, leaderboardEntry.getPrimaryValue());

                var matchHistory = matchService.getMatchHistory();

                var historyEntry =
                                matchHistory.stream()
                                                .filter(item -> item.getId().equals(e2eMatchId))
                                                .findFirst()
                                                .orElseThrow();

                assertEquals("Module 13 E2E Match " + testId, historyEntry.getName());
                assertEquals(MatchStatus.COMPLETED, historyEntry.getStatus());

                var playerHistory =
                                playerHistoryService.getPlayerMatchHistory(e2eBatterAId);

                var playerHistoryEntry =
                                playerHistory.stream()
                                                .filter(item -> item.getMatchId().equals(e2eMatchId))
                                                .findFirst()
                                                .orElseThrow();

                assertEquals(MatchStatus.COMPLETED, playerHistoryEntry.getMatchStatus());
                assertEquals(teamA.getId(), playerHistoryEntry.getTeamId());
                assertTrue(playerHistoryEntry.getPlaying());
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

        @Test
        void startInningsShouldSetMatchStatusToLive() {

                        String testId = String.valueOf(System.currentTimeMillis());

                        Team battingTeam = new Team();
                        battingTeam.setName("Match Status Batting Team " + testId);
                        battingTeam.setShortName("MSA" + testId);
                        battingTeam.setCity("Nellore");
                        battingTeam = teamRepository.save(battingTeam);

                        Team bowlingTeam = new Team();
                        bowlingTeam.setName("Match Status Bowling Team " + testId);
                        bowlingTeam.setShortName("MSB" + testId);
                        bowlingTeam.setCity("Nellore");
                        bowlingTeam = teamRepository.save(bowlingTeam);

                        Match match = new Match();
                        match.setName("Match Status Test Match " + testId);
                        match.setMatchNumber(1);
                        match.setFormat(MatchFormat.T20);
                        match.setTotalOvers(20);
                        match.setMaxPlayersPerTeam(11);
                        match.setScheduledAt(Instant.now());
                        match.setStatus(MatchStatus.SCHEDULED);
                        match = matchRepository.save(match);

                        MatchTeam matchTeamA = new MatchTeam();
                        matchTeamA.setMatch(match);
                        matchTeamA.setTeam(battingTeam);
                        matchTeamA.setSide(MatchTeamSide.TEAM_A);
                        matchTeamRepository.save(matchTeamA);

                        MatchTeam matchTeamB = new MatchTeam();
                        matchTeamB.setMatch(match);
                        matchTeamB.setTeam(bowlingTeam);
                        matchTeamB.setSide(MatchTeamSide.TEAM_B);
                        matchTeamRepository.save(matchTeamB);

                        StartInningsRequest request = new StartInningsRequest();
                        request.setInningsNumber(1);
                        request.setBattingTeamId(battingTeam.getId());
                        request.setBowlingTeamId(bowlingTeam.getId());

                        inningsService.startInnings(match.getId(), request);

                        Match savedMatch = matchRepository.findById(match.getId())
                                .orElseThrow();

                        assertEquals(MatchStatus.LIVE, savedMatch.getStatus());
        }

	@Test
	void matchHistoryShouldReturnMatchesNewestFirst() {

			String testId = String.valueOf(System.currentTimeMillis());

				Team teamA = new Team();
				teamA.setName("Module 11 History Team A " + testId);
				teamA.setShortName("M11A" + testId);
				teamA.setCity("Nellore");
				teamA = teamRepository.save(teamA);

				Team teamB = new Team();
				teamB.setName("Module 11 History Team B " + testId);
				teamB.setShortName("M11B" + testId);
				teamB.setCity("Nellore");
				teamB = teamRepository.save(teamB);

				Match olderMatch = new Match();
				olderMatch.setName("Module 11 Older Match " + testId);
				olderMatch.setMatchNumber(1);
				olderMatch.setFormat(MatchFormat.T20);
				olderMatch.setTotalOvers(1);
				olderMatch.setMaxPlayersPerTeam(3);
				olderMatch.setScheduledAt(
						Instant.parse("2026-01-01T10:00:00Z"));
				olderMatch.setStatus(MatchStatus.COMPLETED);
				olderMatch = matchRepository.save(olderMatch);
				final Long olderMatchId = olderMatch.getId();

				MatchTeam olderTeamA = new MatchTeam();
				olderTeamA.setMatch(olderMatch);
				olderTeamA.setTeam(teamA);
				olderTeamA.setSide(MatchTeamSide.TEAM_A);
				matchTeamRepository.save(olderTeamA);

				MatchTeam olderTeamB = new MatchTeam();
				olderTeamB.setMatch(olderMatch);
				olderTeamB.setTeam(teamB);
				olderTeamB.setSide(MatchTeamSide.TEAM_B);
				matchTeamRepository.save(olderTeamB);

				Match newerMatch = new Match();
				newerMatch.setName("Module 11 Newer Match " + testId);
				newerMatch.setMatchNumber(2);
				newerMatch.setFormat(MatchFormat.T20);
				newerMatch.setTotalOvers(1);
				newerMatch.setMaxPlayersPerTeam(3);
				newerMatch.setScheduledAt(
						Instant.parse("2026-02-01T10:00:00Z"));
				newerMatch.setStatus(MatchStatus.COMPLETED);
				newerMatch = matchRepository.save(newerMatch);
				final Long newerMatchId = newerMatch.getId();

				MatchTeam newerTeamA = new MatchTeam();
				newerTeamA.setMatch(newerMatch);
				newerTeamA.setTeam(teamA);
				newerTeamA.setSide(MatchTeamSide.TEAM_A);
				matchTeamRepository.save(newerTeamA);

				MatchTeam newerTeamB = new MatchTeam();
				newerTeamB.setMatch(newerMatch);
				newerTeamB.setTeam(teamB);
				newerTeamB.setSide(MatchTeamSide.TEAM_B);
				matchTeamRepository.save(newerTeamB);

				var history = matchService.getMatchHistory();

				var newerEntry = history.stream()
						.filter(match ->
								match.getId().equals(newerMatchId))
						.findFirst()
						.orElseThrow();

				var olderEntry = history.stream()
						.filter(match ->
								match.getId().equals(olderMatchId))
						.findFirst()
						.orElseThrow();

				assertTrue(
						history.indexOf(newerEntry)
								< history.indexOf(olderEntry));

				assertEquals(
						"Module 11 Newer Match " + testId,
						newerEntry.getName());

				assertEquals(
						"Module 11 Older Match " + testId,
						olderEntry.getName());	
	}

	@Test
	void playerHistoryShouldReturnPlayingMatchesNewestFirst() {

			String testId = String.valueOf(System.currentTimeMillis());

			Team teamA = new Team();
			teamA.setName("Module 11 Player History Team A " + testId);
			teamA.setShortName("M11PA" + testId);
			teamA.setCity("Nellore");
			teamA = teamRepository.save(teamA);

			Team teamB = new Team();
			teamB.setName("Module 11 Player History Team B " + testId);
			teamB.setShortName("M11PB" + testId);
			teamB.setCity("Nellore");
			teamB = teamRepository.save(teamB);

			Player player = createPlayer(
					"History",
					"Player " + testId,
					"History Player " + testId,
					PlayerRole.BATTER);

			Player otherPlayer = createPlayer(
					"History",
					"Other " + testId,
					"History Other " + testId,
					PlayerRole.BATTER);

			Match olderMatch = new Match();
			olderMatch.setName("Module 11 Player Older Match " + testId);
			olderMatch.setMatchNumber(1);
			olderMatch.setFormat(MatchFormat.T20);
			olderMatch.setTotalOvers(1);
			olderMatch.setMaxPlayersPerTeam(3);
			olderMatch.setScheduledAt(
					Instant.parse("2026-03-01T10:00:00Z"));
			olderMatch.setStatus(MatchStatus.COMPLETED);
			olderMatch = matchRepository.save(olderMatch);

			MatchTeam olderTeamA = new MatchTeam();
			olderTeamA.setMatch(olderMatch);
			olderTeamA.setTeam(teamA);
			olderTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(olderTeamA);

			MatchTeam olderTeamB = new MatchTeam();
			olderTeamB.setMatch(olderMatch);
			olderTeamB.setTeam(teamB);
			olderTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(olderTeamB);

			createLineup(
					olderMatch,
					teamA,
					player,
					1);

			createLineup(
					olderMatch,
					teamA,
					otherPlayer,
					2);

			Match newerMatch = new Match();
			newerMatch.setName("Module 11 Player Newer Match " + testId);
			newerMatch.setMatchNumber(2);
			newerMatch.setFormat(MatchFormat.T20);
			newerMatch.setTotalOvers(1);
			newerMatch.setMaxPlayersPerTeam(3);
			newerMatch.setScheduledAt(
					Instant.parse("2026-04-01T10:00:00Z"));
			newerMatch.setStatus(MatchStatus.LIVE);
			newerMatch = matchRepository.save(newerMatch);

			MatchTeam newerTeamA = new MatchTeam();
			newerTeamA.setMatch(newerMatch);
			newerTeamA.setTeam(teamA);
			newerTeamA.setSide(MatchTeamSide.TEAM_A);
			matchTeamRepository.save(newerTeamA);

			MatchTeam newerTeamB = new MatchTeam();
			newerTeamB.setMatch(newerMatch);
			newerTeamB.setTeam(teamB);
			newerTeamB.setSide(MatchTeamSide.TEAM_B);
			matchTeamRepository.save(newerTeamB);

			createLineup(
					newerMatch,
					teamA,
					player,
					1);

			final Long olderMatchId = olderMatch.getId();
			final Long newerMatchId = newerMatch.getId();

			var history =
					playerHistoryService.getPlayerMatchHistory(
							player.getId());

			assertEquals(
					2,
					history.size());

			assertEquals(
					newerMatchId,
					history.get(0).getMatchId());

			assertEquals(
					olderMatchId,
					history.get(1).getMatchId());

			assertEquals(
					"Module 11 Player Newer Match " + testId,
					history.get(0).getMatchName());

			assertEquals(
					"Module 11 Player Older Match " + testId,
					history.get(1).getMatchName());

			assertEquals(
					teamA.getId(),
					history.get(0).getTeamId());

			assertEquals(
					teamA.getName(),
					history.get(0).getTeamName());

			assertTrue(
					history.get(0).getPlaying());

			assertTrue(
					history.get(1).getPlaying());
	}

	@Test
	void playerHistoryShouldRejectUnknownPlayer() {

			assertThrows(
					ResourceNotFoundException.class,
					() -> playerHistoryService.getPlayerMatchHistory(999999999L));
	}

	@Test
	void apiShouldReturnStandardErrorResponseForUnknownPlayer() throws Exception {

			mockMvc.perform(
					org.springframework.test.web.servlet.request.MockMvcRequestBuilders
								.get("/api/players/999999999/career-stats"))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.status()
										.isNotFound())
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.status")
										.value(404))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.message")
										.value("Player not found: 999999999"))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.timestamp")
										.exists());
	}

	@Test
	void apiShouldReturnStandardErrorResponseForValidationFailure()
			throws Exception {

			mockMvc.perform(
					org.springframework.test.web.servlet.request.MockMvcRequestBuilders
							.post("/api/teams")
							.contentType(
									org.springframework.http.MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "",
										"shortName": "TEST"
									}
									"""))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.status()
									.isBadRequest())
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.status")
									.value(400))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.message")
									.value("Team name is required"))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.timestamp")
									.exists());
	}

	@Test
	void apiShouldReturnStandardErrorResponseForInvalidInningsTeams()
			throws Exception {

				String testId = String.valueOf(System.currentTimeMillis());

				Team team = new Team();
				team.setName("Module 12 Error Team " + testId);
				team.setShortName("M12E" + testId);
				team.setCity("Nellore");
				team = teamRepository.save(team);

				Match match = new Match();
				match.setName("Module 12 Error Match " + testId);
				match.setMatchNumber(1);
				match.setFormat(MatchFormat.T20);
				match.setTotalOvers(1);
				match.setMaxPlayersPerTeam(3);
				match.setScheduledAt(Instant.now());
				match.setStatus(MatchStatus.SCHEDULED);
				match = matchRepository.save(match);

				MatchTeam matchTeam = new MatchTeam();
				matchTeam.setMatch(match);
				matchTeam.setTeam(team);
				matchTeam.setSide(MatchTeamSide.TEAM_A);
				matchTeamRepository.save(matchTeam);

				mockMvc.perform(
						org.springframework.test.web.servlet.request.MockMvcRequestBuilders
								.post("/api/matches/"
										+ match.getId()
										+ "/innings")
								.contentType(
										org.springframework.http.MediaType.APPLICATION_JSON)
								.content("""
										{
											"battingTeamId": %d,
											"bowlingTeamId": %d,
											"inningsNumber": 1
										}
										""".formatted(
										team.getId(),
										team.getId())))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.status()
										.isBadRequest())
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.status")
										.value(400))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.message")
										.value(
												"Batting team and bowling team must be different"))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.timestamp")
										.exists());
	}

	@Test
	void genericExceptionHandlerShouldReturnStandardErrorResponse() {

			GlobalExceptionHandler handler =
					new GlobalExceptionHandler();

			ErrorResponse response =
					handler.handleGenericException(
							new RuntimeException("internal details"));

			assertEquals(
					500,
					response.getStatus());

			assertEquals(
					"An unexpected error occurred",
					response.getMessage());

			assertTrue(
					response.getTimestamp() != null);
	}

	@Test
    void teamExcelImportShouldNotPartiallyCreateTeamsWhenValidationFails()
            throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String validTeamName = "Atomic Import Team " + testId;
        String validShortName = "AI" + testId;

        long initialTeamCount = teamRepository.count();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Teams");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("Team Name");
            header.createCell(1).setCellValue("Short Name");
            header.createCell(2).setCellValue("City");

            org.apache.poi.ss.usermodel.Row validRow =
                    sheet.createRow(1);

            validRow.createCell(0).setCellValue(validTeamName);
            validRow.createCell(1).setCellValue(validShortName);
            validRow.createCell(2).setCellValue("Hyderabad");

            org.apache.poi.ss.usermodel.Row invalidRow =
                    sheet.createRow(2);

            invalidRow.createCell(0).setCellValue("Invalid Import Team " + testId);
            invalidRow.createCell(1).setCellValue("");
            invalidRow.createCell(2).setCellValue("Vijayawada");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .multipart("/api/teams/import")
                                    .file(
                                            new org.springframework.mock.web.MockMultipartFile(
                                                    "file",
                                                    "atomic-import.xlsx",
                                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                    outputStream.toByteArray())))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status()
                                    .isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows")
                                    .value(0))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors.length()")
                                    .value(1))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].rowNumber")
                                    .value(3))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Short name is required"));
        }

        assertEquals(
                initialTeamCount,
                teamRepository.count());

        assertFalse(
                teamRepository.existsByName(validTeamName));

        assertFalse(
                teamRepository.existsByShortName(validShortName));
    }

	@Test
    void teamExcelImportShouldCreateValidTeams()
            throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String teamOneName = "Import Team One " + testId;
        String teamOneShortName = "I1" + testId;

        String teamTwoName = "Import Team Two " + testId;
        String teamTwoShortName = "I2" + testId;

        long initialTeamCount = teamRepository.count();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Teams");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("Team Name");
            header.createCell(1).setCellValue("Short Name");
            header.createCell(2).setCellValue("City");

            org.apache.poi.ss.usermodel.Row firstTeam =
                    sheet.createRow(1);

            firstTeam.createCell(0).setCellValue(teamOneName);
            firstTeam.createCell(1).setCellValue(teamOneShortName);
            firstTeam.createCell(2).setCellValue("Hyderabad");

            org.apache.poi.ss.usermodel.Row secondTeam =
                    sheet.createRow(2);

            secondTeam.createCell(0).setCellValue(teamTwoName);
            secondTeam.createCell(1).setCellValue(teamTwoShortName);
            secondTeam.createCell(2).setCellValue("Vijayawada");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .multipart("/api/teams/import")
                                    .file(
                                            new org.springframework.mock.web.MockMultipartFile(
                                                    "file",
                                                    "teams-import.xlsx",
                                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                    outputStream.toByteArray())))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status()
                                    .isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors")
                                    .isEmpty());
        }

        assertEquals(
                initialTeamCount + 2,
                teamRepository.count());

        assertTrue(
                teamRepository.existsByName(teamOneName));

        assertTrue(
                teamRepository.existsByShortName(teamOneShortName));

        assertTrue(
                teamRepository.existsByName(teamTwoName));

        assertTrue(
                teamRepository.existsByShortName(teamTwoShortName));
    }

	@Test
	void teamExcelValidationShouldAcceptValidWorkbookWithoutCreatingTeams()
			throws Exception {

		long initialTeamCount = teamRepository.count();

		try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
					new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

			org.apache.poi.ss.usermodel.Sheet sheet =
					workbook.createSheet("Teams");

			org.apache.poi.ss.usermodel.Row header =
					sheet.createRow(0);

			header.createCell(0).setCellValue("Team Name");
			header.createCell(1).setCellValue("Short Name");
			header.createCell(2).setCellValue("City");

			org.apache.poi.ss.usermodel.Row firstTeam =
					sheet.createRow(1);

			firstTeam.createCell(0).setCellValue("CMR WARRIORS");
			firstTeam.createCell(1).setCellValue("WAR");
			firstTeam.createCell(2).setCellValue("Hyderabad");

			org.apache.poi.ss.usermodel.Row secondTeam =
					sheet.createRow(2);

			secondTeam.createCell(0).setCellValue("CMR TITANS");
			secondTeam.createCell(1).setCellValue("TIT");
			secondTeam.createCell(2).setCellValue("Vijayawada");

			java.io.ByteArrayOutputStream outputStream =
					new java.io.ByteArrayOutputStream();

			workbook.write(outputStream);

			mockMvc.perform(
							org.springframework.test.web.servlet.request.MockMvcRequestBuilders
									.multipart("/api/teams/import/validate")
									.file(
											new org.springframework.mock.web.MockMultipartFile(
													"file",
													"teams.xlsx",
													"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
													outputStream.toByteArray())))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.status()
									.isOk())
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.totalRows")
									.value(2))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.createdRows")
									.value(0))
					.andExpect(
							org.springframework.test.web.servlet.result.MockMvcResultMatchers
									.jsonPath("$.errors")
									.isEmpty());
		}

		assertEquals(
				initialTeamCount,
				teamRepository.count());
	}

	@Test
    void teamExcelValidationShouldRejectInvalidHeaders()
				throws Exception {

			try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
						new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

				org.apache.poi.ss.usermodel.Sheet sheet =
						workbook.createSheet("Teams");

				org.apache.poi.ss.usermodel.Row header =
						sheet.createRow(0);

				header.createCell(0).setCellValue("Name");
				header.createCell(1).setCellValue("Code");
				header.createCell(2).setCellValue("Location");

				java.io.ByteArrayOutputStream outputStream =
						new java.io.ByteArrayOutputStream();

				workbook.write(outputStream);

				mockMvc.perform(
								org.springframework.test.web.servlet.request.MockMvcRequestBuilders
										.multipart("/api/teams/import/validate")
										.file(
												new org.springframework.mock.web.MockMultipartFile(
														"file",
														"invalid-headers.xlsx",
														"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
														outputStream.toByteArray())))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.status()
										.isBadRequest())
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.status")
										.value(400))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.message")
										.value(
												"Invalid Excel headers. Expected: Team Name, Short Name, City"))
						.andExpect(
								org.springframework.test.web.servlet.result.MockMvcResultMatchers
										.jsonPath("$.timestamp")
										.exists());
			}
    }

    @Test
    void teamExcelValidationShouldReportRowLevelErrors()
            throws Exception {

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Teams");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("Team Name");
            header.createCell(1).setCellValue("Short Name");
            header.createCell(2).setCellValue("City");

            org.apache.poi.ss.usermodel.Row firstBadRow =
                    sheet.createRow(1);

            firstBadRow.createCell(0).setCellValue("");
            firstBadRow.createCell(1).setCellValue("BAD1");
            firstBadRow.createCell(2).setCellValue("Hyderabad");

            org.apache.poi.ss.usermodel.Row secondBadRow =
                    sheet.createRow(2);

            secondBadRow.createCell(0).setCellValue("Valid Team Name");
            secondBadRow.createCell(1).setCellValue("");
            secondBadRow.createCell(2).setCellValue("Vijayawada");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .multipart("/api/teams/import/validate")
                                    .file(
                                            new org.springframework.mock.web.MockMultipartFile(
                                                    "file",
                                                    "row-errors.xlsx",
                                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                    outputStream.toByteArray())))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status()
                                    .isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows")
                                    .value(0))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors.length()").value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].rowNumber")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Team name is required"))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[1].rowNumber")
                                    .value(3))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[1].message")
                                    .value("Short name is required"));
        }
    }

    @Test
    void teamExcelValidationShouldRejectDuplicateNamesAndShortNames()
            throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        Team existingTeam = new Team();
        existingTeam.setName("Existing Team " + testId);
        existingTeam.setShortName("EXT" + testId);
        existingTeam.setCity("Hyderabad");
        existingTeam = teamRepository.save(existingTeam);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Teams");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("Team Name");
            header.createCell(1).setCellValue("Short Name");
            header.createCell(2).setCellValue("City");

            org.apache.poi.ss.usermodel.Row firstRow =
                    sheet.createRow(1);

            firstRow.createCell(0).setCellValue(existingTeam.getName());
            firstRow.createCell(1).setCellValue("NEW" + testId);
            firstRow.createCell(2).setCellValue("Vijayawada");

            org.apache.poi.ss.usermodel.Row secondRow =
                    sheet.createRow(2);

            secondRow.createCell(0).setCellValue("New Team " + testId);
            secondRow.createCell(1).setCellValue(existingTeam.getShortName());
            secondRow.createCell(2).setCellValue("Nellore");

            org.apache.poi.ss.usermodel.Row thirdRow =
                    sheet.createRow(3);

            thirdRow.createCell(0).setCellValue("Workbook Duplicate " + testId);
            thirdRow.createCell(1).setCellValue("DUP" + testId);
            thirdRow.createCell(2).setCellValue("Guntur");

            org.apache.poi.ss.usermodel.Row fourthRow =
                    sheet.createRow(4);

            fourthRow.createCell(0).setCellValue("Workbook Duplicate " + testId);
            fourthRow.createCell(1).setCellValue("DUP2" + testId);
            fourthRow.createCell(2).setCellValue("Guntur");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .multipart("/api/teams/import/validate")
                                    .file(
                                            new org.springframework.mock.web.MockMultipartFile(
                                                    "file",
                                                    "duplicate-teams.xlsx",
                                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                    outputStream.toByteArray())))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status()
                                    .isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows")
                                    .value(4))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows")
                                    .value(0))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors.length()").value(3))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].rowNumber")
                                    .value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Team name already exists"))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[1].rowNumber")
                                    .value(3))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[1].message")
                                    .value("Short name already exists"));
        }
    }

	@Test
	void createPlayerApiShouldReturnPlayerResponse() throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		mockMvc.perform(
						post("/api/players")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										"firstName": "API",
										"lastName": "Player",
										"displayName": "API Player %s",
										"phone": "9000000000",
										"battingStyle": "RIGHT_HAND",
										"bowlingStyle": "NONE",
										"role": "BATTER"
										}
										""".formatted(testId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.firstName").value("API"))
				.andExpect(jsonPath("$.lastName").value("Player"))
				.andExpect(jsonPath("$.displayName").value("API Player " + testId))
				.andExpect(jsonPath("$.phone").value("9000000000"))
				.andExpect(jsonPath("$.battingStyle").value("RIGHT_HAND"))
				.andExpect(jsonPath("$.bowlingStyle").value("NONE"))
				.andExpect(jsonPath("$.role").value("BATTER"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.teams").isArray())
				.andExpect(jsonPath("$.teams").isEmpty());
	}

	@Test
	void getPlayersApiShouldReturnPlayerResponses() throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Player player = createPlayer(
				"API",
				"List " + testId,
				"API List Player " + testId,
				PlayerRole.BATTER);

		mockMvc.perform(get("/api/players"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath(
						"$[?(@.id == %d)].displayName"
								.formatted(player.getId()))
						.value("API List Player " + testId));
	}

	@Test
	void getPlayerByIdApiShouldReturnPlayerResponseWithTeamMembership() throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("API Player Team " + testId);
		team.setShortName("APT" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player player = new Player();
		player.setFirstName("API");
		player.setLastName("Member");
		player.setDisplayName("API Member " + testId);
		player.setPhone("9111111111");
		player.setBattingStyle(BattingStyle.LEFT_HAND);
		player.setBowlingStyle(BowlingStyle.LEFT_ARM_MEDIUM);
		player.setRole(PlayerRole.ALL_ROUNDER);
		player = playerRepository.save(player);

		TeamPlayer teamPlayer = new TeamPlayer();
		teamPlayer.setTeam(team);
		teamPlayer.setPlayer(player);
		teamPlayer.setJerseyNumber(27);
		teamPlayer = teamPlayerRepository.save(teamPlayer);

		mockMvc.perform(get("/api/players/" + player.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(player.getId()))
				.andExpect(jsonPath("$.displayName")
						.value("API Member " + testId))
				.andExpect(jsonPath("$.battingStyle")
						.value("LEFT_HAND"))
				.andExpect(jsonPath("$.bowlingStyle")
						.value("LEFT_ARM_MEDIUM"))
				.andExpect(jsonPath("$.role")
						.value("ALL_ROUNDER"))
				.andExpect(jsonPath("$.teams.length()").value(1))
				.andExpect(jsonPath("$.teams[0].teamId")
						.value(team.getId()))
				.andExpect(jsonPath("$.teams[0].teamName")
						.value("API Player Team " + testId))
				.andExpect(jsonPath("$.teams[0].shortName")
						.value("APT" + testId))
				.andExpect(jsonPath("$.teams[0].jerseyNumber")
						.value(27))
				.andExpect(jsonPath("$.teams[0].joinedAt").exists())
				.andExpect(jsonPath("$.teams[0].leftAt").doesNotExist());
	}

	@Test
	void getUnknownPlayerApiShouldReturnNotFound() throws Exception {

		mockMvc.perform(get("/api/players/999999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message")
						.value("Player not found: 999999999"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
        void playerExcelImportShouldCreateValidPlayers() throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String playerOneName = "Excel Player One " + testId;
        String playerTwoName = "Excel Player Two " + testId;

        long initialPlayerCount = playerRepository.count();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row rowOne =
                    sheet.createRow(1);

            rowOne.createCell(0).setCellValue("Excel");
            rowOne.createCell(1).setCellValue("One");
            rowOne.createCell(2).setCellValue(playerOneName);
            rowOne.createCell(3).setCellValue("9000000001");
            rowOne.createCell(4).setCellValue("RIGHT_HAND");
            rowOne.createCell(5).setCellValue("NONE");
            rowOne.createCell(6).setCellValue("BATTER");

            org.apache.poi.ss.usermodel.Row rowTwo =
                    sheet.createRow(2);

            rowTwo.createCell(0).setCellValue("Excel");
            rowTwo.createCell(1).setCellValue("Two");
            rowTwo.createCell(2).setCellValue(playerTwoName);
            rowTwo.createCell(3).setCellValue("9000000002");
            rowTwo.createCell(4).setCellValue("LEFT_HAND");
            rowTwo.createCell(5).setCellValue("RIGHT_ARM_FAST");
            rowTwo.createCell(6).setCellValue("ALL_ROUNDER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/players/import")
                                .file(
                                        new org.springframework.mock.web.MockMultipartFile(
                                                "file",
                                                "players-import.xlsx",
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                outputStream.toByteArray()))
                                .with(
                                        org.springframework.security.test.web.servlet.request
                                                .SecurityMockMvcRequestPostProcessors
                                                .authentication(
                                                        createAdminAuthentication()
                                                )
                                ))
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .status().isOk())
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .jsonPath("$.totalRows").value(2))
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .jsonPath("$.createdRows").value(2));

            assertEquals(
                    initialPlayerCount + 2,
                    playerRepository.count());

            assertTrue(
                    playerRepository.existsByDisplayName(playerOneName));

            assertTrue(
                    playerRepository.existsByDisplayName(playerTwoName));
        }
    }

    @Test
    void playerExcelValidationShouldNotCreatePlayers() throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String playerName = "Validation Player " + testId;

        long initialPlayerCount = playerRepository.count();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row row =
                    sheet.createRow(1);

            row.createCell(0).setCellValue("Validation");
            row.createCell(1).setCellValue("Player");
            row.createCell(2).setCellValue(playerName);
            row.createCell(3).setCellValue("9000000010");
            row.createCell(4).setCellValue("RIGHT_HAND");
            row.createCell(5).setCellValue("NONE");
            row.createCell(6).setCellValue("BATTER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "players-validation.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                        SecurityMockMvcRequestPostProcessors.authentication(
                                                createAdminAuthentication()
                                        )
                                        ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows").value(1))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows").value(0));

            assertEquals(
                    initialPlayerCount,
                    playerRepository.count());

            assertFalse(
                    playerRepository.existsByDisplayName(playerName));
        }
    }

    @Test
    void playerExcelValidationShouldRejectInvalidHeaders() throws Exception {

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Surname");
            header.createCell(2).setCellValue("Player");
            header.createCell(3).setCellValue("Mobile");
            header.createCell(4).setCellValue("Batting");
            header.createCell(5).setCellValue("Bowling");
            header.createCell(6).setCellValue("Type");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "invalid-player-headers.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isBadRequest())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.status").value(400))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.message")
                                    .value(
                                            "Invalid Excel headers. Expected: First Name, Last Name, Display Name, Phone, Batting Style, Bowling Style, Role"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectDuplicateDisplayNames() throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String existingName = "Existing Excel Player " + testId;

        Player existingPlayer = new Player();

        existingPlayer.setFirstName("Existing");
        existingPlayer.setLastName("Player");
        existingPlayer.setDisplayName(existingName);
        existingPlayer.setPhone("9000000020");
        existingPlayer.setBattingStyle(BattingStyle.RIGHT_HAND);
        existingPlayer.setBowlingStyle(BowlingStyle.NONE);
        existingPlayer.setRole(PlayerRole.BATTER);

        playerRepository.save(existingPlayer);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row row =
                    sheet.createRow(1);

            row.createCell(0).setCellValue("Duplicate");
            row.createCell(1).setCellValue("Player");
            row.createCell(2).setCellValue(existingName);
            row.createCell(3).setCellValue("9000000021");
            row.createCell(4).setCellValue("RIGHT_HAND");
            row.createCell(5).setCellValue("NONE");
            row.createCell(6).setCellValue("BATTER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "duplicate-player.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows").value(1))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows").value(0))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Display name already exists"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectDuplicateDisplayNamesWithinWorkbook() throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());
        String duplicateName = "Workbook Duplicate " + testId;

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            for (int rowNumber = 1; rowNumber <= 2; rowNumber++) {
                org.apache.poi.ss.usermodel.Row row =
                        sheet.createRow(rowNumber);

                row.createCell(0).setCellValue("Workbook");
                row.createCell(1).setCellValue("Player" + rowNumber);
                row.createCell(2).setCellValue(duplicateName);
                row.createCell(3).setCellValue("900000003" + rowNumber);
                row.createCell(4).setCellValue("RIGHT_HAND");
                row.createCell(5).setCellValue("NONE");
                row.createCell(6).setCellValue("BATTER");
            }

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "workbook-duplicates.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.totalRows").value(2))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.createdRows").value(0))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Duplicate display name in Excel file"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectInvalidRole() throws Exception {

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row row = sheet.createRow(1);

            row.createCell(0).setCellValue("Invalid");
            row.createCell(1).setCellValue("Role");
            row.createCell(2).setCellValue(
                    "Invalid Role " + System.currentTimeMillis());
            row.createCell(3).setCellValue("9000000041");
            row.createCell(4).setCellValue("RIGHT_HAND");
            row.createCell(5).setCellValue("NONE");
            row.createCell(6).setCellValue("INVALID_ROLE");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "invalid-role.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value(
                                            "Invalid role. Expected: BATTER, BOWLER, ALL_ROUNDER, WICKET_KEEPER"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectInvalidBattingStyle() throws Exception {

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row row = sheet.createRow(1);

            row.createCell(0).setCellValue("Invalid");
            row.createCell(1).setCellValue("Batting");
            row.createCell(2).setCellValue(
                    "Invalid Batting " + System.currentTimeMillis());
            row.createCell(3).setCellValue("9000000051");
            row.createCell(4).setCellValue("INVALID_BATTING");
            row.createCell(5).setCellValue("NONE");
            row.createCell(6).setCellValue("BATTER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "invalid-batting.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value(
                                            "Invalid batting style. Expected: RIGHT_HAND, LEFT_HAND"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectInvalidBowlingStyle() throws Exception {

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row row = sheet.createRow(1);

            row.createCell(0).setCellValue("Invalid");
            row.createCell(1).setCellValue("Bowling");
            row.createCell(2).setCellValue(
                    "Invalid Bowling " + System.currentTimeMillis());
            row.createCell(3).setCellValue("9000000061");
            row.createCell(4).setCellValue("RIGHT_HAND");
            row.createCell(5).setCellValue("INVALID_BOWLING");
            row.createCell(6).setCellValue("BOWLER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .multipart("/api/players/import/validate")
                            .file(
                                    new org.springframework.mock.web.MockMultipartFile(
                                            "file",
                                            "invalid-bowling.xlsx",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            outputStream.toByteArray())).with(
                                                SecurityMockMvcRequestPostProcessors.authentication(
                                                        createAdminAuthentication()
                                                )
                                                ))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .status().isOk())
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                    .jsonPath("$.errors[0].message")
                                    .value("Invalid bowling style"));
        }
    }

    @Test
    void playerExcelValidationShouldRejectUnsupportedFileExtension() throws Exception {

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "players.csv",
                        "text/csv",
                        "First Name,Last Name,Display Name".getBytes());

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/players/import/validate")
                        .file(file).with(
                                SecurityMockMvcRequestPostProcessors.authentication(
                                        createAdminAuthentication()
                                )
                                ))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isBadRequest())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.status").value(400))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.message")
                                .value("Only .xlsx Excel files are supported"));
    }

    @Test
    void playerExcelImportShouldNotPartiallyCreatePlayersWhenValidationFails()
            throws Exception {

        String testId = String.valueOf(System.currentTimeMillis());

        String validPlayerName = "Atomic Player " + testId;
        String invalidPlayerName = "Invalid Atomic Player " + testId;

        long initialPlayerCount = playerRepository.count();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Players");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Display Name");
            header.createCell(3).setCellValue("Phone");
            header.createCell(4).setCellValue("Batting Style");
            header.createCell(5).setCellValue("Bowling Style");
            header.createCell(6).setCellValue("Role");

            org.apache.poi.ss.usermodel.Row validRow = sheet.createRow(1);

            validRow.createCell(0).setCellValue("Atomic");
            validRow.createCell(1).setCellValue("Player");
            validRow.createCell(2).setCellValue(validPlayerName);
            validRow.createCell(3).setCellValue("9000000071");
            validRow.createCell(4).setCellValue("RIGHT_HAND");
            validRow.createCell(5).setCellValue("NONE");
            validRow.createCell(6).setCellValue("BATTER");

            org.apache.poi.ss.usermodel.Row invalidRow = sheet.createRow(2);

            invalidRow.createCell(0).setCellValue("");
            invalidRow.createCell(1).setCellValue("Invalid");
            invalidRow.createCell(2).setCellValue(invalidPlayerName);
            invalidRow.createCell(3).setCellValue("9000000072");
            invalidRow.createCell(4).setCellValue("RIGHT_HAND");
            invalidRow.createCell(5).setCellValue("NONE");
            invalidRow.createCell(6).setCellValue("BATTER");

            java.io.ByteArrayOutputStream outputStream =
                    new java.io.ByteArrayOutputStream();

            workbook.write(outputStream);

            mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/players/import")
                                .file(
                                        new org.springframework.mock.web.MockMultipartFile(
                                                "file",
                                                "atomic-players.xlsx",
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                outputStream.toByteArray()))
                                .with(
                                        org.springframework.security.test.web.servlet.request
                                                .SecurityMockMvcRequestPostProcessors
                                                .authentication(
                                                        createAdminAuthentication()
                                                )
                                ))
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .status().isOk())
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .jsonPath("$.totalRows").value(2))
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .jsonPath("$.createdRows").value(0));

            assertEquals(
                    initialPlayerCount,
                    playerRepository.count());

            assertFalse(
                    playerRepository.existsByDisplayName(validPlayerName));
        }
    }

	@Test
	void addPlayerToTeamShouldRejectDuplicateActiveJerseyNumber()
			throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("Jersey Team " + testId);
		team.setShortName("JER" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player firstPlayer = new Player();
		firstPlayer.setFirstName("First");
		firstPlayer.setLastName("Player");
		firstPlayer.setDisplayName("First Player " + testId);
		firstPlayer.setRole(PlayerRole.BATTER);
		firstPlayer = playerRepository.save(firstPlayer);

		TeamPlayer firstTeamPlayer = new TeamPlayer();
		firstTeamPlayer.setTeam(team);
		firstTeamPlayer.setPlayer(firstPlayer);
		firstTeamPlayer.setJerseyNumber(18);
		teamPlayerRepository.save(firstTeamPlayer);

		Player secondPlayer = new Player();
		secondPlayer.setFirstName("Second");
		secondPlayer.setLastName("Player");
		secondPlayer.setDisplayName("Second Player " + testId);
		secondPlayer.setRole(PlayerRole.BATTER);
		secondPlayer = playerRepository.save(secondPlayer);

		mockMvc.perform(
				post("/api/teams/" + team.getId()
						+ "/players/" + secondPlayer.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"jerseyNumber": 18
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message")
						.value("Jersey number is already assigned to an active player"));
	}

	@Test
	void removePlayerFromTeamShouldDeactivatePlayerAndReleaseJerseyNumber()
			throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("Remove Team " + testId);
		team.setShortName("REM" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player firstPlayer = new Player();
		firstPlayer.setFirstName("First");
		firstPlayer.setLastName("Player");
		firstPlayer.setDisplayName("Remove First " + testId);
		firstPlayer.setRole(PlayerRole.BATTER);
		firstPlayer = playerRepository.save(firstPlayer);

		TeamPlayer firstTeamPlayer = new TeamPlayer();
		firstTeamPlayer.setTeam(team);
		firstTeamPlayer.setPlayer(firstPlayer);
		firstTeamPlayer.setJerseyNumber(18);
		firstTeamPlayer = teamPlayerRepository.save(firstTeamPlayer);

		mockMvc.perform(
				org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.delete("/api/teams/" + team.getId()
								+ "/players/" + firstPlayer.getId()))
				.andExpect(status().isNoContent());

		TeamPlayer removedPlayer = teamPlayerRepository
				.findById(firstTeamPlayer.getId())
				.orElseThrow();

		assertFalse(removedPlayer.getActive());
		assertTrue(removedPlayer.getLeftAt() != null);

		mockMvc.perform(
				get("/api/teams/" + team.getId() + "/players"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		Player secondPlayer = new Player();
		secondPlayer.setFirstName("Second");
		secondPlayer.setLastName("Player");
		secondPlayer.setDisplayName("Remove Second " + testId);
		secondPlayer.setRole(PlayerRole.BATTER);
		secondPlayer = playerRepository.save(secondPlayer);

		mockMvc.perform(
				post("/api/teams/" + team.getId()
						+ "/players/" + secondPlayer.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"jerseyNumber": 18
								}
								"""))
				.andExpect(status().isCreated());
	}

        @Test
        void removePlayerFromTeamShouldRejectPlayerInFinalizedPlayingXI()
                throws Exception {

                String testId = String.valueOf(System.nanoTime());

                Team team = new Team();
                team.setName("Finalized XI Removal Team " + testId);
                team.setShortName("FXR" + testId.substring(testId.length() - 3));
                team.setCity("Test City");
                team = teamRepository.save(team);

                Match match = new Match();
                match.setName("Finalized XI Removal Match " + testId);
                match.setMatchNumber(1);
                match.setFormat(MatchFormat.T20);
                match.setTotalOvers(20);
                match.setMaxPlayersPerTeam(11);
                match.setScheduledAt(
                        Instant.parse("2027-04-01T10:00:00Z"));
                match.setStatus(MatchStatus.SCHEDULED);
                match = matchRepository.save(match);

                MatchTeam matchTeam = new MatchTeam();
                matchTeam.setMatch(match);
                matchTeam.setTeam(team);
                matchTeam.setSide(MatchTeamSide.TEAM_A);
                matchTeamRepository.save(matchTeam);

                Player captain = null;
                Player wicketKeeper = null;

                /*
                * Create exactly 11 active team members and match lineup entries.
                */
                for (int i = 1; i <= 11; i++) {

                        Player player = createPlayer(
                                "Finalized XI Removal Player " + i,
                                "Test",
                                "Finalized XI Removal Player "
                                        + i + " " + testId,
                                PlayerRole.BATTER);

                        if (i == 1) {
                        captain = player;
                        }

                        if (i == 2) {
                        wicketKeeper = player;
                        }

                        TeamPlayer membership = new TeamPlayer();
                        membership.setTeam(team);
                        membership.setPlayer(player);
                        membership.setJerseyNumber(i);
                        membership.setActive(true);
                        teamPlayerRepository.save(membership);

                        createLineup(match, team, player, i);
                }

                /*
                * Set Captain.
                */
                MatchLineup captainLineup =
                        matchLineupRepository
                                .findByMatchAndPlayer(match, captain)
                                .orElseThrow();

                captainLineup.setCaptain(true);
                matchLineupRepository.save(captainLineup);

                /*
                * Set Wicket Keeper.
                */
                MatchLineup wicketKeeperLineup =
                        matchLineupRepository
                                .findByMatchAndPlayer(match, wicketKeeper)
                                .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);

                /*
                * Finalize the Playing XI.
                */
                mockMvc.perform(
                        post("/api/matches/" + match.getId()
                                + "/lineup/team/" + team.getId()
                                + "/finalize"))
                        .andExpect(status().isOk());

                assertTrue(
                        playingXIRepository
                                .existsByMatchAndTeam(match, team));

                /*
                * Attempt to remove a player who is part of
                * the finalized Playing XI.
                */
                mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .delete("/api/teams/" + team.getId()
                                        + "/players/" + captain.getId()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(
                                        "Player cannot be removed because they are part of a finalized Playing XI"));

                /*
                * The team membership must remain active.
                */
                TeamPlayer membership =
                        teamPlayerRepository
                                .findByTeamAndPlayer(team, captain)
                                .orElseThrow();

                assertTrue(membership.getActive());
                assertTrue(membership.getLeftAt() == null);
        }

	@Test
        void setCaptainApiShouldAssignActiveTeamMemberAsCaptain() throws Exception {

                String testId = String.valueOf(System.currentTimeMillis());

                Team team = new Team();
                team.setName("Captain Team " + testId);
                team.setShortName("CAP" + testId);
                team.setCity("Nellore");
                team = teamRepository.save(team);

                Player player = new Player();
                player.setFirstName("Captain");
                player.setLastName("Player");
                player.setDisplayName("Captain Player " + testId);
                player.setRole(PlayerRole.BATTER);
                player = playerRepository.save(player);

                TeamPlayer teamPlayer = new TeamPlayer();
                teamPlayer.setTeam(team);
                teamPlayer.setPlayer(player);
                teamPlayer.setJerseyNumber(7);
                teamPlayer = teamPlayerRepository.save(teamPlayer);

                mockMvc.perform(put(
                                "/api/teams/" + team.getId()
                                        + "/captain/" + player.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.teamId").value(team.getId()))
                                .andExpect(jsonPath("$.teamName")
                                                .value("Captain Team " + testId))
                                .andExpect(jsonPath("$.shortName")
                                                .value("CAP" + testId))
                                .andExpect(jsonPath("$.teamPlayerId")
                                                .value(teamPlayer.getId()))
                                .andExpect(jsonPath("$.playerId")
                                                .value(player.getId()))
                                .andExpect(jsonPath("$.displayName")
                                                .value("Captain Player " + testId))
                                .andExpect(jsonPath("$.jerseyNumber").value(7))
                                .andExpect(jsonPath("$.active").value(true));

                Team savedTeam = teamRepository.findById(team.getId()).orElseThrow();

                assertEquals(
                                teamPlayer.getId(),
                                savedTeam.getCaptain().getId());
    }

	@Test
	void getTeamCaptainApiShouldReturnCurrentCaptain() throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("Captain Get Team " + testId);
		team.setShortName("CGT" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player player = new Player();
		player.setFirstName("Captain");
		player.setLastName("Get");
		player.setDisplayName("Captain Get " + testId);
		player.setRole(PlayerRole.BATTER);
		player = playerRepository.save(player);

		TeamPlayer teamPlayer = new TeamPlayer();
		teamPlayer.setTeam(team);
		teamPlayer.setPlayer(player);
		teamPlayer.setJerseyNumber(7);
		teamPlayer = teamPlayerRepository.save(teamPlayer);

		team.setCaptain(teamPlayer);
		teamRepository.save(team);

		mockMvc.perform(
				get("/api/teams/" + team.getId() + "/captain"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.teamId").value(team.getId()))
				.andExpect(jsonPath("$.teamPlayerId").value(teamPlayer.getId()))
				.andExpect(jsonPath("$.playerId").value(player.getId()))
				.andExpect(jsonPath("$.displayName")
						.value("Captain Get " + testId))
				.andExpect(jsonPath("$.jerseyNumber").value(7))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void removeCaptainFromTeamShouldClearCaptain() throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("Captain Remove Team " + testId);
		team.setShortName("CRT" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player captain = new Player();
		captain.setFirstName("Captain");
		captain.setLastName("Player");
		captain.setDisplayName("Captain Player " + testId);
		captain.setRole(PlayerRole.BATTER);
		captain = playerRepository.save(captain);

		TeamPlayer teamPlayer = new TeamPlayer();
		teamPlayer.setTeam(team);
		teamPlayer.setPlayer(captain);
		teamPlayer.setJerseyNumber(7);
		teamPlayer = teamPlayerRepository.save(teamPlayer);

		mockMvc.perform(
				put("/api/teams/" + team.getId()
						+ "/captain/" + captain.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.playerId").value(captain.getId()));

		mockMvc.perform(
				org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.delete("/api/teams/" + team.getId()
								+ "/players/" + captain.getId()))
				.andExpect(status().isNoContent());

		Team updatedTeam = teamRepository.findById(team.getId())
				.orElseThrow();

		assertTrue(updatedTeam.getCaptain() == null);

		TeamPlayer removedCaptain = teamPlayerRepository
				.findById(teamPlayer.getId())
				.orElseThrow();

		assertFalse(removedCaptain.getActive());
		assertTrue(removedCaptain.getLeftAt() != null);
	}

    @Test
    void getTeamPlayersApiShouldReturnNonRecursiveRosterResponses() throws Exception {

                String testId = String.valueOf(System.currentTimeMillis());

                Team team = new Team();
                team.setName("Roster Team " + testId);
                team.setShortName("ROS" + testId);
                team.setCity("Nellore");
                team = teamRepository.save(team);

                Player player = new Player();
                player.setFirstName("Roster");
                player.setLastName("Player");
                player.setDisplayName("Roster Player " + testId);
                player.setRole(PlayerRole.BATTER);
                player = playerRepository.save(player);

                TeamPlayer teamPlayer = new TeamPlayer();
                teamPlayer.setTeam(team);
                teamPlayer.setPlayer(player);
                teamPlayer.setJerseyNumber(18);
                teamPlayer = teamPlayerRepository.save(teamPlayer);

                mockMvc.perform(get("/api/teams/" + team.getId() + "/players"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].teamPlayerId")
                                                .value(teamPlayer.getId()))
                                .andExpect(jsonPath("$[0].playerId")
                                                .value(player.getId()))
                                .andExpect(jsonPath("$[0].displayName")
                                                .value("Roster Player " + testId))
                                .andExpect(jsonPath("$[0].jerseyNumber").value(18))
                                .andExpect(jsonPath("$[0].active").value(true))
                                .andExpect(jsonPath("$[0].team").doesNotExist())
                                .andExpect(jsonPath("$[0].player").doesNotExist())
                                .andExpect(jsonPath("$[0].captain").doesNotExist());
    }

	@Test
	void addPlayerToTeamApiShouldReturnSafeRosterResponse()
			throws Exception {

		String testId = String.valueOf(System.currentTimeMillis());

		Team team = new Team();
		team.setName("Response Team " + testId);
		team.setShortName("RES" + testId);
		team.setCity("Nellore");
		team = teamRepository.save(team);

		Player player = new Player();
		player.setFirstName("Response");
		player.setLastName("Player");
		player.setDisplayName("Response Player " + testId);
		player.setRole(PlayerRole.BATTER);
		player = playerRepository.save(player);

		mockMvc.perform(
				post("/api/teams/" + team.getId()
						+ "/players/" + player.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"jerseyNumber": 18
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.teamPlayerId").exists())
				.andExpect(jsonPath("$.playerId").value(player.getId()))
				.andExpect(jsonPath("$.displayName")
						.value("Response Player " + testId))
				.andExpect(jsonPath("$.jerseyNumber").value(18))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.joinedAt").exists())
				.andExpect(jsonPath("$.team").doesNotExist())
				.andExpect(jsonPath("$.player").doesNotExist());
	}

	@Test
	void addPlayerToAnotherTeamInSameSeriesShouldBeRejected() throws Exception {
		String testId = String.valueOf(System.currentTimeMillis());

		Team firstTeam = new Team();
		firstTeam.setName("Series Team One " + testId);
		firstTeam.setShortName("ST1" + testId);
		firstTeam.setCity("Nellore");
		firstTeam = teamRepository.save(firstTeam);

		Team secondTeam = new Team();
		secondTeam.setName("Series Team Two " + testId);
		secondTeam.setShortName("ST2" + testId);
		secondTeam.setCity("Nellore");
		secondTeam = teamRepository.save(secondTeam);

		Series series = new Series();
		series.setName("Player Conflict Series " + testId);
		series.setTotalMatches(5);
		series.setStartDate(java.time.LocalDate.now());
		series = seriesRepository.save(series);

		SeriesTeam firstSeriesTeam = new SeriesTeam();
		firstSeriesTeam.setSeries(series);
		firstSeriesTeam.setTeam(firstTeam);
		seriesTeamRepository.save(firstSeriesTeam);

		SeriesTeam secondSeriesTeam = new SeriesTeam();
		secondSeriesTeam.setSeries(series);
		secondSeriesTeam.setTeam(secondTeam);
		seriesTeamRepository.save(secondSeriesTeam);

		Player player = new Player();
		player.setFirstName("Conflict");
		player.setLastName("Player");
		player.setDisplayName("Conflict Player " + testId);
		player.setRole(PlayerRole.BATTER);
		player = playerRepository.save(player);

		TeamPlayer existingMembership = new TeamPlayer();
		existingMembership.setTeam(firstTeam);
		existingMembership.setPlayer(player);
		existingMembership.setJerseyNumber(18);
		teamPlayerRepository.save(existingMembership);

		mockMvc.perform(
				post("/api/teams/" + secondTeam.getId()
						+ "/players/" + player.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						"jerseyNumber": 27
						}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message")
					.value("Player is already assigned to another team in this series"));

		assertFalse(
				teamPlayerRepository.findByTeamAndPlayer(secondTeam, player)
						.isPresent());
	}

	@Test
	void addPlayerToTeamInDifferentSeriesShouldBeAllowed() throws Exception {
		String testId = String.valueOf(System.currentTimeMillis());

		Team firstTeam = new Team();
		firstTeam.setName("Different Series Team One " + testId);
		firstTeam.setShortName("DS1" + testId);
		firstTeam.setCity("Nellore");
		firstTeam = teamRepository.save(firstTeam);

		Team secondTeam = new Team();
		secondTeam.setName("Different Series Team Two " + testId);
		secondTeam.setShortName("DS2" + testId);
		secondTeam.setCity("Nellore");
		secondTeam = teamRepository.save(secondTeam);

		Series firstSeries = new Series();
		firstSeries.setName("First Series " + testId);
		firstSeries.setTotalMatches(5);
		firstSeries.setStartDate(java.time.LocalDate.now());
		firstSeries = seriesRepository.save(firstSeries);

		Series secondSeries = new Series();
		secondSeries.setName("Second Series " + testId);
		secondSeries.setTotalMatches(5);
		secondSeries.setStartDate(java.time.LocalDate.now());
		secondSeries = seriesRepository.save(secondSeries);

		SeriesTeam firstSeriesTeam = new SeriesTeam();
		firstSeriesTeam.setSeries(firstSeries);
		firstSeriesTeam.setTeam(firstTeam);
		seriesTeamRepository.save(firstSeriesTeam);

		SeriesTeam secondSeriesTeam = new SeriesTeam();
		secondSeriesTeam.setSeries(secondSeries);
		secondSeriesTeam.setTeam(secondTeam);
		seriesTeamRepository.save(secondSeriesTeam);

		Player player = new Player();
		player.setFirstName("Different");
		player.setLastName("Series");
		player.setDisplayName("Different Series Player " + testId);
		player.setRole(PlayerRole.BATTER);
		player = playerRepository.save(player);

		TeamPlayer existingMembership = new TeamPlayer();
		existingMembership.setTeam(firstTeam);
		existingMembership.setPlayer(player);
		existingMembership.setJerseyNumber(18);
		teamPlayerRepository.save(existingMembership);

		mockMvc.perform(
				post("/api/teams/" + secondTeam.getId()
						+ "/players/" + player.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						"jerseyNumber": 27
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.playerId").value(player.getId()))
			.andExpect(jsonPath("$.jerseyNumber").value(27))
			.andExpect(jsonPath("$.active").value(true));

		assertTrue(
				teamPlayerRepository.findByTeamAndPlayer(secondTeam, player)
						.isPresent());
	}

	@Test
	void addPlayerToTeamShouldAllowInactiveMembershipInSameSeries() throws Exception {
		String testId = String.valueOf(System.currentTimeMillis());

		Team firstTeam = new Team();
		firstTeam.setName("Inactive Series Team One " + testId);
		firstTeam.setShortName("IS1" + testId);
		firstTeam.setCity("Nellore");
		firstTeam = teamRepository.save(firstTeam);

		Team secondTeam = new Team();
		secondTeam.setName("Inactive Series Team Two " + testId);
		secondTeam.setShortName("IS2" + testId);
		secondTeam.setCity("Nellore");
		secondTeam = teamRepository.save(secondTeam);

		Series series = new Series();
		series.setName("Inactive Membership Series " + testId);
		series.setTotalMatches(5);
		series.setStartDate(java.time.LocalDate.now());
		series = seriesRepository.save(series);

		SeriesTeam firstSeriesTeam = new SeriesTeam();
		firstSeriesTeam.setSeries(series);
		firstSeriesTeam.setTeam(firstTeam);
		seriesTeamRepository.save(firstSeriesTeam);

		SeriesTeam secondSeriesTeam = new SeriesTeam();
		secondSeriesTeam.setSeries(series);
		secondSeriesTeam.setTeam(secondTeam);
		seriesTeamRepository.save(secondSeriesTeam);

		Player player = new Player();
		player.setFirstName("Inactive");
		player.setLastName("Player");
		player.setDisplayName("Inactive Player " + testId);
		player.setRole(PlayerRole.BATTER);
		player = playerRepository.save(player);

		TeamPlayer previousMembership = new TeamPlayer();
		previousMembership.setTeam(firstTeam);
		previousMembership.setPlayer(player);
		previousMembership.setJerseyNumber(18);
		previousMembership.setActive(false);
		previousMembership.setLeftAt(java.time.Instant.now());
		teamPlayerRepository.save(previousMembership);

		mockMvc.perform(
				post("/api/teams/" + secondTeam.getId()
						+ "/players/" + player.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						"jerseyNumber": 27
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.playerId").value(player.getId()))
			.andExpect(jsonPath("$.jerseyNumber").value(27))
			.andExpect(jsonPath("$.active").value(true));

		assertTrue(
				teamPlayerRepository.findByTeamAndPlayer(secondTeam, player)
						.isPresent());
	}

    @Test
    void finalizePlayingXIShouldCreatePlayingXIForExactlyElevenPlayersWithCaptain() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("XI Team A " + testId);
        teamA.setShortName("XIA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("XI Team B " + testId);
        teamB.setShortName("XIB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Playing XI Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-01T10:00:00Z"));
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

        Player captain = createPlayer("XI Captain", "Player", "XI Captain " + testId, PlayerRole.BATTER);

        TeamPlayer captainMembership = new TeamPlayer();
        captainMembership.setTeam(teamA);
        captainMembership.setPlayer(captain);
        captainMembership.setJerseyNumber(1);
        captainMembership.setActive(true);
        teamPlayerRepository.save(captainMembership);

        createLineup(match, teamA, captain, 1);

        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        for (int i = 2; i <= 11; i++) {
            Player player = createPlayer(
                    "XI Player " + i,
                    "Test",
                    "XI Player " + i + " " + testId,
                    PlayerRole.BATTER);

            TeamPlayer membership = new TeamPlayer();
            membership.setTeam(teamA);
            membership.setPlayer(player);
            membership.setJerseyNumber(i);
            membership.setActive(true);
            teamPlayerRepository.save(membership);

            createLineup(match, teamA, player, i);

                if (i == 2) {
                MatchLineup wicketKeeperLineup = matchLineupRepository
                        .findByMatchAndPlayer(match, player)
                        .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);
                }
        }

        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(match.getId()))
                .andExpect(jsonPath("$.teamId").value(teamA.getId()))
                .andExpect(jsonPath("$.playerCount").value(11))
                .andExpect(jsonPath("$.captainPlayerId").value(captain.getId()))
                .andExpect(jsonPath("$.captainName").value(captain.getDisplayName()));

        assertTrue(playingXIRepository.existsByMatchAndTeam(match, teamA));
    }

    @Test
    void finalizePlayingXIShouldRejectWhenTeamHasFewerThanElevenPlayers() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("XI Validation A " + testId);
        teamA.setShortName("XVA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("XI Validation B " + testId);
        teamB.setShortName("XVB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Playing XI Validation Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-02T10:00:00Z"));
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

        for (int i = 1; i <= 10; i++) {
            Player player = createPlayer(
                    "Validation Player " + i,
                    "Test",
                    "Validation Player " + i + " " + testId,
                    PlayerRole.BATTER);

            TeamPlayer membership = new TeamPlayer();
            membership.setTeam(teamA);
            membership.setPlayer(player);
            membership.setJerseyNumber(i);
            membership.setActive(true);
            teamPlayerRepository.save(membership);

            createLineup(match, teamA, player, i);
        }

        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Exactly 11 players must be selected for the Playing XI"));

        assertFalse(playingXIRepository.existsByMatchAndTeam(match, teamA));
    }

    @Test
    void finalizePlayingXIShouldRejectWhenCaptainIsNotInPlayingXI() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Captain Validation A " + testId);
        teamA.setShortName("CVA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Captain Validation B " + testId);
        teamB.setShortName("CVB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Captain Validation Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-03T10:00:00Z"));
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

        Player captain = createPlayer(
                "Captain",
                "Not Playing",
                "Captain Not Playing " + testId,
                PlayerRole.BATTER);

        TeamPlayer captainMembership = new TeamPlayer();
        captainMembership.setTeam(teamA);
        captainMembership.setPlayer(captain);
        captainMembership.setJerseyNumber(1);
        captainMembership.setActive(true);
        teamPlayerRepository.save(captainMembership);

        createLineup(match, teamA, captain, 1);

        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        captainLineup.setPlaying(false);
        matchLineupRepository.save(captainLineup);

        for (int i = 2; i <= 12; i++) {
            Player player = createPlayer(
                    "Playing Player " + i,
                    "Test",
                    "Playing Player " + i + " " + testId,
                    PlayerRole.BATTER);

            TeamPlayer membership = new TeamPlayer();
            membership.setTeam(teamA);
            membership.setPlayer(player);
            membership.setJerseyNumber(i);
            membership.setActive(true);
            teamPlayerRepository.save(membership);

            createLineup(match, teamA, player, i);
        }

        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Captain must be part of the Playing XI"));

        assertFalse(playingXIRepository.existsByMatchAndTeam(match, teamA));
    }

    @Test
    void finalizePlayingXIShouldRejectSecondFinalizationForSameTeam() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Repeat XI A " + testId);
        teamA.setShortName("RXA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Repeat XI B " + testId);
        teamB.setShortName("RXB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Repeat XI Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-04T10:00:00Z"));
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

        // Create Captain
        Player captain = createPlayer(
                "Repeat Captain",
                "Test",
                "Repeat Captain " + testId,
                PlayerRole.BATTER);

        TeamPlayer captainMembership = new TeamPlayer();
        captainMembership.setTeam(teamA);
        captainMembership.setPlayer(captain);
        captainMembership.setJerseyNumber(1);
        captainMembership.setActive(true);
        teamPlayerRepository.save(captainMembership);

        createLineup(match, teamA, captain, 1);

        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        // Create remaining 10 players.
        // Player 2 will be the Wicket Keeper.
        for (int i = 2; i <= 11; i++) {
                Player player = createPlayer(
                        "Repeat Player " + i,
                        "Test",
                        "Repeat Player " + i + " " + testId,
                        PlayerRole.BATTER);

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match, teamA, player, i);

                if (i == 2) {
                MatchLineup wicketKeeperLineup = matchLineupRepository
                        .findByMatchAndPlayer(match, player)
                        .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);
                }
        }

        // First finalization should succeed.
        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(11));

        // Second finalization should be rejected.
        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Playing XI has already been finalized for this team"));

        assertTrue(playingXIRepository
                .findByMatchAndTeam(match, teamA)
                .isPresent());
    }

    @Test
    void finalizePlayingXIShouldRejectInactivePlayingXIPlayer() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Inactive XI A " + testId);
        teamA.setShortName("IXA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Inactive XI B " + testId);
        teamB.setShortName("IXB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Inactive XI Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-05T10:00:00Z"));
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

        // Create Captain
        Player captain = createPlayer(
                "Inactive Captain",
                "Test",
                "Inactive Captain " + testId,
                PlayerRole.BATTER);

        TeamPlayer captainMembership = new TeamPlayer();
        captainMembership.setTeam(teamA);
        captainMembership.setPlayer(captain);
        captainMembership.setJerseyNumber(1);
        captainMembership.setActive(true);
        teamPlayerRepository.save(captainMembership);

        createLineup(match, teamA, captain, 1);

        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        // Create remaining 10 players.
        // Player 2 will be the valid Wicket Keeper.
        // Player 11 will intentionally be inactive.
        for (int i = 2; i <= 11; i++) {
                Player player = createPlayer(
                        "Inactive XI Player " + i,
                        "Test",
                        "Inactive XI Player " + i + " " + testId,
                        PlayerRole.BATTER);

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match, teamA, player, i);

                if (i == 2) {
                MatchLineup wicketKeeperLineup = matchLineupRepository
                        .findByMatchAndPlayer(match, player)
                        .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);
                }

                if (i == 11) {
                membership.setActive(false);
                membership.setLeftAt(Instant.now());
                teamPlayerRepository.save(membership);
                }
        }

        // Finalization should fail because player 11 is inactive.
        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Playing XI contains an inactive team member")));
    }

    @Test
    void finalizePlayingXIShouldRejectPlayerWithFinalizedPlayingXIAtSameScheduledTime()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());
        Instant scheduledAt = Instant.parse("2027-01-06T10:00:00Z");

        Team teamA = new Team();
        teamA.setName("Conflict Team A " + testId);
        teamA.setShortName("CTA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Conflict Team B " + testId);
        teamB.setShortName("CTB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Team teamC = new Team();
        teamC.setName("Conflict Team C " + testId);
        teamC.setShortName("CTC" + testId.substring(testId.length() - 3));
        teamC.setCity("Test City");
        teamC = teamRepository.save(teamC);

        Team teamD = new Team();
        teamD.setName("Conflict Team D " + testId);
        teamD.setShortName("CTD" + testId.substring(testId.length() - 3));
        teamD.setCity("Test City");
        teamD = teamRepository.save(teamD);

        // ---------------------------------------------------------
        // Match 1
        // ---------------------------------------------------------

        Match match1 = new Match();
        match1.setName("Conflict Match 1 " + testId);
        match1.setMatchNumber(1);
        match1.setFormat(MatchFormat.T20);
        match1.setTotalOvers(20);
        match1.setMaxPlayersPerTeam(11);
        match1.setScheduledAt(scheduledAt);
        match1.setStatus(MatchStatus.SCHEDULED);
        match1 = matchRepository.save(match1);

        MatchTeam match1TeamA = new MatchTeam();
        match1TeamA.setMatch(match1);
        match1TeamA.setTeam(teamA);
        match1TeamA.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(match1TeamA);

        MatchTeam match1TeamB = new MatchTeam();
        match1TeamB.setMatch(match1);
        match1TeamB.setTeam(teamB);
        match1TeamB.setSide(MatchTeamSide.TEAM_B);
        matchTeamRepository.save(match1TeamB);

        // ---------------------------------------------------------
        // Match 2
        // ---------------------------------------------------------

        Match match2 = new Match();
        match2.setName("Conflict Match 2 " + testId);
        match2.setMatchNumber(2);
        match2.setFormat(MatchFormat.T20);
        match2.setTotalOvers(20);
        match2.setMaxPlayersPerTeam(11);
        match2.setScheduledAt(scheduledAt);
        match2.setStatus(MatchStatus.SCHEDULED);
        match2 = matchRepository.save(match2);

        MatchTeam match2TeamC = new MatchTeam();
        match2TeamC.setMatch(match2);
        match2TeamC.setTeam(teamC);
        match2TeamC.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(match2TeamC);

        MatchTeam match2TeamD = new MatchTeam();
        match2TeamD.setMatch(match2);
        match2TeamD.setTeam(teamD);
        match2TeamD.setSide(MatchTeamSide.TEAM_B);
        matchTeamRepository.save(match2TeamD);

        // ---------------------------------------------------------
        // Shared player belongs to both teams
        // ---------------------------------------------------------

        Player sharedPlayer = createPlayer(
                "Conflict Player",
                "Shared",
                "Conflict Player " + testId,
                PlayerRole.BATTER);

        TeamPlayer membershipA = new TeamPlayer();
        membershipA.setTeam(teamA);
        membershipA.setPlayer(sharedPlayer);
        membershipA.setJerseyNumber(1);
        membershipA.setActive(true);
        teamPlayerRepository.save(membershipA);

        TeamPlayer membershipC = new TeamPlayer();
        membershipC.setTeam(teamC);
        membershipC.setPlayer(sharedPlayer);
        membershipC.setJerseyNumber(1);
        membershipC.setActive(true);
        teamPlayerRepository.save(membershipC);

        // ---------------------------------------------------------
        // Match 1 - create Playing XI
        // Shared player is Captain.
        // Match 1 Player 2 is Wicket Keeper.
        // ---------------------------------------------------------

        createLineup(match1, teamA, sharedPlayer, 1);

        MatchLineup match1CaptainLineup = matchLineupRepository
                .findByMatchAndPlayer(match1, sharedPlayer)
                .orElseThrow();

        match1CaptainLineup.setCaptain(true);
        matchLineupRepository.save(match1CaptainLineup);

        for (int i = 2; i <= 11; i++) {
                Player player = createPlayer(
                        "Match1 Player " + i,
                        "Test",
                        "Match1 Player " + i + " " + testId,
                        PlayerRole.BATTER);

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match1, teamA, player, i);

                if (i == 2) {
                MatchLineup wicketKeeperLineup = matchLineupRepository
                        .findByMatchAndPlayer(match1, player)
                        .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);
                }
        }

        // Match 1 finalization must succeed.
        mockMvc.perform(
                post("/api/matches/" + match1.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(11));

        assertTrue(
                playingXIRepository
                        .findByMatchAndTeam(match1, teamA)
                        .isPresent());

        // ---------------------------------------------------------
        // Match 2 - create Playing XI
        // Shared player is Captain.
        // Match 2 Player 2 is Wicket Keeper.
        // ---------------------------------------------------------

        createLineup(match2, teamC, sharedPlayer, 1);

        MatchLineup match2CaptainLineup = matchLineupRepository
                .findByMatchAndPlayer(match2, sharedPlayer)
                .orElseThrow();

        match2CaptainLineup.setCaptain(true);
        matchLineupRepository.save(match2CaptainLineup);

        for (int i = 2; i <= 11; i++) {
                Player player = createPlayer(
                        "Match2 Player " + i,
                        "Test",
                        "Match2 Player " + i + " " + testId,
                        PlayerRole.BATTER);

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamC);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match2, teamC, player, i);

                if (i == 2) {
                MatchLineup wicketKeeperLineup = matchLineupRepository
                        .findByMatchAndPlayer(match2, player)
                        .orElseThrow();

                wicketKeeperLineup.setWicketKeeper(true);
                matchLineupRepository.save(wicketKeeperLineup);
                }
        }

        // Match 2 finalization must fail because the shared player
        // already has a finalized Playing XI at the same scheduled time.
        mockMvc.perform(
                post("/api/matches/" + match2.getId()
                        + "/lineup/team/" + teamC.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Player has a finalized Playing XI conflict at the same scheduled time")));

        assertFalse(
                playingXIRepository
                        .findByMatchAndTeam(match2, teamC)
                        .isPresent());
        }

    @Test
    void finalizePlayingXIShouldRejectTeamThatDoesNotBelongToMatch() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Ownership Team A " + testId);
        teamA.setShortName("OTA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Ownership Team B " + testId);
        teamB.setShortName("OTB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Team unrelatedTeam = new Team();
        unrelatedTeam.setName("Unrelated Team " + testId);
        unrelatedTeam.setShortName("OUT" + testId.substring(testId.length() - 3));
        unrelatedTeam.setCity("Test City");
        unrelatedTeam = teamRepository.save(unrelatedTeam);

        Match match = new Match();
        match.setName("Ownership Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-07T10:00:00Z"));
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

        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + unrelatedTeam.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Team does not belong to this match"));

        assertFalse(playingXIRepository
                .findByMatchAndTeam(match, unrelatedTeam)
                .isPresent());
    }

    @Test
    void finalizePlayingXIShouldRejectWhenWicketKeeperIsNotInPlayingXI() throws Exception {
        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("WK Required Team A " + testId);
        teamA.setShortName("WKA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("WK Required Team B " + testId);
        teamB.setShortName("WKB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("WK Required Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(Instant.parse("2027-01-07T10:00:00Z"));
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

        Player captain = null;

        // Create exactly 11 active team members and lineup entries.
        for (int i = 1; i <= 11; i++) {
                Player player = createPlayer(
                        "WK Required Player " + i,
                        "Test",
                        "WK Required Player " + i + " " + testId,
                        PlayerRole.BATTER);

                if (i == 1) {
                captain = player;
                }

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match, teamA, player, i);
        }

        // Captain is in the Playing XI.
        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        // Intentionally do NOT select a Wicket Keeper.

        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Wicket Keeper must be part of the Playing XI"));

        assertFalse(
                playingXIRepository.existsByMatchAndTeam(match, teamA));
    }

    @Test
    void addPlayerToMatchShouldUpdateExistingLineupInsteadOfCreatingDuplicate()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team team = new Team();
        team.setName("Lineup Update Team " + testId);
        team.setShortName("LUP" + testId.substring(testId.length() - 3));
        team.setCity("Test City");
        team = teamRepository.save(team);

        Player player = createPlayer(
                "Lineup Update Player",
                "Test",
                "Lineup Update Player " + testId,
                PlayerRole.BATTER);

        Match match = new Match();
        match.setName("Lineup Update Test Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-01T10:00:00Z"));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(team);
        matchTeam.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(matchTeam);

        TeamPlayer membership = new TeamPlayer();
        membership.setTeam(team);
        membership.setPlayer(player);
        membership.setJerseyNumber(1);
        membership.setActive(true);
        teamPlayerRepository.save(membership);

        // First selection.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": false,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        assertEquals(
                1,
                matchLineupRepository
                        .findByMatchAndPlayer(match, player)
                        .stream()
                        .count());

        // Select the same player again.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": true,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        // There must still be exactly one lineup row.
        assertEquals(
                1,
                matchLineupRepository
                        .findByMatchAndPlayer(match, player)
                        .stream()
                        .count());

        MatchLineup lineup = matchLineupRepository
                .findByMatchAndPlayer(match, player)
                .orElseThrow();

        // Existing row was updated.
        assertTrue(lineup.getPlaying());
        assertTrue(lineup.getCaptain());
        assertFalse(lineup.getWicketKeeper());
    }

    @Test
    void addPlayerToMatchShouldClearPreviousCaptainWhenCaptainChanges()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team team = new Team();
        team.setName("Captain Switch Team " + testId);
        team.setShortName("CST" + testId.substring(testId.length() - 3));
        team.setCity("Test City");
        team = teamRepository.save(team);

        Player firstPlayer = createPlayer(
                "Captain First",
                "Test",
                "Captain First " + testId,
                PlayerRole.BATTER);

        Player secondPlayer = createPlayer(
                "Captain Second",
                "Test",
                "Captain Second " + testId,
                PlayerRole.BATTER);

        Match match = new Match();
        match.setName("Captain Switch Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-02T10:00:00Z"));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(team);
        matchTeam.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(matchTeam);

        TeamPlayer firstMembership = new TeamPlayer();
        firstMembership.setTeam(team);
        firstMembership.setPlayer(firstPlayer);
        firstMembership.setJerseyNumber(1);
        firstMembership.setActive(true);
        teamPlayerRepository.save(firstMembership);

        TeamPlayer secondMembership = new TeamPlayer();
        secondMembership.setTeam(team);
        secondMembership.setPlayer(secondPlayer);
        secondMembership.setJerseyNumber(2);
        secondMembership.setActive(true);
        teamPlayerRepository.save(secondMembership);

        // Player 1 becomes Captain.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": true,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                firstPlayer.getId())))
                .andExpect(status().isCreated());

        MatchLineup firstLineup = matchLineupRepository
                .findByMatchAndPlayer(match, firstPlayer)
                .orElseThrow();

        assertTrue(firstLineup.getPlaying());
        assertTrue(firstLineup.getCaptain());

        // Player 2 becomes the new Captain.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": true,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                secondPlayer.getId())))
                .andExpect(status().isCreated());

        MatchLineup updatedFirstLineup = matchLineupRepository
                .findByMatchAndPlayer(match, firstPlayer)
                .orElseThrow();

        MatchLineup secondLineup = matchLineupRepository
                .findByMatchAndPlayer(match, secondPlayer)
                .orElseThrow();

        // Previous Captain must be cleared.
        assertTrue(updatedFirstLineup.getPlaying());
        assertFalse(updatedFirstLineup.getCaptain());

        // New Captain must be set.
        assertTrue(secondLineup.getPlaying());
        assertTrue(secondLineup.getCaptain());
    }

    @Test
    void addPlayerToMatchShouldClearPreviousWicketKeeperWhenWicketKeeperChanges()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team team = new Team();
        team.setName("WK Switch Team " + testId);
        team.setShortName("WKS" + testId.substring(testId.length() - 3));
        team.setCity("Test City");
        team = teamRepository.save(team);

        Player firstPlayer = createPlayer(
                "WK First",
                "Test",
                "WK First " + testId,
                PlayerRole.BATTER);

        Player secondPlayer = createPlayer(
                "WK Second",
                "Test",
                "WK Second " + testId,
                PlayerRole.BATTER);

        Match match = new Match();
        match.setName("WK Switch Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-03T10:00:00Z"));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(team);
        matchTeam.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(matchTeam);

        TeamPlayer firstMembership = new TeamPlayer();
        firstMembership.setTeam(team);
        firstMembership.setPlayer(firstPlayer);
        firstMembership.setJerseyNumber(1);
        firstMembership.setActive(true);
        teamPlayerRepository.save(firstMembership);

        TeamPlayer secondMembership = new TeamPlayer();
        secondMembership.setTeam(team);
        secondMembership.setPlayer(secondPlayer);
        secondMembership.setJerseyNumber(2);
        secondMembership.setActive(true);
        teamPlayerRepository.save(secondMembership);

        // Player 1 becomes Wicket Keeper.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": false,
                                "wicketKeeper": true
                                }
                                """.formatted(
                                team.getId(),
                                firstPlayer.getId())))
                .andExpect(status().isCreated());

        MatchLineup firstLineup = matchLineupRepository
                .findByMatchAndPlayer(match, firstPlayer)
                .orElseThrow();

        assertTrue(firstLineup.getPlaying());
        assertTrue(firstLineup.getWicketKeeper());

        // Player 2 becomes the new Wicket Keeper.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": false,
                                "wicketKeeper": true
                                }
                                """.formatted(
                                team.getId(),
                                secondPlayer.getId())))
                .andExpect(status().isCreated());

        MatchLineup updatedFirstLineup = matchLineupRepository
                .findByMatchAndPlayer(match, firstPlayer)
                .orElseThrow();

        MatchLineup secondLineup = matchLineupRepository
                .findByMatchAndPlayer(match, secondPlayer)
                .orElseThrow();

        // Previous Wicket Keeper must be cleared.
        assertTrue(updatedFirstLineup.getPlaying());
        assertFalse(updatedFirstLineup.getWicketKeeper());

        // New Wicket Keeper must be set.
        assertTrue(secondLineup.getPlaying());
        assertTrue(secondLineup.getWicketKeeper());
    }

    @Test
    void addPlayerToMatchShouldClearCaptainWhenCaptainIsDeselected()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team team = new Team();
        team.setName("Captain Deselect Team " + testId);
        team.setShortName("CDT" + testId.substring(testId.length() - 3));
        team.setCity("Test City");
        team = teamRepository.save(team);

        Player player = createPlayer(
                "Captain Deselect",
                "Test",
                "Captain Deselect " + testId,
                PlayerRole.BATTER);

        Match match = new Match();
        match.setName("Captain Deselect Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-04T10:00:00Z"));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(team);
        matchTeam.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(matchTeam);

        TeamPlayer membership = new TeamPlayer();
        membership.setTeam(team);
        membership.setPlayer(player);
        membership.setJerseyNumber(1);
        membership.setActive(true);
        teamPlayerRepository.save(membership);

        // Select player as Captain.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": true,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        MatchLineup lineup = matchLineupRepository
                .findByMatchAndPlayer(match, player)
                .orElseThrow();

        assertTrue(lineup.getPlaying());
        assertTrue(lineup.getCaptain());

        // Deselect the player.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": false,
                                "captain": false,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        MatchLineup updatedLineup = matchLineupRepository
                .findByMatchAndPlayer(match, player)
                .orElseThrow();

        // Player is no longer playing.
        assertFalse(updatedLineup.getPlaying());

        // Captain role must also be cleared.
        assertFalse(updatedLineup.getCaptain());
    }

    @Test
    void addPlayerToMatchShouldClearWicketKeeperWhenWicketKeeperIsDeselected()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team team = new Team();
        team.setName("WK Deselect Team " + testId);
        team.setShortName("WDT" + testId.substring(testId.length() - 3));
        team.setCity("Test City");
        team = teamRepository.save(team);

        Player player = createPlayer(
                "WK Deselect",
                "Test",
                "WK Deselect " + testId,
                PlayerRole.BATTER);

        Match match = new Match();
        match.setName("WK Deselect Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-05T10:00:00Z"));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(team);
        matchTeam.setSide(MatchTeamSide.TEAM_A);
        matchTeamRepository.save(matchTeam);

        TeamPlayer membership = new TeamPlayer();
        membership.setTeam(team);
        membership.setPlayer(player);
        membership.setJerseyNumber(1);
        membership.setActive(true);
        teamPlayerRepository.save(membership);

        // Select player as Wicket Keeper.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": true,
                                "captain": false,
                                "wicketKeeper": true
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        MatchLineup lineup = matchLineupRepository
                .findByMatchAndPlayer(match, player)
                .orElseThrow();

        assertTrue(lineup.getPlaying());
        assertTrue(lineup.getWicketKeeper());

        // Deselect the player.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": false,
                                "captain": false,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                team.getId(),
                                player.getId())))
                .andExpect(status().isCreated());

        MatchLineup updatedLineup = matchLineupRepository
                .findByMatchAndPlayer(match, player)
                .orElseThrow();

        // Player is no longer playing.
        assertFalse(updatedLineup.getPlaying());

        // Wicket Keeper role must also be cleared.
        assertFalse(updatedLineup.getWicketKeeper());
    }

    @Test
    void addPlayerToMatchShouldRejectChangesAfterPlayingXIIsFinalized()
                throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Locked XI Team A " + testId);
        teamA.setShortName("LXA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Locked XI Team B " + testId);
        teamB.setShortName("LXB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Locked XI Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-06T10:00:00Z"));
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

        Player captain = null;
        Player wicketKeeper = null;

        // Create exactly 11 active players and select them.
        for (int i = 1; i <= 11; i++) {

                Player player = createPlayer(
                        "Locked XI Player " + i,
                        "Test",
                        "Locked XI Player " + i + " " + testId,
                        PlayerRole.BATTER);

                if (i == 1) {
                captain = player;
                }

                if (i == 2) {
                wicketKeeper = player;
                }

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match, teamA, player, i);
        }

        // Set Captain.
        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        // Set Wicket Keeper.
        MatchLineup wicketKeeperLineup = matchLineupRepository
                .findByMatchAndPlayer(match, wicketKeeper)
                .orElseThrow();

        wicketKeeperLineup.setWicketKeeper(true);
        matchLineupRepository.save(wicketKeeperLineup);

        // Finalize the Playing XI.
        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isOk());

        assertTrue(
                playingXIRepository.existsByMatchAndTeam(
                        match,
                        teamA));

        // Try to deselect the Captain after lock.
        mockMvc.perform(
                post("/api/matches/" + match.getId() + "/lineup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "teamId": %d,
                                "playerId": %d,
                                "playing": false,
                                "captain": false,
                                "wicketKeeper": false
                                }
                                """.formatted(
                                teamA.getId(),
                                captain.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Playing XI has already been finalized for this team"));

        // Verify the lineup remains unchanged.
        MatchLineup lockedCaptainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        assertTrue(lockedCaptainLineup.getPlaying());
        assertTrue(lockedCaptainLineup.getCaptain());
    }

    @Test
    void getFinalizedPlayingXIShouldReturnPersistedPlayingXI() throws Exception {

        String testId = String.valueOf(System.nanoTime());

        Team teamA = new Team();
        teamA.setName("Persisted XI Team A " + testId);
        teamA.setShortName("PXA" + testId.substring(testId.length() - 3));
        teamA.setCity("Test City");
        teamA = teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setName("Persisted XI Team B " + testId);
        teamB.setShortName("PXB" + testId.substring(testId.length() - 3));
        teamB.setCity("Test City");
        teamB = teamRepository.save(teamB);

        Match match = new Match();
        match.setName("Persisted XI Match " + testId);
        match.setMatchNumber(1);
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setMaxPlayersPerTeam(11);
        match.setScheduledAt(
                Instant.parse("2027-02-07T10:00:00Z"));
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

        Player captain = null;

        for (int i = 1; i <= 11; i++) {

                Player player = createPlayer(
                        "Persisted XI Player " + i,
                        "Test",
                        "Persisted XI Player " + i + " " + testId,
                        PlayerRole.BATTER);

                if (i == 1) {
                captain = player;
                }

                TeamPlayer membership = new TeamPlayer();
                membership.setTeam(teamA);
                membership.setPlayer(player);
                membership.setJerseyNumber(i);
                membership.setActive(true);
                teamPlayerRepository.save(membership);

                createLineup(match, teamA, player, i);
        }

        MatchLineup captainLineup = matchLineupRepository
                .findByMatchAndPlayer(match, captain)
                .orElseThrow();

        captainLineup.setCaptain(true);
        matchLineupRepository.save(captainLineup);

        MatchLineup wicketKeeperLineup =
                matchLineupRepository
                        .findByMatchAndPlayer(
                                match,
                                matchLineupRepository
                                        .findByMatchAndTeam(match, teamA)
                                        .get(1)
                                        .getPlayer())
                        .orElseThrow();

        wicketKeeperLineup.setWicketKeeper(true);
        matchLineupRepository.save(wicketKeeperLineup);

        // Finalize the XI first.
        mockMvc.perform(
                post("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId() + "/finalize"))
                .andExpect(status().isOk());

        // Read the persisted finalized XI.
        mockMvc.perform(
                get("/api/matches/" + match.getId()
                        + "/lineup/team/" + teamA.getId()
                        + "/finalized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playingXIId").exists())
                .andExpect(jsonPath("$.matchId")
                        .value(match.getId()))
                .andExpect(jsonPath("$.teamId")
                        .value(teamA.getId()))
                .andExpect(jsonPath("$.teamName")
                        .value(teamA.getName()))
                .andExpect(jsonPath("$.teamShortName")
                        .value(teamA.getShortName()))
                .andExpect(jsonPath("$.finalizedAt").exists())
                .andExpect(jsonPath("$.playerCount").value(11))
                .andExpect(jsonPath("$.captainPlayerId")
                        .value(captain.getId()))
                .andExpect(jsonPath("$.captainName")
                        .value(captain.getDisplayName()));
    }

    @Test
    void adminAccessShouldBeFalseWhenNoEntitlementExists() {

        User user = createAdminAccessTestUser(true);

        assertFalse(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeTrueWhenEntitlementIsActive() {

        User user = createAdminAccessTestUser(true);

        AdminEntitlement entitlement =
                createAdminEntitlement(
                        user,
                        AdminSubscriptionStatus.ACTIVE);

        assertTrue(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeTrueWhenActiveEntitlementHasFutureExpiry() {

        User user = createAdminAccessTestUser(true);

        AdminEntitlement entitlement =
                createAdminEntitlement(
                        user,
                        AdminSubscriptionStatus.ACTIVE);

        entitlement.setExpiresAt(
                Instant.now().plusSeconds(3600));

        adminEntitlementRepository.save(entitlement);

        assertTrue(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeFalseWhenEntitlementIsCancelled() {

        User user = createAdminAccessTestUser(true);

        createAdminEntitlement(
                user,
                AdminSubscriptionStatus.CANCELLED);

        assertFalse(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeFalseWhenEntitlementIsExpired() {

        User user = createAdminAccessTestUser(true);

        createAdminEntitlement(
                user,
                AdminSubscriptionStatus.EXPIRED);

        assertFalse(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeFalseWhenActiveEntitlementHasPastExpiry() {

        User user = createAdminAccessTestUser(true);

        AdminEntitlement entitlement =
                createAdminEntitlement(
                        user,
                        AdminSubscriptionStatus.ACTIVE);

        entitlement.setExpiresAt(
                Instant.now().minusSeconds(3600));

        adminEntitlementRepository.save(entitlement);

        assertFalse(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void adminAccessShouldBeFalseWhenUserIsInactive() {

        User user = createAdminAccessTestUser(false);

        AdminEntitlement entitlement =
                createAdminEntitlement(
                        user,
                        AdminSubscriptionStatus.ACTIVE);

        assertFalse(
                adminAccessService.hasActiveAdminAccess(user));
    }

    @Test
    void newlyCreatedPlayerShouldDefaultToPendingRegistrationStatus() {
        Player player = createPlayer(
                "Pending",
                "Player",
                "Pending Player",
                PlayerRole.BATTER
        );

        assertEquals(
                PlayerRegistrationStatus.PENDING,
                player.getRegistrationStatus()
        );
    }

    @Test
    void backfillShouldCreateInvitationForPendingPlayerWithoutInvitation() {

        Player player = createPlayer(
                "Backfill",
                "Pending",
                "Backfill Pending Player",
                PlayerRole.BATTER
        );

        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING
        );

        playerRepository.saveAndFlush(player);

        User admin = createPlayerRegistrationTestUser();

        List<PlayerRegistrationInvitation> before =
                playerRegistrationInvitationRepository
                        .findByPlayer(player);

        assertTrue(before.isEmpty());

        playerRegistrationInvitationBackfillService
                .backfill(admin);

        List<PlayerRegistrationInvitation> after =
                playerRegistrationInvitationRepository
                        .findByPlayer(player);

        assertEquals(1, after.size());

        PlayerRegistrationInvitation invitation = after.get(0);

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                invitation.getStatus()
        );

        assertNotNull(invitation.getToken());
        assertNotNull(invitation.getExpiresAt());

        assertEquals(
                admin.getId(),
                invitation.getInvitedBy().getId()
        );
    }

    @Test
    void backfillShouldSkipPlayerWithExistingInvitation() {

        Player player = createPlayer(
                "Backfill",
                "Existing",
                "Backfill Existing Invitation",
                PlayerRole.BATTER
        );

        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING
        );

        playerRepository.saveAndFlush(player);

        User admin = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation existingInvitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        admin
                );

        assertNotNull(existingInvitation.getId());

        playerRegistrationInvitationBackfillService
                .backfill(admin);

        List<PlayerRegistrationInvitation> invitations =
                playerRegistrationInvitationRepository
                        .findByPlayer(player);

        assertEquals(1, invitations.size());

        assertEquals(
                existingInvitation.getId(),
                invitations.get(0).getId()
        );
    }

    @Test
    void backfillShouldSkipRegisteredPlayer() {

        Player player = createPlayer(
                "Backfill",
                "Registered",
                "Backfill Registered Player",
                PlayerRole.BATTER
        );

        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        playerRepository.saveAndFlush(player);

        User admin = createPlayerRegistrationTestUser();

        playerRegistrationInvitationBackfillService
                .backfill(admin);

        assertTrue(
                playerRegistrationInvitationRepository
                        .findByPlayer(player)
                        .isEmpty()
        );
    }

    @Test
    void backfillShouldBeIdempotent() {

        Player player = createPlayer(
                "Backfill",
                "Idempotent",
                "Backfill Idempotent Player",
                PlayerRole.BATTER
        );

        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING
        );

        playerRepository.saveAndFlush(player);

        User admin = createPlayerRegistrationTestUser();

        playerRegistrationInvitationBackfillService
                .backfill(admin);

        List<PlayerRegistrationInvitation> afterFirstRun =
                playerRegistrationInvitationRepository
                        .findByPlayer(player);

        assertEquals(1, afterFirstRun.size());

        Long invitationId =
                afterFirstRun.get(0).getId();

        playerRegistrationInvitationBackfillService
                .backfill(admin);

        List<PlayerRegistrationInvitation> afterSecondRun =
                playerRegistrationInvitationRepository
                        .findByPlayer(player);

        assertEquals(1, afterSecondRun.size());

        assertEquals(
                invitationId,
                afterSecondRun.get(0).getId()
        );
    }
    
    @Test
    void playerShouldBeAssociatedWithUser() {
        Player player = createPlayer(
                "Registered",
                "Player",
                "Registered Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        player.setUser(user);
        player = playerRepository.save(player);

        Player persistedPlayer = playerRepository.findById(player.getId())
                .orElseThrow();

        assertNotNull(persistedPlayer.getUser());
        assertEquals(user.getId(), persistedPlayer.getUser().getId());
    }

    @Test
    void playerShouldTransitionToRegistered() {
        Player player = createPlayer(
                "Transition",
                "Player",
                "Transition Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        player.setUser(user);
        player.setRegistrationStatus(PlayerRegistrationStatus.REGISTERED);

        player = playerRepository.save(player);

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                player.getRegistrationStatus()
        );
    }

    @Test
    void registrationStatusShouldPersistCorrectly() {
        Player player = createPlayer(
                "Persisted",
                "Player",
                "Persisted Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        player.setUser(user);
        player.setRegistrationStatus(PlayerRegistrationStatus.REGISTERED);

        Long playerId = playerRepository.save(player).getId();

        Player persistedPlayer = playerRepository.findById(playerId)
                .orElseThrow();

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                persistedPlayer.getRegistrationStatus()
        );
        assertEquals(
                user.getId(),
                persistedPlayer.getUser().getId()
        );
    }

    @Test
    void oneUserCannotBeLinkedToTwoPlayers() {
        Player firstPlayer = createPlayer(
                "First",
                "Player",
                "First Player",
                PlayerRole.BATTER
        );

        Player secondPlayer = createPlayer(
                "Second",
                "Player",
                "Second Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        firstPlayer.setUser(user);
        firstPlayer.setRegistrationStatus(PlayerRegistrationStatus.REGISTERED);
        playerRepository.saveAndFlush(firstPlayer);

        secondPlayer.setUser(user);
        secondPlayer.setRegistrationStatus(PlayerRegistrationStatus.REGISTERED);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> playerRepository.saveAndFlush(secondPlayer)
        );
    }

    @Test
    void registeredPlayerShouldContinueWorkingWithTeamPlayerRelationship() {
        Player player = createPlayer(
                "Team",
                "Player",
                "Registered Team Player",
                PlayerRole.ALL_ROUNDER
        );

        User user = createPlayerRegistrationTestUser();

        player.setUser(user);
        player.setRegistrationStatus(PlayerRegistrationStatus.REGISTERED);
        player = playerRepository.save(player);

        Team team = new Team();
        team.setName("Registration Test Team");
        team.setShortName("REGTEST");
        team.setCity("Vizag");
        team.setActive(true);
        team = teamRepository.save(team);

        TeamPlayer teamPlayer = new TeamPlayer();
        teamPlayer.setTeam(team);
        teamPlayer.setPlayer(player);
        teamPlayer.setJerseyNumber(99);
        teamPlayer.setActive(true);

        TeamPlayer savedTeamPlayer = teamPlayerRepository.save(teamPlayer);

        assertNotNull(savedTeamPlayer.getId());
        assertEquals(player.getId(), savedTeamPlayer.getPlayer().getId());
        assertEquals(team.getId(), savedTeamPlayer.getTeam().getId());
        assertEquals(99, savedTeamPlayer.getJerseyNumber());
        assertTrue(savedTeamPlayer.getActive());
    }

    @Test
    void newInvitationShouldDefaultToPending() {
        Player player = createPlayer(
                "Invitation",
                "Pending",
                "Invitation Pending",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "token-pending-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        null
                );

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                invitation.getStatus()
        );
    }

    @Test
    void invitationShouldBeAssociatedWithCorrectPlayer() {
        Player player = createPlayer(
                "Invitation",
                "Player",
                "Invitation Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "token-player-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        null
                );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertNotNull(persisted.getPlayer());
        assertEquals(
                player.getId(),
                persisted.getPlayer().getId()
        );
    }

    @Test
    void invitationTokenShouldPersistAndBeFindableByToken() {
        Player player = createPlayer(
                "Invitation",
                "Token",
                "Invitation Token",
                PlayerRole.BATTER
        );

        String token = "token-find-" + uniqueTestId();

        createRegistrationInvitation(
                player,
                token,
                Instant.now().plusSeconds(86400),
                null
        );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findByToken(token)
                        .orElseThrow();

        assertEquals(token, persisted.getToken());
        assertEquals(player.getId(), persisted.getPlayer().getId());
    }

    @Test
    void duplicateInvitationTokensShouldBeRejected() {
        Player firstPlayer = createPlayer(
                "First",
                "Invitation",
                "First Invitation",
                PlayerRole.BATTER
        );

        Player secondPlayer = createPlayer(
                "Second",
                "Invitation",
                "Second Invitation",
                PlayerRole.BATTER
        );

        String token = "duplicate-token-" + uniqueTestId();

        createRegistrationInvitation(
                firstPlayer,
                token,
                Instant.now().plusSeconds(86400),
                null
        );

        PlayerRegistrationInvitation duplicate =
                new PlayerRegistrationInvitation();

        duplicate.setPlayer(secondPlayer);
        duplicate.setToken(token);
        duplicate.setStatus(
                PlayerRegistrationInvitationStatus.PENDING
        );
        duplicate.setExpiresAt(
                Instant.now().plusSeconds(86400)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> playerRegistrationInvitationRepository
                        .saveAndFlush(duplicate)
        );
    }

    @Test
    void invitationExpiryShouldPersistCorrectly() {
        Player player = createPlayer(
                "Invitation",
                "Expiry",
                "Invitation Expiry",
                PlayerRole.BATTER
        );

        Instant expiresAt = Instant.now().plusSeconds(172800);

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "token-expiry-" + uniqueTestId(),
                        expiresAt,
                        null
                );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(expiresAt, persisted.getExpiresAt());
    }

    @Test
    void invitationInvitedByUserShouldPersistCorrectly() {
        Player player = createPlayer(
                "Invitation",
                "Admin",
                "Invitation Admin",
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "token-admin-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        invitedBy
                );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertNotNull(persisted.getInvitedBy());
        assertEquals(
                invitedBy.getId(),
                persisted.getInvitedBy().getId()
        );
    }

    @Test
    void invitationShouldTransitionFromPendingToUsedWithCompletedAt() {
        Player player = createPlayer(
                "Invitation",
                "Used",
                "Invitation Used",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "token-used-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        null
                );

        Instant completedAt = Instant.now();

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );
        invitation.setCompletedAt(completedAt);

        playerRegistrationInvitationRepository.saveAndFlush(invitation);

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.USED,
                persisted.getStatus()
        );
        assertEquals(
                completedAt,
                persisted.getCompletedAt()
        );
    }

    @Test
    void invitationShouldBeFindableByPlayerAndStatus() {
        Player player = createPlayer(
                "Invitation",
                "Status",
                "Invitation Status",
                PlayerRole.BATTER
        );

        createRegistrationInvitation(
                player,
                "token-status-" + uniqueTestId(),
                Instant.now().plusSeconds(86400),
                null
        );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findByPlayerAndStatus(
                                player,
                                PlayerRegistrationInvitationStatus.PENDING
                        )
                        .orElseThrow();

        assertEquals(player.getId(), persisted.getPlayer().getId());
        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                persisted.getStatus()
        );
    }

    @Test
    void invitationServiceShouldGenerateSecureToken() {
        Player player = createPlayer(
                "Secure",
                "Token",
                "Secure Token Player",
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        assertThat(invitation.getToken())
                .isNotBlank();

        assertThat(invitation.getToken().length())
                .isGreaterThanOrEqualTo(40);
    }

    @Test
    void invitationServiceShouldGenerateDifferentTokens() {
        Player firstPlayer = createPlayer(
                "Token",
                "One",
                "Token One",
                PlayerRole.BATTER
        );

        Player secondPlayer = createPlayer(
                "Token",
                "Two",
                "Token Two",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation first =
                playerRegistrationInvitationService.createInvitation(
                        firstPlayer,
                        null
                );

        PlayerRegistrationInvitation second =
                playerRegistrationInvitationService.createInvitation(
                        secondPlayer,
                        null
                );

        assertNotEquals(first.getToken(), second.getToken());
    }

    @Test
    void invitationServiceShouldUseSevenDayDefaultExpiry() {
        Player player = createPlayer(
                "Default",
                "Expiry",
                "Default Expiry Player",
                PlayerRole.BATTER
        );

        Instant before = Instant.now();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null
                );

        Instant after = Instant.now();

        Instant minimumExpected =
                before.plus(Duration.ofDays(7));

        Instant maximumExpected =
                after.plus(Duration.ofDays(7));

        assertFalse(
                invitation.getExpiresAt().isBefore(minimumExpected)
        );

        assertFalse(
                invitation.getExpiresAt().isAfter(maximumExpected)
        );
    }

    @Test
    void invitationServiceShouldSupportCustomExpiry() {
        Player player = createPlayer(
                "Custom",
                "Expiry",
                "Custom Expiry Player",
                PlayerRole.BATTER
        );

        Duration lifetime = Duration.ofHours(12);

        Instant before = Instant.now();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null,
                        lifetime
                );

        Instant after = Instant.now();

        Instant minimumExpected = before.plus(lifetime);
        Instant maximumExpected = after.plus(lifetime);

        assertFalse(
                invitation.getExpiresAt().isBefore(minimumExpected)
        );

        assertFalse(
                invitation.getExpiresAt().isAfter(maximumExpected)
        );
    }

    @Test
    void validPendingTokenShouldBeAccepted() {
        Player player = createPlayer(
                "Valid",
                "Token",
                "Valid Token Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null
                );

        PlayerRegistrationInvitation validated =
                playerRegistrationInvitationService.validateToken(
                        invitation.getToken()
                );

        assertEquals(
                invitation.getId(),
                validated.getId()
        );

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                validated.getStatus()
        );
    }

    @Test
    void invalidTokenShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> playerRegistrationInvitationService.validateToken(
                        "this-token-does-not-exist"
                )
        );
    }

    @Test
    void expiredTokenShouldBeRejected() {
        Player player = createPlayer(
                "Expired",
                "Token",
                "Expired Token Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "expired-token-" + uniqueTestId(),
                        Instant.now().minusSeconds(60),
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationInvitationService.validateToken(
                        invitation.getToken()
                )
        );
    }

    @Test
    void usedTokenShouldBeRejected() {
        Player player = createPlayer(
                "Used",
                "Token",
                "Used Token Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "used-token-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        null
                );

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );

        playerRegistrationInvitationRepository.saveAndFlush(invitation);

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationInvitationService.validateToken(
                        invitation.getToken()
                )
        );
    }

    @Test
    void cancelledTokenShouldBeRejected() {
        Player player = createPlayer(
                "Cancelled",
                "Token",
                "Cancelled Token Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "cancelled-token-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        null
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        assertEquals(
                PlayerRegistrationInvitationStatus.CANCELLED,
                invitation.getStatus()
        );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationInvitationService.validateToken(
                        invitation.getToken()
                )
        );
    }

    @Test
    void pendingInvitationShouldBeCancellable() {
        Player player = createPlayer(
                "Cancel",
                "Invitation",
                "Cancel Invitation Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        PlayerRegistrationInvitation persisted =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.CANCELLED,
                persisted.getStatus()
        );
    }

    @Test
    void regenerationShouldCreateNewToken() {
        Player player = createPlayer(
                "Regenerate",
                "Token",
                "Regenerate Token Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation oldInvitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null
                );

        String oldToken = oldInvitation.getToken();

        PlayerRegistrationInvitation newInvitation =
                playerRegistrationInvitationService.regenerateInvitation(
                        oldInvitation,
                        null
                );

        assertNotEquals(
                oldToken,
                newInvitation.getToken()
        );

        assertEquals(
                PlayerRegistrationInvitationStatus.CANCELLED,
                oldInvitation.getStatus()
        );

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                newInvitation.getStatus()
        );
    }

    @Test
    void regenerationShouldKeepSamePlayer() {
        Player player = createPlayer(
                "Same",
                "Player",
                "Same Player Registration",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation oldInvitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        null
                );

        PlayerRegistrationInvitation newInvitation =
                playerRegistrationInvitationService.regenerateInvitation(
                        oldInvitation,
                        null
                );

        assertEquals(
                player.getId(),
                newInvitation.getPlayer().getId()
        );
    }

    @Test
    void validInvitationShouldRegisterPlayer() {
        Player player = createPlayer(
                "Complete",
                "Registration",
                "Complete Registration Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        Player registeredPlayer =
                playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        user
                );

        assertEquals(
                player.getId(),
                registeredPlayer.getId()
        );

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                registeredPlayer.getRegistrationStatus()
        );
    }

    @Test
    void registrationConfirmationShouldUpdatePlayerDetails() {
        Player player = createPlayer(
                "Pending",
                "Player",
                "Pending Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        PlayerRegistrationUpdateRequest request =
                new PlayerRegistrationUpdateRequest();

        request.setFirstName("Rohit");
        request.setLastName("Sharma");
        request.setDisplayName("Rohit Sharma");
        request.setPhone("9000000000");
        request.setBattingStyle(BattingStyle.RIGHT_HAND);
        request.setBowlingStyle(BowlingStyle.RIGHT_ARM_OFF_SPIN);
        request.setRole(PlayerRole.ALL_ROUNDER);

        Player registeredPlayer =
                playerRegistrationService.confirmAndCompleteRegistration(
                        invitation.getToken(),
                        user,
                        request
                );

        assertEquals(
                "Rohit",
                registeredPlayer.getFirstName()
        );

        assertEquals(
                "Sharma",
                registeredPlayer.getLastName()
        );

        assertEquals(
                "Rohit Sharma",
                registeredPlayer.getDisplayName()
        );

        assertEquals(
                "9000000000",
                registeredPlayer.getPhone()
        );

        assertEquals(
                BattingStyle.RIGHT_HAND,
                registeredPlayer.getBattingStyle()
        );

        assertEquals(
                BowlingStyle.RIGHT_ARM_OFF_SPIN,
                registeredPlayer.getBowlingStyle()
        );

        assertEquals(
                PlayerRole.ALL_ROUNDER,
                registeredPlayer.getRole()
        );

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                registeredPlayer.getRegistrationStatus()
        );

        assertEquals(
                user.getId(),
                registeredPlayer.getUser().getId()
        );
    }

    @Test
    void registrationConfirmationShouldMarkInvitationAsUsed() {
        Player player = createPlayer(
                "Invitation",
                "Confirmation",
                "Invitation Confirmation Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        PlayerRegistrationUpdateRequest request =
                new PlayerRegistrationUpdateRequest();

        request.setFirstName("Confirmed");
        request.setLastName("Player");
        request.setDisplayName("Confirmed Player");
        request.setPhone("9111111111");
        request.setBattingStyle(BattingStyle.RIGHT_HAND);
        request.setBowlingStyle(BowlingStyle.RIGHT_ARM_MEDIUM);
        request.setRole(PlayerRole.BATTER);

        playerRegistrationService.confirmAndCompleteRegistration(
                invitation.getToken(),
                user,
                request
        );

        PlayerRegistrationInvitation persistedInvitation =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.USED,
                persistedInvitation.getStatus()
        );

        assertNotNull(
                persistedInvitation.getCompletedAt()
        );
    }

    @Test
    void confirmationShouldRejectAlreadyRegisteredPlayer() {
        Player player = createPlayer(
                "Already",
                "Registered",
                "Already Registered Confirmation Player",
                PlayerRole.BATTER
        );

        User firstUser = createPlayerRegistrationTestUser();

        player.setUser(firstUser);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        playerRepository.saveAndFlush(player);

        User secondUser = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        secondUser
                );

        PlayerRegistrationUpdateRequest request =
                new PlayerRegistrationUpdateRequest();

        request.setFirstName("Changed");
        request.setLastName("Player");
        request.setDisplayName("Changed Player");
        request.setPhone("9222222222");
        request.setBattingStyle(BattingStyle.RIGHT_HAND);
        request.setBowlingStyle(BowlingStyle.RIGHT_ARM_MEDIUM);
        request.setRole(PlayerRole.BATTER);

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.confirmAndCompleteRegistration(
                        invitation.getToken(),
                        secondUser,
                        request
                )
        );
    }

    @Test
    void confirmationShouldRejectInactiveUser() {
        Player player = createPlayer(
                "Inactive",
                "Confirmation",
                "Inactive Confirmation Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        user.setActive(false);
        userRepository.saveAndFlush(user);

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        PlayerRegistrationUpdateRequest request =
                new PlayerRegistrationUpdateRequest();

        request.setFirstName("Inactive");
        request.setLastName("Player");
        request.setDisplayName("Inactive Player");
        request.setPhone("9333333333");
        request.setBattingStyle(BattingStyle.RIGHT_HAND);
        request.setBowlingStyle(BowlingStyle.RIGHT_ARM_MEDIUM);
        request.setRole(PlayerRole.BATTER);

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.confirmAndCompleteRegistration(
                        invitation.getToken(),
                        user,
                        request
                )
        );
    }

    @Test
    void registrationShouldLinkPlayerToUser() {
        Player player = createPlayer(
                "Link",
                "User",
                "Link User Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationService.completeRegistration(
                invitation.getToken(),
                user
        );

        Player persistedPlayer =
                playerRepository.findById(player.getId())
                        .orElseThrow();

        assertNotNull(persistedPlayer.getUser());
        assertEquals(
                user.getId(),
                persistedPlayer.getUser().getId()
        );
    }

    @Test
    void registrationShouldChangePlayerStatusToRegistered() {
        Player player = createPlayer(
                "Status",
                "Change",
                "Status Change Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationService.completeRegistration(
                invitation.getToken(),
                user
        );

        Player persistedPlayer =
                playerRepository.findById(player.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                persistedPlayer.getRegistrationStatus()
        );
    }

    @Test
    void registrationShouldMarkInvitationAsUsed() {
        Player player = createPlayer(
                "Invitation",
                "Used",
                "Invitation Used Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationService.completeRegistration(
                invitation.getToken(),
                user
        );

        PlayerRegistrationInvitation persistedInvitation =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.USED,
                persistedInvitation.getStatus()
        );
    }

    @Test
    void registrationShouldSetInvitationCompletedAt() {
        Player player = createPlayer(
                "Completed",
                "At",
                "Completed At Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        assertNull(invitation.getCompletedAt());

        playerRegistrationService.completeRegistration(
                invitation.getToken(),
                user
        );

        PlayerRegistrationInvitation persistedInvitation =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertNotNull(persistedInvitation.getCompletedAt());
        }

    
    @Test
    void registeredPlayerShouldRemainLinkedToSameUser() {
        Player player = createPlayer(
                "Same",
                "User",
                "Same User Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationService.completeRegistration(
                invitation.getToken(),
                user
        );

        Player persistedPlayer =
                playerRepository.findById(player.getId())
                        .orElseThrow();

        assertEquals(
                user.getId(),
                persistedPlayer.getUser().getId()
        );
    }

    @Test
    void expiredInvitationShouldRejectRegistration() {
        Player player = createPlayer(
                "Expired",
                "Registration",
                "Expired Registration Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "registration-expired-" + uniqueTestId(),
                        Instant.now().minusSeconds(60),
                        user
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        user
                )
        );

        Player persistedPlayer =
                playerRepository.findById(player.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationStatus.PENDING,
                persistedPlayer.getRegistrationStatus()
        );
    }

    @Test
    void cancelledInvitationShouldRejectRegistration() {
        Player player = createPlayer(
                "Cancelled",
                "Registration",
                "Cancelled Registration Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        user
                )
        );
    }

    @Test
    void alreadyRegisteredPlayerShouldRejectRegistration() {
        Player player = createPlayer(
                "Already",
                "Registered",
                "Already Registered Player",
                PlayerRole.BATTER
        );

        User firstUser = createPlayerRegistrationTestUser();

        player.setUser(firstUser);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        playerRepository.saveAndFlush(player);

        User secondUser = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        secondUser
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        secondUser
                )
        );
    }

    @Test
    void playerLinkedToAnotherUserShouldRejectRegistration() {
        Player player = createPlayer(
                "Different",
                "User",
                "Different User Player",
                PlayerRole.BATTER
        );

        User firstUser = createPlayerRegistrationTestUser();

        player.setUser(firstUser);
        playerRepository.saveAndFlush(player);

        User secondUser = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "different-user-" + uniqueTestId(),
                        Instant.now().plusSeconds(86400),
                        secondUser
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        secondUser
                )
        );
    }

    @Test
    void userAlreadyLinkedToAnotherPlayerShouldRejectRegistration() {
        User user = createPlayerRegistrationTestUser();

        Player firstPlayer = createPlayer(
                "First",
                "Linked",
                "First Linked Player",
                PlayerRole.BATTER
        );

        firstPlayer.setUser(user);
        firstPlayer.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        playerRepository.saveAndFlush(firstPlayer);

        Player secondPlayer = createPlayer(
                "Second",
                "Linked",
                "Second Linked Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        secondPlayer,
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        user
                )
        );
    }

    @Test
    void inactiveUserShouldRejectRegistration() {
        Player player = createPlayer(
                "Inactive",
                "User",
                "Inactive User Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();
        user.setActive(false);
        userRepository.saveAndFlush(user);

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationService.completeRegistration(
                        invitation.getToken(),
                        user
                )
        );
    }

    @Test
    void invalidInvitationTokenShouldRejectRegistration() {
        User user = createPlayerRegistrationTestUser();

        assertThrows(
                IllegalArgumentException.class,
                () -> playerRegistrationService.completeRegistration(
                        "invalid-registration-token-" + uniqueTestId(),
                        user
                )
        );
    }

    @Test
    void validRegistrationInvitationShouldReturnPlayerDetails() {
        Player player = createPlayer(
                "Invitation",
                "Preview",
                "Invitation Preview Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        ResponseEntity<PlayerRegistrationInvitationResponse> response =
                playerRegistrationController.getInvitation(
                        invitation.getToken()
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                player.getId(),
                response.getBody().playerId()
        );

        assertEquals(
                player.getDisplayName(),
                response.getBody().displayName()
        );

        assertEquals(
                PlayerRegistrationStatus.PENDING.name(),
                response.getBody().registrationStatus()
        );
    }

    @Test
    void authenticatedUserShouldConfirmRegistrationThroughController()
                throws Exception {

        User user = createPlayerRegistrationTestUser();

        String googleSubject =
                "google-confirm-registration-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(user);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE);
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication);

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", user.getEmail(),
                                "name", user.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        Player player = createPlayer(
                "Pending",
                "Confirmation",
                "Pending Confirmation Player",
                PlayerRole.BATTER
        );

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        mockMvc.perform(
                post(
                        "/api/player-registration/invitations/{token}/confirm",
                        invitation.getToken()
                )
                .with(
                        SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
                .contentType(
                        org.springframework.http.MediaType.APPLICATION_JSON
                )
                .content("""
                        {
                                "firstName": "Rohit",
                                "lastName": "Sharma",
                                "displayName": "Rohit Sharma",
                                "phone": "9000000000",
                                "battingStyle": "RIGHT_HAND",
                                "bowlingStyle": "RIGHT_ARM_OFF_SPIN",
                                "role": "ALL_ROUNDER"
                        }
                        """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.playerId")
                .value(player.getId()))
        .andExpect(jsonPath("$.displayName")
                .value("Rohit Sharma"))
        .andExpect(jsonPath("$.registrationStatus")
                .value("REGISTERED"));
    }

    @Test
    void unauthenticatedUserShouldNotConfirmRegistrationThroughController()
                throws Exception {

        Player player = createPlayer(
                "Unauthenticated",
                "Confirmation",
                "Unauthenticated Confirmation Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        mockMvc.perform(
                post(
                        "/api/player-registration/invitations/{token}/confirm",
                        invitation.getToken()
                )
                .contentType(
                        org.springframework.http.MediaType.APPLICATION_JSON
                )
                .content("""
                        {
                                "firstName": "Test",
                                "lastName": "Player",
                                "displayName": "Test Player",
                                "phone": "9111111111",
                                "battingStyle": "RIGHT_HAND",
                                "bowlingStyle": "RIGHT_ARM_MEDIUM",
                                "role": "BATTER"
                        }
                        """)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void adminShouldBeAbleToViewPlayerRegistrationDetail()
                throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        String googleSubject =
                "google-detail-admin-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(admin);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE);
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication);

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", admin.getEmail(),
                                "name", admin.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        Player player = new Player();
        player.setFirstName("API");
        player.setLastName("Detail");
        player.setDisplayName(
                "API Detail " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        mockMvc.perform(
                get("/api/players/registrations/{playerId}",
                        savedPlayer.getId())
                        .with(
                                SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.playerId")
                .value(savedPlayer.getId()))
        .andExpect(jsonPath("$.displayName")
                .value(savedPlayer.getDisplayName()))
        .andExpect(jsonPath("$.registrationStatus")
                .value("PENDING"));
    }

    @Test
    void playerWithoutAdminAccessShouldNotViewPlayerRegistrationDetail()
                throws Exception {

        User player = createPlayerRegistrationTestUser();

        String googleSubject =
                "google-detail-player-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(player);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE);
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication);

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", player.getEmail(),
                                "name", player.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        Player targetPlayer = new Player();
        targetPlayer.setFirstName("Detail");
        targetPlayer.setLastName("Protected");
        targetPlayer.setDisplayName(
                "Detail Protected " + uniqueTestId());
        targetPlayer.setRole(PlayerRole.BATTER);
        targetPlayer.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedTargetPlayer =
                playerRepository.saveAndFlush(targetPlayer);

        mockMvc.perform(
                get(
                        "/api/players/registrations/{playerId}",
                        savedTargetPlayer.getId()
                )
                .with(
                        SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotViewPlayerRegistrationDetail()
                throws Exception {

        mockMvc.perform(
                get(
                        "/api/players/registrations/{playerId}",
                        1L
                )
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void invalidRegistrationInvitationShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> playerRegistrationController.getInvitation(
                        "invalid-registration-token-" + uniqueTestId()
                )
        );
    }

    @Test
    void expiredRegistrationInvitationShouldBeRejectedByController() {
        Player player = createPlayer(
                "Expired",
                "Preview",
                "Expired Preview Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                createRegistrationInvitation(
                        player,
                        "expired-controller-" + uniqueTestId(),
                        Instant.now().minusSeconds(60),
                        user
                );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationController.getInvitation(
                        invitation.getToken()
                )
        );
    }

    @Test
    void cancelledRegistrationInvitationShouldBeRejectedByController() {
        Player player = createPlayer(
                "Cancelled",
                "Preview",
                "Cancelled Preview Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        assertThrows(
                IllegalStateException.class,
                () -> playerRegistrationController.getInvitation(
                        invitation.getToken()
                )
        );
    }

    @Test
    void registrationCompletionShouldRejectUnauthenticatedRequest() {
        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        createPlayer(
                                "Unauthenticated",
                                "Player",
                                "Unauthenticated Player",
                                PlayerRole.BATTER
                        ),
                        null
                );

        ResponseEntity<PlayerRegistrationInvitationResponse> response =
                playerRegistrationController.completeRegistration(
                        invitation.getToken(),
                        null
                );

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void authenticatedUserShouldCompleteRegistrationThroughController() {
        Player player = createPlayer(
                "Authenticated",
                "Registration",
                "Authenticated Registration Player",
                PlayerRole.BATTER
        );

        User user = createPlayerRegistrationTestUser();

        String googleSubject = "google-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(user);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(
                googleSubject
        );

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        user
                );

        OAuth2User oauth2User = new DefaultOAuth2User(
                java.util.List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ),
                Map.of(
                        "sub", googleSubject,
                        "email", user.getEmail(),
                        "name", user.getDisplayName()
                ),
                "sub"
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        ResponseEntity<PlayerRegistrationInvitationResponse> response =
                playerRegistrationController.completeRegistration(
                        invitation.getToken(),
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                player.getId(),
                response.getBody().playerId()
        );

        assertEquals(
                PlayerRegistrationStatus.REGISTERED.name(),
                response.getBody().registrationStatus()
        );

        Player persistedPlayer =
                playerRepository.findById(player.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationStatus.REGISTERED,
                persistedPlayer.getRegistrationStatus()
        );

        assertNotNull(persistedPlayer.getUser());

        assertEquals(
                user.getId(),
                persistedPlayer.getUser().getId()
        );

        PlayerRegistrationInvitation persistedInvitation =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.USED,
                persistedInvitation.getStatus()
        );

        assertNotNull(
                persistedInvitation.getCompletedAt()
        );
    }

    @Test
    void playerImportShouldCreatePendingPlayersAndInvitations()
                throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        String testId = uniqueTestId();

        String displayName = "Ravi Kumar " + testId;

        MockMultipartFile file =
                createPlayerImportExcel(
                        "Ravi|Kumar|" + displayName
                                + "|9000000001|RIGHT_HAND|RIGHT_ARM_MEDIUM|BATTER"
                );

        PlayerImportResponse response =
                playerImportService.importExcel(
                        file,
                        admin
                );

        assertEquals(1, response.getTotalRows());
        assertEquals(1, response.getCreatedRows());
        assertTrue(response.getErrors().isEmpty());

        assertEquals(1, response.getRegistrations().size());

        PlayerRegistrationImportResponse registration =
                response.getRegistrations().get(0);

        Player player =
                playerRepository.findById(
                        registration.playerId()
                ).orElseThrow();

        assertEquals(
                PlayerRegistrationStatus.PENDING,
                player.getRegistrationStatus()
        );

        assertNotNull(player.getId());

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationRepository
                        .findByPlayer(player)
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING,
                invitation.getStatus()
        );

        assertEquals(
                admin.getId(),
                invitation.getInvitedBy().getId()
        );
    }

    @Test
    void playerImportShouldReturnRegistrationInformation()
                throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        MockMultipartFile file =
                createPlayerImportExcel(
                        "Suresh|Naidu|Suresh Naidu|9000000002|LEFT_HAND|RIGHT_ARM_MEDIUM|ALL_ROUNDER"
                );

        PlayerImportResponse response =
                playerImportService.importExcel(
                        file,
                        admin
                );

        PlayerRegistrationImportResponse registration =
                response.getRegistrations().get(0);

        assertNotNull(registration.playerId());
        assertEquals(
                "Suresh Naidu",
                registration.displayName()
        );

        assertEquals(
                PlayerRegistrationStatus.PENDING.name(),
                registration.registrationStatus()
        );

        assertEquals(
                PlayerRegistrationInvitationStatus.PENDING.name(),
                registration.invitationStatus()
        );

        assertNotNull(registration.registrationLink());
        assertFalse(registration.registrationLink().isBlank());
    }

    @Test
    void multipleImportedPlayersShouldGetUniqueInvitations()
                throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        MockMultipartFile file =
                createPlayerImportExcel(
                        "Player|One|Player One|9000000011|RIGHT_HAND|RIGHT_ARM_FAST|BATTER",
                        "Player|Two|Player Two|9000000012|LEFT_HAND|RIGHT_ARM_MEDIUM|BOWLER",
                        "Player|Three|Player Three|9000000013|RIGHT_HAND|RIGHT_ARM_FAST|ALL_ROUNDER"
                );

        PlayerImportResponse response =
                playerImportService.importExcel(
                        file,
                        admin
                );

        assertEquals(3, response.getCreatedRows());
        assertEquals(3, response.getRegistrations().size());

        String token1 =
                response.getRegistrations().get(0).registrationLink();

        String token2 =
                response.getRegistrations().get(1).registrationLink();

        String token3 =
                response.getRegistrations().get(2).registrationLink();

        assertNotEquals(token1, token2);
        assertNotEquals(token1, token3);
        assertNotEquals(token2, token3);

        List<Player> importedPlayers =
                response.getRegistrations()
                        .stream()
                        .map(registration ->
                                playerRepository
                                        .findById(registration.playerId())
                                        .orElseThrow()
                        )
                        .toList();

        assertEquals(
                3,
                playerRegistrationInvitationRepository
                        .findByPlayerIn(importedPlayers)
                        .size()
        );
    }

    @Test
    void invalidPlayerImportShouldCreateNoPlayersOrInvitations()
                throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        MockMultipartFile file =
                createPlayerImportExcel(
                        "|Kumar|Invalid Player|9000000021|RIGHT_HAND|RIGHT_ARM_FAST|BATTER"
                );

        PlayerImportResponse response =
                playerImportService.importExcel(
                        file,
                        admin
                );

        assertEquals(1, response.getTotalRows());
        assertEquals(0, response.getCreatedRows());
        assertFalse(response.getErrors().isEmpty());

        assertTrue(
                response.getRegistrations() == null
                        || response.getRegistrations().isEmpty()
        );
    }

    @Test
    void playerImportShouldRejectNullImportingAdmin()
                throws Exception {

        MockMultipartFile file =
                createPlayerImportExcel(
                        "Admin|Missing|Admin Missing|9000000031|RIGHT_HAND|RIGHT_ARM_FAST|BATTER"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> playerImportService.importExcel(
                        file,
                        null
                )
        );
    }

    @Test
    void registrationManagementShouldReturnEmptyWhenNoPlayersExist() {
        List<PlayerRegistrationManagementResponse> result =
                playerRegistrationManagementService.getRegistrations();

        assertThat(result).isNotNull();
    }

    @Test
    void registrationManagementShouldReturnPlayerWithoutInvitation() {
        String testId = uniqueTestId();

        Player player = new Player();
        player.setFirstName("Test");
        player.setLastName("Player");
        player.setDisplayName("Management Player " + testId);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);
        player.setRole(PlayerRole.BATTER);

        Player savedPlayer = playerRepository.saveAndFlush(player);

        List<PlayerRegistrationManagementResponse> result =
                playerRegistrationManagementService.getRegistrations();

        PlayerRegistrationManagementResponse response =
                result.stream()
                        .filter(item -> item.playerId().equals(savedPlayer.getId()))
                        .findFirst()
                        .orElseThrow();

        assertThat(response.displayName())
                .isEqualTo(savedPlayer.getDisplayName());

        assertThat(response.registrationStatus())
                .isEqualTo(PlayerRegistrationStatus.PENDING.name());

        assertThat(response.invitationStatus())
                .isNull();

        assertThat(response.invitationCreatedAt())
                .isNull();

        assertThat(response.invitationExpiresAt())
                .isNull();
    }

    @Test
    void registrationManagementShouldReturnInvitationInformation() {
        String testId = uniqueTestId();

        Player player = new Player();
        player.setFirstName("Test");
        player.setLastName("Invitation");
        player.setDisplayName("Invitation Player " + testId);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);
        player.setRole(PlayerRole.BATTER);

        Player savedPlayer = playerRepository.saveAndFlush(player);

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        savedPlayer,
                        user
                );

        List<PlayerRegistrationManagementResponse> result =
                playerRegistrationManagementService.getRegistrations();

        PlayerRegistrationManagementResponse response =
                result.stream()
                        .filter(item -> item.playerId().equals(savedPlayer.getId()))
                        .findFirst()
                        .orElseThrow();

        assertThat(response.invitationStatus())
                .isEqualTo(PlayerRegistrationInvitationStatus.PENDING.name());

        assertThat(response.invitationCreatedAt())
                .isEqualTo(invitation.getCreatedAt());

        assertThat(response.invitationExpiresAt())
                .isEqualTo(invitation.getExpiresAt());
    }

    @Test
    void registrationManagementShouldReturnLatestInvitation() {
        String testId = uniqueTestId();

        Player player = new Player();
        player.setFirstName("Test");
        player.setLastName("Latest");
        player.setDisplayName("Latest Invitation Player " + testId);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);
        player.setRole(PlayerRole.BATTER);

        Player savedPlayer = playerRepository.saveAndFlush(player);

        User user = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation first =
                playerRegistrationInvitationService.createInvitation(
                        savedPlayer,
                        user
                );

        PlayerRegistrationInvitation second =
                playerRegistrationInvitationService.regenerateInvitation(
                        first,
                        user
                );

        List<PlayerRegistrationManagementResponse> result =
                playerRegistrationManagementService.getRegistrations();

        PlayerRegistrationManagementResponse response =
                result.stream()
                        .filter(item -> item.playerId().equals(savedPlayer.getId()))
                        .findFirst()
                        .orElseThrow();

        assertThat(response.invitationStatus())
                .isEqualTo(PlayerRegistrationInvitationStatus.PENDING.name());

        assertThat(response.invitationCreatedAt())
                .isEqualTo(second.getCreatedAt());

        assertThat(response.invitationExpiresAt())
                .isEqualTo(second.getExpiresAt());

        assertThat(response.invitationStatus())
                .isNotEqualTo(first.getStatus().name());
    }

    @Test
    void adminShouldBeAbleToViewPlayerRegistrations() throws Exception {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        String googleSubject =
                "google-registration-admin-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(admin);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", admin.getEmail(),
                                "name", admin.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .get("/api/players/registrations")
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminShouldBeAbleToViewPlayerRegistrationSummary() throws Exception {
        Authentication authentication =
                createAdminAuthentication();

        mockMvc.perform(
                get("/api/players/registrations/summary")
                        .with(
                                SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPlayers").isNumber())
        .andExpect(jsonPath("$.registeredPlayers").isNumber())
        .andExpect(jsonPath("$.pendingPlayers").isNumber())
        .andExpect(jsonPath("$.expiredInvitations").isNumber());
    }

    @Test
    void playerWithoutAdminAccessShouldNotViewPlayerRegistrations()
                throws Exception {

        User player = createPlayerRegistrationTestUser();

        String googleSubject =
                "google-registration-player-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(player);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", player.getEmail(),
                                "name", player.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .get("/api/players/registrations")
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void playerWithoutAdminAccessShouldNotViewPlayerRegistrationSummary()
                throws Exception {

        User player = createPlayerRegistrationTestUser();

        String googleSubject =
                "google-summary-player-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(player);
        userAuthentication.setProvider(AuthenticationProvider.GOOGLE);
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication);

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", player.getEmail(),
                                "name", player.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        mockMvc.perform(
                get("/api/players/registrations/summary")
                        .with(
                                SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotViewPlayerRegistrationSummary()
                throws Exception {

        mockMvc.perform(
                get("/api/players/registrations/summary")
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void unauthenticatedUserShouldNotViewPlayerRegistrations()
                throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .get("/api/players/registrations")
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void registrationSummaryShouldCountPlayersByRegistrationStatus() {
        Player registeredPlayer = new Player();
        registeredPlayer.setFirstName("Summary");
        registeredPlayer.setLastName("Registered");
        registeredPlayer.setDisplayName(
                "Summary Registered " + uniqueTestId());
        registeredPlayer.setRole(PlayerRole.BATTER);
        registeredPlayer.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED);
        playerRepository.saveAndFlush(registeredPlayer);

        Player pendingPlayer = new Player();
        pendingPlayer.setFirstName("Summary");
        pendingPlayer.setLastName("Pending");
        pendingPlayer.setDisplayName(
                "Summary Pending " + uniqueTestId());
        pendingPlayer.setRole(PlayerRole.BATTER);
        pendingPlayer.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);
        playerRepository.saveAndFlush(pendingPlayer);

        PlayerRegistrationSummaryResponse summary =
                playerRegistrationSummaryService.getSummary();

        assertThat(summary.totalPlayers()).isGreaterThanOrEqualTo(2);
        assertThat(summary.registeredPlayers()).isGreaterThanOrEqualTo(1);
        assertThat(summary.pendingPlayers()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void registrationSummaryShouldCountExpiredInvitations() {
        Player player = new Player();
        player.setFirstName("Summary");
        player.setLastName("Expired");
        player.setDisplayName(
                "Summary Expired " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);

        Player savedPlayer = playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "summary-expired-token-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.EXPIRED);
        
       invitation.setExpiresAt(
        Instant.now().plus(1, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        PlayerRegistrationSummaryResponse summary =
                playerRegistrationSummaryService.getSummary();

        assertThat(summary.expiredInvitations())
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void registrationSummaryShouldNotCountPendingInvitationAsExpired() {
        Player player = new Player();
        player.setFirstName("Summary");
        player.setLastName("Active");
        player.setDisplayName(
                "Summary Active " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);

        Player savedPlayer = playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "summary-pending-token-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        long expiredBefore =
                playerRegistrationSummaryService
                        .getSummary()
                        .expiredInvitations();

        PlayerRegistrationSummaryResponse summary =
                playerRegistrationSummaryService.getSummary();

        assertThat(summary.expiredInvitations())
                .isEqualTo(expiredBefore);
    }

    @Test
    void registrationSummaryShouldReturnConsistentPlayerCounts() {
        PlayerRegistrationSummaryResponse summary =
                playerRegistrationSummaryService.getSummary();

        assertThat(summary.totalPlayers())
                .isEqualTo(
                        summary.registeredPlayers()
                                + summary.pendingPlayers());
    }


    @Test
    void registrationDetailShouldReturnPlayerWithoutInvitation() {
        Player player = new Player();
        player.setFirstName("Detail");
        player.setLastName("Player");
        player.setDisplayName(
                "Detail Player " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationDetailResponse response =
                playerRegistrationDetailService
                        .getRegistrationDetail(savedPlayer.getId());

        assertThat(response.playerId())
                .isEqualTo(savedPlayer.getId());

        assertThat(response.displayName())
                .isEqualTo(savedPlayer.getDisplayName());

        assertThat(response.registrationStatus())
                .isEqualTo("PENDING");

        assertThat(response.invitationStatus())
                .isNull();

        assertThat(response.userId())
                .isNull();
    }

    @Test
    void registrationDetailShouldReturnInvitationInformation() {
        Player player = new Player();
        player.setFirstName("Detail");
        player.setLastName("Invitation");
        player.setDisplayName(
                "Detail Invitation " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "detail-token-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        PlayerRegistrationInvitation savedInvitation =
                playerRegistrationInvitationRepository
                        .saveAndFlush(invitation);

        PlayerRegistrationDetailResponse response =
                playerRegistrationDetailService
                        .getRegistrationDetail(savedPlayer.getId());

        assertThat(response.invitationStatus())
                .isEqualTo("PENDING");

        assertThat(response.invitationCreatedAt())
                .isNotNull();

        assertThat(response.invitationExpiresAt())
                .isEqualTo(savedInvitation.getExpiresAt());

        assertThat(response.invitationCompletedAt())
                .isNull();
    }

    @Test
    void registrationDetailShouldReturnLatestInvitation() {
        Player player = new Player();
        player.setFirstName("Detail");
        player.setLastName("Latest");
        player.setDisplayName(
                "Detail Latest " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation first =
                new PlayerRegistrationInvitation();

        first.setPlayer(savedPlayer);
        first.setToken(
                "detail-first-" + uniqueTestId());
        first.setStatus(
                PlayerRegistrationInvitationStatus.CANCELLED);
        first.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(first);

        PlayerRegistrationInvitation second =
                new PlayerRegistrationInvitation();

        second.setPlayer(savedPlayer);
        second.setToken(
                "detail-second-" + uniqueTestId());
        second.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        second.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        PlayerRegistrationInvitation savedSecond =
                playerRegistrationInvitationRepository
                        .saveAndFlush(second);

        PlayerRegistrationDetailResponse response =
                playerRegistrationDetailService
                        .getRegistrationDetail(savedPlayer.getId());

        assertThat(response.invitationStatus())
                .isEqualTo("PENDING");

        assertThat(response.invitationExpiresAt())
                .isEqualTo(savedSecond.getExpiresAt());
    }

    @Test
    void registrationDetailShouldReturnLinkedUserInformation() {
        User user = createPlayerRegistrationTestUser();

        Player player = new Player();
        player.setFirstName("Detail");
        player.setLastName("Registered");
        player.setDisplayName(
                "Detail Registered " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED);
        player.setUser(user);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationDetailResponse response =
                playerRegistrationDetailService
                        .getRegistrationDetail(savedPlayer.getId());

        assertThat(response.registrationStatus())
                .isEqualTo("REGISTERED");

        assertThat(response.userId())
                .isEqualTo(user.getId());

        assertThat(response.userEmail())
                .isEqualTo(user.getEmail());

        assertThat(response.userDisplayName())
                .isEqualTo(user.getDisplayName());
    }

    @Test
    void registrationDetailShouldRejectUnknownPlayer() {
        Long unknownPlayerId =
                Long.MAX_VALUE;

        assertThatThrownBy(() ->
                playerRegistrationDetailService
                        .getRegistrationDetail(unknownPlayerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found");
    }

    @Test
    void regenerationServiceShouldCreateNewInvitation() {
        User admin = createPlayerRegistrationTestUser();

        Player player = new Player();
        player.setFirstName("Regenerate");
        player.setLastName("Player");
        player.setDisplayName(
                "Regenerate Player " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation oldInvitation =
                new PlayerRegistrationInvitation();

        oldInvitation.setPlayer(savedPlayer);
        oldInvitation.setToken(
                "regenerate-old-" + uniqueTestId());
        oldInvitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        oldInvitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        PlayerRegistrationInvitation savedOldInvitation =
                playerRegistrationInvitationRepository
                        .saveAndFlush(oldInvitation);

        PlayerRegistrationRegenerateResponse response =
                playerRegistrationRegenerateService
                        .regenerateInvitation(
                                savedPlayer.getId(),
                                admin);

        assertThat(response.playerId())
                .isEqualTo(savedPlayer.getId());

        assertThat(response.displayName())
                .isEqualTo(savedPlayer.getDisplayName());

        assertThat(response.invitationStatus())
                .isEqualTo("PENDING");

        assertThat(response.registrationLink())
                .isNotBlank();

        PlayerRegistrationInvitation updatedOldInvitation =
                playerRegistrationInvitationRepository
                        .findById(savedOldInvitation.getId())
                        .orElseThrow();

        assertThat(updatedOldInvitation.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.CANCELLED);

        assertThat(response.registrationLink())
                .isNotEqualTo(savedOldInvitation.getToken());
    }

    @Test
    void regenerationServiceShouldKeepSamePlayer() {
        User admin = createPlayerRegistrationTestUser();

        Player player = new Player();
        player.setFirstName("Same");
        player.setLastName("Player");
        player.setDisplayName(
                "Same Player " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "same-player-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        PlayerRegistrationRegenerateResponse response =
                playerRegistrationRegenerateService
                        .regenerateInvitation(
                                savedPlayer.getId(),
                                admin);

        assertThat(response.playerId())
                .isEqualTo(savedPlayer.getId());

        assertThat(playerRepository.findById(savedPlayer.getId()))
                .isPresent();

        assertThat(playerRepository.findById(savedPlayer.getId())
                .orElseThrow()
                .getDisplayName())
                .isEqualTo(savedPlayer.getDisplayName());
    }

    @Test
    void regenerationServiceShouldRejectRegisteredPlayer() {
        User admin = createPlayerRegistrationTestUser();

        Player player = new Player();
        player.setFirstName("Registered");
        player.setLastName("Player");
        player.setDisplayName(
                "Registered Player " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "registered-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED);
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        assertThatThrownBy(() ->
                playerRegistrationRegenerateService
                        .regenerateInvitation(
                                savedPlayer.getId(),
                                admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Player is already registered");
    }

    @Test
    void regenerationServiceShouldRejectUnknownPlayer() {
        User admin = createPlayerRegistrationTestUser();

        assertThatThrownBy(() ->
                playerRegistrationRegenerateService
                        .regenerateInvitation(
                                Long.MAX_VALUE,
                                admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found");
    }

    @Test
    void regenerationServiceShouldRejectUsedInvitation() {
        User admin = createPlayerRegistrationTestUser();

        Player player = new Player();
        player.setFirstName("Used");
        player.setLastName("Invitation");
        player.setDisplayName(
                "Used Invitation " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedPlayer);
        invitation.setToken(
                "used-invitation-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED);
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setCompletedAt(Instant.now());

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        assertThatThrownBy(() ->
                playerRegistrationRegenerateService
                        .regenerateInvitation(
                                savedPlayer.getId(),
                                admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invitation has already been used");
    }

    @Test
    void adminShouldBeAbleToRegeneratePlayerRegistrationInvitation()
                throws Exception {

        Authentication authentication =
                createAdminAuthentication();

        Player player = new Player();
        player.setFirstName("API");
        player.setLastName("Regenerate");
        player.setDisplayName(
                "API Regenerate " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedPlayer =
                playerRepository.saveAndFlush(player);

        PlayerRegistrationInvitation oldInvitation =
                new PlayerRegistrationInvitation();

        oldInvitation.setPlayer(savedPlayer);
        oldInvitation.setToken(
                "api-regenerate-old-" + uniqueTestId());
        oldInvitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        oldInvitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        PlayerRegistrationInvitation savedOldInvitation =
                playerRegistrationInvitationRepository
                        .saveAndFlush(oldInvitation);

        mockMvc.perform(
                post(
                        "/api/players/registrations/{playerId}/regenerate",
                        savedPlayer.getId()
                )
                .with(
                        SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.playerId")
                .value(savedPlayer.getId()))
        .andExpect(jsonPath("$.displayName")
                .value(savedPlayer.getDisplayName()))
        .andExpect(jsonPath("$.registrationStatus")
                .value("PENDING"))
        .andExpect(jsonPath("$.invitationStatus")
                .value("PENDING"))
        .andExpect(jsonPath("$.invitationCreatedAt")
                .exists())
        .andExpect(jsonPath("$.invitationExpiresAt")
                .exists())
        .andExpect(jsonPath("$.registrationLink")
                .isNotEmpty());

        PlayerRegistrationInvitation updatedOldInvitation =
                playerRegistrationInvitationRepository
                        .findById(savedOldInvitation.getId())
                        .orElseThrow();

        assertThat(updatedOldInvitation.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.CANCELLED);
    }

    @Test
    void playerWithoutAdminAccessShouldNotRegeneratePlayerRegistrationInvitation()
                throws Exception {

        User player = createPlayerRegistrationTestUser();

        String googleSubject =
                "google-regenerate-player-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(player);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE);
        userAuthentication.setProviderUserId(googleSubject);

        userAuthenticationRepository.saveAndFlush(
                userAuthentication);

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", player.getEmail(),
                                "name", player.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        Player targetPlayer = new Player();
        targetPlayer.setFirstName("Protected");
        targetPlayer.setLastName("Regenerate");
        targetPlayer.setDisplayName(
                "Protected Regenerate " + uniqueTestId());
        targetPlayer.setRole(PlayerRole.BATTER);
        targetPlayer.setRegistrationStatus(
                PlayerRegistrationStatus.PENDING);

        Player savedTargetPlayer =
                playerRepository.saveAndFlush(targetPlayer);

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(savedTargetPlayer);
        invitation.setToken(
                "protected-regenerate-" + uniqueTestId());
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING);
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));

        playerRegistrationInvitationRepository
                .saveAndFlush(invitation);

        mockMvc.perform(
                post(
                        "/api/players/registrations/{playerId}/regenerate",
                        savedTargetPlayer.getId()
                )
                .with(
                        SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotRegeneratePlayerRegistrationInvitation()
                throws Exception {

        mockMvc.perform(
                post(
                        "/api/players/registrations/{playerId}/regenerate",
                        1L
                )
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void playerRegistrationExcelExportShouldGenerateValidExcelFile()
                throws Exception {

        Player player = new Player();
        player.setFirstName("Excel");
        player.setLastName("Export");
        player.setDisplayName("Excel Export " + uniqueTestId());
        player.setRole(PlayerRole.BATTER);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);
        player = playerRepository.saveAndFlush(player);

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        byte[] excel =
                playerRegistrationExcelExportService.exportRegistrations();

        assertThat(excel)
                .isNotNull()
                .isNotEmpty();

        try (Workbook workbook =
                        new XSSFWorkbook(new ByteArrayInputStream(excel))) {

                assertThat(workbook.getNumberOfSheets())
                        .isGreaterThanOrEqualTo(1);

                assertThat(workbook.getSheet("Player Registrations"))
                        .isNotNull();
        }

        assertThat(invitation.getToken())
                .isNotBlank();
    }

    @Test
    void playerRegistrationExcelExportShouldContainExpectedHeaders()
                throws Exception {

        byte[] excel =
                playerRegistrationExcelExportService.exportRegistrations();

        try (Workbook workbook =
                        new XSSFWorkbook(new ByteArrayInputStream(excel))) {

                var sheet =
                        workbook.getSheet("Player Registrations");

                var headerRow =
                        sheet.getRow(0);

                assertThat(headerRow.getCell(0).getStringCellValue())
                        .isEqualTo("Player Name");

                assertThat(headerRow.getCell(1).getStringCellValue())
                        .isEqualTo("Jersey Number");

                assertThat(headerRow.getCell(2).getStringCellValue())
                        .isEqualTo("Team");

                assertThat(headerRow.getCell(3).getStringCellValue())
                        .isEqualTo("Batting Style");

                assertThat(headerRow.getCell(4).getStringCellValue())
                        .isEqualTo("Bowling Style");

                assertThat(headerRow.getCell(5).getStringCellValue())
                        .isEqualTo("Role");

                assertThat(headerRow.getCell(6).getStringCellValue())
                        .isEqualTo("Registration Status");

                assertThat(headerRow.getCell(7).getStringCellValue())
                        .isEqualTo("Invitation Created Date");

                assertThat(headerRow.getCell(8).getStringCellValue())
                        .isEqualTo("Invitation Expiry Date");

                assertThat(headerRow.getCell(9).getStringCellValue())
                        .isEqualTo("Registration Link");
        }
     }


     @Test
     void playerRegistrationExcelExportShouldContainPlayerRegistrationData()
                throws Exception {

        Player player = new Player();
        player.setFirstName("Registration");
        player.setLastName("Test");
        player.setDisplayName("Registration Test " + uniqueTestId());
        player.setBattingStyle(BattingStyle.RIGHT_HAND);
        player.setBowlingStyle(BowlingStyle.RIGHT_ARM_MEDIUM);
        player.setRole(PlayerRole.ALL_ROUNDER);
        player.setRegistrationStatus(PlayerRegistrationStatus.PENDING);

        player = playerRepository.saveAndFlush(player);

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                        playerRegistrationInvitationService.createInvitation(
                                player,
                                invitedBy
                        );

        byte[] excel =
                playerRegistrationExcelExportService.exportRegistrations();

        try (Workbook workbook =
                        new XSSFWorkbook(new ByteArrayInputStream(excel))) {

                var sheet =
                        workbook.getSheet("Player Registrations");

                boolean foundPlayer = false;

                for (int rowIndex = 1;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++) {

                var row = sheet.getRow(rowIndex);

                if (row == null || row.getCell(0) == null) {
                        continue;
                }

                String displayName =
                        row.getCell(0).getStringCellValue();

                        if (displayName.equals(player.getDisplayName())) {

                                foundPlayer = true;

                                assertThat(
                                        row.getCell(3).getStringCellValue()
                                ).isEqualTo("RIGHT_HAND");

                                assertThat(
                                        row.getCell(4).getStringCellValue()
                                ).isEqualTo("RIGHT_ARM_MEDIUM");

                                assertThat(
                                        row.getCell(5).getStringCellValue()
                                ).isEqualTo("ALL_ROUNDER");

                                assertThat(
                                        row.getCell(6).getStringCellValue()
                                ).isEqualTo("PENDING");

                                assertThat(
                                        row.getCell(9).getStringCellValue()
                                )
                                .contains("http://localhost:5173/player-registration/")
                                .contains(invitation.getToken());

                                break;
                        }
                }

                assertThat(foundPlayer)
                        .isTrue();
        }
    }

    @Test
    void adminShouldBeAbleToDownloadPlayerRegistrationExcel()
                throws Exception {

        Authentication authentication =
                createAdminAuthentication();

        mockMvc.perform(
                get("/api/players/registrations/export")
                        .with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"player-registrations.xlsx\""
                )
        )
        .andExpect(
                header().string(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
    }

    @Test
    void playerWithoutAdminAccessShouldNotDownloadPlayerRegistrationExcel()
                throws Exception {

        User player =
                createPlayerRegistrationTestUser();

        String googleSubject =
                "google-player-export-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(player);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(
                googleSubject
        );

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", player.getEmail(),
                                "name", player.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        mockMvc.perform(
                get("/api/players/registrations/export")
                        .with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                        .authentication(authentication)
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotDownloadPlayerRegistrationExcel()
                throws Exception {

        mockMvc.perform(
                get("/api/players/registrations/export")
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void pendingPlayerRegistrationInvitationShouldReturnLink() {

        Player player = createPlayer(
                "Link",
                "Test",
                "Link Test " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        PlayerRegistrationLinkResponse response =
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                );

        assertThat(response.playerId())
                .isEqualTo(player.getId());

        assertThat(response.displayName())
                .isEqualTo(player.getDisplayName());

        assertThat(response.registrationStatus())
                .isEqualTo("PENDING");

        assertThat(response.invitationStatus())
                .isEqualTo("PENDING");

        assertThat(response.registrationLink())
                .startsWith(
                        "http://localhost:5173/player-registration/"
                );

        assertThat(response.registrationLink())
                .contains(invitation.getToken());

        assertThat(response.expiresAt())
                .isEqualTo(invitation.getExpiresAt());
    }

    @Test
    void registeredPlayerShouldNotReturnRegistrationLink() {

        Player player = createPlayer(
                "Registered",
                "Player",
                "Registered Player " + uniqueTestId(),
                PlayerRole.BATTER
        );

        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        playerRepository.saveAndFlush(player);

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Player is already registered");
    }

    @Test
    void expiredPlayerRegistrationInvitationShouldBeRejected() {

        Player player = createPlayer(
                "Expired",
                "Link",
                "Expired Link " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        invitation.setExpiresAt(
                java.time.Instant.now().minusSeconds(1)
        );

        playerRegistrationInvitationRepository.saveAndFlush(
                invitation
        );

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Registration invitation has expired");
    }

    @Test
    void cancelledPlayerRegistrationInvitationShouldBeRejected() {

        Player player = createPlayer(
                "Cancelled",
                "Link",
                "Cancelled Link " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Registration invitation is not active");
    }

    @Test
    void usedPlayerRegistrationInvitationShouldBeRejected() {

        Player player = createPlayer(
                "Used",
                "Link",
                "Used Link " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );

        playerRegistrationInvitationRepository.saveAndFlush(invitation);

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Registration invitation is not active");
    }

    @Test
    void playerWithoutRegistrationInvitationShouldBeRejected() {

        Player player = createPlayer(
                "No",
                "Invitation",
                "No Invitation " + uniqueTestId(),
                PlayerRole.BATTER
        );

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        player.getId()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No invitation exists for player");
    }

    @Test
    void unknownPlayerShouldBeRejectedWhenGettingRegistrationLink() {

        assertThatThrownBy(() ->
                playerRegistrationLinkService.getRegistrationLink(
                        Long.MAX_VALUE
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Player not found");
    }

    @Test
    void adminShouldBeAbleToGetPlayerRegistrationLink()
                throws Exception {

        Authentication authentication =
                createAdminAuthentication();

        Player player = createPlayer(
                "API",
                "Link",
                "API Link " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        mockMvc.perform(
                get(
                        "/api/players/registrations/{playerId}/link",
                        player.getId()
                )
                .with(
                        org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.playerId")
                .value(player.getId().intValue()))
        .andExpect(jsonPath("$.displayName")
                .value(player.getDisplayName()))
        .andExpect(jsonPath("$.registrationStatus")
                .value("PENDING"))
        .andExpect(jsonPath("$.invitationStatus")
                .value("PENDING"))
        .andExpect(jsonPath("$.registrationLink")
                .value(
                        "http://localhost:5173/player-registration/"
                                + invitation.getToken()
                ))
        .andExpect(jsonPath("$.expiresAt")
                .exists());
    }

    @Test
    void playerWithoutAdminAccessShouldNotGetPlayerRegistrationLink()
                throws Exception {

        User playerUser =
                createPlayerRegistrationTestUser();

        String googleSubject =
                "google-player-link-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(playerUser);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(
                googleSubject
        );

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", playerUser.getEmail(),
                                "name", playerUser.getDisplayName()
                        ),
                        "sub"
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities()
                );

        mockMvc.perform(
                get(
                        "/api/players/registrations/{playerId}/link",
                        1L
                )
                .with(
                        org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors
                                .authentication(authentication)
                )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotGetPlayerRegistrationLink()
                throws Exception {

        mockMvc.perform(
                get(
                        "/api/players/registrations/{playerId}/link",
                        1L
                )
        )
        .andExpect(status().is3xxRedirection());
    }

    @Test
    void pendingExpiredInvitationShouldBeMarkedExpired() {

        Player player = createPlayer(
                "Maintenance",
                "Expired",
                "Maintenance Expired " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        invitation.setExpiresAt(
                java.time.Instant.now().minusSeconds(1)
        );

        playerRegistrationInvitationRepository.saveAndFlush(
                invitation
        );

        int expiredCount =
                playerRegistrationInvitationMaintenanceService
                        .markExpiredInvitations();

        PlayerRegistrationInvitation refreshed =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertThat(expiredCount)
                .isGreaterThanOrEqualTo(1);

        assertThat(refreshed.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.EXPIRED
                );
    }

    @Test
    void futurePendingInvitationShouldRemainPending() {

        Player player = createPlayer(
                "Maintenance",
                "Future",
                "Maintenance Future " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        int expiredCount =
                playerRegistrationInvitationMaintenanceService
                        .markExpiredInvitations();

        PlayerRegistrationInvitation refreshed =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertThat(refreshed.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.PENDING
                );
    }

    @Test
    void cancelledInvitationShouldRemainCancelled() {

        Player player = createPlayer(
                "Maintenance",
                "Cancelled",
                "Maintenance Cancelled " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        playerRegistrationInvitationService.cancelInvitation(
                invitation
        );

        invitation.setExpiresAt(
                java.time.Instant.now().minusSeconds(1)
        );

        playerRegistrationInvitationRepository.saveAndFlush(
                invitation
        );

        playerRegistrationInvitationMaintenanceService
                .markExpiredInvitations();

        PlayerRegistrationInvitation refreshed =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertThat(refreshed.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.CANCELLED
                );
    }

    @Test
    void usedInvitationShouldRemainUsed() {

        Player player = createPlayer(
                "Maintenance",
                "Used",
                "Maintenance Used " + uniqueTestId(),
                PlayerRole.BATTER
        );

        User invitedBy = createPlayerRegistrationTestUser();

        PlayerRegistrationInvitation invitation =
                playerRegistrationInvitationService.createInvitation(
                        player,
                        invitedBy
                );

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );

        invitation.setExpiresAt(
                java.time.Instant.now().minusSeconds(1)
        );

        playerRegistrationInvitationRepository.saveAndFlush(
                invitation
        );

        playerRegistrationInvitationMaintenanceService
                .markExpiredInvitations();

        PlayerRegistrationInvitation refreshed =
                playerRegistrationInvitationRepository
                        .findById(invitation.getId())
                        .orElseThrow();

        assertThat(refreshed.getStatus())
                .isEqualTo(
                        PlayerRegistrationInvitationStatus.USED
                );
    }


            
    private AdminEntitlement createAdminEntitlement(
            User user,
            AdminSubscriptionStatus status) {

        AdminEntitlement entitlement =
                new AdminEntitlement();

        entitlement.setUser(user);
        entitlement.setStatus(status);

        return adminEntitlementRepository.save(entitlement);
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

    private User createAdminAccessTestUser(boolean active) {

        String testId =
                String.valueOf(System.currentTimeMillis());

        User user = new User();

        user.setEmail(
                "admin-access-" + testId + "@example.com");

        user.setDisplayName(
                "Admin Access Test " + testId);

        user.setRole(UserRole.PLAYER);

        user.setActive(active);

        return userRepository.save(user);
    }

    private User createPlayerRegistrationTestUser() {
        String testId = String.valueOf(System.currentTimeMillis())
                + "-" + System.nanoTime();

        User user = new User();
        user.setEmail("player-registration-" + testId + "@example.com");
        user.setDisplayName("Player Registration Test " + testId);
        user.setRole(UserRole.PLAYER);
        user.setActive(true);

        return userRepository.save(user);
    }

    private Authentication createAdminAuthentication() {

        User admin = createPlayerRegistrationTestUser();

        adminAccessService.activateAdminAccess(
                admin,
                Instant.now(),
                null
        );

        String googleSubject =
                "google-admin-test-" + uniqueTestId();

        UserAuthentication userAuthentication =
                new UserAuthentication();

        userAuthentication.setUser(admin);
        userAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        userAuthentication.setProviderUserId(
                googleSubject
        );

        userAuthenticationRepository.saveAndFlush(
                userAuthentication
        );

        OAuth2User oauth2User =
                new DefaultOAuth2User(
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        Map.of(
                                "sub", googleSubject,
                                "email", admin.getEmail(),
                                "name", admin.getDisplayName()
                        ),
                        "sub"
                );

        return new UsernamePasswordAuthenticationToken(
                oauth2User,
                null,
                oauth2User.getAuthorities()
        );
    }

    private PlayerRegistrationInvitation createRegistrationInvitation(
        Player player,
        String token,
        Instant expiresAt,
        User invitedBy
    ) {
        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(player);
        invitation.setToken(token);
        invitation.setExpiresAt(expiresAt);
        invitation.setInvitedBy(invitedBy);

        return playerRegistrationInvitationRepository.saveAndFlush(
                invitation
        );
  }

  private String uniqueTestId() {
        return System.currentTimeMillis()
                + "-"
                + System.nanoTime();
        }

  private MockMultipartFile createPlayerImportExcel(
        String... playerRows) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Players");

        Row header = sheet.createRow(0);

        String[] headers = {
                "First Name",
                "Last Name",
                "Display Name",
                "Phone",
                "Batting Style",
                "Bowling Style",
                "Role"
        };

        for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
        }

        for (int rowIndex = 0; rowIndex < playerRows.length; rowIndex++) {

                String[] values = playerRows[rowIndex].split("\\|", -1);

                Row row = sheet.createRow(rowIndex + 1);

                for (int column = 0; column < values.length; column++) {
                row.createCell(column).setCellValue(values[column]);
                }
        }

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "players.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );
  }

}

