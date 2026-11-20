\set ON_ERROR_STOP on

BEGIN;
SET LOCAL lock_timeout = '15s';

DELETE FROM fall_of_wickets;
DELETE FROM fielding_events;
DELETE FROM short_run_adjustments;
DELETE FROM batting_innings;
DELETE FROM bowling_innings;
DELETE FROM deliveries;
DELETE FROM innings_states;
DELETE FROM partnerships;
DELETE FROM innings;

DELETE FROM match_lineups;
DELETE FROM match_results;
DELETE FROM match_teams;
DELETE FROM playing_xis;
DELETE FROM score_display_access;
DELETE FROM score_operator_access;
DELETE FROM score_operator_session;
DELETE FROM tosses;

DELETE FROM matches;
DELETE FROM series_participations;
DELETE FROM series_teams;
DELETE FROM series;

UPDATE teams SET captain_team_player_id = NULL;
DELETE FROM team_players;
DELETE FROM teams;
