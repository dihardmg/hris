# iOS Mobile App Template Design System

## Table of Contents
1. [Design System Overview](#design-system-overview)
2. [Color System](#color-system)
3. [Typography](#typography)
4. [Spacing & Layout](#spacing--layout)
5. [Components Library](#components-library)
6. [Navigation Patterns](#navigation-patterns)
7. [Form Templates](#form-templates)
8. [Data Visualization](#data-visualization)
9. [Animation & Transitions](#animation--transitions)
10. [Dark Mode Support](#dark-mode-support)
11. [Accessibility Guidelines](#accessibility-guidelines)

---

## Design System Overview

### Core Principles
- **Clarity**: Clean, uncluttered interfaces with clear visual hierarchy
- **Consistency**: Unified design language across all components
- **Efficiency**: Minimal steps to complete tasks
- **Flexibility**: Scalable system that adapts to different content needs
- **Accessibility**: Inclusive design for all users

### Device Specifications
- **Target Devices**: iPhone 12 and later
- **Screen Sizes**:
  - iPhone 12/13/14: 390 × 844 points
  - iPhone 12/13/14 Plus: 428 × 926 points
  - iPhone 12/13/14 Pro: 393 × 852 points
  - iPhone 12/13/14 Pro Max: 430 × 932 points
- **Safe Areas**: Account for notch, Dynamic Island, and home indicator

---

## Color System

### Primary Colors
```swift
// Brand Colors
static let brandPrimary = UIColor(red: 0.00, green: 0.48, blue: 1.00, alpha: 1.00) // #007AFF
static let brandSecondary = UIColor(red: 0.20, green: 0.60, blue: 1.00, alpha: 1.00) // #3399FF
static let brandAccent = UIColor(red: 0.00, green: 0.38, blue: 0.84, alpha: 1.00) // #0061D6

// Semantic Colors
static let success = UIColor(red: 0.20, green: 0.78, blue: 0.35, alpha: 1.00) // #34C759
static let warning = UIColor(red: 1.00, green: 0.58, blue: 0.00, alpha: 1.00) // #FF9500
static let error = UIColor(red: 1.00, green: 0.23, blue: 0.19, alpha: 1.00) // #FF3B30
static let info = UIColor(red: 0.00, green: 0.48, blue: 1.00, alpha: 1.00) // #007AFF
```

### Neutral Colors
```swift
// Gray Scale
static let gray900 = UIColor(red: 0.06, green: 0.09, blue: 0.13, alpha: 1.00) // #11171C
static let gray800 = UIColor(red: 0.13, green: 0.18, blue: 0.25, alpha: 1.00) // #222E3F
static let gray700 = UIColor(red: 0.20, green: 0.27, blue: 0.38, alpha: 1.00) // #334560
static let gray600 = UIColor(red: 0.38, green: 0.44, blue: 0.53, alpha: 1.00) // #607087
static let gray500 = UIColor(red: 0.56, green: 0.60, blue: 0.67, alpha: 1.00) // #8E99A9
static let gray400 = UIColor(red: 0.72, green: 0.76, blue: 0.80, alpha: 1.00) // #B8C1D1
static let gray300 = UIColor(red: 0.87, green: 0.89, blue: 0.92, alpha: 1.00) // #DEE3EB
static let gray200 = UIColor(red: 0.94, green: 0.95, blue: 0.96, alpha: 1.00) // #EFF2F7
static let gray100 = UIColor(red: 0.98, green: 0.98, blue: 0.98, alpha: 1.00) // #FAFBFC
```

### Background Colors
```swift
// Light Mode
static let primaryBackground = UIColor.systemBackground
static let secondaryBackground = UIColor.secondarySystemBackground
static let tertiaryBackground = UIColor.tertiarySystemBackground
static let groupedBackground = UIColor.systemGroupedBackground

// Dark Mode Colors
static let darkPrimary = UIColor.systemBackground
static let darkSecondary = UIColor.secondarySystemBackground
static let darkTertiary = UIColor.tertiarySystemBackground
```

---

## Typography

### Font Hierarchy
```swift
// Display Fonts
enum DisplayFont {
    case largeTitle    // 34pt, Bold
    case title1        // 28pt, Bold
    case title2        // 22pt, Bold
    case title3        // 20pt, Semibold
}

// Body Fonts
enum BodyFont {
    case headline      // 17pt, Semibold
    case body          // 17pt, Regular
    case callout       // 16pt, Regular
    case subheadline   // 15pt, Regular
    case footnote      // 13pt, Regular
    case caption1      // 12pt, Regular
    case caption2      // 11pt, Regular
}
```

### Typography Usage
```
┌─────────────────────────────────────┐
│  LARGE TITLE                        │ Large Title - 34pt Bold
│  Title 1                            │ Title 1 - 28pt Bold
│  Title 2                            │ Title 2 - 22pt Bold
│  Title 3                            │ Title 3 - 20pt Semibold
│                                     │
│  Headline                           │ Headline - 17pt Semibold
│  Body text regular weight           │ Body - 17pt Regular
│  Callout text                       │ Callout - 16pt Regular
│  Subheadline text                   │ Subheadline - 15pt Regular
│  Footnote text                      │ Footnote - 13pt Regular
│  Caption 1 text                     │ Caption 1 - 12pt Regular
│  Caption 2 text                     │ Caption 2 - 11pt Regular
└─────────────────────────────────────┘
```

---

## Spacing & Layout

### Spacing Scale
```swift
enum Spacing {
    static let xs: CGFloat = 4     // Extra Small
    static let sm: CGFloat = 8     // Small
    static let md: CGFloat = 16    // Medium
    static let lg: CGFloat = 24    // Large
    static let xl: CGFloat = 32    // Extra Large
    static let xxl: CGFloat = 48   // Extra Extra Large
}
```

### Grid System
```
┌─────────────────────────────────────┐
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │ 4-Column Grid
│  │  1  │  │  2  │  │  3  │  │  4  │ │ (8pt gutters)
│  └─────┘  └─────┘  └─────┘  └─────┘ │
│                                     │
│  ┌─────────┐  ┌─────────┐           │ 2-Column Grid
│  │    1    │  │    2    │           │ (16pt gutters)
│  └─────────┘  └─────────┘           │
│                                     │
│  ┌─────────────────────┐           │ Single Column
│  │         1           │           │ (Full width)
│  └─────────────────────┘           │
└─────────────────────────────────────┘
```

### Component Spacing
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ 16pt padding
│  │        Component            │   │
│  │  ┌─────────────────────┐     │   │ 8pt internal padding
│  │  │   Inner Content     │     │   │
│  │  └─────────────────────┘     │   │
│  │                             │   │ 8pt spacing between elements
│  │  ┌─────────────────────┐     │   │
│  │  │   Another Element   │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │ 24pt margin to next component
│                                     │
└─────────────────────────────────────┘
```

---

## Components Library

### 1. Buttons

#### Primary Button
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │      Primary Action         │   │ Height: 50pt
│  └─────────────────────────────┘   │ Background: Brand Primary
│                                     │ Text: White, Semibold
└─────────────────────────────────────┘ Corner Radius: 12pt
```

#### Secondary Button
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │     Secondary Action        │   │ Height: 50pt
│  └─────────────────────────────┘   │ Border: Brand Primary
│                                     │ Text: Brand Primary
└─────────────────────────────────────┘ Corner Radius: 12pt
```

#### Tertiary Button
```
┌─────────────────────────────────────┐
│         Tertiary Action             │ Text Only
│                                     │ Color: Brand Primary
└─────────────────────────────────────┘ Underline on hover
```

#### Destructive Button
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │       Delete Item           │   │ Height: 50pt
│  └─────────────────────────────┘   │ Background: Error
│                                     │ Text: White, Semibold
└─────────────────────────────────────┘ Corner Radius: 12pt
```

#### Button States
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Normal
│  │         Button              │   │ Solid background
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Pressed
│  │      Button (Pressed)       │   │ Darker background
│  └─────────────────────────────┘   │ Slightly smaller
│                                     │
│  ┌─────────────────────────────┐   │ Disabled
│  │      Button (Disabled)      │   │ 50% opacity
│  └─────────────────────────────┘   │ No interaction
└─────────────────────────────────────┘
```

### 2. Cards

#### Standard Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ 16pt padding
│  │        Card Title           │   │ 12pt corner radius
│  │                             │   │ Subtle shadow
│  │  Card content goes here     │   │ White background
│  │  with multiple lines of     │   │
│  │  text and information.      │   │
│  │                             │   │
│  │  ┌─────────────────────┐     │   │
│  │  │   Action Button     │     │   │ 8pt margin from
│  │  └─────────────────────┘     │   │ card edges
│  └─────────────────────────────┘   │ 24pt margin between
│                                     │ cards
└─────────────────────────────────────┘
```

#### Stats Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📊 Metric Name             │   │ Icon + Label
│  │  42                         │   │ Large number
│  │  +5% from last month        │   │ Trend indicator
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  👥 Active Users            │   │
│  │  1,234                      │   │
│  │  ↗️ 12% growth              │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### List Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📋 Recent Activity         │   │ Card Header
│  ├─────────────────────────────┤   │ Divider
│  │  • Item 1                   │   │ List items
│  │  • Item 2                   │   │ with bullets
│  │  • Item 3                   │   │
│  │         [View All]         │   │ Action link
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 3. Input Fields

#### Text Field
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📧 Email Address           │   │ Label with icon
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  user@example.com          │   │ Input field
│  └─────────────────────────────┘   │ 48pt height
│                                     │ 12pt corner radius
│                                     │ 1pt border
└─────────────────────────────────────┘
```

#### Password Field
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  🔒 Password                │   │ Label with icon
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  ••••••••••••            👁️ │   │ Password input
│  └─────────────────────────────┘   │ with eye toggle
│                                     │
│  ✅ Must contain 8+ characters     │ Validation hints
│  ✅ Include uppercase and lowercase │
│  ❌ Missing special character       │
└─────────────────────────────────────┘
```

#### Dropdown/Picker
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  🏖️ Leave Type              │   │ Label with icon
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  Annual Leave (10 days)   ▼ │   │ Selected option
│  └─────────────────────────────┘   │ with dropdown arrow
│                                     │
│  ┌─────────────────────────────┐   │ Expanded state
│  │  📅 Annual Leave            │   │ Option 1
│  │  🤒 Sick Leave              │   │ Option 2
│  │  🤱 Maternity Leave         │   │ Option 3
│  │  👨 Paternity Leave         │   │ Option 4
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Text Area
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📝 Reason                  │   │ Label with icon
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  Enter your reason here...   │   │ Multi-line text
│  │                             │   │ Minimum 100pt height
│  │                             │   │ Expandable
│  │                             │   │
│  └─────────────────────────────┘   │
│  0/200 characters                │ Character count
└─────────────────────────────────────┘
```

### 4. Status Badges

#### Status Badge Variations
```
┌─────────────────────────────────────┐
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │
│  │ ✅  │  │ ⏳  │  │ ❌  │  │ 📋  │ │ Small badges
│  │ Yes │  │ Pend│  │ No  │  │ New │ │ 20pt height
│  └─────┘  └─────┘  └─────┘  └─────┘ │
│                                     │
│  ┌─────────────┐  ┌─────────────┐   │
│  │   ✅ Approved │  │ ⏳ Pending   │   │ Medium badges
│  └─────────────┘  └─────────────┘   │ 32pt height
│                                     │
│  ┌─────────────────────────────┐   │
│  │        ✅ Approved          │   │ Large badges
│  └─────────────────────────────┘   │ 40pt height
└─────────────────────────────────────┘
```

#### Badge Color Variants
```
┌─────────────────────────────────────┐
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │
│  │ ✅  │  │ ⏳  │  │ ❌  │  │ 🔵  │ │ Success (Green)
│  │ Yes │  │ Pend│  │ No  │  │ Info│ │ Warning (Orange)
│  └─────┘  └─────┘  └─────┘  └─────┘ │ Error (Red)
│                                     │ Info (Blue)
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │
│  │ 🏖️ │  │ ✈️ │  │ 👤 │  │ 📊 │ │ Themed badges
│  │Leave│  │Trav │  │User │  │Data │ │ With icons
│  └─────┘  └─────┘  └─────┘  └─────┘ │
└─────────────────────────────────────┘
```

### 5. Alerts & Notifications

#### Success Alert
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  ✅ Success!                │   │ Green header
│  ├─────────────────────────────┤   │
│  │  Your action was completed  │   │ Message body
│  │  successfully.             │   │
│  └─────────────────────────────┘   │ 8pt radius
│                                     │ Green background
└─────────────────────────────────────┘
```

#### Warning Alert
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  ⚠️ Warning                 │   │ Orange header
│  ├─────────────────────────────┤   │
│  │  Please review your action  │   │ Message body
│  │  before proceeding.         │   │
│  └─────────────────────────────┘   │ Orange border
│                                     │ Light orange background
└─────────────────────────────────────┘
```

#### Error Alert
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  ❌ Error                   │   │ Red header
│  ├─────────────────────────────┤   │
│  │  Something went wrong.      │   │ Message body
│  │  Please try again.          │   │
│  └─────────────────────────────┘   │ Red background
│                                     │ White text
└─────────────────────────────────────┘
```

---

## Navigation Patterns

### 1. Tab Bar Navigation
```
┌─────────────────────────────────────┐
│                                     │ Content Area
│  ┌─────────────────────────────┐   │
│  │        Screen Content       │   │ Main content
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Tab Bar
│  │  🏠    📊    ✈️    🏖️    👤 │  │ 5 tabs max
│  │ Home Stats Travel Leave Profile│  │ SF Symbols icons
│  └─────────────────────────────┘   │ 50pt height
│                                     │ Safe area bottom
└─────────────────────────────────────┘
```

### 2. Navigation Bar
```
┌─────────────────────────────────────┐
│  ←    Screen Title            ⚙️   │ Large Title
│                                     │ Navigation buttons
│  ┌─────────────────────────────┐   │
│  │        Content              │   │ Scrollable content
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        More Content         │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 3. Modal Navigation
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Modal Container
│  │  ←    Modal Title        ❌  │   │ Navigation bar
│  ├─────────────────────────────┤   │
│  │                             │   │
│  │        Modal Content        │   │ Modal content
│  │                             │   │
│  │  ┌─────────────────────┐     │   │
│  │  │   Action Button     │     │   │ Actions
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │ Rounded corners
│                                     │ Semi-transparent overlay
└─────────────────────────────────────┘
```

### 4. Sheet Presentation
```
┌─────────────────────────────────────┐
│                                     │ Content underneath
│  ┌─────────────────────────────┐   │ (dimmed)
│  │  ←    Sheet Title         ❌  │ Sheet header
│  ├─────────────────────────────┤   │
│  │                             │   │
│  │        Sheet Content        │   │ Sheet content
│  │                             │   │
│  │  ┌─────────────────────┐     │   │
│  │  │   Done               │     │   │ Primary action
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │ Grabber handle
│           ━━━━━━━                 │ Draggable
└─────────────────────────────────────┘
```

---

## Form Templates

### 1. Login Form Template
```
┌─────────────────────────────────────┐
│  ←    Sign In                      │ Navigation
├─────────────────────────────────────┤
│                                     │
│         [App Logo]                  │ Logo area
│         Welcome Back!               │ Welcome message
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📧 Email                   │   │ Email field
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  🔒 Password            👁️   │   │ Password field
│  └─────────────────────────────┘   │
│                                     │
│         [Forgot Password?]           │ Link
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Sign In              │   │ Primary action
│  └─────────────────────────────┘   │
│                                     │
│  ──────────  OR  ──────────        │ Divider
│                                     │
│     [Face ID]    [Touch ID]         │ Alternative auth
│                                     │
│         Don't have an account?      │ Sign up link
│              [Sign Up]              │
│                                     │
└─────────────────────────────────────┘
```

### 2. Multi-Step Form Template
```
┌─────────────────────────────────────┐
│  ←    Request Leave                 │ Navigation
├─────────────────────────────────────┤
│                                     │
│  ●○○○                               │ Progress indicator
│  Step 1 of 4                        │ Step counter
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🏖️ Leave Type              │   │ Current step
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  📅 Start Date              │   │ Form fields
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  📅 End Date                │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Next                │   │ Next button
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 3. Validation Form Template
```
┌─────────────────────────────────────┐
│  ←    Registration                  │ Navigation
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📧 Email                   │   │ Email field
│  │  ✅ Valid email address     │   │ Success state
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🔒 Password                │   │ Password field
│  │  ❌ Password too short      │   │ Error state
│  │  Must be at least 8 chars   │   │ Error message
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📱 Phone                   │   │ Phone field
│  │  ⚠️ Please include country  │   │ Warning state
│  │     code (+62)              │   │ Warning message
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Create Account            │   │ Submit (disabled)
│  └─────────────────────────────┘   │ if validation fails
│                                     │
└─────────────────────────────────────┘
```

### 4. Search & Filter Template
```
┌─────────────────────────────────────┐
│  ←    Employees                    📊 │ Navigation
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │ Search bar
│  │  🔍 Search employees...     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Filter chips
│  │  🏢 Department ▼           │   │ Dropdown filters
│  │  🎭 Role ▼                 │   │
│  │  ✅ Active Only             │   │ Toggle filters
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Results list
│  │  👤 John Doe                │   │ List items
│  │  👨‍💼 Senior Developer       │   │ with avatar
│  │  🏢 Technology              │   │ and details
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  👤 Jane Smith              │   │
│  │  👩‍💼 HR Manager             │   │
│  │  🏢 Human Resources         │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Load More]                 │ Load more
│                                     │
└─────────────────────────────────────┘
```

---

## Data Visualization

### 1. Progress Indicators

#### Linear Progress Bar
```
┌─────────────────────────────────────┐
│  Attendance Rate                    │ Label
│  ████████░░ 80%                    │ Progress bar
│  176 of 220 days                   │ Current/Total
└─────────────────────────────────────┘
```

#### Circular Progress
```
┌─────────────────────────────────────┐
│         Leave Balance               │ Title
│                                     │
│         ┌─────────────┐            │ Circular progress
│       8 / 12 days                    │ Current/Total in center
│       ████████░░                    │ 75% complete
│         └─────────────┘            │
│                                     │
└─────────────────────────────────────┘
```

#### Step Progress
```
┌─────────────────────────────────────┐
│  ●─────●─────●─────●               │ Step indicator
│  1     2     3     4               │ Step numbers
│  Done  Done  Current  Pending        │ Step states
│                                     │
│  Personal   Employment   Account   Review │ Step labels
│  Info       Details      Setup      & Submit │
└─────────────────────────────────────┘
```

### 2. Charts & Graphs

#### Bar Chart
```
┌─────────────────────────────────────┐
│  Monthly Attendance                 │ Chart title
│                                     │
│  100% ┤                             │ Y-axis
│   80% ┤ ██                          │
│   60% ┤ ██ ██                       │ Bars
│   40% ┤ ██ ██ ██                    │
│   20% ┤ ██ ██ ██ ██                 │
│    0% └─────────────────────        │ X-axis
│         J F M A M J J A S O N D      │ Months
│                                     │
│  Legend: ██ Present │ ██ Absent     │ Legend
└─────────────────────────────────────┘
```

#### Pie Chart
```
┌─────────────────────────────────────┐
│  Leave Distribution                 │ Chart title
│                                     │
│         ┌─────────────┐            │ Pie chart
│       ██ ███████ ██                 │ Segments
│     ██ ███████ ██ ██               │ with labels
│       ██ ███████ ██                 │
│         └─────────────┘            │
│                                     │
│  🏖️ Annual: 45%                    │ Legend items
│  🤒 Sick: 25%                      │ with percentages
│  🤱 Maternity: 20%                 │
│  👨 Paternity: 10%                 │
└─────────────────────────────────────┘
```

#### Line Chart
```
┌─────────────────────────────────────┐
│  Attendance Trend                   │ Chart title
│                                     │
│  100% ┤                             │ Y-axis
│   90% ┤     •───•                   │ Data points
│   80% ┤   •─/   •                   │ Connected lines
│   70% ┤ •─/       •                 │
│   60% ┤─/           •               │
│   50% └─────────────────────        │ X-axis
│         J F M A M J J A S O N D      │ Months
│                                     │
│  Target: 85%                        │ Target line
└─────────────────────────────────────┘
```

### 3. Statistics Cards

#### Metric Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  👥 Total Employees         │   │ Icon + Title
│  │  250                        │   │ Large number
│  │  ↗️ +12 from last month      │   │ Trend indicator
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✅ Active Today            │   │
│  │  235                        │   │
│  │  → Same as yesterday        │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Comparison Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📊 This Month vs Last       │   │ Comparison title
│  │                             │   │
│  │  ████████████░░ 85%         │   │ Current month
│  │  This Month                 │   │
│  │                             │   │
│  │  ████████░░░░░░░ 65%         │   │ Last month
│  │  Last Month                 │   │
│  │                             │   │
│  │  ↗️ +20% improvement        │   │ Change indicator
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Animation & Transitions

### 1. Page Transitions

#### Slide Transition
```
Screen A → Screen B
┌─────────┐    ┌─────────┐
│  A      │    │         │  ← Slide in from right
│         │ →  │    B    │  0.3s ease-in-out
│         │    │         │  Maintain context
└─────────┘    └─────────┘
```

#### Modal Transition
```
Main Screen    Modal Screen
┌─────────┐    ┌─────────┐
│         │    │  ┌─────┐ │  ← Scale up from center
│  Main   │ →  │  │ Mod │ │  0.25s ease-out
│         │    │  └─────┘ │  Dim background
└─────────┘    └─────────┘
```

#### Tab Transition
```
Tab 1 → Tab 2
┌─────────┐    ┌─────────┐
│  Tab 1  │    │  Tab 2  │  ← Crossfade
│ Content │ →  │ Content │  0.2s ease-in-out
│         │    │         │  Maintain tab bar
└─────────┘    └─────────┘
```

### 2. Micro-interactions

#### Button Press
```
Normal → Pressed → Normal
┌─────────┐    ┌─────────┐    ┌─────────┐
│  Button │ →  │Button   │ →  │  Button │
│         │    │Pressed  │    │         │
│  Scale 1│    │Scale 0.95│   │  Scale 1│
└─────────┘    └─────────┘    └─────────┘
  0s → 0.1s → 0.2s
```

#### Card Hover
```
Normal → Hover → Normal
┌─────────┐    ┌─────────┐    ┌─────────┐
│  Card   │ →  │  Card   │ →  │  Card   │
│ Shadow  │    │Shadow↑  │    │ Shadow  │
│  Offset │    │ Offset↑ │    │  Offset │
└─────────┘    └─────────┘    └─────────┘
  0s → 0.2s → 0.3s
```

#### Loading State
```
Loading Spinner
    ⭕
  ⭕     ⭕
    ⭕
  ← Continuous rotation
  1s per rotation
```

#### Skeleton Loading
```
┌─────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ Shimmer effect
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ ← → ← → ←
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ 1.5s cycle
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
└─────────────────────────────────────┘
```

### 3. Success/Error Feedback

#### Success Animation
```
✅ Success State
    ✨
  ✅ ✅ ✅
    ✅
  ← Checkmark appears
  ← Sparkles animate
  ← 0.5s duration
```

#### Error Shake
```
❌ Error State
┌─────────┐
│ Error   │ ← Shake left-right-left
│ Message │  ← 0.1s per shake
└─────────┘   ← 3 shakes total
```

---

## Dark Mode Support

### Color Adaptations
```swift
// Dynamic Colors
static let label = UIColor.label
static let secondaryLabel = UIColor.secondaryLabel
static let tertiaryLabel = UIColor.tertiaryLabel

static let systemBackground = UIColor.systemBackground
static let secondarySystemBackground = UIColor.secondarySystemBackground
static let tertiarySystemBackground = UIColor.tertiarySystemBackground

// Custom Dark Mode Colors
static let cardBackground = UIColor { traitCollection in
    return traitCollection.userInterfaceStyle == .dark
        ? UIColor(red: 0.1, green: 0.1, blue: 0.1, alpha: 1.0)
        : UIColor.white
}
```

### Dark Mode Layout
```
┌─────────────────────────────────────┐
│  ←    Screen Title            ⚙️   │ Dark navigation
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │ Dark cards
│  │  Dark card with            │   │ Reduced contrast
│  │  appropriate text colors  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Another dark card         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Dark buttons
│  │      Primary Action         │   │ with proper contrast
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Accessibility Guidelines

### 1. VoiceOver Support

#### Semantic Labels
```swift
// Accessible elements
button.accessibilityLabel = "Submit leave request"
button.accessibilityHint = "Submits your leave request for approval"

// Navigation elements
navigationItem.titleView?.accessibilityLabel = "Leave Request Form"
tabBar.items?[0].accessibilityLabel = "Dashboard, tab 1 of 5"
```

#### Reading Order
```
┌─────────────────────────────────────┐
│  1 ← 2 Screen Title         3 ⚙️   │ Logical reading order
├─────────────────────────────────────┤
│                                     │ Numbers indicate
│  ┌─────────────────────────────┐   │ VoiceOver order
│  │  4 Card Title               │   │
│  │  5 Card content             │   │ Top to bottom,
│  │  6 [Action Button]          │   │ left to right
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  7 Another Card             │   │
│  │  8 Content                  │   │
│  │  9 [Button]                 │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2. Dynamic Type Support

#### Font Scaling
```
┌─────────────────────────────────────┐
│  LARGE TITLE (34pt)                 │ Extra Large
│  Title 1 (28pt)                     │ Large
│  Headline (17pt)                    │ Medium
│  Body (17pt)                        │ Default
│  Caption 1 (12pt)                   │ Small
│  Caption 2 (11pt)                   │ Extra Small
└─────────────────────────────────────┘

All text scales proportionally
maintaining hierarchy and readability
```

### 3. High Contrast Mode

#### Enhanced Visuals
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Higher contrast borders
│  │  High Contrast Card         │   │ Bolder text
│  │  with increased contrast    │   │ Clearer visual hierarchy
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  [High Contrast Button]     │   │ Defined button edges
│  └─────────────────────────────┘   │
│                                     │
│  ✅ Clear status indicators        │ Obvious icons
│  ❌ Error states                   │ with backgrounds
└─────────────────────────────────────┘
```

### 4. Motor Accessibility

#### Touch Targets
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Minimum 44×44pt
│  │    Large Touch Target       │   │ touch targets
│  │     (44pt minimum)          │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────┐  ┌─────┐  ┌─────┐       │ Increased spacing
│  │  ✔  │  │  ✔  │  │  ✔  │       │ between interactive
│  └─────┘  └─────┘  └─────┘       │ elements
│                                     │
│  ┌─────────────────────────────┐   │ Padded tap areas
│  │  Icon Button with Padding   │   │ around icons
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 5. Reduce Motion Support

#### Alternative Animations
```
Standard:        Reduce Motion:
┌─────┐ → ┌─────┐    ┌─────┐    ┌─────┐
│  A  │   │  B  │    │  A  │ →  │  B  │
Slide         Fade   Instant
transition    transition  appearance
```

---

## Usage Guidelines

### 1. Component Usage Rules
- **Buttons**: Use primary for main actions, secondary for alternatives
- **Cards**: Group related information, maintain consistent spacing
- **Forms**: Provide clear labels, real-time validation, helpful error messages
- **Navigation**: Keep tab bar to 5 items max, use clear icons and labels

### 2. Content Guidelines
- **Text**: Use active voice, be concise, maintain consistent terminology
- **Icons**: Use SF Symbols when possible, maintain semantic meaning
- **Images**: Optimize for performance, use appropriate formats
- **Colors**: Use semantic colors, maintain adequate contrast ratios

### 3. Performance Guidelines
- **Images**: Compress appropriately, use lazy loading
- **Animations**: Keep under 0.3s for UI, use hardware acceleration
- **Data**: Cache appropriately, implement pagination for large lists
- **Memory**: Profile regularly, manage object lifecycles properly

This comprehensive mobile template design system provides all the necessary components, patterns, and guidelines to create consistent, accessible, and user-friendly iOS applications following Apple's design principles and modern mobile UI/UX best practices.