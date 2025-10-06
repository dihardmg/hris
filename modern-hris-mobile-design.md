# Modern HRIS Mobile App Design
*Inspired by Behance HRIS Redesign*

## Design Overview

Based on the Behance HRIS redesign, this modern mobile design emphasizes:
- **Clean card-based layouts** with subtle shadows
- **Blue and white color scheme** for professional appearance
- **Rounded corners** for friendly, modern feel
- **Clear visual hierarchy** with proper spacing
- **Tab-based navigation** with intuitive icons

---

## Color System

### Primary Color Palette
```swift
// Primary Blues
static let primaryBlue = UIColor(red: 0.20, green: 0.60, blue: 1.00, alpha: 1.00) // #3399FF
static let darkBlue = UIColor(red: 0.15, green: 0.45, blue: 0.80, alpha: 1.00)   // #2673CC
static let lightBlue = UIColor(red: 0.85, green: 0.92, blue: 1.00, alpha: 1.00) // #DAEBFF
static let paleBlue = UIColor(red: 0.96, green: 0.98, blue: 1.00, alpha: 1.00) // #F5FAFF

// Neutrals
static let pureWhite = UIColor.white
static let lightGray = UIColor(red: 0.98, green: 0.98, blue: 0.98, alpha: 1.00) // #FAFAFA
static let mediumGray = UIColor(red: 0.95, green: 0.95, blue: 0.95, alpha: 1.00) // #F2F2F2
static let textGray = UIColor(red: 0.47, green: 0.47, blue: 0.47, alpha: 1.00) // #787878
static let darkText = UIColor(red: 0.20, green: 0.20, blue: 0.20, alpha: 1.00) // #333333

// Accent Colors
static let successGreen = UIColor(red: 0.20, green: 0.78, blue: 0.35, alpha: 1.00) // #34C759
static let warningOrange = UIColor(red: 1.00, green: 0.58, blue: 0.00, alpha: 1.00) // #FF9500
static let errorRed = UIColor(red: 1.00, green: 0.23, blue: 0.19, alpha: 1.00) // #FF3B30
```

---

## Typography System

### Font Hierarchy
```swift
// Modern, Clean Typography
enum ModernFont {
    // Display
    case largeTitle    // 28pt, Bold - Screen headers
    case title1        // 24pt, Bold - Card titles
    case title2        // 20pt, Semibold - Section headers
    case headline      // 18pt, Semibold - Card headlines
    case body          // 16pt, Regular - Body text
    case caption       // 14pt, Regular - Secondary info
    case footnote      // 12pt, Regular - Fine print
}
```

### Typography Examples
```
┌─────────────────────────────────────┐
│  Welcome Back, John!               │ Large Title - 28pt Bold
│                                     │
│  Today's Overview                  │ Title 1 - 24pt Bold
│                                     │
│  Attendance Status                 │ Title 2 - 20pt Semibold
│                                     │
│  You're clocked in                 │ Headline - 18pt Semibold
│  since 8:00 AM                     │ Body - 16pt Regular
│                                     │
│  Last updated: 2 min ago           │ Caption - 14pt Regular
│  Version 1.0.1                     │ Footnote - 12pt Regular
└─────────────────────────────────────┘
```

---

## Layout & Spacing

### Modern Spacing System
```swift
enum ModernSpacing {
    static let xs: CGFloat = 4    // Extra small gaps
    static let sm: CGFloat = 8    // Small spacing
    static let md: CGFloat = 16   // Medium spacing
    static let lg: CGFloat = 24   // Large spacing
    static let xl: CGFloat = 32   // Extra large spacing
    static let xxl: CGFloat = 48  // Section breaks
}
```

### Card-Based Layout Structure
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ 16pt screen padding
│  │        Screen Header        │   │ Large title
│  └─────────────────────────────┘   │
│                                     │ 24pt spacing
│  ┌─────────────────────────────┐   │
│  │  ┌─────────────────────┐     │   │ Card container
│  │  │   Card Title        │     │   │ 16pt card padding
│  │  ├─────────────────────┤     │   │ 12pt corner radius
│  │  │                     │     │   │ 8pt shadow
│  │  │   Card Content      │     │   │
│  │  │   with proper       │     │   │
│  │  │   spacing           │     │   │
│  │  │                     │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │ 16pt between cards
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ┌─────────────────────┐     │   │ Multiple cards
│  │  │   Another Card      │     │   │ in grid layout
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Bottom action area
│  │      [Primary Action]       │   │ 24pt from bottom
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Components Library

