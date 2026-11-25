import type React from "react";
import UserProfiles from "./pages/UserProfiles";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useLocation,
} from "react-router";

import AppLayout from "./layout/AppLayout";
import { ScrollToTop } from "./components/common/ScrollToTop";

import SignIn from "./pages/AuthPages/SignIn";
import SignUp from "./pages/AuthPages/SignUp";
import NotFound from "./pages/OtherPage/NotFound";

import GlobalPlayers from "./pages/GlobalPlayers";
import LocalPlayers from "./pages/LocalPlayers";
import Home from "./pages/Dashboard/Home";
import Teams from "./pages/Teams";
import Players from "./pages/Players";
import PlayerRegistrations from "./pages/PlayerRegistrations";
import Series from "./pages/Series";
import Matches from "./pages/Matches";
import PlaceholderPage from "./pages/PlaceholderPage";
import MatchPreparation from "./pages/MatchPreparation";
import ScoreOperator from "./pages/ScoreOperator";
import ScorecardDetails from "./pages/ScorecardDetails";
import ScoreDisplay from "./pages/ScoreDisplay/ScoreDisplay";
import ScoreOperatorAccess from "./pages/ScoreOperatorAccess";

import PlayerRegistration from "./pages/AuthPages/PlayerRegistration";
import LoginSuccess from "./pages/AuthPages/LoginSuccess";
import PlayerRegistrationConfirm from "./pages/AuthPages/PlayerRegistrationConfirm";

import { AuthProvider, useAuth } from "./context/AuthContext";
import AdminRoute from "./components/auth/AdminRoute";
import SuperAdminRoute from "./components/auth/SuperAdminRoute";
import PermissionRoute from "./components/auth/PermissionRoute";
import SuperAdminUserPermissions from "./pages/SuperAdminUserPermissions";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Loading CricketLocal...
        </p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/signin" replace />;
  }

  return <>{children}</>;
}

