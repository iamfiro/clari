# WebSocket STT API Documentation

## Endpoint
```
ws://SERVER_IP:8000/ws/stt
```

## Client → Server (Audio Input)
```json
{
  "audio": "base64_encoded_pcm_16khz_mono_audio"
}
```

## Server → Client (Real-time Transcript)

### 1. Partial (Real-time recognition)
```json
{
  "type": "partial",
  "text": "Real-time transcript...",
  "chunks": ["Line 1", "Line 2", "Line 3"]
}
```

### 2. Committed (Finalized original)
```json
{
  "type": "committed",
  "text": "Finalized original text",
  "chunks": ["Line 1", "Line 2", "Line 3"]
}
```

### 3. Formatted (AI-corrected)
```json
{
  "type": "formatted",
  "text": "AI-corrected text with grammar/spelling fixes",
  "chunks": ["Line 1", "Line 2", "Line 3"]
}
```

## Text Chunking Rules
- Max 40 chars per line
- Max 3 lines per message
- Auto-split long sentences
- Optimized for Android display

## Audio Format
- **Sample Rate:** 16kHz
- **Format:** PCM 16-bit
- **Channels:** Mono
- **Encoding:** Base64

## Implementation Notes
- Keep WebSocket connection alive
- Send audio chunks continuously
- Handle 3 message types: `partial`, `committed`, `formatted`
- Display `chunks` array line by line in UI
- `partial` updates in real-time (same position)
- `committed` shows final recognized text
- `formatted` shows AI-enhanced result
Now

Add an Emoji, Sticker, or GIF

