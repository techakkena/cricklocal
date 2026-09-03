# CricketLocal Database Design

## 1. Purpose

This document defines the initial database/domain model for CricketLocal.

CricketLocal is a community cricket application supporting:

- Teams
- Players
- Matches
- Playing XI
- Toss
- Innings
- Ball-by-ball scoring
- Scorecards
- Batting statistics
- Bowling statistics
- Fielding statistics
- Career statistics
- Leaderboards

---

## 2. Core Design Principle

Ball-by-ball delivery data is the primary source of truth for match statistics.

The system should not depend only on manually entered final statistics.

The flow is:

Team / Player
        ↓
Match
        ↓
Playing XI
        ↓
Innings
        ↓
Delivery events
        ↓
Match scorecard
        ↓
Player match statistics
        ↓
Career statistics
        ↓
Leaderboards

---

## 3. Initial Core Entities

### TEAM

Represents a cricket team.

Initial fields:

- id
- name
- shortName
- city
- active
- createdAt

### PLAYER

Represents a cricket player.

Initial fields:

- id
- firstName
- lastName
- displayName
- phone
- battingStyle
- bowlingStyle
- role
- active
- createdAt

### TEAM_PLAYER

Represents the relationship between a player and a team.

Initial fields:

- id
- teamId
- playerId
- joinedAt
- leftAt
- active
- jerseyNumber

---

## 4. Future Match Entities

The following entities will be added after the initial Team/Player model:

- MATCH
- MATCH_TEAM
- PLAYING_XI
- TOSS
- INNINGS
- DELIVERY
- FIELDING_EVENT
- PARTNERSHIP
- FALL_OF_WICKET
- MATCH_RESULT

---

## 5. Statistics Principle

Statistics should be calculated from delivery and match events wherever practical.

Examples:

Batting:
- Runs
- Balls
- Fours
- Sixes
- Strike rate
- Average
- Highest score
- 50s
- 100s
- Not outs

Bowling:
- Overs
- Runs conceded
- Wickets
- Maidens
- Economy
- Average
- Strike rate
- Best bowling

Fielding:
- Catches
- Run outs
- Stumpings

---

## 6. Technology

Backend:

- Java 21
- Spring Boot
- Spring Data JPA

Database:

- PostgreSQL

Frontend:

- React
- Vite

Architecture:

Browser / Mobile
        ↓
React Frontend
        ↓
Spring Boot REST API
        ↓
PostgreSQL