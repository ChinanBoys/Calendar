import request from './request'

/** F-VOICE-01/02/03 — 语音/文本解析 */
export function parseVoiceText(data) {
  return request.post('/voice/parse', data)
}
