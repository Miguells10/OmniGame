import { useState, type ReactNode } from 'react';
import {
  Gamepad2,
  Search,
  Menu,
  X,
  Sparkles,
  Shield,
  TrendingUp,
  Crown,
  Bot,
} from 'lucide-react';
import { AiChatWidget } from '../features/AiChatWidget';

interface AppShellProps {
  children: ReactNode;
}

/**
 * AppShell — Main layout wrapper (Nexus-style dark UI)
 *
 * Provides the persistent top navbar, collapsible sidebar,
 * and the floating "The Collector" AI chat trigger button.
 */
export function AppShell({ children }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [chatOpen, setChatOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="flex h-screen overflow-hidden bg-nexus-bg">
      {/* ── Sidebar ────────────────────────────────────────────── */}
      <aside
        className={`${
          sidebarOpen ? 'w-64' : 'w-0'
        } transition-all duration-300 ease-out overflow-hidden flex-shrink-0`}
      >
        <div className="w-64 h-full bg-nexus-surface border-r border-nexus-border flex flex-col">
          {/* Logo */}
          <div className="p-5 border-b border-nexus-border">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-accent-orange to-yellow-500 flex items-center justify-center shadow-lg shadow-accent-orange/20">
                <Gamepad2 className="w-5 h-5 text-white" />
              </div>
              <div>
                <h1 className="text-lg font-bold text-white tracking-tight">
                  OmniGame
                </h1>
                <span className="text-xs font-medium text-accent-cyan">
                  AI Platform
                </span>
              </div>
            </div>
          </div>

          {/* Nav Links */}
          <nav className="flex-1 p-4 space-y-1">
            <NavItem icon={<TrendingUp className="w-4 h-4" />} label="Trending" active />
            <NavItem icon={<Gamepad2 className="w-4 h-4" />} label="All Games" />
            <NavItem icon={<Shield className="w-4 h-4" />} label="Security Audited" />
            <NavItem icon={<Crown className="w-4 h-4" />} label="Premium" />

            <div className="pt-6 pb-2">
              <p className="text-[10px] uppercase tracking-widest text-nexus-muted font-semibold px-3">
                AI Agents
              </p>
            </div>
            <NavItem
              icon={<Bot className="w-4 h-4" />}
              label="The Collector"
              accent="cyan"
              onClick={() => setChatOpen(true)}
            />
            <NavItem
              icon={<Sparkles className="w-4 h-4" />}
              label="The Harvester"
              accent="cyan"
            />
          </nav>

          {/* Sidebar Footer */}
          <div className="p-4 border-t border-nexus-border">
            <button className="btn-primary w-full text-sm flex items-center justify-center gap-2">
              <Crown className="w-4 h-4" />
              Upgrade to Premium
            </button>
          </div>
        </div>
      </aside>

      {/* ── Main Content Area ──────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Navbar */}
        <header className="h-16 bg-nexus-surface/80 backdrop-blur-lg border-b border-nexus-border flex items-center px-5 gap-4 flex-shrink-0 z-20">
          {/* Menu Toggle */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="btn-ghost p-2"
            aria-label="Toggle sidebar"
          >
            {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>

          {/* Search Bar */}
          <div className="flex-1 max-w-xl relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-nexus-muted" />
            <input
              type="text"
              placeholder="Search games, mods, assets..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input-nexus w-full pl-10 text-sm"
            />
          </div>

          {/* Right Side Actions */}
          <div className="flex items-center gap-3 ml-auto">
            <button
              onClick={() => setChatOpen(!chatOpen)}
              className="btn-ai text-sm flex items-center gap-2"
            >
              <Bot className="w-4 h-4" />
              <span className="hidden sm:inline">Ask The Collector</span>
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>

      {/* ── The Collector AI Chat Widget ───────────────────────── */}
      <AiChatWidget isOpen={chatOpen} onClose={() => setChatOpen(false)} />
    </div>
  );
}

// ── Sidebar Navigation Item ──────────────────────────────────────

interface NavItemProps {
  icon: ReactNode;
  label: string;
  active?: boolean;
  accent?: 'orange' | 'cyan';
  onClick?: () => void;
}

function NavItem({ icon, label, active, accent, onClick }: NavItemProps) {
  const baseClasses =
    'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 cursor-pointer';

  const stateClasses = active
    ? 'bg-accent-orange/10 text-accent-orange'
    : accent === 'cyan'
    ? 'text-accent-cyan/80 hover:text-accent-cyan hover:bg-accent-cyan/5'
    : 'text-gray-400 hover:text-white hover:bg-nexus-elevated';

  return (
    <button className={`${baseClasses} ${stateClasses} w-full text-left`} onClick={onClick}>
      {icon}
      {label}
    </button>
  );
}