function AppContent() {
  const location = useLocation();

  /*
   * Score operator and score display pages must work without
   * normal CricketLocal authentication.
   *
   * Operator access uses the secure operator link + code.
   * Display access uses the secure display token.
   */
  const isScoreOperatorRoute =
    location.pathname.startsWith("/score/operator/") ||
    location.pathname.startsWith("/score/match/") ||
    location.pathname.startsWith("/score/display/");

  const routes = (
    <Routes>
      {/* Main Application */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Home />} />

        <Route path="/profile" element={<UserProfiles />} />

        <Route
          path="/super-admin/user-permissions"
          element={
            <SuperAdminRoute>
              <SuperAdminUserPermissions />
            </SuperAdminRoute>
          }
        />

        {/* Manage */}
        <Route path="/teams" element={<PermissionRoute moduleKey="TEAMS"><Teams /></PermissionRoute>} />
        <Route path="/players" element={<PermissionRoute moduleKey="PLAYERS"><Players /></PermissionRoute>} />
        <Route path="/global-players" element={<PermissionRoute moduleKey="GLOBAL_PLAYERS"><GlobalPlayers /></PermissionRoute>} />
        <Route path="/local-players" element={<PermissionRoute moduleKey="LOCAL_PLAYERS"><LocalPlayers /></PermissionRoute>} />

        <Route
          path="/player-registrations"
          element={
            <AdminRoute>
              <PermissionRoute moduleKey="PLAYER_REGISTRATIONS">
                <PlayerRegistrations />
              </PermissionRoute>
            </AdminRoute>
          }
        />

        <Route path="/series" element={<PermissionRoute moduleKey="SERIES"><Series /></PermissionRoute>} />
        <Route path="/matches" element={<PermissionRoute moduleKey="MATCHES"><Matches /></PermissionRoute>} />

        <Route
          path="/matches/:matchId"
          element={<PermissionRoute moduleKey="MATCH_PREPARATION"><MatchPreparation /></PermissionRoute>}
        />

        <Route
          path="/scorecards/:matchId"
          element={<PermissionRoute moduleKey="SCORECARDS"><ScorecardDetails /></PermissionRoute>}
        />

        {/* Match Center */}
        <Route
          path="/matches/upcoming"
          element={
            <PermissionRoute moduleKey="MATCH_CENTER">
              <PlaceholderPage
              title="Upcoming Matches"
              description="View scheduled and upcoming matches."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/matches/live"
          element={
            <PermissionRoute moduleKey="MATCH_CENTER">
              <PlaceholderPage
              title="Live Matches"
              description="View matches currently in progress."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/matches/history"
          element={
            <PermissionRoute moduleKey="HISTORY">
              <PlaceholderPage
              title="Match History"
              description="Browse completed and historical matches."
            />
            </PermissionRoute>
          }
        />

        {/* Match */}
        <Route
          path="/live-scoring"
          element={
            <PermissionRoute moduleKey="LIVE_SCORING">
              <PlaceholderPage
                title="Live Scoring"
                description="Record ball-by-ball cricket scoring."
              />
            </PermissionRoute>
          }
        />

        <Route
          path="/scorecards"
          element={
            <PermissionRoute moduleKey="SCORECARDS">
              <PlaceholderPage
                title="Scorecards"
                description="View complete match scorecards and results."
              />
            </PermissionRoute>
          }
        />

        {/* Statistics */}
        <Route
          path="/statistics/batting"
          element={
            <PermissionRoute moduleKey="STATISTICS">
              <PlaceholderPage
              title="Batting Statistics"
              description="Explore batting statistics and performance."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/statistics/bowling"
          element={
            <PermissionRoute moduleKey="STATISTICS">
              <PlaceholderPage
              title="Bowling Statistics"
              description="Explore bowling statistics and performance."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/statistics/fielding"
          element={
            <PermissionRoute moduleKey="STATISTICS">
              <PlaceholderPage
              title="Fielding Statistics"
              description="Explore fielding statistics and performance."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/statistics/leaderboards"
          element={
            <PermissionRoute moduleKey="LEADERBOARDS">
              <PlaceholderPage
              title="Leaderboards"
              description="View CricketLocal player leaderboards."
            />
            </PermissionRoute>
          }
        />

        {/* History */}
        <Route
          path="/history/matches"
          element={
            <PermissionRoute moduleKey="HISTORY">
              <PlaceholderPage
              title="Match History"
              description="Browse CricketLocal match history."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/history/players"
          element={
            <PermissionRoute moduleKey="HISTORY">
              <PlaceholderPage
              title="Player History"
              description="Browse player match history and career activity."
            />
            </PermissionRoute>
          }
        />

        <Route
          path="/history/teams"
          element={
            <PermissionRoute moduleKey="HISTORY">
              <PlaceholderPage
              title="Team History"
              description="Browse team history and past performances."
            />
            </PermissionRoute>
          }
        />

        {/* Settings */}
        <Route
          path="/settings"
          element={
            <PermissionRoute moduleKey="SETTINGS">
              <PlaceholderPage
              title="Settings"
              description="Configure CricketLocal application settings."
            />
            </PermissionRoute>
          }
        />
      </Route>

      {/* Authentication */}
      <Route path="/signin" element={<SignIn />} />
      <Route path="/signup" element={<SignUp />} />
      <Route path="/login/success" element={<LoginSuccess />} />

      <Route
        path="/player-registration/:token"
        element={<PlayerRegistration />}
      />

      <Route
        path="/player-registration/confirm/:token"
        element={<PlayerRegistrationConfirm />}
      />

      {/* Score Operator */}
      <Route
        path="/score/operator/:accessToken"
        element={<ScoreOperatorAccess />}
      />

      <Route
        path="/score/match/:matchId"
        element={<ScoreOperator />}
      />

      {/* Score Display */}
      <Route
        path="/score/display/:displayToken"
        element={<ScoreDisplay />}
      />

      {/* Fallback */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );

  /*
   * Operator and display routes bypass the normal AuthProvider.
   *
   * All other CricketLocal routes continue to use
   * the existing authentication system.
   */
  if (isScoreOperatorRoute) {
    return routes;
  }

  return <AuthProvider>{routes}</AuthProvider>;
}

export default function App() {
  return (
    <Router>
      <ScrollToTop />
      <AppContent />
    </Router>
  );
}