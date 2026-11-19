---
name: Mindful Habit Flow
colors:
  surface: '#0f131d'
  surface-dim: '#0f131d'
  surface-bright: '#353944'
  surface-container-lowest: '#0a0e18'
  surface-container-low: '#171b26'
  surface-container: '#1c1f2a'
  surface-container-high: '#262a35'
  surface-container-highest: '#313540'
  on-surface: '#dfe2f1'
  on-surface-variant: '#c7c4d7'
  inverse-surface: '#dfe2f1'
  inverse-on-surface: '#2c303b'
  outline: '#908fa0'
  outline-variant: '#464554'
  surface-tint: '#c0c1ff'
  primary: '#c0c1ff'
  on-primary: '#1000a9'
  primary-container: '#8083ff'
  on-primary-container: '#0d0096'
  inverse-primary: '#494bd6'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffb95f'
  on-tertiary: '#472a00'
  tertiary-container: '#ca8100'
  on-tertiary-container: '#3e2400'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#0f131d'
  on-background: '#dfe2f1'
  surface-variant: '#313540'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: -0.005em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.25rem
  margin: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system expresses calm intentionality, clinical focus, and quiet digital mastery. Built for personal habit telemetry and deep-work rituals, it rejects chaotic gamification in favor of a sleek, dark-slate sanctuary where daily discipline feels atmospheric and luminous.

### Design Movement
- **Atmospheric Dark Precision:** Deep obsidian canvas layers juxtaposed with restrained bioluminescent glows, high-clarity geometry, and surgical spatial rhythm.
- **Tone:** Meditative, refined, intelligent, resolute.
- **Emotional Response:** Controlled calm, intrinsic motivation, reduced eye strain, and clear spatial separation between high-priority rituals and observational metrics.

## Colors

The palette operates in strict hierarchy against deep space surfaces, giving semantic meaning to luminous accents without visual fatigue.

### Color Tokens & Semantic Roles
- **Base Canvas (`#0B0F19`):** Ground zero background canvas; limits photopic glare during dawn and midnight reviews.
- **Surface Elevation 1 (`#111827`):** Recessed rails, secondary card containers, and quiet module segments.
- **Surface Elevation 2 (`#182234`):** Active card backgrounds, elevated dashboard tiles, and modal structures.
- **Surface Elevation 3 (`#1E293B`):** Hover states, interactive item backdrops, and active toggle nodes.
- **Subtle Stroke (`#334155`):** Structural borders (0.5px to 1px) used to maintain architectural crispness on OLED screens.
- **Primary Electric Indigo (`#6366F1`):** Primary interactions, core focus timers, active states, and focal glows.
- **Secondary Emerald (`#10B981`):** Shielded states, protected intervals, positive habit completions, and baseline stability metrics.
- **Tertiary Amber (`#F59E0B`):** Habit streaks, compounding consistency runs, time warnings, and energetic momentum indicators.
- **High-Contrast Text (`#F8FAFC`):** Primary headings, numerical metrics, and active states.
- **Muted Metadata (`#94A3B8`):** Body typography, contextual notes, secondary timestamps, and unselected tab labels.

## Typography

Typography prioritizes fast numerical legibility and uncluttered hierarchy. `Plus Jakarta Sans` provides open apertures and clean geometric terminal strokes that remain legible against dark containers.

### Typographic Principles
- Headings use negative letter-spacing (`-0.01em` to `-0.02em`) to bind titles together tightly.
- Numeric counters, streak values, and digital clock metrics inherit semi-bold and bold weights with tabular lining figures enabled.
- Micro-labels (`label-sm`) default to uppercase styling with expanded tracking (`0.04em`) to establish crisp metadata anchors without distracting from core values.

## Layout & Spacing

The spatial architecture utilizes a 12-column responsive grid on desktop viewports and a single-column stacked layout on mobile devices.

### Grid & Spatial Matrix
- **Desktop (1024px+):** 12-column fluid grid, `1.5rem` (24px) gutters, and up to `2.5rem` page margins bounded by a max container width of 1280px.
- **Tablet (768px - 1023px):** 8-column layout, `1.25rem` (20px) gutters, `1.5rem` margins.
- **Mobile (<768px):** 4-column layout or flex-stack, `1rem` (16px) margins and gutters.

