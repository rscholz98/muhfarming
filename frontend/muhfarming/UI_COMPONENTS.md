# UI Components Overview

## Color Scheme (SAP Fiori Design)

### Primary Colors
- **Fiori Blue** (#0070F2) - Primary brand color
- **Light Blue** (#5899DA) - Secondary elements
- **Dark Blue** (#003F8F) - Accent color
- **White** (#FFFFFF) - Cards and surfaces
- **Light Gray** (#F5F6F7) - Background

### Status Colors
- **Success Green** (#107E3E)
- **Warning Orange** (#E76500)
- **Error Red** (#BB0000)
- **Info Blue** (#0070F2)

### Severity Colors (Pest Management)
- **High Severity** (#BB0000) - Red
- **Medium Severity** (#E76500) - Orange
- **Low Severity** (#F8B000) - Yellow

## Screen Layouts

### 1. Weather Forecast Screen
**Components:**
- Top App Bar (Blue background, white text)
- Location Card (Large blue card with location name)
- Hourly Forecast Section
  - Horizontal scrollable cards
  - Each card shows: time, temperature, condition, humidity, wind speed
- 7-Day Forecast Section
  - Vertical list of daily forecast cards
  - Shows: day, condition, high/low temp, precipitation

**Card Style:**
- Rounded corners (12dp)
- White background
- 2dp elevation
- 16dp padding

### 2. Pest Management Screen
**Components:**
- Top App Bar (Blue background, white text)
- Info Card (Light blue background with info icon)
- Pest List (Vertical scrollable)
  - Each card contains:
    - Pest name (bold, large)
    - Severity badge (color-coded)
    - Description
    - Season tag with calendar icon

**Severity Badge:**
- Rounded pill shape (16dp radius)
- Bold uppercase text
- High: Red background, white text
- Medium: Orange background, white text
- Low: Yellow background, black text

### 3. Settings Screen
**Components:**
- Top App Bar (Blue background, white text)
- Settings Cards
  - Language Setting (with globe icon)
  - Location Setting (with location pin icon)
- About Section
  - App Version card
  - Build info card

**Interactive Elements:**
- Clickable cards with chevron (›)
- Modal dialogs for selections
- Radio button selections (blue when selected)

## Navigation

### Bottom Navigation Bar
- White background
- Three tabs with icons and labels
- Blue indicator for selected tab
- Icons: ☀️ Weather, 🐛 Pests, ⚙️ Settings

**Selected State:**
- Blue icon and text color
- Light blue background indicator

**Unselected State:**
- Dark gray icon and text color

## Typography

Using Material Design 3 default typography with adjustments:
- Headlines: Bold weight
- Titles: Bold weight
- Body text: Regular weight
- Labels: Medium weight

## Spacing

- Screen padding: 16dp horizontal
- Card spacing: 12-16dp
- Internal padding: 12-20dp depending on component
- Element spacing: 4-8dp

## Interactive States

### Buttons
- Primary buttons: Blue background, white text
- Rounded corners (8dp)
- Bold text

### Cards
- Default: White background, 2dp elevation
- Hover/Press: (Material3 handles automatically)

### Dialogs
- Rounded corners (16dp)
- White background
- Blue accent for buttons and selections

## Loading & Error States

### Loading
- Centered circular progress indicator
- Blue color

### Error
- Centered icon (⚠️ or specific to context)
- Error message in red
- Blue "Retry" button

## Accessibility

- High contrast between text and backgrounds
- Touch targets meet minimum size requirements
- Descriptive labels for all interactive elements
- Support for screen readers through semantic Compose components
