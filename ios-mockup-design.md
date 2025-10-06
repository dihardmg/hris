# GSJ HRIS iOS Mobile App Mockup Design

## Design System

### Colors
- **Primary**: #007AFF (iOS Blue)
- **Success**: #34C759 (Green)
- **Warning**: #FF9500 (Orange)
- **Error**: #FF3B30 (Red)
- **Background**: #F2F2F7 (Light Gray)
- **Card Background**: #FFFFFF
- **Text Primary**: #000000
- **Text Secondary**: #8E8E93
- **Border**: #E5E5EA

### Typography
- **Large Title**: SF Pro Display, 34pt, Bold
- **Title 1**: SF Pro Display, 28pt, Bold
- **Title 2**: SF Pro Display, 22pt, Bold
- **Headline**: SF Pro Text, 17pt, Semibold
- **Body**: SF Pro Text, 17pt, Regular
- **Caption**: SF Pro Text, 12pt, Regular

### Components
- **Navigation Bar**: Standard iOS navigation with large titles
- **Tab Bar**: 5 tabs with SF Symbols icons
- **Cards**: White background, rounded corners (12pt), subtle shadow
- **Buttons**: Primary (filled blue), Secondary (outlined), Destructive (red)
- **Status Bars**: Colored badges for different states
- **Form Fields**: iOS-style text fields with floating labels

---

## 1. Authentication Screens

### 1.1 Login Screen
```
┌─────────────────────────────────────┐
│ ←    Sign In to GSJ HRIS          ⚙️ │ Navigation Bar
├─────────────────────────────────────┤
│                                     │
│      [GSJ HRIS Logo]                │ Large Logo
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📧 Email                    │   │ Email Field
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔒 Password            👁️   │   │ Password Field
│   └─────────────────────────────┘   │
│                                     │
│           [Forgot Password?]        │ Link
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Sign In                 │   │ Primary Button
│   └─────────────────────────────┘   │
│                                     │
│  ──────────  OR  ──────────        │ Divider
│                                     │
│   [Face ID]   [Touch ID]           │ Biometric Options
│                                     │
└─────────────────────────────────────┘
```

### 1.2 Password Reset Flow

#### Forgot Password Screen
```
┌─────────────────────────────────────┐
│ ←    Reset Password               ❌ │ Navigation Bar
├─────────────────────────────────────┤
│                                     │
│   🔐 Forgot Your Password?          │ Title
│                                     │
│   Enter your email address and      │ Description
│   we'll send you a link to reset    │
│   your password.                   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📧 Email Address            │   │ Email Field
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Send Reset Link        │   │ Primary Button
│   └─────────────────────────────┘   │
│                                     │
│   If you don't receive an email     │ Help Text
│   within a few minutes, check      │
│   your spam folder.               │
│                                     │
└─────────────────────────────────────┘
```