### 1. Modern Cards

#### Primary Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📊 Attendance Summary      │   │ Icon + Title
│  ├─────────────────────────────┤   │ Light divider
│  │                             │   │
│  │  ┌─────────────────────┐     │   │ Content area
│  │  │  ✅ Clocked In      │     │   │ Status badge
│  │  │  8:00 AM            │     │   │ Time display
│  │  │                     │     │   │
│  │  │  📍 Office Location  │     │   │ Location info
│  │  │  Jakarta, Indonesia  │     │   │
│  │  └─────────────────────┘     │   │
│  │                             │   │
│  │  ┌─────────────────────┐     │   │ Action area
│  │  │    [Clock Out]      │     │   │ Primary button
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │ 12pt corner radius
│                                     │ Subtle shadow
└─────────────────────────────────────┘
```

#### Stats Card Grid
```
┌─────────────────────────────────────┐
│  ┌─────────┐  ┌─────────┐           │ 2-column grid
│  │  👥     │  │  🏖️     │           │ 8pt spacing
│  │  250    │  │  10/12   │           │ between cards
│  │Employees│  │ Leave   │           │
│  └─────────┘  └─────────┘           │
│                                     │
│  ┌─────────┐  ┌─────────┐           │
│  │  ✅     │  │  ✈️     │           │
│  │  95%    │  │   3     │           │
│  │Attendance│ Travel │           │
│  └─────────┘  └─────────┘           │
│                                     │ Consistent card
│  ┌─────────┐  ┌─────────┐           │ heights and
│  │  📊     │  │  🔔     │           │ styling
│  │  176.5h │  │   5     │           │
│  │Total   │ Pending │           │
│  └─────────┘  └─────────┘           │
└─────────────────────────────────────┘
```

#### List Card
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📋 Recent Activity         │   │ Card header
│  ├─────────────────────────────┤   │ Divider
│  │                             │   │
│  │  • Leave approved by Jane   │   │ List items with
│  │    2 hours ago              │   │ timestamps
│  │                             │   │
│  │  • Clock in: Today 8:00 AM  │   │
│  │  • Travel request sent      │   │
│  │                             │   │
│  │  • Password reset completed │   │
│  │    Yesterday                │   │
│  │                             │   │
│  │         [View All]         │   │ View more link
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2. Modern Buttons

#### Primary Button (Rounded)
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │      Clock In               │   │ 50pt height
│  └─────────────────────────────┘   │ 25pt corner radius
│                                     │ Background: Primary Blue
│  ┌─────────────────────────────┐   │ Text: White, Semibold
│  │    Submit Request           │   │
│  └─────────────────────────────┘   │ Subtle shadow
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Approve              │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Secondary Button (Outlined)
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │      View Details           │   │ 50pt height
│  └─────────────────────────────┘   │ 25pt corner radius
│                                     │ Border: Primary Blue
│  ┌─────────────────────────────┐   │ Background: Transparent
│  │        Cancel               │   │ Text: Primary Blue
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Edit                 │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Button States
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Normal State
│  │      Primary Action         │   │ Full opacity
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Pressed State
│  │    Primary Action           │   │ 0.8 opacity
│  └─────────────────────────────┘   │ Slightly darker
│                                     │
│  ┌─────────────────────────────┐   │ Disabled State
│  │    Primary Action           │   │ 0.5 opacity
│  └─────────────────────────────┘   │ No interaction
└─────────────────────────────────────┘
```

#### Small Action Buttons
```
┌─────────────────────────────────────┐
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │ 32pt height
│  │  👁️ │  │  ✏️  │  │  🗑️ │  │  📤  │ │ 32pt width
│  └─────┘  └─────┘  └─────┘  └─────┘ │ 16pt radius
│                                     │ Circle buttons
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐ │ with icons
│  │  ✔️  │  │  ❌  │  │  ⏰  │  │  📍  │ │ Light background
│  └─────┘  └─────┘  └─────┘  └─────┘ │
└─────────────────────────────────────┘
```

