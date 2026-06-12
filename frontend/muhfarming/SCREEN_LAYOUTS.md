# App Screen Layouts - Visual Guide

## 🎨 Color Reference
- **Primary Blue**: #0070F2 (SAP Fiori Blue)
- **White**: #FFFFFF
- **Light Gray Background**: #F5F6F7
- **Dark Gray Text**: #6A6D70
- **Black Text**: #32363A

---

## 📱 Screen 1: Weather Forecast

```
┌──────────────────────────────────────┐
│   ☀️ Weather Forecast         [Blue] │  ← Top App Bar (Blue #0070F2)
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │        CAMEROON          [Blue]│  │  ← Location Card (Blue)
│  └────────────────────────────────┘  │
│                                      │
│  Hourly Forecast                     │
│                                      │
│  ┌───────┐ ┌───────┐ ┌───────┐     │
│  │12:00  │ │15:00  │ │18:00  │ →   │  ← Horizontal Scroll
│  │       │ │       │ │       │     │
│  │ 28°C  │ │ 31°C  │ │ 29°C  │     │
│  │ Sunny │ │Cloudy │ │Cloudy │     │
│  │       │ │       │ │       │     │
│  │💧65%  │ │💧58%  │ │💧70%  │     │
│  │💨12   │ │💨15   │ │💨10   │     │
│  └───────┘ └───────┘ └───────┘     │
│                                      │
│  7-Day Forecast                      │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ Today          💧10%   32° 22° │  │  ← Daily Card
│  │ Sunny                          │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ Friday         💧20%   31° 23° │  │
│  │ Partly Cloudy                  │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ Saturday       💧40%   30° 22° │  │
│  │ Cloudy                         │  │
│  └────────────────────────────────┘  │
│                                      │
│  [More days...]                      │
│                                      │
├──────────────────────────────────────┤
│   ☀️      🐛       ⚙️         [Nav]  │  ← Bottom Navigation
│ Weather  Pests  Settings            │
└──────────────────────────────────────┘
```

---

## 📱 Screen 2: Pest Management

```
┌──────────────────────────────────────┐
│   🐛 Pest Management          [Blue] │  ← Top App Bar
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ ℹ️  Pest Information           │  │  ← Info Card (Light Blue BG)
│  │    Monitor pests by season     │  │
│  │    and severity                │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ Aphids              [🔴 HIGH]  │  │  ← Pest Card
│  │                                │  │
│  │ Small sap-sucking insects      │  │
│  │ that damage leaves and stems.  │  │
│  │                                │  │
│  │ ─────────────────────────────  │  │  ← Divider
│  │                                │  │
│  │ 📅 Season: Spring              │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ Spider Mites      [🟠 MEDIUM] │  │
│  │                                │  │
│  │ Tiny arachnids that cause      │  │
│  │ leaf discoloration & webbing.  │  │
│  │                                │  │
│  │ ─────────────────────────────  │  │
│  │                                │  │
│  │ 📅 Season: Summer              │  │
│  └────────────────────────────────┘  │
│                                      │
│  [More pests...]                     │
│                                      │
├──────────────────────────────────────┤
│   ☀️      🐛       ⚙️         [Nav]  │
│ Weather  Pests  Settings            │
└──────────────────────────────────────┘
```