#### Reset Password Screen
```
┌─────────────────────────────────────┐
│ ←    Create New Password          ❌ │ Navigation Bar
├─────────────────────────────────────┤
│                                     │
│   🔐 Create New Password            │ Title
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔒 New Password             │   │ Password Field
│   └─────────────────────────────┘   │
│   ✓ 8+ characters                │ Requirements
│   ✓ Uppercase & lowercase        │ Checkmarks
│   ✓ Number or symbol             │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔒 Confirm Password         │   │ Confirm Field
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Reset Password         │   │ Primary Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 2. Main Navigation & Dashboard

### 2.1 Tab Bar Navigation
```
┌─────────────────────────────────────┐
│           Dashboard                 │ Tab 1: Active
│                                     │
│    [Dashboard Widget Cards]         │
│                                     │
│                                     │
│   ┌─────────────────────────────┐   │
│   │  🏠 📊 ✈️ 🏖️ 👤          │   │ Tab Bar Icons
│   │ Home  Stats Travel Leave    │   │ Tab Labels
│   │             Profile         │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2.2 Dashboard Screen
```
┌─────────────────────────────────────┐
│    Dashboard                        │ Large Title
├─────────────────────────────────────┤
│   👋 Welcome back, John!            │ Welcome Message
│   Monday, October 5, 2025           │ Date
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🕐 Today's Attendance      │   │ Attendance Card
│   │                             │   │
│   │  Clocked In at 8:00 AM     │   │ Status
│   │  📍 Office Location        │   │ Location
│   │  ⏱️ 5h 23m total today     │   │ Duration
│   │                             │   │
│   │         [Clock Out]         │   │ Action Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Quick Stats             │   │ Stats Card
│   │  🏖️ 10/12 Leave Days       │   │ Leave Balance
│   │  ✈️ 1 Active Travel        │   │ Travel Status
│   │  ⭐ Attendance: 95%         │   │ Attendance Rate
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📋 Recent Activity         │   │ Activity Card
│   │  • Leave approved by Jane   │   │
│   │  • Clock in: Today 8:00 AM │   │
│   │  • Travel request sent     │   │
│   │         [View All]         │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔔 Notifications           │   │ Notifications Card
│   │  • 3 pending approvals     │   │
│   │  • Password expires in 7d  │   │
│   │         [View All]         │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 3. Attendance Management

### 3.1 Clock In/Out Screen with Face Recognition
```
┌─────────────────────────────────────┐
│ ←    Attendance                     │ Navigation
├─────────────────────────────────────┤
│                                     │
│   🕐 Today's Attendance            │ Title
│   Monday, October 5, 2025           │ Date
│                                     │
│   ┌─────────────────────────────┐   │
│   │  📍 Location Check          │   │ Location Card
│   │  ✓ Within office area       │   │ Status
│   │  📍 -6.2088, 106.8456       │   │ Coordinates
│   │  🏢 GSJ Office, Jakarta     │   │ Address
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │  📸 Face Verification       │   │ Face Recognition Card
│   │                             │   │
│   │    [Camera Preview]         │   │ Camera View
│   │         👤                   │   │ Face Overlay
│   │                             │   │
│   │  ⚡ 95% Confidence          │   │ Confidence Score
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      📸 Clock In           │   │ Primary Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📋 Attendance History      │   │ History Section
│   │  📅 Oct 4: Clock 8:05-17:10│   │ Yesterday
│   │  📅 Oct 3: Clock 8:00-17:30│   │ Previous
│   │  📅 Oct 2: Clock 8:15-17:05│   │ Previous
│   │         [View All]         │   │ View More
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 3.2 Attendance History Screen
```
┌─────────────────────────────────────┐
│ ←    Attendance History           📊 │ Navigation
├─────────────────────────────────────┤
│   📅 October 2025                  │ Month Selector
│   ┌─────────────────────────────┐   │
│   │  📊 Summary                 │   │ Summary Card
│   │  ✅ 22/23 Work Days        │   │ Attendance Rate
│   │  ⏱️ 176.5 Total Hours      │   │ Total Hours
│  │  ⚡ 8:15 Avg Clock-in       │   │ Average Times
│   │  🏃 17:30 Avg Clock-out    │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │  📅 Mon, Oct 5             │   │ Today
│   │  🟢 Clock In: 8:00 AM      │   │
│   │  ⏱️ Working: 5h 23m        │   │ Current Session
│   │         [Clock Out]         │   │ Action
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │  📅 Fri, Oct 4             │   │ Previous Day
│   │  ✅ Clock In: 8:05 AM      │   │
│   │  ✅ Clock Out: 5:10 PM     │   │
│   │  ⏱️ Total: 9h 5m          │   │
│   │  📍 Office Location        │   │
│   └─────────────────────────────┘   │
│                                     │
│   [More days...]                   │ Scrollable List
│                                     │
│   ┌─────────────────────────────┐   │
│   │  📅 Mon, Sep 28            │   │ Previous Week
│   │  🏖️ On Leave               │   │
│   │  🏖️ Annual Leave           │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 4. Leave Management

### 4.1 Leave Dashboard
```
┌─────────────────────────────────────┐
│    Leave Management                 │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🏖️ Leave Balance            │   │ Balance Card
│   │  📊 Annual: 10/12 days     │   │ Annual Leave
│   │  🤒 Sick: 5/6 days         │   │ Sick Leave
│   │  🤱 Maternity: 45/90 days  │   │ Maternity Leave
│   │  👨 Paternity: 6/7 days    │   │ Paternity Leave
│   │         [Request Leave]     │   │ Action Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📅 Current Leave            │   │ Current Status Card
│   │  ✅ Approved: Oct 15-17     │   │ Approved Leave
│  │  🏖️ Annual Leave           │   │
│  │  ⏰ Starts in 10 days       │   │ Countdown
│  └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📋 Recent Requests          │   │ Recent Requests Card
│   │                             │   │
│  │  📅 Sep 20-22               │   │ Request 1
│  │  ✅ Approved by Jane Smith  │   │ Status
│  │  🏖️ Annual Leave           │   │ Type
│  │                             │   │
│  │  📅 Sep 5                   │   │ Request 2
│  │  ❌ Rejected by John Doe    │   │ Status
│  │  🤒 Sick Leave              │   │ Type
│  │         [View All]         │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 4.2 Request Leave Screen
```
┌─────────────────────────────────────┐
│ ←    Request Leave                 │ Navigation
├─────────────────────────────────────┤
│                                     │
│   📝 New Leave Request              │ Title
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🏖️ Leave Type               │   │ Type Selector
│   │  📅 Annual Leave (10 days)  │   │ Selected
│   │  🤒 Sick Leave (5 days)     │   │ Available Options
│  │  🤱 Maternity Leave (45 days)│   │
│  │  👨 Paternity Leave (6 days) │   │
│  │  ...                         │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📅 Start Date               │   │ Date Picker
│   │  🗓️ Oct 15, 2025           │   │ Selected Date
│  │                             │   │
│  │ 📅 End Date                 │   │
│  │  🗓️ Oct 17, 2025           │   │ Selected Date
│   └─────────────────────────────┘   │
│                                     │
│   📊 Request Summary                │ Summary Section
│   • Duration: 3 days                │ Details
│   • Return date: Oct 20, 2025       │
│   • Remaining after: 7 days         │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📝 Reason (Optional)        │   │ Reason Field
│   │  Family vacation...         │   │ Text Input
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Submit Request         │   │ Submit Button
│   └─────────────────────────────┘   │
│                                     │
│   📋 Overlap Check                 │ Validation
│   ✅ No conflicts found            │ Status
│                                     │
└─────────────────────────────────────┘
```

