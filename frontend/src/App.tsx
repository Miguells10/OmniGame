import { Routes, Route } from 'react-router-dom';
import { AppShell } from './widgets/AppShell';
import { GameDashboard } from './pages/GameDashboard';

/**
 * Root application component with route definitions.
 */
export function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<GameDashboard />} />
        {/* Future routes:
          <Route path="/games/:slug" element={<ModDetails />} />
          <Route path="/upgrade" element={<UpgradeToPremium />} />
        */}
      </Routes>
    </AppShell>
  );
}
