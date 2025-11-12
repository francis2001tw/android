# DeepThinking App - Testing Guide

## 🎯 Overview
This guide will help you test the **Deep Thinking Card** feature and verify that it displays AI reasoning in real-time, consistent with the rikkahub project.

## 📱 Installation Status
✅ **App Installed**: The app has been successfully installed on emulator-5554  
✅ **App Launched**: MainActivity is running

## 🧪 Testing the Deep Thinking Card

### Expected Behavior (from rikkahub)

The Deep Thinking Card should:

1. **Start Hidden (Collapsed State)**
   - Initially not visible when a new message starts

2. **Auto-Expand to Preview (3 lines) When Streaming Starts**
   - As soon as `reasoning_content` starts arriving from the API
   - Shows first 3 lines of reasoning
   - Displays shimmer loading effect
   - Auto-scrolls to bottom as new content arrives

3. **Real-Time Content Updates**
   - Text appears character by character as it streams from the API
   - Smooth scrolling to keep latest content visible
   - Character count updates in real-time
   - Duration timer shows elapsed time

4. **Manual Expansion**
   - Click to expand to full view (Expanded State)
   - Click again to collapse back to Preview
   - Scroll through full reasoning content

5. **Auto-Collapse When Complete**
   - When streaming finishes, card auto-collapses to Collapsed state
   - Reasoning content is transferred to the main message
   - Can still be expanded manually to view reasoning

### Test Scenarios

#### Test 1: Basic Text Message with Reasoning
**Steps:**
1. Open the app (already running on emulator)
2. Type a message that requires reasoning, e.g., "Explain quantum entanglement"
3. Press Send

**Expected Results:**
- ✅ Message appears in chat
- ✅ Deep Thinking Card appears and auto-expands to Preview (3 lines)
- ✅ Shimmer effect shows while loading
- ✅ Reasoning text streams in character by character
- ✅ Character count updates (e.g., "1,234 chars")
- ✅ Duration shows elapsed time (e.g., "5.2s")
- ✅ Auto-scrolls to bottom as content grows
- ✅ When complete, card auto-collapses
- ✅ Final answer appears in main message bubble

#### Test 2: Manual Expansion/Collapse
**Steps:**
1. After a message with reasoning is complete
2. Click on the collapsed Deep Thinking Card
3. Click again to collapse

**Expected Results:**
- ✅ Card expands to show full reasoning content
- ✅ Can scroll through all reasoning text
- ✅ Card collapses back when clicked again

#### Test 3: Multiple Messages with Reasoning
**Steps:**
1. Send multiple messages in sequence
2. Observe each message's reasoning card

**Expected Results:**
- ✅ Each message has its own Deep Thinking Card
- ✅ Previous cards remain collapsed
- ✅ Only the current streaming card is expanded
- ✅ No interference between cards

#### Test 4: Multimodal Input
**Steps:**
1. Click the image icon to upload an image
2. Add a text prompt like "What's in this image?"
3. Send the message

**Expected Results:**
- ✅ Image appears in the message
- ✅ Deep Thinking Card shows reasoning about the image
- ✅ Streaming behavior is identical to text-only messages

## 🔍 Monitoring Real-Time Behavior

### Logcat Monitoring
The app is currently being monitored with logcat. To see detailed logs:

```bash
C:\Users\frank\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat -s DeepThinking:* AndroidRuntime:E
```

### Key Events to Watch For:
1. **API Connection**: SSE connection established
2. **Chunk Received**: Each streaming chunk from DeepSeek API
3. **Reasoning Content**: `reasoning_content` field updates
4. **Content Field**: Main `content` field updates
5. **Card State Changes**: Collapsed → Preview → Expanded transitions

## 🎨 Visual Indicators

### Deep Thinking Card States

**Collapsed (Hidden)**
```
[No card visible]
```

**Preview (3 lines, auto-expanded during streaming)**
```
┌─────────────────────────────────────┐
│ 🧠 Deep Thinking... (shimmer)      │
│ First line of reasoning...          │
│ Second line of reasoning...         │
│ Third line of reasoning...          │
│ 1,234 chars • 5.2s              ▼   │
└─────────────────────────────────────┘
```

**Expanded (full view)**
```
┌─────────────────────────────────────┐
│ 🧠 Deep Thinking                    │
│ [Full reasoning content scrollable] │
│ ...                                 │
│ ...                                 │
│ ...                                 │
│ 1,234 chars • 5.2s              ▲   │
└─────────────────────────────────────┘
```

## 🐛 Troubleshooting

### Issue: Card doesn't appear
**Check:**
- API key is valid in `DeepSeekApi.kt`
- Internet connection is available
- DeepSeek API is responding with `reasoning_content`

### Issue: No streaming, content appears all at once
**Check:**
- SSE connection is working (check logcat)
- `stream: true` is set in API request
- EventSource is properly handling events

### Issue: Card doesn't auto-scroll
**Check:**
- `LaunchedEffect` is triggering on `reasoning.reasoning` changes
- `scrollState.animateScrollTo(scrollState.maxValue)` is being called

### Issue: Shimmer effect not showing
**Check:**
- `isLoading` parameter is true during streaming
- Infinite transition animation is running

## 📊 Debug Page Testing

### Steps:
1. Click the "Debug" button in the top bar
2. View conversation logs

**Expected Results:**
- ✅ All conversations listed in JSON format
- ✅ Each log shows:
  - Timestamp
  - Input type (text/image/document)
  - Output model (deepseek-chat)
  - Token usage (prompt, completion, reasoning, total)
  - Full message content
- ✅ "Copy All" button copies all logs to clipboard

## 🎯 Success Criteria

The Deep Thinking feature is working correctly if:

1. ✅ Reasoning appears in real-time as it streams from the API
2. ✅ Card auto-expands to Preview (3 lines) when streaming starts
3. ✅ Content updates character by character (not all at once)
4. ✅ Auto-scrolls to keep latest content visible
5. ✅ Shows character count and duration
6. ✅ Shimmer effect displays during loading
7. ✅ Card auto-collapses when streaming completes
8. ✅ Can manually expand/collapse to view full reasoning
9. ✅ Behavior is consistent with rikkahub project

## 🚀 Next Steps

After testing, you can:

1. **Adjust the API key** if needed in `DeepSeekApi.kt`
2. **Customize the UI** colors, fonts, or card styling
3. **Add more features** like:
   - Export conversations
   - Search through reasoning history
   - Reasoning quality metrics
4. **Deploy to production** after thorough testing

## 📝 Test Results Template

Use this template to record your test results:

```
Test Date: ___________
Tester: ___________

Test 1 - Basic Reasoning: ☐ Pass ☐ Fail
Notes: _________________________________

Test 2 - Manual Expansion: ☐ Pass ☐ Fail
Notes: _________________________________

Test 3 - Multiple Messages: ☐ Pass ☐ Fail
Notes: _________________________________

Test 4 - Multimodal Input: ☐ Pass ☐ Fail
Notes: _________________________________

Overall Assessment: ☐ Ready ☐ Needs Work
```

---

**Happy Testing! 🎉**