### 3. Input Fields

#### Modern Text Field
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  📧 Email Address           │   │ Label with icon
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  user@example.com          │   │ Input field
│  └─────────────────────────────┘   │ 56pt height
│                                     │ 16pt corner radius
│  ┌─────────────────────────────┐   │ 2pt border
│  │  🔒 Password                │   │ Light gray background
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  ••••••••••••            👁️   │   │ Icon indicators
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Search Bar
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │  🔍 Search employees...     │   │ Search field
│  └─────────────────────────────┘   │ 48pt height
│                                     │ 24pt corner radius
│  ┌─────────────────────────────┐   │ Light background
│  │  🔍 John Doe                │   │ With search icon
│  │         [Clear]             │   │ Clear button
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 4. Status Indicators

#### Modern Status Badges
```
┌─────────────────────────────────────┐
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │ Rounded badges
│  │  ✅     │  │  ⏳     │  │  ❌     │ │ 20pt height
│  │  Active  │  │  Pending │  │  Rejected│ │ 16pt radius
│  └─────────┘  └─────────┘  └─────────┘ │ Colored backgrounds
│                                     │
│  ┌─────────────────────┐           │ Medium badges
│  │    ✅ Approved      │           │ 32pt height
│  └─────────────────────┘           │
│                                     │
│  ┌─────────────────────────────┐   │ Large badges
│  │         On Leave            │   │ 40pt height
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Progress Indicators
```
┌─────────────────────────────────────┐
│  Attendance Progress                 │ Label
│                                     │
│  ████████████░░ 80%                 │ Progress bar
│  176 of 220 days                    │ Current/Total
│                                     │
│  ┌─────────────────────────────┐   │ Circular progress
│  │         ┌─────┐             │   │
│  │       8/12 days            │   │ Center text
│  │      ████████░░            │   │
│  │         └─────┘             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Navigation Design

### Tab-Based Navigation
```
┌─────────────────────────────────────┐
│                                     │ Content Area
│  ┌─────────────────────────────┐   │ Scrollable content
│  │        Dashboard             │   │ with cards
│  │  ┌─────────────────────┐     │   │
│  │  │   Content Cards     │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Tab Bar
│  │  🏠      📊      ✈️        │   │ 4 tabs with icons
│  │ Home    Stats   Travel      │   │ Clean design
│  │   🏖️      👤              │   │ Active state highlighted
│  │ Leave   Profile            │   │ 50pt height
│  └─────────────────────────────┘   │ Rounded tab bar
│                                     │
└─────────────────────────────────────┘
```

