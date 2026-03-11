import { useState, useRef, useEffect, useCallback } from 'react';
import {
  Bot,
  X,
  Send,
  Sparkles,
  Loader2,
  Zap,
  Minimize2,
  Maximize2,
  Terminal,
} from 'lucide-react';
import type { ChatMessage } from '../shared/types';
import { streamCollectorChat } from '../shared/api';

interface AiChatWidgetProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * The Collector — AI Chat Widget
 *
 * Glassmorphism slide-over panel with terminal-style dark chat interface.
 * Features SSE streaming response rendering, context awareness badges,
 * and multi-turn conversation support.
 */
export function AiChatWidget({ isOpen, onClose }: AiChatWidgetProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [currentGame, setCurrentGame] = useState('skyrim');
  const [isExpanded, setIsExpanded] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Focus input when opened
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 300);
    }
  }, [isOpen]);

  const handleSend = useCallback(async () => {
    const trimmed = input.trim();
    if (!trimmed || isStreaming) return;

    const userMessage: ChatMessage = {
      role: 'user',
      content: trimmed,
      timestamp: Date.now(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsStreaming(true);

    // Create placeholder for streaming response
    const assistantMessage: ChatMessage = {
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
    };
    setMessages((prev) => [...prev, assistantMessage]);

    try {
      const stream = streamCollectorChat({
        gameSlug: currentGame,
        message: trimmed,
        conversationHistory: messages.slice(-10), // Last 10 messages for context
      });

      let fullContent = '';
      for await (const token of stream) {
        fullContent += token;
        setMessages((prev) => {
          const updated = [...prev];
          const lastMsg = updated[updated.length - 1];
          if (lastMsg?.role === 'assistant') {
            updated[updated.length - 1] = { ...lastMsg, content: fullContent };
          }
          return updated;
        });
      }
    } catch {
      // Demo mode: simulate a response when backend is not available
      const demoResponse = generateDemoResponse(trimmed, currentGame);
      let fullContent = '';
      for (const char of demoResponse) {
        fullContent += char;
        const captured = fullContent;
        await new Promise((r) => setTimeout(r, 15));
        setMessages((prev) => {
          const updated = [...prev];
          const lastMsg = updated[updated.length - 1];
          if (lastMsg?.role === 'assistant') {
            updated[updated.length - 1] = { ...lastMsg, content: captured };
          }
          return updated;
        });
      }
    } finally {
      setIsStreaming(false);
    }
  }, [input, isStreaming, messages, currentGame]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const panelWidth = isExpanded ? 'w-[600px]' : 'w-[420px]';

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40 transition-opacity duration-300"
          onClick={onClose}
        />
      )}

      {/* Chat Panel */}
      <div
        className={`fixed top-0 right-0 h-full ${panelWidth} z-50 transition-transform duration-300 ease-out ${
          isOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        <div className="h-full glass-panel rounded-l-2xl flex flex-col border-r-0 rounded-r-none">
          {/* ── Header ──────────────────────────────────────── */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-nexus-border/50">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-accent-cyan to-blue-500 flex items-center justify-center shadow-lg shadow-accent-cyan/20 animate-glow">
                <Bot className="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 className="font-bold text-white text-sm flex items-center gap-1.5">
                  The Collector
                  <Sparkles className="w-3.5 h-3.5 text-accent-cyan" />
                </h3>
                <p className="text-[11px] text-nexus-muted">
                  RAG-Powered Modding Expert
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="btn-ghost p-2 text-nexus-muted hover:text-white"
                aria-label={isExpanded ? 'Minimize' : 'Maximize'}
              >
                {isExpanded ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
              </button>
              <button
                onClick={onClose}
                className="btn-ghost p-2 text-nexus-muted hover:text-white"
                aria-label="Close chat"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* ── Context Bar ─────────────────────────────────── */}
          <div className="px-5 py-2.5 bg-nexus-bg/50 border-b border-nexus-border/30 flex items-center gap-2">
            <Terminal className="w-3.5 h-3.5 text-accent-cyan" />
            <span className="text-[11px] text-nexus-muted">Context:</span>
            <select
              value={currentGame}
              onChange={(e) => setCurrentGame(e.target.value)}
              className="text-[11px] bg-nexus-elevated border border-nexus-border rounded px-2 py-0.5 text-accent-cyan focus:outline-none focus:border-accent-cyan/50"
            >
              <option value="skyrim">Skyrim</option>
              <option value="minecraft">Minecraft</option>
              <option value="elden-ring">Elden Ring</option>
              <option value="cyberpunk-2077">Cyberpunk 2077</option>
              <option value="stardew-valley">Stardew Valley</option>
            </select>
            <span className="text-[11px] text-accent-green flex items-center gap-1 ml-auto">
              <Zap className="w-3 h-3" />
              Online
            </span>
          </div>

          {/* ── Messages Area ───────────────────────────────── */}
          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
            {messages.length === 0 && <WelcomeMessage />}

            {messages.map((msg, idx) => (
              <MessageBubble key={idx} message={msg} isStreaming={isStreaming && idx === messages.length - 1} />
            ))}

            <div ref={messagesEndRef} />
          </div>

          {/* ── Input Area ──────────────────────────────────── */}
          <div className="px-5 py-4 border-t border-nexus-border/50">
            <div className="flex items-center gap-2">
              <input
                ref={inputRef}
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask about mods, load orders, conflicts..."
                className="input-nexus flex-1 text-sm bg-nexus-bg/80"
                disabled={isStreaming}
              />
              <button
                onClick={handleSend}
                disabled={!input.trim() || isStreaming}
                className="btn-ai p-2.5 rounded-lg"
                aria-label="Send message"
              >
                {isStreaming ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Send className="w-4 h-4" />
                )}
              </button>
            </div>
            <p className="text-[10px] text-nexus-muted mt-2 text-center">
              Powered by RAG + pgvector • Responses based on OmniGame knowledge base
            </p>
          </div>
        </div>
      </div>
    </>
  );
}

// ── Message Bubble ───────────────────────────────────────────────

function MessageBubble({
  message,
  isStreaming,
}: {
  message: ChatMessage;
  isStreaming: boolean;
}) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} animate-slide-up`}>
      <div
        className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
          isUser
            ? 'bg-accent-orange/15 text-gray-100 border border-accent-orange/20 rounded-br-md'
            : 'bg-nexus-bg border border-nexus-border rounded-bl-md font-mono text-gray-300'
        }`}
      >
        {!isUser && (
          <div className="flex items-center gap-1.5 mb-1.5">
            <Bot className="w-3.5 h-3.5 text-accent-cyan" />
            <span className="text-[10px] text-accent-cyan font-semibold uppercase tracking-wider">
              The Collector
            </span>
          </div>
        )}
        <div className="whitespace-pre-wrap">
          {message.content}
          {isStreaming && (
            <span className="inline-block w-1.5 h-4 bg-accent-cyan ml-0.5 animate-pulse" />
          )}
        </div>
      </div>
    </div>
  );
}

// ── Welcome Message ──────────────────────────────────────────────

function WelcomeMessage() {
  return (
    <div className="text-center py-12 space-y-4 animate-fade-in">
      <div className="w-16 h-16 mx-auto rounded-2xl bg-gradient-to-br from-accent-cyan to-blue-600 flex items-center justify-center shadow-xl shadow-accent-cyan/20 animate-glow">
        <Bot className="w-8 h-8 text-white" />
      </div>
      <div>
        <h4 className="text-lg font-bold text-white">The Collector</h4>
        <p className="text-nexus-muted text-sm mt-1 max-w-xs mx-auto">
          Your AI modding expert. Ask about load orders, mod conflicts,
          compatibility, or any technical question.
        </p>
      </div>
      <div className="space-y-2 pt-2">
        <SuggestionChip text="How do I fix the Skyrim load order crash?" />
        <SuggestionChip text="Best mods for Minecraft performance?" />
        <SuggestionChip text="Is SkyUI compatible with USSEP?" />
      </div>
    </div>
  );
}

function SuggestionChip({ text }: { text: string }) {
  return (
    <button className="chip text-[11px] px-3 py-1.5 mx-1 hover:text-accent-cyan hover:border-accent-cyan/30">
      {text}
    </button>
  );
}

// ── Demo Response Generator ──────────────────────────────────────

function generateDemoResponse(query: string, game: string): string {
  const gameNames: Record<string, string> = {
    skyrim: 'Skyrim',
    minecraft: 'Minecraft',
    'elden-ring': 'Elden Ring',
    'cyberpunk-2077': 'Cyberpunk 2077',
    'stardew-valley': 'Stardew Valley',
  };

  const gameName = gameNames[game] || game;

  return `**The Collector** — Analysis for **${gameName}**

Based on the OmniGame knowledge base, here's what I found regarding your query:

> "${query}"

**Key findings:**
1. This is a common question in the ${gameName} modding community
2. The recommended approach involves checking your load order sequence
3. Always verify mod compatibility using the OmniGame Security Audit

**Recommended next steps:**
- Check the conflict resolution tab for known incompatibilities
- Review the load order guide in the ${gameName} wiki section
- Run the Security Auditor on any new mods before installation

⚡ *This is a demo response. Connect to the backend for RAG-powered answers.*`;
}