**Severity Badge Colors:**
- 🔴 HIGH = Red (#BB0000)
- 🟠 MEDIUM = Orange (#E76500)
- 🟡 LOW = Yellow (#F8B000)

---

## 📱 Screen 3: Settings

```
┌──────────────────────────────────────┐
│   ⚙️ Settings                  [Blue] │  ← Top App Bar
├──────────────────────────────────────┤
│                                      │
│  Preferences                         │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 🌍  Language             ›     │  │  ← Setting Card (Clickable)
│  │     English                    │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 📍  Location             ›     │  │
│  │     Cameroon                   │  │
│  └────────────────────────────────┘  │
│                                      │
│  About                               │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ ℹ️  App Version          1.0.0 │  │  ← Info Card
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 📱  Build         2026.06.12   │  │
│  └────────────────────────────────┘  │
│                                      │
│                                      │
│                                      │
├──────────────────────────────────────┤
│   ☀️      🐛       ⚙️         [Nav]  │
│ Weather  Pests  Settings            │
└──────────────────────────────────────┘
```

---

## 💬 Dialog: Language Selection

```
        ┌──────────────────────┐
        │ Select Language      │
        ├──────────────────────┤
        │                      │
        │ ◉ English            │  ← Selected (Blue)
        │ ○ French             │
        │ ○ Arabic             │
        │ ○ Spanish            │
        │                      │
        ├──────────────────────┤
        │            [Cancel]  │  ← Blue text button
        └──────────────────────┘
```

---

## 💬 Dialog: Location Selection

```
        ┌────────────────────────┐
        │ Select Location        │
        ├────────────────────────┤
        │                        │
        │ ◉ Cameroon             │  ← Selected (Blue)
        │ ○ Yaoundé, Cameroon    │
        │ ○ Douala, Cameroon     │
        │ ○ Garoua, Cameroon     │
        │ ○ Bamenda, Cameroon    │
        │                        │
        ├────────────────────────┤
        │              [Cancel]  │
        └────────────────────────┘
```

---

## 🔄 Loading State

```
┌──────────────────────────────────────┐
│   Screen Title                [Blue] │
├──────────────────────────────────────┤
│                                      │
│                                      │
│                                      │
│              ⏳                       │  ← Blue Spinner
│            Loading...                │
│                                      │
│                                      │
│                                      │
│                                      │
├──────────────────────────────────────┤
│   Navigation Bar                     │
└──────────────────────────────────────┘
```

---

## ❌ Error State

```
┌──────────────────────────────────────┐
│   Screen Title                [Blue] │
├──────────────────────────────────────┤
│                                      │
│                                      │
│              ⚠️                       │  ← Warning Icon
│                                      │
│    Unable to Load Data               │
│    [Error message here]              │
│                                      │
│         ┌──────────┐                 │
│         │  Retry   │  [Blue Button] │
│         └──────────┘                 │
│                                      │
├──────────────────────────────────────┤
│   Navigation Bar                     │
└──────────────────────────────────────┘
```

---

## 🎨 Design Specifications

### Cards
- **Corner Radius**: 12dp
- **Elevation**: 2dp shadow
- **Background**: White (#FFFFFF)
- **Padding**: 16dp internal
- **Margin**: 16dp horizontal, 12dp vertical gap

### Top App Bar
- **Height**: 64dp (standard)
- **Background**: Fiori Blue (#0070F2)
- **Text**: White, Bold

### Bottom Navigation
- **Height**: 80dp (standard)
- **Background**: White
- **Selected Color**: Blue (#0070F2)
- **Unselected Color**: Dark Gray (#6A6D70)
- **Indicator**: Light blue background circle

### Typography
- **Headline Large**: 32sp, Bold
- **Headline Medium**: 28sp, Bold
- **Title Large**: 22sp, Bold
- **Title Medium**: 16sp, Bold
- **Body Large**: 16sp, Regular
- **Body Medium**: 14sp, Regular
- **Label Medium**: 12sp, Medium

### Spacing System
- **Extra Small**: 4dp
- **Small**: 8dp
- **Medium**: 12dp
- **Large**: 16dp
- **Extra Large**: 20dp

### Icons
- **Size**: 24sp for bottom nav
- **Style**: Emoji-based for consistency

---

## 🎯 Interactive Elements

### Buttons
```
┌──────────────┐
│    ACTION    │  ← Blue background (#0070F2)
└──────────────┘     White text, 8dp rounded corners
```

### Radio Buttons
- Selected: Blue circle with blue dot
- Unselected: Gray circle, empty

### Cards (Clickable)
- Normal: White background
- Pressed: Slight gray overlay (Material3 handles)

---

## 📐 Screen Dimensions

- **Screen Width**: Responsive (match_parent)
- **Max Card Width**: Full width minus 32dp (16dp each side)
- **Hourly Card Width**: 120dp fixed
- **Minimum Touch Target**: 48dp × 48dp

---

## 🌈 Color Usage Summary

| Element               | Color                    | Hex Code |
|-----------------------|--------------------------|----------|
| Top App Bar           | Fiori Blue              | #0070F2  |
| Background            | Light Gray              | #F5F6F7  |
| Cards                 | White                   | #FFFFFF  |
| Primary Text          | Black                   | #32363A  |
| Secondary Text        | Dark Gray               | #6A6D70  |
| Selected Nav Item     | Fiori Blue              | #0070F2  |
| Unselected Nav Item   | Dark Gray               | #6A6D70  |
| High Severity Badge   | Red                     | #BB0000  |
| Medium Severity Badge | Orange                  | #E76500  |
| Low Severity Badge    | Yellow                  | #F8B000  |
| Error Text            | Error Red               | #BB0000  |
| Success (future)      | Success Green           | #107E3E  |

---

This layout follows SAP Fiori Design System for Android v24.12 guidelines with bright blue (#0070F2) as the primary color and white (#FFFFFF) as the surface color throughout the application.
