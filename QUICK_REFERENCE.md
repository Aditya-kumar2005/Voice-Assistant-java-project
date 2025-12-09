# 🚀 Friend Application - Complete Quick Reference Guide

> **Quick Navigation:**  
> - [How to Use Friend](#-how-to-use-friend) | [All Commands](#-complete-command-reference) | [Preferences](#-preferences-settings) | [Troubleshooting](#-troubleshooting)

---

## 🎤 How to Use Friend

### Getting Started

1. **Start the Application**
   ```bash
   java -jar Friend-1.0-SNAPSHOT.jar
   ```

2. **First Launch Setup** (Onboarding Wizard)
   - Microphone check: Friend tests your microphone
   - TTS test: Hears a voice sample
   - Permission verification: Checks system access
   - Telemetry opt-in: Choose whether to send error reports
   - First-launch complete ✅

3. **Main Window Appears**
   - "Friend: Listening..." status message
   - Ready for voice commands

4. **Give a Voice Command**
   ```
   User: "open gmail"
   Friend: "Opening Gmail in Chrome"
   → Gmail opens in your web browser
   ```

### Basic Voice Command Pattern

```
[Verb] [Object]

Examples:
├─ open gmail           (Verb: open, Object: gmail)
├─ play music           (Verb: play, Object: music)
├─ close notepad        (Verb: close, Object: notepad)
├─ increase volume      (Verb: increase, Object: volume)
├─ open downloads       (Verb: open, Object: downloads)
└─ show commands        (Verb: show, Object: commands)
```

### If Friend Doesn't Hear You

1. **Loud Background Noise**
   - Move to quieter location
   - Adjust microphone levels in Windows Sound Settings

2. **Unclear Pronunciation**
   - Speak clearly and at normal pace
   - Try alternate phrasing: "open gmail" or "go to gmail"

3. **Microphone Issues**
   - Check Preferences → General tab
   - Run health check: Say "help" to see if Friend responds
   - If failed, try "preferences" → General → Check microphone

---

## 📋 Complete Command Reference

### 📱 APPLICATION COMMANDS (Launch/Close Applications)

#### System Utilities
```
OPEN:
├─ "open notepad"           → Launches text editor
├─ "start calculator"       → Opens calculator app
├─ "launch file explorer"   → Opens file browser
├─ "open task manager"      → Opens task manager
├─ "start settings"         → Opens Windows Settings app
├─ "open control panel"     → Opens Control Panel
├─ "launch snipping tool"   → Opens screenshot tool
└─ "open paint"             → Opens paint application

CLOSE:
├─ "close notepad"          → Closes notepad
├─ "exit calculator"        → Closes calculator
├─ "quit task manager"      → Closes task manager
└─ "terminate settings"     → Closes settings

EXAMPLE USAGE:
User:   "open notepad"
Friend: "Launching Notepad"
Result: Notepad window opens ✅
```

#### Microsoft Office
```
OPEN:
├─ "open word"              → Microsoft Word
├─ "launch excel"           → Excel spreadsheet
├─ "start powerpoint"       → PowerPoint presentation
├─ "open access"            → Access database
├─ "launch publisher"       → Publisher application
└─ "open onenote"           → OneNote notes

CLOSE:
├─ "close word"             → Exit Word
├─ "exit excel"             → Exit Excel
└─ [similar for others]

EXAMPLE USAGE:
User:   "open excel"
Friend: "Starting Microsoft Excel"
Result: Excel launches ✅
```

#### Development Tools
```
OPEN:
├─ "open visual studio code" → VS Code editor
├─ "launch sublime text"    → Sublime Text editor
├─ "start eclipse"          → Eclipse IDE
├─ "open pycharm"           → PyCharm IDE
├─ "launch intellij"        → IntelliJ IDEA
├─ "open android studio"    → Android Studio
└─ "start netbeans"         → NetBeans IDE

EXAMPLE USAGE:
User:   "open visual studio code"
Friend: "Launching VS Code"
Result: VS Code opens ✅
```

#### Communication & Collaboration
```
OPEN:
├─ "open teams"             → Microsoft Teams
├─ "start skype"            → Skype
├─ "launch zoom"            → Zoom video conference
├─ "open discord"           → Discord chat
├─ "start slack"            → Slack messaging
├─ "open whatsapp"          → WhatsApp desktop

CLOSE:
├─ "close teams"            → Exit Teams
├─ "quit zoom"              → Exit Zoom
└─ [similar for others]

EXAMPLE USAGE:
User:   "open teams"
Friend: "Starting Microsoft Teams"
Result: Teams launches and loads ✅
```

#### Web Browsers
```
OPEN:
├─ "open chrome"            → Google Chrome
├─ "start firefox"          → Mozilla Firefox
├─ "launch edge"            → Microsoft Edge
├─ "open safari"            → Apple Safari
├─ "start opera"            → Opera browser
├─ "launch brave"           → Brave browser
└─ "open internet explorer" → Internet Explorer

CLOSE:
├─ "close chrome"           → Exit Chrome
├─ "exit firefox"           → Exit Firefox
└─ [similar for others]

KEY FEATURE:
These commands respect your browser preference!
If you set preferred browser to Edge:
  User: "open gmail"
  → Opens Gmail in EDGE (not Chrome by default)

EXAMPLE USAGE:
User:   "open firefox"
Friend: "Launching Firefox"
Result: Firefox browser opens ✅
```

### 🌐 WEBSITE/WEB COMMANDS

#### Popular Websites
```
OPEN:
├─ "open gmail"             → Gmail email
├─ "visit youtube"          → YouTube video platform
├─ "browse google"          → Google search
├─ "go to facebook"         → Facebook social media
├─ "open twitter"           → Twitter (X)
├─ "visit instagram"        → Instagram
├─ "browse linkedin"        → LinkedIn professional
├─ "open stack overflow"    → Stack Overflow Q&A
├─ "visit github"           → GitHub repository
├─ "browse reddit"          → Reddit community
├─ "open quora"             → Quora Q&A
├─ "visit amazon"           → Amazon shopping
├─ "browse flipkart"        → Flipkart shopping (India)
├─ "open news"              → Google News
└─ "visit weather"          → Weather.com

ALL RESPECTS BROWSER PREFERENCE!
If your preferred browser is Microsoft Edge:
  User: "open github"
  → Opens GitHub in EDGE (not Chrome)

CLOSE:
├─ "close chrome"           → Exit browser
├─ "quit firefox"           → Exit browser
└─ "exit browser"           → Close all browsers

EXAMPLE USAGE:
User:   "open gmail"
Friend: "Opening Gmail in your preferred browser"
Result: Gmail website opens in selected browser ✅
```

### 🎵 MEDIA COMMANDS (Audio/Video Control)

#### Playback Control
```
PLAY:
├─ "play music"             → Start music player
├─ "play"                   → Resume playback
├─ "start"                  → Start media playback
└─ "resume"                 → Resume paused media

PAUSE/STOP:
├─ "pause music"            → Pause playback
├─ "pause"                  → Pause current media
├─ "stop music"             → Stop playback
└─ "stop"                   → Stop media playback

MUTE/UNMUTE:
├─ "mute"                   → Mute audio output
├─ "unmute"                 → Unmute audio
├─ "mute audio"             → Mute (explicit)
└─ "unmute audio"           → Unmute (explicit)

EXAMPLE USAGE:
User:   "play music"
Friend: "Playing music"
Result: Default media player starts ✅

User:   "pause"
Friend: "Paused"
Result: Current song pauses ✅
```

#### Volume Control
```
INCREASE VOLUME:
├─ "increase volume"        → Raise volume by 10%
├─ "volume up"              → Raise volume
├─ "louder"                 → Increase loudness
├─ "turn up volume"         → Raise volume level
└─ "make it louder"         → Increase volume

DECREASE VOLUME:
├─ "decrease volume"        → Lower volume by 10%
├─ "volume down"            → Lower volume
├─ "quieter"                → Decrease loudness
├─ "turn down volume"       → Lower volume level
└─ "make it quieter"        → Decrease volume

STATUS CHECK:
├─ "what's playing"         → Show current media
├─ "is anything playing"    → Check playback status
├─ "what's playing now"     → Current track
└─ "show now playing"       → Display current media

EXAMPLE USAGE:
User:   "volume up"
Friend: "Volume increased"
Result: System volume raises by ~10% ✅

User:   "what's playing"
Friend: "Song name: [Current track]"
Result: Shows current media ✅

NOTE: These commands control Windows system volume
      (Affects all audio on computer)
```

### 💻 SYSTEM COMMANDS (Windows Control)

#### Power Control
```
SHUTDOWN:
├─ "shutdown"               → Initiate shutdown
├─ "shut down"              → Shutdown computer
├─ "power off"              → Turn off computer
├─ "close system"           → Shutdown computer
└─ "system shutdown"        → Initiate power down

RESTART:
├─ "restart"                → Restart computer
├─ "reboot"                 → Reboot system
├─ "restart computer"       → Initiate restart
└─ "reboot computer"        → Reboot computer

SLEEP/LOCK:
├─ "sleep"                  → Put computer to sleep
├─ "lock"                   → Lock Windows session
├─ "lock screen"            → Lock computer
└─ "go to sleep"            → Sleep mode

EXAMPLE USAGE:
User:   "shutdown"
Friend: "Shutting down the computer. Goodbye!"
Result: Windows begins shutdown sequence ✅

User:   "lock"
Friend: "Locking the screen"
Result: Windows login screen appears ✅
```

#### System Utilities & Settings
```
OPEN UTILITIES:
├─ "open settings"          → Windows Settings app
├─ "open control panel"     → Control Panel
├─ "open task scheduler"    → Task Scheduler
├─ "open calendar"          → Calendar app
├─ "open clock"             → Clock app
├─ "open copilot"           → Windows Copilot AI
└─ "open whatsapp"          → WhatsApp desktop

EXAMPLE USAGE:
User:   "open task scheduler"
Friend: "Opening Task Scheduler"
Result: Task Scheduler window opens ✅

USER NOTE:
⚠️ "open settings" = Windows Settings app
   "settings" (alone) = Friend Preferences dialog
   These are DIFFERENT!
```

### 📁 FOLDER COMMANDS (File System Navigation)

#### Opening Folders
```
OPEN SYSTEM FOLDERS:
├─ "open desktop"           → Opens Desktop folder
├─ "open documents"         → Opens Documents folder
├─ "open downloads"         → Opens Downloads folder
├─ "open music"             → Opens Music folder

VARIATIONS (same effect):
├─ "explore desktop"        → Opens Desktop in Explorer
├─ "browse documents"       → Opens Documents in Explorer
├─ "go to downloads"        → Opens Downloads folder
└─ "show music folder"      → Opens Music folder

OPEN METHODS:
1. File Explorer (default) - Graphical file browser
2. PowerShell - Command-line interface
3. Command Prompt (cmd) - Windows command line

EXAMPLE USAGE:
User:   "open downloads"
Friend: "Opening Downloads folder"
Result: Downloads folder opens in File Explorer ✅

User:   "explore documents"
Friend: "Opening Documents in Explorer"
Result: Documents folder opens ✅
```

#### Searching in Folders
```
SEARCH OPERATIONS:
├─ "search desktop"         → Open PowerShell in Desktop
├─ "search documents"       → Open PowerShell in Documents
├─ "search downloads"       → Open PowerShell in Downloads
└─ "search music"           → Open PowerShell in Music

Alternative:
├─ "find in desktop"        → PowerShell in Desktop
├─ "look in documents"      → PowerShell in Documents
└─ "browse downloads"       → PowerShell in Downloads

EXAMPLE USAGE:
User:   "search desktop"
Friend: "Opening PowerShell in Desktop"
Result: PowerShell window opens in Desktop directory ✅
```

#### Folder Customization
```
IN PREFERENCES DIALOG:
1. Open Preferences (say "preferences")
2. Go to "Folders" tab
3. Click [Browse...] for each folder:
   ├─ Desktop
   ├─ Documents
   ├─ Downloads
   └─ Music
4. Select your preferred folder
5. Click [Save]

EXAMPLE:
If you set:
├─ Desktop: C:\My Stuff\Desktop
├─ Documents: D:\OneDrive\Documents
├─ Downloads: E:\Downloads\Files
└─ Music: F:\My Music\Playlists

Then:
User: "open downloads"
→ Opens E:\Downloads\Files (your custom path) ✅
```

### ⏳ LIFECYCLE & HELP COMMANDS

#### Help & Documentation
```
HELP COMMANDS:
├─ "help"                   → Speaks all available commands
├─ "show commands"          → Display help window
├─ "show help"              → Display help GUI
├─ "list commands"          → List available commands
└─ "what can you do"        → List capabilities

SETTINGS & PREFERENCES:
├─ "preferences"            → Open preferences dialog
├─ "settings"               → Open preferences dialog (alias)
├─ "open settings"          → Open preferences dialog
└─ "configure"              → Open preferences

EXAMPLE USAGE:
User:   "help"
Friend: Speaks list of all 50+ available commands
        Provides categories and examples

User:   "show commands"
Friend: Opens help window with full command list ✅

User:   "preferences"
Friend: Opens JavaFX preferences dialog
        → 5 tabs (General, Audio, Browser, Folders, Advanced)
```

#### Pause & Resume
```
PAUSE RECOGNITION:
├─ "pause"                  → Pause voice listening
├─ "stop listening"         → Pause recognition
├─ "pause listening"        → Stop hearing voice commands
└─ "quiet"                  → Pause voice input

RESUME RECOGNITION:
├─ "resume"                 → Resume voice listening
├─ "start listening"        → Start recognition again
├─ "listen"                 → Begin listening again
└─ "activate"               → Activate recognition

EXAMPLE USAGE:
User:   "pause"
Friend: "Paused"
Result: Friend stops listening for commands ✅

User:   "resume"
Friend: "Resuming"
Result: Friend listens for commands again ✅

NOTE: Useful when you want to have normal conversation
      without triggering Friend accidentally
```

---

## ⚙️ PREFERENCES & SETTINGS

### Opening Preferences

```
METHOD 1: Voice Command
├─ Say: "preferences"
└─ Say: "settings"
Result: Preferences dialog opens

METHOD 2: GUI Click (if available)
├─ Click [Settings] button in main window
└─ Preferences dialog opens

METHOD 3: Programmatic (developers only)
├─ MergedEchoPilotApp.showPreferencesDialog()
└─ Dialog opens

All methods show the same JavaFX preferences dialog
with 5 tabs and 11 configurable settings.
```

### Tab 1: GENERAL Settings

```
┌─────────────────────────────────────────────┐
│              GENERAL TAB                    │
├─────────────────────────────────────────────┤
│                                              │
│  1. WAKE WORD                               │
│     Current: "friend"                       │
│     Can change to any word/phrase           │
│     Examples: "activate", "listen", "hey"   │
│     Purpose: Activate Friend app            │
│     How to use: Type new word, click Save   │
│                                              │
│  2. THEME                                   │
│     Options: light (default) / dark         │
│     Light: White background, dark text      │
│     Dark: Dark background, light text       │
│     Purpose: Comfortable viewing            │
│     How to use: Click dropdown, select, Save│
│     Changes immediately in real-time ✨     │
│                                              │
│  3. ENABLE TELEMETRY                        │
│     Default: OFF (unchecked)                │
│     When enabled: Error reports are sent    │
│     Purpose: Help improve Friend app        │
│     How to use: Check/uncheck box, Save     │
│     Note: Optional, you choose              │
│                                              │
└─────────────────────────────────────────────┘

EXAMPLE USAGE:
1. User says: "preferences"
   → Preferences dialog opens
2. Click "General" tab
3. Change Wake Word to: "activate"
4. Change Theme to: "dark"
5. Enable Telemetry: [☑]
6. Click [Save]
7. Settings saved and applied ✅
```

### Tab 2: AUDIO Settings

```
┌─────────────────────────────────────────────┐
│               AUDIO TAB                     │
├─────────────────────────────────────────────┤
│                                              │
│  1. VOICE                                   │
│     Options: default / male / female        │
│     Default: Uses system default voice      │
│     Purpose: Choose TTS voice type          │
│     How to use: Select from dropdown, Save  │
│     Effect: Changes how Friend sounds       │
│                                              │
│  2. GENDER                                  │
│     Options: male / female                  │
│     Default: "male"                         │
│     Purpose: Additional voice selection     │
│     How to use: Select gender, click Save   │
│     Effect: Adjusts voice characteristics   │
│                                              │
│  3. VOLUME                                  │
│     Range: 0% (silent) to 100% (maximum)   │
│     Default: 80%                            │
│     Slider: Drag to adjust                  │
│     Live feedback: Shows current percentage │
│     How to use: Drag slider, click Save     │
│     Note: App volume × Windows volume       │
│                                              │
│  4. SPEECH RATE                             │
│     Range: 0.5x (half speed) to 2.0x (double) │
│     Default: 1.0x (normal speed)            │
│     Slider: Drag to adjust                  │
│     Examples:                               │
│       0.5x = Very slow (easy to understand) │
│       1.0x = Normal (default)               │
│       1.5x = Faster (intermediate)          │
│       2.0x = Very fast (double speed)       │
│     How to use: Drag slider, click Save     │
│                                              │
└─────────────────────────────────────────────┘

EXAMPLE USAGE:
1. User says: "preferences"
   → Preferences opens
2. Click "Audio" tab
3. Select Voice: "male"
4. Select Gender: "male"
5. Drag Volume slider to 70%
6. Drag Speech Rate to 1.5x (faster)
7. Click [Save]
8. Friend now speaks faster in male voice ✅

REAL-WORLD SCENARIO:
- Want Friend to speak faster for accessibility
- Set Speech Rate to 2.0x (double speed)
- Set Volume to 100% for hearing impaired
- Save changes
- Friend now speaks twice as fast ✅
```

### Tab 3: BROWSER Settings

```
┌─────────────────────────────────────────────┐
│              BROWSER TAB                    │
├─────────────────────────────────────────────┤
│                                              │
│  PREFERRED BROWSER                          │
│  Options: chrome / msedge / firefox / ie    │
│  Default: "chrome"                          │
│                                              │
│  This affects ALL web URLs!                 │
│                                              │
│  Examples of impact:                        │
│    User: "open gmail"                       │
│    ├─ Browser = Chrome                      │
│    │  → Opens Gmail in Chrome               │
│    │                                        │
│    ├─ Browser = Edge                        │
│    │  → Opens Gmail in Microsoft Edge       │
│    │                                        │
│    ├─ Browser = Firefox                     │
│    │  → Opens Gmail in Firefox              │
│    │                                        │
│    └─ Browser = IE                          │
│       → Opens Gmail in Internet Explorer    │
│                                              │
│  TEST BUTTON: [Test Browser]                │
│  ├─ Verifies browser is installed           │
│  ├─ Launches test URL (google.com)          │
│  ├─ Confirms settings work                  │
│  └─ Shows success/error message             │
│                                              │
└─────────────────────────────────────────────┘

HOW TO USE:
1. User says: "preferences"
   → Dialog opens
2. Click "Browser" tab
3. Select your preferred browser:
   └─ msedge (Microsoft Edge)
4. Click [Test Browser]
   → Google opens in Edge to verify
   → Success message appears ✅
5. Click [Save]
6. All web commands now use Edge

AFFECTED COMMANDS:
These now launch in your preferred browser:
├─ "open gmail"
├─ "visit github"
├─ "browse amazon"
├─ "open youtube"
├─ "go to facebook"
└─ [All 15+ web commands]
```

### Tab 4: FOLDERS Settings

```
┌─────────────────────────────────────────────┐
│               FOLDERS TAB                   │
├─────────────────────────────────────────────┤
│                                              │
│  Customize default folder locations         │
│                                              │
│  Four Folders You Can Customize:            │
│  ├─ Desktop                                 │
│  ├─ Documents                               │
│  ├─ Downloads                               │
│  └─ Music                                   │
│                                              │
│  For each folder:                           │
│  ├─ Current path shown in text field        │
│  ├─ [Browse...] button to select new path  │
│  └─ Changes affect "open [folder]" commands│
│                                              │
└─────────────────────────────────────────────┘

HOW TO USE:
1. User says: "preferences"
   → Dialog opens
2. Click "Folders" tab
3. See current folder paths:
   ├─ Desktop: C:\Users\You\Desktop
   ├─ Documents: C:\Users\You\Documents
   ├─ Downloads: C:\Users\You\Downloads
   └─ Music: C:\Users\You\Music

4. Change Downloads folder:
   ├─ Click [Browse...] for Downloads
   ├─ Select new location: D:\My Downloads
   ├─ Path updates in text field
   └─ Click [Save]

5. Now when you say:
   User: "open downloads"
   → Opens D:\My Downloads (not default) ✅

ADVANCED EXAMPLE:
Set up cloud storage:
├─ Desktop: OneDrive\Desktop
├─ Documents: OneDrive\Documents
├─ Downloads: Dropbox\Downloads
└─ Music: Google Drive\Music

Then all commands use cloud-synced folders!
```

### Tab 5: ADVANCED Settings

```
┌─────────────────────────────────────────────┐
│              ADVANCED TAB                   │
├─────────────────────────────────────────────┤
│                                              │
│  1. LOG LEVEL                               │
│     Options: TRACE (0) through ERROR (4)    │
│     Default: INFO (2)                       │
│                                              │
│     Levels (most to least verbose):         │
│     ├─ TRACE (0) = All details (very verbose) │
│     ├─ DEBUG (1) = Debug information        │
│     ├─ INFO (2) = General info (default)    │
│     ├─ WARN (3) = Warnings only             │
│     └─ ERROR (4) = Errors only (quiet)      │
│                                              │
│     Purpose: Debug troubleshooting          │
│     How to use: Select level, click Save    │
│     Effect: Changes logs/console output     │
│                                              │
│  2. LANGUAGE MODEL PATH (Extensible)        │
│     Currently: [Not implemented]            │
│     Future: Point to custom grammar files   │
│     How to use: Type path to .model file    │
│                                              │
│  3. GRAMMAR FILE PATH (Extensible)          │
│     Currently: [Not implemented]            │
│     Future: Point to custom grammar files   │
│     How to use: Type path to .gram file     │
│                                              │
└─────────────────────────────────────────────┘

EXAMPLE USAGE:
For troubleshooting:
1. User says: "preferences"
2. Click "Advanced" tab
3. Change Log Level from INFO (2) → DEBUG (1)
4. Click [Save]
5. Check logs/friend.log for detailed output
6. Debug information now shows in console ✅

CHECKING LOGS:
├─ Console: Real-time output while running
├─ File: logs/friend.log (all messages)
├─ Errors: logs/friend-errors.log (errors only)
└─ Report: logs/error-report.log (opt-in)
```

### Preferences Quick Reference Table

```
┌──────────────────────┬──────────────────┬──────────┬─────────────────────┐
│ Setting              │ Type             │ Default  │ Range/Options       │
├──────────────────────┼──────────────────┼──────────┼─────────────────────┤
│ GENERAL              │                  │          │                     │
│ Wake Word            │ String           │ friend   │ Any text            │
│ Theme                │ Dropdown         │ light    │ light / dark        │
│ Telemetry            │ Checkbox         │ OFF      │ ON / OFF            │
├──────────────────────┼──────────────────┼──────────┼─────────────────────┤
│ AUDIO                │                  │          │                     │
│ Voice                │ Dropdown         │ default  │ default/male/female │
│ Gender               │ Dropdown         │ male     │ male / female       │
│ Volume               │ Slider           │ 80%      │ 0% - 100%           │
│ Speech Rate          │ Slider           │ 1.0x     │ 0.5x - 2.0x         │
├──────────────────────┼──────────────────┼──────────┼─────────────────────┤
│ BROWSER              │                  │          │                     │
│ Preferred Browser    │ Dropdown         │ chrome   │ chrome/edge/ff/ie   │
├──────────────────────┼──────────────────┼──────────┼─────────────────────┤
│ FOLDERS              │                  │          │                     │
│ Desktop              │ Path selector    │ System   │ Any accessible path │
│ Documents            │ Path selector    │ System   │ Any accessible path │
│ Downloads            │ Path selector    │ System   │ Any accessible path │
│ Music                │ Path selector    │ System   │ Any accessible path │
├──────────────────────┼──────────────────┼──────────┼─────────────────────┤
│ ADVANCED             │                  │          │                     │
│ Log Level            │ Dropdown         │ INFO (2) │ TRACE(0)-ERROR(4)   │
│ Language Model Path  │ Text field       │ (empty)  │ Any .model file     │
│ Grammar File Path    │ Text field       │ (empty)  │ Any .gram file      │
└──────────────────────┴──────────────────┴──────────┴─────────────────────┘
```

---

## 🆘 TROUBLESHOOTING

### Problem: Friend Won't Start

**Symptoms:** No window opens, application doesn't start

**Solutions:**
```
1. Check Java Installation
   Command: java -version
   Expected: Java 21 or higher
   If failed: Install Java 21 JDK

2. Check File Permissions
   ├─ Right-click Friend.jar
   ├─ Properties → Security
   ├─ Unblock if needed
   └─ Run again

3. Check Microphone Connection
   ├─ Plug in microphone (if USB)
   ├─ Check Windows Sound Settings
   ├─ Set as default recording device
   └─ Run Friend again

4. Run in Command Line (for error messages)
   Command: java -jar Friend-1.0-SNAPSHOT.jar
   → Look for error messages
   → Search error online or check logs/
```

### Problem: Microphone Not Detected

**Symptoms:** "Microphone unavailable" message, no voice input

**Solutions:**
```
1. Windows Sound Settings
   ├─ Settings → System → Sound
   ├─ Recording tab
   ├─ Check if microphone listed
   ├─ Set as default recording device
   └─ Test microphone

2. Update Audio Drivers
   ├─ Device Manager
   ├─ Audio inputs and outputs
   ├─ Right-click microphone
   ├─ Update driver
   └─ Restart computer

3. Privacy Settings (Windows 10/11)
   ├─ Settings → Privacy → Microphone
   ├─ Turn on microphone access
   ├─ Enable for apps
   └─ Restart Friend

4. Check Health
   ├─ Say: "help"
   ├─ If no response: Microphone issue
   ├─ Try health check
   ├─ Run "preferences"
   └─ Check settings
```

### Problem: Friend Doesn't Recognize Commands

**Symptoms:** Says "Didn't understand" or no response, even clear speech

**Solutions:**
```
1. Speak Clearly
   ├─ Slow down speech
   ├─ Speak at normal volume
   ├─ Avoid mumbling
   ├─ Try again

2. Try Exact Phrases
   ├─ Instead of: "open the email"
   ├─ Try: "open gmail"
   ├─ Use exact command phrases
   ├─ Reference QUICK_REFERENCE

3. Reduce Background Noise
   ├─ Move to quiet location
   ├─ Close applications with audio
   ├─ Close window/door for noise
   ├─ Try command again

4. Adjust Microphone Levels
   ├─ Windows Sound Settings
   ├─ Microphone level too low?
   ├─ Increase microphone boost
   ├─ Try command again

5. Check Grammar Files
   ├─ Say: "preferences"
   ├─ Advanced tab
   ├─ Verify grammar files present
   ├─ Check logs for errors

6. See Available Commands
   ├─ Say: "help"
   ├─ Or: "show commands"
   ├─ Displays help window
   ├─ Try exact command phrases
```

### Problem: Volume/Audio Issues

**Symptoms:** Can't hear Friend, audio too loud/quiet

**Solutions:**
```
1. Adjust Friend Volume
   ├─ Say: "preferences"
   ├─ Audio tab
   ├─ Adjust Volume slider (0-100%)
   ├─ Click Save
   └─ Test

2. Adjust Windows System Volume
   ├─ Click speaker icon (system tray)
   ├─ Adjust system volume slider
   ├─ Or say: "volume up" / "volume down"
   └─ Test

3. Check Speaker/Headphones
   ├─ Are speakers plugged in?
   ├─ Is volume turned on?
   ├─ Try different speaker/headphone
   ├─ Check Windows sound output device

4. Adjust Speech Rate
   ├─ Say: "preferences"
   ├─ Audio tab
   ├─ Speech Rate slider
   ├─ Try 2.0x for clearer speech
   └─ Save

5. Unmute Audio
   ├─ Say: "unmute"
   ├─ Or manually click speaker icon
   ├─ Check mute status
   └─ Try again
```

### Problem: Preferences Won't Open

**Symptoms:** Say "preferences" but nothing happens

**Solutions:**
```
1. Try Different Command
   ├─ Try: "settings" (alternate)
   ├─ Or: "open settings" (system)
   ├─ Note: Different result expected
   ├─ Try all variants

2. Check if Friend is Listening
   ├─ Say: "help"
   ├─ If response: Friend working
   ├─ Try "preferences" again
   ├─ Wait for dialog

3. Check Window
   ├─ Dialog might be behind main window
   ├─ Check taskbar for new window
   ├─ Click on taskbar to bring front
   └─ Or click main window then preferences

4. Restart Application
   ├─ Close Friend completely
   ├─ Wait 2 seconds
   ├─ Restart application
   ├─ Try "preferences" again

5. Check Logs
   ├─ Open logs/friend.log
   ├─ Search for error messages
   ├─ Look for exception stack trace
   ├─ Report error if needed
```

### Problem: Settings Not Saving

**Symptoms:** Change setting but reverts after restart

**Solutions:**
```
1. Verify Click Save
   ├─ Make change in preferences
   ├─ Confirm you clicked [Save] button
   ├─ Watch for "Settings saved" message
   ├─ Try again if not saved

2. Check File Permissions
   ├─ Navigate to app folder
   ├─ Right-click settings.json
   ├─ Properties → General
   ├─ Check if file is read-only
   ├─ If read-only: Uncheck, Apply, OK
   ├─ Try saving preferences again

3. Check Disk Space
   ├─ Windows Settings → System → Storage
   ├─ Check available space
   ├─ If < 100MB: Free up space
   ├─ Try saving again

4. Backup & Reset
   ├─ Open preferences
   ├─ Click [Reset to Defaults]
   ├─ Confirm dialog
   ├─ Click [Save]
   ├─ Make new settings
   ├─ Click [Save]
   └─ Should persist now

5. Manual Reset
   ├─ Close Friend application
   ├─ Delete settings.json file
   ├─ Restart Friend
   ├─ Onboarding runs
   ├─ Create new settings
   └─ Configure again
```

### Problem: Browser Won't Open

**Symptoms:** Say "open gmail" but browser doesn't launch

**Solutions:**
```
1. Verify Browser Installed
   ├─ Open Preferences
   ├─ Browser tab
   ├─ Click [Test Browser]
   ├─ Google should open
   ├─ If fails: Browser not installed

2. Install Missing Browser
   ├─ Download from official site:
   │  ├─ Chrome: google.com/chrome
   │  ├─ Edge: microsoft.com/edge
   │  ├─ Firefox: mozilla.org
   │  └─ Or: built into Windows (IE)
   ├─ Install and restart
   ├─ Try command again

3. Check Browser Preference
   ├─ Say: "preferences"
   ├─ Browser tab
   ├─ Verify selected browser installed
   ├─ Change to different browser if needed
   ├─ Click [Test Browser]
   ├─ Save

4. Manual Launch
   ├─ Open browser manually
   ├─ Go to URL (gmail.com, etc)
   ├─ Verify working
   ├─ If manual works: Friend issue
   ├─ Check logs for errors

5. Try Command Variation
   ├─ Instead: "open gmail"
   ├─ Try: "visit gmail"
   ├─ Or: "go to gmail"
   ├─ Or: "browse gmail"
   └─ Some variations may work better
```

### Problem: Getting Error Messages

**Symptoms:** Error alerts or exceptions in console

**Solutions:**
```
1. Read Error Message
   ├─ Note exact error text
   ├─ Take screenshot
   ├─ Search error online
   └─ Look for pattern/cause

2. Check Logs
   ├─ Open logs/friend.log
   ├─ Search for error message
   ├─ Look for stack trace
   ├─ Note time and context
   └─ May reveal root cause

3. Increase Log Level
   ├─ Say: "preferences"
   ├─ Advanced tab
   ├─ Log Level → DEBUG (1) or TRACE (0)
   ├─ Click Save
   ├─ Reproduce error
   ├─ Check logs again
   └─ More details visible

4. Try Reset
   ├─ Say: "preferences"
   ├─ Click [Reset to Defaults]
   ├─ Confirm and Save
   ├─ Restart application
   ├─ Try again

5. Clean Reinstall
   ├─ Delete Friend folder
   ├─ Delete settings.json
   ├─ Delete logs/ folder
   ├─ Re-extract application
   ├─ Run fresh
   └─ Configure again
```

### Emergency Contact / Support

```
If issue persists:

1. Gather Information
   ├─ Error message (exact text)
   ├─ Log file (logs/friend.log)
   ├─ steps to reproduce
   ├─ Screenshot of issue
   └─ Your system info (Windows version, Java version)

2. Online Resources
   ├─ Check GitHub issues
   ├─ Search Stack Overflow
   ├─ Review documentation
   └─ Check FAQ

3. Manual Workarounds
   ├─ Try different command phrase
   ├─ Use GUI instead of voice (if available)
   ├─ Restart application
   ├─ Restart computer
   └─ Update Java/drivers
```

---

## 📚 Additional Resources

- **Documentation Folder:** See companion files
  - `VISUAL_SUMMARY.md` - Architecture diagrams
  - `PHASE2_PREFERENCES_SUMMARY.md` - Preferences details
  - `ENHANCEMENT_SUMMARY.md` - All features overview

- **Log Files:** Located in `logs/` directory
  - `friend.log` - All application messages
  - `friend-errors.log` - Errors only
  - Check these for troubleshooting

- **Settings File:** `settings.json`
  - Can be manually edited (JSON format)
  - Back up before major changes
  - Auto-loads on application start

---

**Quick Tip:** Save this reference guide for easy access!  
**Have fun with Friend!** 🎉

---

*Friend Application - Your voice-activated assistant*  
*Version 1.0 | Java 21 | November 2025*
