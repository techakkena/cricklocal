import DashboardMetrics from "../../components/dashboard/DashboardMetrics";
import LiveMatches from "../../components/dashboard/LiveMatches";
import UpcomingMatches from "../../components/dashboard/UpcomingMatches";
import RecentResults from "../../components/dashboard/RecentResults";
import TopPerformers from "../../components/dashboard/TopPerformers";
import RecentActivity from "../../components/dashboard/RecentActivity";
import PageMeta from "../../components/common/PageMeta";

export default function Home() {
  return (
    <>
      <PageMeta
        title="CrickLocal Dashboard"
        description="CrickLocal cricket management and live scoring dashboard"
      />

      <div className="space-y-6">
        <DashboardMetrics />
        <LiveMatches />
        <UpcomingMatches />
        <RecentResults />
        <TopPerformers />
        <RecentActivity />
      </div>
    </>
  );
}