### Modern Navigation Bar
```
┌─────────────────────────────────────┐
│  ←    Dashboard              🔔   │ Large title
│                                     │ Back button + icon
│  ┌─────────────────────────────┐   │
│  │                             │   │ Content area
│  │  [Scrollable Cards]         │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  [Floating Action Button]   │   │ Optional FAB
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Screen Designs

### 1. Modern Dashboard
```
┌─────────────────────────────────────┐
│    Dashboard                        │ Large Title
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │ Welcome Card
│  │  👋 Welcome back, John!     │   │ Personal greeting
│  │  Monday, October 5, 2025     │   │ Current date
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Main Status Card
│  │  🕐 Today's Attendance      │   │
│  │  ┌─────────────────────┐     │   │
│  │  │  ✅ Clocked In      │     │   │ Status indicator
│  │  │  8:00 AM            │     │   │ Time display
│  │  │                     │     │   │
│  │  │  📍 Office          │     │   │ Location
│  │  │  Jakarta            │     │   │
│  │  └─────────────────────┘     │   │
│  │                             │   │
│  │  ┌─────────────────────┐     │   │ Action button
│  │  │    [Clock Out]      │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────┐  ┌─────────┐           │ Stats Grid
│  │  👥     │  │  🏖️     │           │ 2x2 grid
│  │  250    │  │  10/12   │           │ Quick stats
│  │Employees│  │ Leave   │           │
│  └─────────┘  └─────────┘           │
│                                     │
│  ┌─────────┐  ┌─────────┐           │
│  │  ✅     │  │  ✈️     │           │
│  │  95%    │  │   3     │           │
│  │Attendance│ Travel │           │
│  └─────────┘  └─────────┘           │
│                                     │
│  ┌─────────────────────────────┐   │ Activity Card
│  │  📋 Recent Activity         │   │
│  │  • Leave approved           │   │ Recent items
│  │  • Clock in today           │   │
│  │  • Travel request sent      │   │
│  │         [View All]         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Notifications
│  │  🔔 You have 3 notifications│   │ Alert card
│  │         [View All]         │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2. Attendance Screen
```
┌─────────────────────────────────────┐
│    Attendance                       │ Large Title
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │ Clock Status
│  │  🕐 Today's Status          │   │
│  │  ┌─────────────────────┐     │   │
│  │  │  ✅ Clocked In      │     │   │ Current status
│  │  │  8:00 AM            │     │   │ Clock time
│  │  │                     │     │   │
│  │  │  📍 Office Location  │     │   │ Location check
│  │  │  Within geofence     │     │   │
│  │  └─────────────────────┘     │   │
│  │                             │   │
│  │  ┌─────────────────────┐     │   │ Clock action
│  │  │    [Clock Out]      │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Face Recognition
│  │  📸 Face Verification       │   │
│  │  ┌─────────────────────┐     │   │ Camera area
│  │  │    [Camera View]     │     │   │
│  │  │         👤           │     │   │ Face overlay
│  │  │                     │     │   │
│  │  │  ⚡ 95% Confidence   │     │   │ Confidence score
│  │  └─────────────────────┘     │   │
│  │                             │   │
│  │  ┌─────────────────────┐     │   │ Retake option
│  │  │    [Retake Photo]   │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ History
│  │  📊 This Week               │   │
│  │  ┌─────────────────────┐     │   │ Weekly stats
│  │  │  Mon: 8:00-17:30     │     │   │ Daily records
│  │  │  Tue: 8:05-17:15     │     │   │
│  │  │  Wed: 8:00-17:45     │     │   │
│  │  │  Thu: 7:55-17:20     │     │   │
│  │  │  Fri: 8:10-17:00     │     │   │
│  │  └─────────────────────┘     │   │
│  │         [View All]         │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 3. Leave Management
```
┌─────────────────────────────────────┐
│    Leave Management                 │ Large Title
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │ Leave Balance
│  │  🏖️ Leave Balance            │   │
│  │  ┌─────────┐  ┌─────────┐   │ 2x2 grid
│  │  │  📅     │  │  🤒     │   │ Leave types
│  │  │  10/12  │  │  5/6    │   │
│  │  │ Annual │  │  Sick   │   │
│  │  └─────────┘  └─────────┘   │   │
│  │                             │   │
│  │  ┌─────────┐  ┌─────────┐   │   │
│  │  │  🤱     │  │  👨     │   │   │
│  │  │  45/90  │  │  6/7    │   │   │
│  │  │Maternity│ Paternity│   │   │
│  │  └─────────┘  └─────────┘   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Request Button
│  │  ┌─────────────────────┐     │   │
│  │  │  + Request Leave     │     │   │ Primary action
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Current Leave
│  │  📅 Current Leave            │   │
│  │  ┌─────────────────────┐     │   │ If active
│  │  │  ✅ Oct 15-17        │     │   │
│  │  │  🏖️ Annual Leave     │     │   │
│  │  │  Starts in 10 days   │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Recent Requests
│  │  📋 Recent Requests          │   │
│  │  ┌─────────────────────┐     │   │ List of requests
│  │  │  📅 Sep 20-22       │     │   │ with status
│  │  │  ✅ Approved        │     │   │
│  │  │  🏖️ Annual Leave     │     │   │
│  │  └─────────────────────┘     │   │
│  │  ┌─────────────────────┐     │   │
│  │  │  📅 Sep 5           │     │   │
│  │  │  ❌ Rejected        │     │   │
│  │  │  🤒 Sick Leave      │     │   │
│  │  └─────────────────────┘     │   │
│  │         [View All]         │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 4. Profile Screen
```
┌─────────────────────────────────────┐
│    Profile                          │ Large Title
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │ Profile Header
│  │      [Profile Photo]        │   │ Large avatar
│  │         👤                  │   │
│  │                             │   │
│  │  John Doe                   │   │ Name
│  │  Senior Developer            │   │ Position
│  │  EMP001                     │   │ Employee ID
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Information Cards
│  │  📊 Employment Details       │   │
│  │  🏢 Technology Department   │   │ Department
│  │  👥 Jane Smith (Supervisor) │   │ Supervisor
│  │  📅 Joined: Jan 15, 2020   │   │ Join date
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📱 Contact Information      │   │
│  │  📧 john.doe@company.com   │   │ Email
│  │  📱 +62 812-3456-7890      │   │ Phone
│  │  📍 Jakarta, Indonesia     │   │ Location
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🔐 Security Settings        │   │
│  │  🔑 Change Password         │   │ Password
│  │  👤 Face ID Enabled         │   │ Biometric
│  │  📱 Two-Factor Auth         │   │ 2FA
│  │         [Manage]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📊 Quick Stats             │   │
│  │  ⏰ 5+ years at company     │   │ Tenure
│  │  🏖️ 95% attendance rate    │   │ Attendance
│  │  📈 3 promotions           │   │ Growth
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │          Sign Out           │   │ Sign out button
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Interactive Elements

### 1. Touch Interactions

#### Card Tap Effects
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Normal state
│  │  Card Content               │   │ Subtle shadow
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ Pressed state
│  │  Card Content               │   │ Shadow reduced
│  └─────────────────────────────┘   │ Scale 0.98
│                                     │
│  ┌─────────────────────────────┐   │ Hover (if applicable)
│  │  Card Content               │   │ Shadow enhanced
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Button Ripple Effect
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Before tap
│  │      Button                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ During tap
│  │      Button                 │   │ Ripple animation
│  │     ○○○○○○○○○○○            │   │ from center
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │ After tap
│  │      Button                 │   │ Ripple fades
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2. Loading States

#### Skeleton Loading
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │ Card skeleton
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ Animated shimmer
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ effect
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ Left to right
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ animation
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────┐  ┌─────────┐           │ Stats skeleton
│  │  ▓▓▓▓   │  │  ▓▓▓▓   │           │ Different
│  │  ▓▓▓▓   │  │  ▓▓▓▓   │           │ element shapes
│  └─────────┘  └─────────┘           │
└─────────────────────────────────────┘
```

