/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // ── Nexus-style Dark Palette ──
        nexus: {
          bg:       '#0f1117',   // Deepest background
          surface:  '#171b24',   // Card / panel backgrounds
          elevated: '#1e2330',   // Elevated surfaces (modals, dropdowns)
          border:   '#2a3041',   // Subtle borders
          muted:    '#6b7a99',   // Muted text, secondary info
        },
        // ── Accent Colors ──
        accent: {
          orange:   '#da8e35',   // "Nexus Orange" — primary CTA
          hover:    '#e9a54c',   // Orange hover state
          cyan:     '#00d4ff',   // AI/Collector accent
          cyanDim:  '#0099b8',   // Cyan muted variant
          green:    '#34d399',   // Success / Security Audited
          red:      '#f87171',   // Error / Warning
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      backdropBlur: {
        xs: '2px',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'glow':       'glow 2s ease-in-out infinite alternate',
        'slide-up':   'slideUp 0.3s ease-out',
        'fade-in':    'fadeIn 0.2s ease-out',
      },
      keyframes: {
        glow: {
          '0%':   { boxShadow: '0 0 5px rgba(0, 212, 255, 0.3)' },
          '100%': { boxShadow: '0 0 20px rgba(0, 212, 255, 0.6)' },
        },
        slideUp: {
          '0%':   { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [],
};