### 4.3 Leave Status Screen
```
┌─────────────────────────────────────┐
│ ←    Leave Request                 │ Navigation
├─────────────────────────────────────┤
│                                     │
│   📋 Leave Request Details          │ Title
│   UUID: b350343f-ab94-4e37...        │ Reference
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Status                   │   │ Status Card
│   │  ⏳ PENDING                 │   │ Current Status
│   │  📅 Submitted: Oct 5, 2025  │   │ Submission Info
│   │  👤 Supervisor: Jane Smith  │   │ Approver
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📅 Request Details          │   │ Details Card
│   │  🏖️ Type: Annual Leave     │   │ Leave Type
│   │  📅 Duration: 3 days       │   │ Duration
│   │  🗓️ Oct 15 - Oct 17, 2025  │   │ Date Range
│   │  📅 Return: Oct 20, 2025   │   │ Return Date
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📝 Reason                   │   │ Reason Card
│   │  Family vacation with      │   │
│   │  parents and siblings      │   │
│   │  visiting hometown         │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 👤 Requester                │   │ Requester Card
│   │  John Doe                   │   │ Name
│   │  EMP001                     │   │ Employee ID
│   │  john.doe@company.com       │   │ Email
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │          Cancel             │   │ Cancel Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 5. Business Travel Management

### 5.1 Travel Dashboard
```
┌─────────────────────────────────────┐
│    Business Travel                  │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ✈️ Current Travel           │   │ Current Travel Card
│   │  🏨 Singapore Trip         │   │ Destination
│   │  📅 Nov 10-12, 2025        │   │ Dates
│   │  ⏳ Approved                │   │ Status
│   │  📊 3 days duration        │   │ Duration
│   │         [View Details]      │   │ Action
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Travel Statistics        │   │ Stats Card
│  │  ✈️ 5 trips this year      │   │ Trip Count
│  │  🏨 12 total travel days    │   │ Total Days
│  │  🌍 3 countries visited    │   │ Countries
│  │  💰 Average: 5 days/trip   │   │ Average
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📋 Recent Requests          │   │ Recent Requests Card
│   │                             │   │
│   │  📅 Oct 20-22              │   │ Request 1
│   │  ✅ Approved               │   │ Status
│   │  🏨 Surabaya, Indonesia    │   │ Destination
│   │                             │   │
│   │  📅 Sep 15                │   │ Request 2
│   │  ❌ Rejected               │   │ Status
│   │  🏨 Bandung, Indonesia     │   │ Destination
│   │         [View All]         │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      + Request Travel       │   │ Add Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 5.2 Request Travel Screen
```
┌─────────────────────────────────────┐
│ ←    Request Business Travel        │ Navigation
├─────────────────────────────────────┤
│                                     │
│   📝 New Travel Request             │ Title
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🏨 Destination City         │   │ Destination Field
│   │  Singapore                  │   │ Input
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📅 Travel Dates             │   │ Date Pickers
│   │  🗓️ Start: Nov 10, 2025    │   │ Start Date
│   │  🗓️ End: Nov 12, 2025      │   │ End Date
│   └─────────────────────────────┘   │
│                                     │
│   📊 Trip Summary                  │ Summary Section
│   • Duration: 3 days                │ Details
│   • Departure: Mon, Nov 10          │
│   • Return: Wed, Nov 12             │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📝 Reason for Travel        │   │ Reason Field
│   │  Client meeting and         │   │
│   │  project discussion         │   │
│   │  with Singapore team        │   │ Text Input
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 💰 Estimated Cost           │   │ Cost Estimation
│   │  🏨 Accommodation: $300     │   │ Breakdown
│   │  ✈️ Transportation: $200    │   │
│   │  🍽️ Meals: $150            │   │
│   │  💵 Total: $650            │   │ Total
│   └─────────────────────────────┘   │
│                                     │
│   📋 Overlap Check                 │ Validation
│   ✅ No leave conflicts found       │ Status
│   ✅ No other travel conflicts      │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Submit Request         │   │ Submit Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 6. Profile & Settings

### 6.1 Profile Screen
```
┌─────────────────────────────────────┐
│    Profile                          │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │      [Profile Photo]        │   │ Profile Section
│   │         👤                  │   │ Photo
│   │                             │   │
│   │  John Doe                   │   │ Name
│   │  Senior Developer            │   │ Position
│   │  EMP001                     │   │ Employee ID
│   │  john.doe@company.com       │   │ Email
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Employment Details       │   │ Employment Card
│   │  🏢 Technology Department   │   │ Department
│   │  👥 Jane Smith (Supervisor) │   │ Supervisor
│   │  📅 Joined: Jan 15, 2020   │   │ Join Date
│   │  🏖️ 12 days annual leave   │   │ Leave Entitlement
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📱 Contact Information      │   │ Contact Card
│   │  📧 john.doe@company.com   │   │ Email
│   │  📱 +62 812-3456-7890      │   │ Phone
│   │  📍 Jakarta, Indonesia     │   │ Location
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔐 Security Settings        │   │ Security Card
│   │  🔑 Change Password         │   │ Password
│   │  👤 Face ID Enabled         │   │ Biometric
│   │  📱 Two-Factor Auth         │   │ 2FA
│   │         [Manage]           │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │          Sign Out           │   │ Sign Out Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 6.2 Settings Screen
```
┌─────────────────────────────────────┐
│    Settings                         │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔔 Notifications            │   │ Notifications Section
│   │  📧 Email Notifications     │   │ Toggle
│   │                             │   │ Switch
│   │  📱 Push Notifications      │   │ Toggle
│   │                             │   │ Switch
│   │  🔔 Leave Reminders         │   │ Toggle
│  │                             │   │ Switch
│   │  ⏰ Clock-in Reminders      │   │ Toggle
│  │                             │   │ Switch
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🌐 App Preferences          │   │ App Preferences
│  │  🌙 Dark Mode               │   │ Toggle
│  │                             │   │ Switch
│  │  🌏 Language: English       │   │ Language
│  │  📅 Date Format: MM/DD/YYYY │   │ Date Format
│  │  ⏰ Timezone: WIB (UTC+7)   │   │ Timezone
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔐 Privacy & Security       │   │ Privacy Section
│   │  👤 Face ID / Touch ID      │   │ Biometric
│  │  🔒 Auto-lock: 5 minutes   │   │ Auto-lock
│  │  📊 Usage Analytics         │   │ Analytics Toggle
│  │                             │   │ Switch
│   │  🗑️ Clear Cache            │   │ Cache Clear
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📱 About                    │   │ About Section
│  │  📱 GSJ HRIS v1.0.0         │   │ App Version
│  │  📄 Terms of Service        │   │ Terms
│  │  🔒 Privacy Policy          │   │ Privacy
│  │  📧 Support: support@gsj.com│   │ Support
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 7. HR Admin Features

### 7.1 Employee Management Dashboard
```
┌─────────────────────────────────────┐
│    HR Dashboard                     │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Quick Stats              │   │ Stats Cards
│   │  👥 250 Total Employees    │   │ Total Count
│   │  ✅ 235 Active              │   │ Active Employees
│   │  📊 15 Pending Approval     │   │ Pending
│   │  🏖️ 45 On Leave Today      │   │ On Leave
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ⏰ Pending Approvals        │   │ Approvals Card
│   │  🏖️ 8 Leave Requests       │   │ Leave Pending
│  │  ✈️ 5 Travel Requests       │   │ Travel Pending
│  │  👤 3 New Registrations     │   │ New Employees
│  │         [Review All]        │   │ Review Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 👥 Recent Activity          │   │ Activity Card
│   │  👤 New: John Doe registered│   │ Registration
│  │  🏖️ Leave: Jane Smith request│   │ Leave Request
│  │  ✈️ Travel: Mike Chen approved│ │ Travel Approval
│  │  📊 Report: Monthly report   │   │ Report
│  │         [View All]          │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │          + Add Employee     │   │ Add Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Reports & Analytics      │   │ Reports Card
│   │  📊 Attendance Report       │   │ Attendance
│  │  🏖️ Leave Analytics         │   │ Leave
│  │  ✈️ Travel Summary          │   │ Travel
│  │  👥 Employee Performance    │   │ Performance
│  │         [View Reports]      │   │
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 7.2 Employee Registration Screen
```
┌─────────────────────────────────────┐
│ ←    Register New Employee          │ Navigation
├─────────────────────────────────────┤
│                                     │
│   👤 New Employee Registration       │ Title
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📛 Personal Information     │   │ Personal Info
│   │  📧 Email                   │   │ Email Field
│   │  👤 First Name              │   │ First Name
│   │  👤 Last Name               │   │ Last Name
│   │  🆔 Employee ID             │   │ Employee ID
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🏢 Employment Details       │   │ Employment Info
│   │  🏢 Department             │   │ Department
│   │  👨‍💼 Position               │   │ Position
│   │  👤 Supervisor              │   │ Supervisor
│   │  🎭 Role                    │   │ Role Selection
│   │  • EMPLOYEE                 │   │
│   │  • SUPERVISOR               │   │
│   │  • HR                       │   │
│   │  • ADMIN                    │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔐 Account Security         │   │ Security
│   │  🔒 Temporary Password      │   │ Password
│   │  👤 Face Template           │   │ Face Template
│   │         [Upload Photo]      │   │ Upload Button
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Leave Configuration      │   │ Leave Setup
│  │  🏖️ Annual Leave: 12 days   │   │ Entitlements
│  │  🤒 Sick Leave: 6 days      │   │
│  │  📅 Start Date              │   │ Leave Start
│  └─────────────────────────────┘   │
│                                     │
│   📋 Summary                      │ Summary Section
│   • John Doe - Senior Developer     │
│   • Technology Department           │
│   • Reports to: Jane Smith          │
│   • Role: EMPLOYEE                  │
│   • Leave: 18 days total            │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Register Employee      │   │ Register Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 8. Notification System

### 8.1 Notification Center
```
┌─────────────────────────────────────┐
│    Notifications                    │ Large Title
├─────────────────────────────────────┤
│                                     │
│   📊 Today                          │ Section Header
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ✅ Leave Approved           │   │ Notification Card
│   │  Your leave request for     │   │
│  │  Oct 15-17 has been         │   │
│  │  approved by Jane Smith     │   │
│  │  2 hours ago                │   │ Timestamp
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ⏰ Clock-in Reminder        │   │ Notification Card
│   │  Don't forget to clock in!  │   │
│  │  Office hours: 8:00 AM      │   │
│  │  30 minutes ago             │   │ Timestamp
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔐 Password Expiring        │   │ Warning Card
│  │  Your password expires in    │   │
│  │  7 days. Please update.      │   │
│  │  Yesterday                  │   │ Timestamp
│   └─────────────────────────────┘   │
│                                     │
│   📊 Yesterday                     │ Section Header
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ✈️ Travel Approved          │   │ Notification Card
│  │  Singapore trip approved     │   │
│  │  by Admin                    │   │
│  │  Yesterday                  │   │ Timestamp
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 📊 Monthly Report Available │   │ Info Card
│  │  October attendance report   │   │
│  │  is now available            │   │
│  │  Yesterday                  │   │ Timestamp
│   └─────────────────────────────┘   │
│                                     │
│   [Mark all as read]               │ Action Button
│   [Notification Settings]          │ Settings Link
│                                     │
└─────────────────────────────────────┘
```

---

## 9. Error States & Empty States

### 9.1 No Internet Connection
```
┌─────────────────────────────────────┐
│    Dashboard                        │ Large Title
├─────────────────────────────────────┤
│                                     │
│                                     │
│         🌐                         │ Icon
│                                     │
│      No Internet Connection        │ Title
│                                     │
│   Please check your internet       │ Message
│   connection and try again.        │
│                                     │
│   ┌─────────────────────────────┐   │
│   │         Retry               │   │ Retry Button
│   └─────────────────────────────┘   │
│                                     │
│   You can still view cached data    │ Info
│   from your last session.           │
│                                     │
│   ┌─────────────────────────────┐   │
│   │     View Cached Data        │   │ Alternative Action
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 9.2 Empty Leave History
```
┌─────────────────────────────────────┐
│    Leave History                    │ Large Title
├─────────────────────────────────────┤
│                                     │
│                                     │
│         🏖️                         │ Icon
│                                     │
│      No Leave Requests Yet          │ Title
│                                     │
│   You haven't submitted any leave   │ Message
│   requests. Start by requesting     │
│   your first leave!                 │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Request Leave          │   │ Primary Action
│   └─────────────────────────────┘   │
│                                     │
│   📊 Leave Balance: 12 days available│ Balance Info
│                                     │
└─────────────────────────────────────┘
```

