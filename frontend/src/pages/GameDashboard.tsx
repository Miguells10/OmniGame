import { useState, useEffect } from 'react';
import {
  Download,
  Shield,
  Star,
  TrendingUp,
  Gamepad2,
  Flame,
  ChevronRight,
  Filter,
} from 'lucide-react';
import type { Game } from '../shared/types';
import { fetchGames } from '../shared/api';

/**
 * GameDashboard — Main landing page (Nexus-style game catalog)
 *
 * Features:
 * - Hero section with trending mods banner
 * - Filter sidebar with entity type chips
 * - Responsive grid of game cards with cover art, entity counts, and stats
 */
export function GameDashboard() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState<string>('all');

  useEffect(() => {
    loadGames();
  }, []);

  async function loadGames() {
    try {
      const response = await fetchGames(0, 20);
      setGames(response.content);
    } catch {
      // In development without backend, show mock data
      setGames(MOCK_GAMES);
    } finally {
      setLoading(false);
    }
  }

  const filters = ['all', 'MOD', 'PATCH', 'ASSET', 'TOOL'];

  return (
    <div className="max-w-[1400px] mx-auto space-y-8 animate-fade-in">
      {/* ── Hero Section ────────────────────────────────────── */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-nexus-surface via-nexus-elevated to-nexus-surface border border-nexus-border">
        <div className="absolute inset-0 bg-gradient-to-r from-accent-orange/5 via-transparent to-accent-cyan/5" />
        <div className="relative px-8 py-10 md:py-14">
          <div className="flex items-start justify-between">
            <div className="space-y-4 max-w-2xl">
              <div className="flex items-center gap-2">
                <Flame className="w-5 h-5 text-accent-orange" />
                <span className="text-accent-orange font-semibold text-sm uppercase tracking-wider">
                  Trending Now
                </span>
              </div>
              <h2 className="text-3xl md:text-4xl font-bold text-white leading-tight">
                Discover & Manage Mods{' '}
                <span className="text-gradient-orange">Intelligently</span>
              </h2>
              <p className="text-nexus-muted text-lg leading-relaxed">
                Browse thousands of mods, patches, and assets across all your
                favorite games. Let{' '}
                <span className="text-accent-cyan font-medium">The Collector</span>{' '}
                resolve conflicts and guide your load order with AI precision.
              </p>
              <div className="flex gap-3 pt-2">
                <button className="btn-primary flex items-center gap-2">
                  <TrendingUp className="w-4 h-4" />
                  Explore Trending
                </button>
                <button className="btn-ai flex items-center gap-2">
                  <Gamepad2 className="w-4 h-4" />
                  Browse All Games
                </button>
              </div>
            </div>

            {/* Stats */}
            <div className="hidden lg:flex gap-8 pt-4">
              <StatCard value="60K+" label="Total Mods" icon={<Download className="w-5 h-5" />} />
              <StatCard value="5" label="Games" icon={<Gamepad2 className="w-5 h-5" />} />
              <StatCard value="99.2%" label="Safe Files" icon={<Shield className="w-5 h-5" />} />
            </div>
          </div>
        </div>
      </section>

      {/* ── Filters ─────────────────────────────────────────── */}
      <section className="flex items-center gap-3 flex-wrap">
        <Filter className="w-4 h-4 text-nexus-muted" />
        {filters.map((filter) => (
          <button
            key={filter}
            onClick={() => setActiveFilter(filter)}
            className={activeFilter === filter ? 'chip-active' : 'chip'}
          >
            {filter === 'all' ? 'All Categories' : filter}
          </button>
        ))}
      </section>

      {/* ── Game Catalog Grid ───────────────────────────────── */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-bold text-white">
            Game Catalog
            <span className="text-nexus-muted font-normal text-base ml-2">
              ({games.length} games)
            </span>
          </h3>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
            {Array.from({ length: 8 }).map((_, i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
            {games.map((game) => (
              <GameCard key={game.id} game={game} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

// ── Game Card Component ──────────────────────────────────────────

function GameCard({ game }: { game: Game }) {
  return (
    <div className="nexus-card group cursor-pointer" id={`game-card-${game.slug}`}>
      {/* Cover Image */}
      <div className="relative aspect-[3/4] overflow-hidden bg-nexus-elevated">
        {game.coverUrl ? (
          <img
            src={game.coverUrl}
            alt={game.name}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <Gamepad2 className="w-12 h-12 text-nexus-muted" />
          </div>
        )}

        {/* Overlay on hover */}
        <div className="absolute inset-0 bg-gradient-to-t from-nexus-bg/90 via-nexus-bg/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
          <div className="absolute bottom-4 left-4 right-4">
            <button className="btn-primary w-full text-sm flex items-center justify-center gap-2">
              Explore
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Entity Count Badge */}
        <div className="absolute top-3 right-3">
          <span className="badge bg-nexus-bg/80 backdrop-blur-sm text-white border border-nexus-border">
            <Download className="w-3 h-3" />
            {game.entityCount.toLocaleString()}
          </span>
        </div>
      </div>

      {/* Card Info */}
      <div className="p-4 space-y-2">
        <h4 className="font-semibold text-white group-hover:text-accent-orange transition-colors line-clamp-1">
          {game.name}
        </h4>
        <p className="text-xs text-nexus-muted line-clamp-2 leading-relaxed">
          {game.description || 'No description available.'}
        </p>
        <div className="flex items-center gap-2 pt-1">
          <span className="seal-audited">
            <Shield className="w-3 h-3" />
            Audited
          </span>
          <span className="badge bg-nexus-elevated text-nexus-muted">
            <Star className="w-3 h-3" />
            Popular
          </span>
        </div>
      </div>
    </div>
  );
}

// ── Stat Card ────────────────────────────────────────────────────

function StatCard({
  value,
  label,
  icon,
}: {
  value: string;
  label: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="text-center space-y-1">
      <div className="text-accent-cyan mx-auto mb-1">{icon}</div>
      <p className="text-2xl font-bold text-white">{value}</p>
      <p className="text-xs text-nexus-muted">{label}</p>
    </div>
  );
}

// ── Skeleton Loading Card ────────────────────────────────────────

function SkeletonCard() {
  return (
    <div className="bg-nexus-surface border border-nexus-border rounded-xl overflow-hidden animate-pulse">
      <div className="aspect-[3/4] bg-nexus-elevated" />
      <div className="p-4 space-y-3">
        <div className="h-4 bg-nexus-elevated rounded w-3/4" />
        <div className="h-3 bg-nexus-elevated rounded w-full" />
        <div className="h-3 bg-nexus-elevated rounded w-2/3" />
      </div>
    </div>
  );
}

// ── Mock Data (Development without backend) ──────────────────────

const MOCK_GAMES: Game[] = [
  {
    id: '1',
    name: 'The Elder Scrolls V: Skyrim',
    slug: 'skyrim',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tnw.webp',
    description: 'The legendary open-world RPG. Massive modding community with 60,000+ mods.',
    entityCount: 61234,
    createdAt: new Date().toISOString(),
  },
  {
    id: '2',
    name: 'Minecraft',
    slug: 'minecraft',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co49x5.webp',
    description: 'The sandbox phenomenon. Supports Forge, Fabric, and Bukkit mod loaders.',
    entityCount: 148000,
    createdAt: new Date().toISOString(),
  },
  {
    id: '3',
    name: 'Elden Ring',
    slug: 'elden-ring',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.webp',
    description: "FromSoftware's open-world masterpiece. Growing modding scene.",
    entityCount: 3420,
    createdAt: new Date().toISOString(),
  },
  {
    id: '4',
    name: 'Cyberpunk 2077',
    slug: 'cyberpunk-2077',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4hku.webp',
    description: 'Night City dystopian RPG. Active modding with REDmod support.',
    entityCount: 8750,
    createdAt: new Date().toISOString(),
  },
  {
    id: '5',
    name: 'Stardew Valley',
    slug: 'stardew-valley',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/xrpmydnu9rpxvxfjkiu7.webp',
    description: 'Beloved farming simulator. SMAPI-powered modding ecosystem.',
    entityCount: 14200,
    createdAt: new Date().toISOString(),
  },
  {
    id: '6',
    name: 'Baldur\'s Gate 3',
    slug: 'baldurs-gate-3',
    coverUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co670h.webp',
    description: 'Larian\'s D&D RPG epic. Rapidly growing mod community.',
    entityCount: 5600,
    createdAt: new Date().toISOString(),
  },
];