### Rhythmic Rules
- **Micro Spacing (`space-xs`, `space-sm`):** Binds icons to labels, badges to titles, and subtext inside list rows.
- **Structural Padding (`space-md`, `space-lg`):** Encapsulates habit cards, metrics panels, and modal shells.
- **Section Breaks (`space-xl`):** Separates distinct telemetry clusters (e.g., active daily rituals vs. weekly streak projections).

## Elevation & Depth

Depth is established through dark luminous stacking rather than heavy directional drop-shadows. The obsidian floor recedes while active elements project soft, targeted photonic energy.

### Elevation Tiers
- **Tier 0 (Base Canvas):** Solid `#0B0F19`. Zero elevation.
- **Tier 1 (Recessed Modules):** `#111827` bordered with a 1px solid hairline of `#334155` (30% opacity).
- **Tier 2 (Floating Cards & Dashboard Panels):** `#182234` with 1px border of `#334155` (50% opacity) and an ambient shadow: `0 8px 32px -8px rgba(0, 0, 0, 0.5)`.
- **Tier 3 (Active Focus States & Modals):** `#1E293B` surrounded by a soft perimeter glow:
  - **Indigo Focus Glow:** `0 0 24px -4px rgba(99, 102, 241, 0.25), 0 8px 16px -4px rgba(0, 0, 0, 0.6)`.
  - **Emerald Protection Glow:** `0 0 20px -4px rgba(16, 185, 129, 0.25)`.
  - **Amber Streak Glow:** `0 0 20px -4px rgba(245, 158, 11@, 0.25)`.

## Shapes

The design system incorporates modern 20px outer corner treatments for primary dashboard cards, paired with nested geometric reductions to maintain concentric rhythm across sub-components.

### Shape Tokens & Application
- **Primary Shells & Cards:** `1.25rem` (20px) — Defines all main habit containers, timer tiles, and modal structures.
- **Nested Inner Items & Lists:** `0.75rem` (12px) — Used for internal habit rows, input zones, and nested action states.
- **Interactive Controls (Buttons & Selectors):** `0.625rem` (10px) — Crisp actionable radius that feels structured yet tactile.
- **Tags, Chips, & Badges:** `9999px` (Pill) — Fully rounded to demarcate categorical metadata from rectangular structural containers.

## Components

### Buttons
- **Primary Action (Electric Indigo):** Solid background of `#6366F1`, text `#FFFFFF`, rounded to 10px. Interactive hover initiates a soft drop-bloom: `box-shadow: 0 0 16px rgba(99, 102, 241, 0.4)`.
- **Secondary (Ghost Container):** Background `#1E293B`, 1px border `#334155`, text `#F8FAFC`. Active state transitions border to `#6366F1`.
- **Destructive/Break Timer:** Dark crimson tint background with muted red border and label.

### Cards (20px Radius)
- Main cards feature background `#182234`, border 1px `#334155`, padding `1.5rem` (`space-lg`), and `border-radius: 20px`.
- Optional subtle gradient overlay at the top edge (`linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0) 100%)`) to create glass-like rim illumination.

### Chips & Badges
- **Habit Streaks:** Pill shape, background `rgba(245, 158, 11, 0.12)`, border `1px solid rgba(245, 158, 11, 0.3)`, text `#F59E0B`. Accompanied by a spark/flame vector glyph.
- **Protection Shield:** Pill shape, background `rgba(16, 185, 129, 0.12)`, border `1px solid rgba(16, 185, 129, 0.3)`, text `#10B981`.

### Lists & Habit Rows
- Contained within cards using alternating or hoverable rows.
- Height: 60px default; padding `0 1rem`; border-radius 12px; background transparent with hover transitioning to `#1E293B`.
- Left-aligned title with right-aligned metadata counter and interactive status trigger.

### Checkboxes & Completion Rings
- Habit completion utilizes an SVG ring or circular checkbox (24px diameter).
- Incomplete: Border 2px solid `#334155`, interior transparent.
- Complete: Background `#10B981`, border `#10B981`, containing a white checkmark with a localized emerald bloom (`0 0 10px rgba(16, 185, 129, 0.4)`).

### Input Fields
- Background `#111827`, border 1px solid `#334155`, border-radius 10px, text `#F8FAFC`, placeholder text `#64748B`.
- Focus state: Border shifts to `#6366F1` with an inner and outer ambient glow (`0 0 0 3px rgba(99, 102, 241, 0.2)`).

### Habit Progress Telemetry (Custom Specialized Component)
- Track background `#1E293B` (6px height, rounded pill).
- Progress fill: Gradient from `#6366F1` to `#10B981` ending in an intensified glow beacon at the leading edge.