---

## 10. Loading States & Skeleton Screens

### 10.1 Dashboard Loading
```
┌─────────────────────────────────────┐
│    Dashboard                        │ Large Title
├─────────────────────────────────────┤
│                                     │
│   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ Skeleton Line
│   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │ Skeleton Line
│                                     │
│   ┌─────────────────────────────┐   │ Skeleton Card
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │ Skeleton Card
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │ Skeleton Card
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓   │
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## 11. Interactive Elements & Animations

### 11.1 Face Recognition Animation
```
┌─────────────────────────────────────┐
│ ←    Clock In                       │ Navigation
├─────────────────────────────────────┤
│                                     │
│   📸 Scanning Face...               │ Status Text
│                                     │
│   ┌─────────────────────────────┐   │
│   │    [Camera Preview]         │   │ Camera View
│   │         👤                   │   │ Face
│   │   ⚡ Analyzing...           │   │ Scanning Animation
│   │   🔄 Processing...          │   │ Processing Animation
│   │   ✅ Match Found!           │   │ Success State
│   └─────────────────────────────┘   │
│                                     │
│   Confidence: ████████░░ 80%        │ Progress Bar
│   Verifying facial features...       │ Status Message
│                                     │
│   ┌─────────────────────────────┐   │
│   │       ✅ Clock In           │   │ Success Button
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### 11.2 Success Celebration Animation
```
┌─────────────────────────────────────┐
│    Success!                         │ Navigation
├─────────────────────────────────────┤
│                                     │
│         🎉                         │ Celebration Icon
│                                     │
│      Clock In Successful!           │ Success Message
│                                     │
│   ┌─────────────────────────────┐   │ Success Card
│   │  ✅ Time: 8:00 AM           │   │ Details
│   │  📍 Location: Office        │   │
│   │  👤 Verified: Face ID       │   │
│   │  ⚡ Confidence: 95%         │   │
│   └─────────────────────────────┘   │
│                                     │
│   Have a great day! 🌟              │ Encouragement
│                                     │
│   ┌─────────────────────────────┐   │
│   │        Done                 │   │ Action Button
│   └─────────────────────────────┘   │
│                                     │
│   🎊 Celebrating with confetti     │ Animation Note
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│   🎊 Sparkles and stars animation   │
│                                     │
└─────────────────────────────────────┘
```