#### Progress Indicators
```
┌─────────────────────────────────────┐
│         Loading...                  │ Loading text
│                                     │
│         ○○○○○○                    │ Dots animation
│       ○○○○○○○○○○                  │ Pulsing dots
│     ○○○○○○○○○○○○○○                │
│         ○○○○○○                    │
│                                     │
│  ┌─────────────────────────────┐   │ Progress bar
│  │  ████████░░░░░░░░░░ 40%     │   │ Animated fill
│  └─────────────────────────────┘   │
│                                     │
│         [Cancel]                   │ Cancel option
└─────────────────────────────────────┘
```

### 3. Success/Error States

#### Success Animation
```
┌─────────────────────────────────────┐
│         ✅ Success!                │ Success message
│                                     │
│         ○ ✨ ○                    │ Checkmark appears
│       ✨ ○ ✨ ○ ✨                │ Sparkles animate
│         ○ ✨ ○                    │
│                                     │
│  Clock in successful at 8:00 AM     │ Details
│                                     │
│  ┌─────────────────────────────┐   │ Action button
│  │        Done                 │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Error State
```
┌─────────────────────────────────────┐
│         ❌ Error                   │ Error message
│                                     │
│  Unable to clock in. Please check:  │ Error description
│  • Location services               │ Bullet points
│  • Face recognition                │
│  • Network connection              │
│                                     │
│  ┌─────────────────────────────┐   │ Retry button
│  │        Try Again             │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Contact Support]          │ Support link
└─────────────────────────────────────┘
```

---

## Responsive Design

### iPhone SE (4.7" screen)
```
┌─────────────────────────┐
│  Dashboard              │ Single column
│                         │ Compact cards
│  ┌─────────────────────┐ │ Reduced spacing
│  │   Card Content      │ │
│  └─────────────────────┘ │
│                         │
│  ┌─────┐  ┌─────┐     │ 2-column grid
│  │  👥 │  │  🏖️ │     │ max
│  └─────┘  └─────┘     │
└─────────────────────────┘
```

### iPhone 14 Pro (6.1" screen)
```
┌─────────────────────────────┐
│  Dashboard                  │ Standard layout
│                             │ Full cards
│  ┌─────────────────────┐     │
│  │   Card Content      │     │
│  └─────────────────────┘     │
│                             │
│  ┌─────────┐  ┌─────────┐   │ 2-column grid
│  │  👥     │  │  🏖️     │   │
│  │  250    │  │  10/12   │   │
│  └─────────┘  └─────────┘   │
└─────────────────────────────┘
```

### iPhone 14 Plus (6.7" screen)
```
┌───────────────────────────────┐
│  Dashboard                    │ Larger layout
│                               │ More content
│  ┌─────────────────────┐       │
│  │   Card Content      │       │
│  └─────────────────────┘       │
│                               │
│  ┌─────────┐  ┌─────────┐     │ 3-column grid
│  │  👥     │  │  🏖️     │     │ possible
│  │  250    │  │  10/12   │     │
│  └─────────┘  └─────────┘     │
│  ┌─────────┐  ┌─────────┐     │
│  │  ✅     │  │  ✈️     │     │
│  │  95%    │  │   3     │     │
│  └─────────┘  └─────────┘     │
└───────────────────────────────┘
```

---

## Dark Mode Support

### Dark Theme Colors
```swift
// Dark mode adaptations
static let darkCardBackground = UIColor(red: 0.1, green: 0.1, blue: 0.1, alpha: 1.0)
static let darkPrimaryText = UIColor.white
static let darkSecondaryText = UIColor(red: 0.8, green: 0.8, blue: 0.8, alpha: 1.0)
static let darkBorder = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1.0)
```

### Dark Mode Layout
```
┌─────────────────────────────────────┐
│    Dashboard                        │ White text
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │ Dark cards
│  │  👋 Welcome back, John!     │   │ Reduced contrast
│  │  Monday, October 5, 2025     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🕐 Today's Attendance      │   │ Dark backgrounds
│  │  ┌─────────────────────┐     │   │
│  │  │  ✅ Clocked In      │     │   │ Blue highlights
│  │  │  8:00 AM            │     │   │
│  │  └─────────────────────┘     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────┐  ┌─────────┐           │ Stats adapt to
│  │  👥     │  │  🏖️     │           │ dark mode
│  │  250    │  │  10/12   │           │
│  │Employees│  │ Leave   │           │
│  └─────────┘  └─────────┘           │
└─────────────────────────────────────┘
```

---

## Implementation Guidelines

### 1. Design Tokens
```swift
// Consistent design values
struct DesignTokens {
    static let cornerRadius: CGFloat = 16
    static let cardPadding: CGFloat = 16
    static let shadowOpacity: Float = 0.1
    static let shadowRadius: CGFloat = 8
    static let shadowOffset = CGSize(width: 0, height: 2)
}
```

### 2. Component Reusability
- Create base card component that can be customized
- Implement consistent button styles
- Use semantic colors for easy theming
- Build reusable form components

### 3. Animation Timing
```swift
// Standard animation durations
static let quickAnimation: TimeInterval = 0.2
static let standardAnimation: TimeInterval = 0.3
static let slowAnimation: TimeInterval = 0.5

// Standard easing curves
static let defaultCurve = UIView.AnimationCurve.easeInOut
static let springCurve = UIView.AnimationCurve.easeOut
```

### 4. Accessibility
- Maintain 4.5:1 contrast ratio for normal text
- 3:1 for large text (18pt+)
- Support Dynamic Type scaling
- Provide VoiceOver labels for all interactive elements

This modern HRIS mobile design combines the best practices from the Behance reference with clean, card-based layouts, modern blue/white theming, and intuitive navigation patterns. The design emphasizes usability, accessibility, and a professional appearance perfect for HR applications.