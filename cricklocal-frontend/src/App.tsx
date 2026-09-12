import { BrowserRouter as Router, Routes, Route } from "react-router";
import AppLayout from "./layout/AppLayout";
import { ScrollToTop } from "./components/common/ScrollToTop";
import SignIn from "./pages/AuthPages/SignIn";
import SignUp from "./pages/AuthPages/SignUp";
import NotFound from "./pages/OtherPage/NotFound";
import Home from "./pages/Dashboard/Home";
import Teams from "./pages/Teams";
import PlaceholderPage from "./pages/PlaceholderPage";

export default function App() {
  return (
    <Router>
      <ScrollToTop />

      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Home />} />

          {/* Manage */}
          <Route path="/teams" element={<Teams />} />
          <Route
            path="/players"
            element={
              <PlaceholderPage
                title="Players"
                description="Manage CricketLocal players and player profiles."
              />
            }
          />
          <Route
            path="/series"
            element={
              <PlaceholderPage
                title="Series"
                description="Create and manage cricket series."
              />
            }
          />
          <Route
            path="/matches"
            element={
              <PlaceholderPage
                title="Matches"
                description="Create, schedule, and manage matches."
              />
            }
          />

          {/* Match Center */}
          <Route
            path="/matches/upcoming"
            element={
              <PlaceholderPage
                title="Upcoming Matches"
                description="View scheduled and upcoming matches."
              />
            }
          />
          <Route
            path="/matches/live"
            element={
              <PlaceholderPage
                title="Live Matches"
                description="View matches currently in progress."
              />
            }
          />
          <Route
            path="/matches/history"
            element={
              <PlaceholderPage
                title="Match History"
                description="Browse completed and historical matches."
              />
            }
          />

          {/* Match */}
          <Route
            path="/live-scoring"
            element={
              <PlaceholderPage
                title="Live Scoring"
                description="Record ball-by-ball cricket scoring."
              />
            }
          />
          <Route
            path="/scorecards"
            element={
              <PlaceholderPage
                title="Scorecards"
                description="View complete match scorecards and results."
              />
            }
          />

          {/* Statistics */}
          <Route
            path="/statistics/batting"
            element={
              <PlaceholderPage
                title="Batting Statistics"
                description="Explore batting statistics and performance."
              />
            }
          />
          <Route
            path="/statistics/bowling"
            element={
              <PlaceholderPage
                title="Bowling Statistics"
                description="Explore bowling statistics and performance."
              />
            }
          />
          <Route
            path="/statistics/fielding"
            element={
              <PlaceholderPage
                title="Fielding Statistics"
                description="Explore fielding statistics and performance."
              />
            }
          />
          <Route
            path="/statistics/leaderboards"
            element={
              <PlaceholderPage
                title="Leaderboards"
                description="View CricketLocal player leaderboards."
              />
            }
          />

          {/* History */}
          <Route
            path="/history/matches"
            element={
              <PlaceholderPage
                title="Match History"
                description="Browse CricketLocal match history."
              />
            }
          />
          <Route
            path="/history/players"
            element={
              <PlaceholderPage
                title="Player History"
                description="Browse player match history and career activity."
              />
            }
          />
          <Route
            path="/history/teams"
            element={
              <PlaceholderPage
                title="Team History"
                description="Browse team history and past performances."
              />
            }
          />

          {/* Settings */}
          <Route
            path="/settings"
            element={
              <PlaceholderPage
                title="Settings"
                description="Configure CricketLocal application settings."
              />
            }
          />
        </Route>

        {/* Authentication */}
        <Route path="/signin" element={<SignIn />} />
        <Route path="/signup" element={<SignUp />} />

        {/* Fallback */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}