---

## 12. Onboarding Flow

### 12.1 Welcome Screen
```
┌─────────────────────────────────────┐
│                                     │ No Navigation
├─────────────────────────────────────┤
│                                     │
│         [GSJ HRIS Logo]             │ Large Logo
│                                     │
│   Welcome to GSJ HRIS!              │ Welcome Title
│                                     │
│   Your complete HR management       │ Description
│   solution right in your pocket.   │
│                                     │
│   • Clock in/out with face ID       │ Feature List
│   • Request leave and travel        │
│   • Track attendance history        │
│   • Approve team requests           │
│   • View reports and analytics      │
│                                     │
│   ┌─────────────────────────────┐   │
│   │        Get Started          │   │ Primary Button
│   └─────────────────────────────┘   │
│                                     │
│   [Skip for now]                    │ Skip Option
│                                     │
│   ●○○○                             │ Progress Indicator
│                                     │
└─────────────────────────────────────┘
```

### 12.2 Feature Introduction
```
┌─────────────────────────────────────┐
│                                     │ No Navigation
├─────────────────────────────────────┤
│                                     │
│         📸                         │ Feature Icon
│                                     │
│   Face Recognition Attendance       │ Feature Title
│                                     │
│   Clock in and out securely using   │ Description
│   facial recognition technology.    │
│   Fast, accurate, and touchless.   │
│                                     │
│   ┌─────────────────────────────┐   │ Feature Preview
│   │    [Face Scan Demo]         │   │ Demo Area
│   │         👤                   │   │
│   │   ✅ Verifying...           │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │         Next                │   │ Action Button
│   └─────────────────────────────┘   │
│                                     │
│   [Skip Tour]                      │ Skip Option
│                                     │
│   ●●○○                             │ Progress Indicator
│                                     │
└─────────────────────────────────────┘
```

---

## Design Principles & User Experience

### Core UX Principles
1. **Simplicity**: Clean, intuitive interface following iOS design patterns
2. **Efficiency**: Quick access to frequently used features
3. **Security**: Biometric authentication and secure data handling
4. **Accessibility**: VoiceOver support and high contrast options
5. **Performance**: Fast loading times and smooth animations

### Key Features Implementation
- **Biometric Integration**: Face ID/Touch ID for quick authentication
- **Geofencing**: Automatic location verification for attendance
- **Offline Support**: Cached data for core functionality
- **Push Notifications**: Real-time updates for approvals and reminders
- **Dark Mode**: System-wide dark mode support
- **Widget Support**: Home screen widgets for quick status

### Security Considerations
- **Data Encryption**: All sensitive data encrypted at rest and in transit
- **Session Management**: Automatic timeout and secure session handling
- **Biometric Security**: Local device authentication for sensitive actions
- **Audit Trail**: Complete logging of all user actions

This comprehensive iOS mockup design covers all the features mentioned in the GSJ HRIS README, providing a complete mobile experience for employees and HR administrators.